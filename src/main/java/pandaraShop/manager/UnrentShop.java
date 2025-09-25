package pandaraShop.manager;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import pandaraShop.Main;
import pandaraShop.manager.Admin.RestoreFlags;

import java.io.File;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class UnrentShop {

    public static void onUnrent(UUID uuid, RegionManager regions) throws WorldEditException {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        File file = new File(Main.getShopsDir(), uuid + ".yml");

        if (!file.exists()) {
            if (offlinePlayer.isOnline()) {
                Player online = offlinePlayer.getPlayer();
                if (online != null) {
                    online.sendMessage(ChatColor.RED + "You are not currently renting a shop, or the shop isn't primarily yours.");
                }
            }
            return;
        }

        FileConfiguration editFile = YamlConfiguration.loadConfiguration(file);
        Location shopLocation = new Location(Bukkit.getWorld("shop"),
                editFile.getInt("Shop.Center.x"),
                editFile.getInt("Shop.Center.y"),
                editFile.getInt("Shop.Center.z"));

        ApplicableRegionSet set = regions.getApplicableRegions(BlockVector3.at(
                shopLocation.getBlockX(), shopLocation.getBlockY(), shopLocation.getBlockZ()));

        for (ProtectedRegion region : set.getRegions()) {
            if (!region.getOwners().contains(uuid)) continue;

            // Clear ownership & members
            DefaultDomain members = region.getMembers();
            DefaultDomain owners = region.getOwners();
            owners.removeAll();
            members.removeAll();
            region.setMembers(members);
            region.setOwners(owners);

            // Remove entities INSIDE the region only
            for (Entity ent : shopLocation.getWorld().getEntities()) {
                Location loc = ent.getLocation();
                if (!region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) continue;

                if (ent instanceof Player) {
                    Location spawn = new Location(Bukkit.getWorld("shop"), 0.001, -19, 0.001);
                    int yawPick = new Random().nextInt(4);
                    spawn.setYaw(new float[]{0f, 90f, 180f, 270f}[yawPick]);
                    ent.teleport(spawn);
                    ((Player) ent).sendMessage(ChatColor.GOLD + "The shop you were at has been unrented!");
                } else if (ent.getType() != EntityType.ITEM) {
                    // Remove shopkeepers or any other non-item entities within region
                    ent.remove();
                }
            }

            // Remove Shopkeepers named after the owner (original behavior)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper removeall " + offlinePlayer.getName());
            Bukkit.getScheduler().runTaskLater(Main.getInstance(),
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper confirm"), 5L);

            // Restore flags & schematic
            RestoreFlags.consoleRestore(regions);
            Location min = new Location(Bukkit.getWorld("shop"),
                    region.getMinimumPoint().getBlockX(),
                    region.getMinimumPoint().getBlockY(),
                    region.getMinimumPoint().getBlockZ());

            if (region.getId().toLowerCase().matches("shop[abcd]0.*")) {
                LoadSchematic.place(min, "large");
            } else {
                LoadSchematic.place(min, "small");
            }

            if (!file.delete()) {
                Bukkit.getLogger().warning("Failed to delete shop file for " + uuid);
            }

            if (offlinePlayer.isOnline()) {
                Player online = offlinePlayer.getPlayer();
                if (online != null) {
                    online.sendMessage(ChatColor.GREEN + "Your shop has been successfully deleted");
                    online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + "A new shop is available for rent in the " + ChatColor.GREEN + "/shopworld");
            return; // done for the found region
        }

        // No owned region matched
        if (offlinePlayer.isOnline()) {
            Player online = offlinePlayer.getPlayer();
            if (online != null) {
                online.sendMessage(ChatColor.RED + "You can't unrent this area! Please contact an Admin!");
            }
        }
        Bukkit.getLogger().info("Failed to unrent shop: no owned region found for " + offlinePlayer.getName());
    }

    public static void onAdminUnrent(UUID adminUuid, RegionManager regions, ProtectedRegion standingRegion) throws WorldEditException {
        Logger log = Bukkit.getLogger();

        Player admin = Bukkit.getPlayer(adminUuid);
        if (standingRegion == null) {
            if (admin != null) admin.sendMessage(ChatColor.RED + "You are not standing inside a shop region.");
            return;
        }

        log.info("[restore] Requested by " + (admin != null ? admin.getName() : adminUuid)
                + " in region " + standingRegion.getId());

        // 1) Resolve the shop "owner" UUID to pick a file
        UUID shopOwnerUuid = null;

        // A) Owner UUIDs first
        Set<UUID> ownerUuids = standingRegion.getOwners().getUniqueIds();
        if (ownerUuids != null && !ownerUuids.isEmpty()) {
            shopOwnerUuid = ownerUuids.iterator().next();
            log.info("[restore] Using region OWNER uuid: " + shopOwnerUuid);
        } else {
            // B) Fallback: first member UUID (your requested behavior)
            Set<UUID> memberUuids = standingRegion.getMembers().getUniqueIds();
            if (memberUuids != null && !memberUuids.isEmpty()) {
                shopOwnerUuid = memberUuids.iterator().next();
                log.info("[restore] No owner on region. Using first MEMBER uuid: " + shopOwnerUuid);
            } else {
                log.info("[restore] Region " + standingRegion.getId() + " has no owners or members.");
            }
        }

        // 2) Find the shop YAML file
        File shopFile = null;
        if (shopOwnerUuid != null) {
            File candidate = new File(Main.getShopsDir(), shopOwnerUuid + ".yml");
            if (candidate.exists()) {
                shopFile = candidate;
                log.info("[restore] Matched shop file by UUID: " + shopFile.getName());
            } else {
                log.info("[restore] UUID-based file not found: " + candidate.getName());
            }
        }

        // C) Fallback: match by Shop.Shopname ≈ region id (strip Shop/Shop_ prefix)
        if (shopFile == null) {
            String regionId = standingRegion.getId() == null ? "" : standingRegion.getId();
            String ridNoPrefix = regionId.replaceFirst("(?i)^shop_?", "");
            File[] files = Main.getShopsDir().listFiles((d, n) -> n.endsWith(".yml"));
            if (files != null) {
                for (File f : files) {
                    FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
                    String name = fc.getString("Shop.Shopname");
                    if (name != null && (name.equalsIgnoreCase(regionId) || name.equalsIgnoreCase(ridNoPrefix))) {
                        shopFile = f;
                        // Try to backfill owner UUID from file if we didn’t have one
                        if (shopOwnerUuid == null) {
                            String ownerStr = fc.getString("Shop.Owner");
                            try { if (ownerStr != null) shopOwnerUuid = UUID.fromString(ownerStr); } catch (IllegalArgumentException ignored) {}
                        }
                        log.info("[restore] Matched shop file by Shop.Shopname: " + name + " -> " + f.getName());
                        break;
                    }
                }
            }
        }

        // 3) Clear WG ownership/members and entities inside the region, restore schematic/flags
        DefaultDomain owners = standingRegion.getOwners();
        DefaultDomain members = standingRegion.getMembers();
        owners.removeAll();
        members.removeAll();
        standingRegion.setOwners(owners);
        standingRegion.setMembers(members);
        try {
            regions.save();
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[restore] Failed to save RegionManager after clearing claim: " + ex.getMessage());
        }
        log.info("[restore] Cleared region owners/members for " + standingRegion.getId());

        // Remove ONLY entities inside the region
        World shopWorld = Bukkit.getWorld("shop"); // your code already targets the shop world
        if (shopWorld != null) {
            for (Entity ent : shopWorld.getEntities()) {
                Location loc = ent.getLocation();
                if (!standingRegion.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) continue;

                if (ent instanceof Player) {
                    Location spawn = new Location(shopWorld, 0.001, -19, 0.001);
                    int yawPick = new Random().nextInt(4);
                    spawn.setYaw(new float[]{0f, 90f, 180f, 270f}[yawPick]);
                    ent.teleport(spawn);
                    ((Player) ent).sendMessage(ChatColor.GOLD + "The shop you were at has been unrented!");
                } else if (ent.getType() != EntityType.ITEM) {
                    ent.remove();
                }
            }
        }

        // Remove Shopkeepers linked to the owner name (best-effort)
        if (shopOwnerUuid != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(shopOwnerUuid);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper removeall " + owner.getName());
            Bukkit.getScheduler().runTaskLater(Main.getInstance(),
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper confirm"), 5L);
        }

        // Restore flags & schematic
        RestoreFlags.consoleRestore(regions);
        Location min = new Location(Bukkit.getWorld("shop"),
                standingRegion.getMinimumPoint().getBlockX(),
                standingRegion.getMinimumPoint().getBlockY(),
                standingRegion.getMinimumPoint().getBlockZ());

        if (standingRegion.getId().toLowerCase().matches("shop[abcd]0.*")) {
            LoadSchematic.place(min, "large");
        } else {
            LoadSchematic.place(min, "small");
        }
        log.info("[restore] Schematic restored for " + standingRegion.getId());

        // 4) Delete the shop file (if we found one)
        if (shopFile != null && shopFile.exists()) {
            if (!shopFile.delete()) {
                log.warning("[restore] Failed to delete shop file: " + shopFile.getName());
            } else {
                log.info("[restore] Deleted shop file: " + shopFile.getName());
            }
        } else {
            log.warning("[restore] No matching shop file found to delete for region " + standingRegion.getId());
        }

        // 5) Staff + broadcast messaging
        Bukkit.broadcastMessage(ChatColor.GOLD + "A new shop is now available for rent in the "
                + ChatColor.GREEN + "/shopworld");

        if (admin != null) {
            String ownerName = (shopOwnerUuid != null ? Bukkit.getOfflinePlayer(shopOwnerUuid).getName() : null);
            admin.sendMessage(ChatColor.GREEN + "You have restored the shop "
                    + ChatColor.GOLD + standingRegion.getId()
                    + ChatColor.GREEN + (ownerName != null ? (" (previously owned by " + ownerName + ")") : " (previous owner unknown)"));
        }
    }

}
