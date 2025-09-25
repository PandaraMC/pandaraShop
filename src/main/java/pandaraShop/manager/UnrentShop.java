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
import java.util.UUID;

public class UnrentShop {

    public static void onUnrent(UUID uuid, RegionManager regions) throws WorldEditException {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        File file = new File(Main.getInstance().getDataFolder(), "shops/" + uuid + ".yml");
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
        if (standingRegion == null) {
            Player admin = Bukkit.getPlayer(adminUuid);
            if (admin != null) admin.sendMessage(ChatColor.RED + "You are not standing inside a shop region.");
            return;
        }

        DefaultDomain owners = standingRegion.getOwners();
        if (owners == null || owners.getUniqueIds().isEmpty()) {
            Player admin = Bukkit.getPlayer(adminUuid);
            if (admin != null) admin.sendMessage(ChatColor.RED + "This shop has no owner recorded. Cannot unrent.");
            return;
        }

        UUID shopOwnerUuid = owners.getUniqueIds().iterator().next();
        File shopFile = new File(Main.getInstance().getDataFolder(), "shops/" + shopOwnerUuid + ".yml");
        if (!shopFile.exists()) {
            Player admin = Bukkit.getPlayer(adminUuid);
            if (admin != null) admin.sendMessage(ChatColor.RED + "No shop file found for that owner. Cannot unrent.");
            return;
        }

        onUnrent(shopOwnerUuid, regions);

        Player admin = Bukkit.getPlayer(adminUuid);
        if (admin != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(shopOwnerUuid);
            admin.sendMessage(ChatColor.GREEN + "You have unrented the shop owned by " +
                    (owner.getName() != null ? owner.getName() : shopOwnerUuid.toString()));
        }
    }
}
