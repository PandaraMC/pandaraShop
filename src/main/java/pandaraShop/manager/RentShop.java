package pandaraShop.manager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class RentShop {

    // ---------- OP override confirmation ----------
    private static final Map<UUID, String> PENDING_OVERRIDE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long>   PENDING_EXPIRES  = new ConcurrentHashMap<>();
    private static final long OVERRIDE_WINDOW_MS = 20_000L;

    public static void requestOverride(Player player, ProtectedRegion region) {
        PENDING_OVERRIDE.put(player.getUniqueId(), region.getId());
        PENDING_EXPIRES.put(player.getUniqueId(), System.currentTimeMillis() + OVERRIDE_WINDOW_MS);

        player.sendMessage(ChatColor.YELLOW + "This shop is currently owned. "
                + ChatColor.GOLD + "Are you sure you want to override ownership?");
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.AQUA + "/shop rentconfirm"
                + ChatColor.GRAY + " within 20 seconds to proceed.");

        // Optional clickable confirm (safe to ignore if API not present)
        try {
            net.md_5.bungee.api.chat.TextComponent tc =
                    new net.md_5.bungee.api.chat.TextComponent(ChatColor.GREEN + "[Click to Confirm]");
            tc.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                    net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/shop rentconfirm"));
            player.spigot().sendMessage(tc);
        } catch (Throwable ignore) {}
    }

    public static boolean consumeOverrideIfValid(Player player, ProtectedRegion region) {
        UUID u = player.getUniqueId();
        String pendingRegion = PENDING_OVERRIDE.get(u);
        Long   expires       = PENDING_EXPIRES.get(u);

        if (pendingRegion == null || expires == null) return false;
        if (!pendingRegion.equals(region.getId())) return false;
        if (System.currentTimeMillis() > expires) {
            PENDING_OVERRIDE.remove(u);
            PENDING_EXPIRES.remove(u);
            return false;
        }
        PENDING_OVERRIDE.remove(u);
        PENDING_EXPIRES.remove(u);
        return true;
    }

    /**
     * Called by the command: RegionManager already resolved. We derive the shop region
     * from the player's current location using this RegionManager.
     */
    public static void onRent(UUID requesterUuid, RegionManager regionManager) {
        Player player = Bukkit.getPlayer(requesterUuid);
        if (player == null) return;

        Logger log = Main.getInstance().getLogger();

        if (regionManager == null) {
            player.sendMessage(ChatColor.RED + "WorldGuard is not available in this world.");
            return;
        }

        // ---- Find the shop region at the player's feet using the provided RegionManager ----
        BlockVector3 pos = BukkitAdapter.asBlockVector(player.getLocation());
        ApplicableRegionSet ars = regionManager.getApplicableRegions(pos);
        if (ars == null || ars.size() == 0) {
            player.sendMessage(ChatColor.RED + "You are not standing inside a shop region.");
            return;
        }

        // Prefer regions whose id looks like a shop; otherwise fallback to highest priority
        ProtectedRegion region = ars.getRegions().stream()
                .filter(r -> {
                    String id = r.getId() == null ? "" : r.getId().toLowerCase();
                    return id.startsWith("shop") || id.contains("shop_") || id.equals("shop");
                })
                .max(Comparator.comparingInt(ProtectedRegion::getPriority))
                .orElseGet(() -> ars.getRegions().stream()
                        .max(Comparator.comparingInt(ProtectedRegion::getPriority))
                        .orElse(null));

        if (region == null) {
            player.sendMessage(ChatColor.RED + "You are not standing inside a shop region.");
            return;
        }

        // ---- Claimed detection (owners OR members) ----
        Set<UUID> ownerSet = region.getOwners().getUniqueIds();
        Set<UUID> memberSet = region.getMembers().getUniqueIds();
        boolean hasOwners = ownerSet != null && !ownerSet.isEmpty();
        boolean hasMembers = memberSet != null && !memberSet.isEmpty();

        if (hasOwners || hasMembers) {
            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "You can't rent a rented shop!");
                log.info("[rent] Blocked rent for " + player.getName() +
                        " at " + region.getId() + " (owners=" + hasOwners + ", members=" + hasMembers + ")");
                return;
            }

            // OP path: confirmation required
            if (!consumeOverrideIfValid(player, region)) {
                requestOverride(player, region);
                log.info("[rent] OP " + player.getName() +
                        " requested override at " + region.getId() +
                        " (owners=" + hasOwners + ", members=" + hasMembers + ")");
                return;
            }

            // Confirmed: clear claim and persist
            DefaultDomain owners = region.getOwners();
            DefaultDomain members = region.getMembers();
            owners.removeAll();
            members.removeAll();
            region.setOwners(owners);
            region.setMembers(members);
            try {
                regionManager.save();
            } catch (Exception ex) {
                log.warning("[rent] Failed to save WG after OP override on " +
                        region.getId() + ": " + ex.getMessage());
            }

            player.sendMessage(ChatColor.YELLOW + "Ownership cleared. Proceeding with rent...");
            log.info("[rent] OP override cleared claim at " + region.getId() +
                    " by " + player.getName());
        }

        // ---- Proceed with normal rent ----
        performRent(player, regionManager, region);
    }

    private static void performRent(Player player, RegionManager regions, ProtectedRegion region) {
        Logger log = Main.getInstance().getLogger();

        // 1) Assign player as region owner
        DefaultDomain owners = region.getOwners();
        owners.addPlayer(player.getUniqueId());
        region.setOwners(owners);
        try {
            regions.save();
        } catch (Exception ex) {
            log.warning("[rent] Failed to save WG after assigning owner on " +
                    region.getId() + ": " + ex.getMessage());
        }

        // 2) Write/update YAML file
        File f = new File(Main.getShopsDir(), player.getUniqueId().toString() + ".yml");
        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);

        fc.set("Shop.Owner", player.getUniqueId().toString());
        fc.set("Shop.Shopname", deriveShopNameFromRegion(region.getId()));
        fc.set("Shop.Date", System.currentTimeMillis()); // used by /shop checktime

        // Optional: keep these updated here; remove if you set elsewhere
        Location tp = player.getLocation();
        fc.set("Shop.TP.x", tp.getBlockX());
        fc.set("Shop.TP.y", tp.getBlockY());
        fc.set("Shop.TP.z", tp.getBlockZ());
        fc.set("Shop.TP.yaw", tp.getYaw());
        fc.set("Shop.TP.pitch", tp.getPitch());

        int cx = (region.getMinimumPoint().getBlockX() + region.getMaximumPoint().getBlockX()) / 2;
        int cy = (region.getMinimumPoint().getBlockY() + region.getMaximumPoint().getBlockY()) / 2;
        int cz = (region.getMinimumPoint().getBlockZ() + region.getMaximumPoint().getBlockZ()) / 2;
        fc.set("Shop.Center.x", cx);
        fc.set("Shop.Center.y", cy);
        fc.set("Shop.Center.z", cz);

        String size = region.getId().toLowerCase().matches("shop[abcd]0.*") ? "large" : "small";
        fc.set("Shop.Size", size);

        try {
            fc.save(f);
        } catch (IOException e) {
            log.warning("[rent] Failed to save shop file " + f.getName() + ": " + e.getMessage());
        }

        // 3) Feedback
        player.sendMessage(ChatColor.GREEN + "You have rented " + ChatColor.GOLD + region.getId() + ChatColor.GREEN + "!");
        log.info("[rent] " + player.getName() + " rented region " + region.getId() + " (" + size + ")");
    }

    private static String deriveShopNameFromRegion(String regionId) {
        if (regionId == null) return "Shop";
        String rid = regionId;
        if (rid.toLowerCase().startsWith("shop_")) rid = rid.substring(5);
        else if (rid.equalsIgnoreCase("shop")) rid = "Shop";
        return rid;
    }
}
