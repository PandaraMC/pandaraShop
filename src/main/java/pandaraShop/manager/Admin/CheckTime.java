package pandaraShop.manager.Admin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CheckTime {

    // /shop checktime -> only checks the shop region the player is currently in
    public static void checkMe(UUID requester) {
        Player player = Bukkit.getPlayer(requester);
        if (player == null) return;

        Logger log = Main.getInstance().getLogger();
        World world = player.getWorld();
        BlockVector3 pos = BukkitAdapter.asBlockVector(player.getLocation());
        log.info("[checktime] Requested by " + player.getName() + " (" + requester + ") at "
                + world.getName() + " " + pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ());

        // 1) Regions at player's location
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = container.get(BukkitAdapter.adapt(world));
        if (rm == null) {
            log.warning("[checktime] RegionManager is null for world " + world.getName());
            player.sendMessage(ChatColor.RED + "WorldGuard isn’t available in this world.");
            return;
        }

        ApplicableRegionSet ars = rm.getApplicableRegions(pos);
        if (ars == null || ars.size() == 0) {
            log.info("[checktime] No WorldGuard regions cover this location.");
            player.sendMessage(ChatColor.GOLD + "You are not standing inside a shop.");
            return;
        }

        // Log all region ids found here
        StringBuilder found = new StringBuilder();
        for (ProtectedRegion pr : ars) {
            if (found.length() > 0) found.append(", ");
            found.append(pr.getId()).append("(p=").append(pr.getPriority()).append(")");
        }
        log.info("[checktime] Regions at location: [" + found + "]");

        // 2) Choose a "shop" region: prefer ids containing "shop", tie-break by highest priority
        ProtectedRegion shopRegion = ars.getRegions().stream()
                .filter(r -> {
                    String id = (r.getId() == null ? "" : r.getId().toLowerCase());
                    return id.startsWith("shop") || id.contains("shop_") || id.equals("shop");
                })
                .max(Comparator.comparingInt(ProtectedRegion::getPriority))
                .orElse(null);

        // If none matched naming, fall back to the highest-priority region at this spot
        if (shopRegion == null) {
            shopRegion = ars.getRegions().stream()
                    .max(Comparator.comparingInt(ProtectedRegion::getPriority))
                    .orElse(null);
            if (shopRegion != null) {
                log.info("[checktime] Falling back to highest-priority region: " + shopRegion.getId());
            }
        } else {
            log.info("[checktime] Selected shop region: " + shopRegion.getId() + " (priority " + shopRegion.getPriority() + ")");
        }

        if (shopRegion == null) {
            player.sendMessage(ChatColor.GOLD + "You are not standing inside a shop.");
            return;
        }

        // 3) Resolve YAML file for this region
        File shopFile = resolveShopFileForRegion(shopRegion, log);
        if (shopFile == null) {
            log.warning("[checktime] Could not resolve a shop YAML file for region " + shopRegion.getId());
            player.sendMessage(ChatColor.GOLD + "The shop information you require does not exist!");
            return;
        }
        log.info("[checktime] Candidate shop file: " + shopFile.getAbsolutePath() + " exists=" + shopFile.exists());

        if (!shopFile.exists()) {
            player.sendMessage(ChatColor.GOLD + "The shop information you require does not exist!");
            return;
        }

        // 4) Read Shop.Date
        FileConfiguration fc = YamlConfiguration.loadConfiguration(shopFile);
        long then = fc.getLong("Shop.Date", -1L);
        String shopName = fc.getString("Shop.Shopname", shopRegion.getId());
        String ownerStr = fc.getString("Shop.Owner", "(unknown)");

        if (then <= 0) {
            log.info("[checktime] Shop.Date missing/invalid in file " + shopFile.getName());
            player.sendMessage(ChatColor.GOLD + "The shop information you require does not exist!");
            return;
        }

        long diff = Math.max(0L, System.currentTimeMillis() - then);
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        diff -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        diff -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

        log.info("[checktime] Success for " + player.getName() + " (" + requester + ") "
                + "region=" + shopRegion.getId()
                + " ownerField=" + ownerStr
                + " date=" + then + " (ms since=" + (System.currentTimeMillis() - then) + ")");

        player.sendMessage(
                ChatColor.GREEN + "Shop " + ChatColor.GOLD + shopName + ChatColor.GREEN +
                        " was last visited " + ChatColor.GOLD + days + ChatColor.GREEN + " days, " +
                        ChatColor.GOLD + hours + ChatColor.GREEN + " hours, and " +
                        ChatColor.GOLD + minutes + ChatColor.GREEN + " minutes ago."
        );
    }

    private static File resolveShopFileForRegion(ProtectedRegion region, Logger log) {
        // A) Owner UUIDs on the region (fast path)
        Set<UUID> owners = region.getOwners().getUniqueIds();
        if (owners != null && !owners.isEmpty()) {
            log.info("[checktime] Region owner UUIDs: " + owners);
            UUID firstOwner = owners.iterator().next();
            File f = new File(Main.getShopsDir(), firstOwner.toString() + ".yml");
            log.info("[checktime] Trying owner-based file: " + f.getName() + " for region " + region.getId());
            if (f.exists()) return f;
        } else {
            log.info("[checktime] Region " + region.getId() + " has no owners.");
            // A.1) NEW: fall back to the first MEMBER UUID if present
            Set<UUID> members = region.getMembers().getUniqueIds();
            if (members != null && !members.isEmpty()) {
                log.info("[checktime] Region member UUIDs: " + members);
                UUID firstMember = members.iterator().next();
                File f = new File(Main.getShopsDir(), firstMember.toString() + ".yml");
                log.info("[checktime] Trying member-based file: " + f.getName() + " for region " + region.getId());
                if (f.exists()) return f;
            } else {
                log.info("[checktime] Region " + region.getId() + " has no members either.");
            }
        }

        // B) Fallback: match by Shop.Shopname ≈ region id (with/without "Shop"/"Shop_" prefix)
        String regionId = region.getId() == null ? "" : region.getId();
        String ridNoPrefix = regionId.replaceFirst("(?i)^shop_?", ""); // strip leading "Shop" or "Shop_"

        log.info("[checktime] Fallback scan for Shop.Shopname matching '" + regionId + "' or '" + ridNoPrefix + "'");

        File dir = Main.getShopsDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return null;

        for (File f : files) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
            String name = fc.getString("Shop.Shopname");
            if (name == null) continue;
            if (name.equalsIgnoreCase(regionId) || name.equalsIgnoreCase(ridNoPrefix)) {
                log.info("[checktime] Matched by Shop.Shopname: " + name + " -> " + f.getName());
                return f;
            }
        }
        return null;
    }
}
