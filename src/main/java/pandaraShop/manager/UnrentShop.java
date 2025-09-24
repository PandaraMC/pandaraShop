package pandaraShop.manager;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pandaraShop.Main;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UnrentShop {

    private static final File shopsDir = new File(Main.getInstance().getDataFolder(), "shops");

    public static void onUnrent(UUID ownerUuid, RegionManager regions) throws WorldEditException {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUuid);

        // 1) Find the owner’s shop file
        File file = new File(Main.getInstance().getDataFolder(), ownerUuid + ".yml");
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String shopName = cfg.getString("Shop.Shopname");
        if (shopName == null) return;

        // 2) Clear WorldGuard region
        ProtectedRegion region = regions.getRegion(shopName.toLowerCase());
        if (region != null) {
            region.getOwners().clear();
            region.getMembers().clear();
        }

        // 3) Reset shop file
        cfg.set("Shop.Owner", null);
        cfg.set("Shop.Shopname", null);
        cfg.set("Shop.Date", null);
        try {
            cfg.save(file);
        } catch (IOException e) {
            Main.getInstance().getLogger().warning("Could not reset shop file for " + ownerUuid);
        }

        // 4) Remove Shopkeepers (fallback command if API not used)
        if (Bukkit.getPluginManager().isPluginEnabled("Shopkeepers")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper removeall " + owner.getName());
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper confirm"), 20L);
        }

        // 5) Optional: Restore flags / reset schematic
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shop restoreflags " + shopName);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shop restore " + shopName);

        Main.getInstance().getLogger().info("Shop unrented: " + shopName + " (Owner: " + owner.getName() + ")");
    }

    /*public static void onUnrent(UUID uuid, RegionManager regions) throws WorldEditException {

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        File file = new File(Main.getInstance().getDataFolder(), uuid + ".yml");

        if (!file.exists()) {
            if (offlinePlayer.isOnline()) {
                Player online = offlinePlayer.getPlayer();
                if (online != null) {
                    online.sendMessage(ChatColor.RED + "You are not currently renting a shop, or the shop you are a member of isn't primarily yours to unrent!");
                }
            }
            return;
        }

        FileConfiguration editFile = YamlConfiguration.loadConfiguration(file);
        Location shopLocation = new Location(Bukkit.getWorld("shop"),
                editFile.getInt("Shop.Center.x"),
                editFile.getInt("Shop.Center.y"),
                editFile.getInt("Shop.Center.z"));

        ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(
                shopLocation.getBlockX(),
                shopLocation.getBlockY(),
                shopLocation.getBlockZ()));

        boolean foundOwnedRegion = false;

        for (ProtectedRegion region : applicableRegionSet.getRegions()) {
            if (!region.getMembers().contains(uuid)) continue;

            foundOwnedRegion = true;

            // Remove all members
            DefaultDomain members = region.getMembers();
            members.removeAll();
            region.setMembers(members);

            // Clear entities
            Chunk chunk = shopLocation.getChunk();
            for (Entity ent : chunk.getEntities()) {
                if (ent instanceof Player) {
                    Location loc = new Location(Bukkit.getWorld("shop"), 0.001f, -19, 0.001f, 0, 0);
                    Random rand = new Random();
                    loc.setYaw(switch (rand.nextInt(4)) {
                        case 0 -> 90;
                        case 1 -> 180;
                        case 2 -> 270;
                        default -> 0;
                    });

                    ent.teleport(loc);
                    if (ent instanceof Player p) {
                        p.sendMessage(ChatColor.GOLD + "The shop you were at has been unrented!");
                    }

                } else {
                    boolean isShopkeeper = ent.getScoreboardTags().contains("shopkeeper");
                    Location loc = ent.getLocation();
                    if (isShopkeeper && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                        ent.remove();
                    } else if (!ent.getType().equals(EntityType.ITEM)) {
                        ent.remove();
                    }
                }
            }

            // Remove shopkeeper NPCs
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper removeall " + offlinePlayer.getName());
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shopkeeper confirm"), 5L);

            // Restore flags
            RestoreFlags.consoleRestore(regions);

            // Restore schematic
            Location min = new Location(Bukkit.getWorld("shop"),
                    region.getMinimumPoint().getBlockX(),
                    region.getMinimumPoint().getBlockY(),
                    region.getMinimumPoint().getBlockZ());

            if (region.getId().toLowerCase().matches("shop[abcd]0.*")) {
                LoadSchematic.place(min, "large");
            } else {
                LoadSchematic.place(min, "small");
            }

            // Delete shop file
            if (!file.delete()) {
                Bukkit.getLogger().warning("Failed to delete shop file for " + uuid);
            }

            // Notify player (if online)
            if (offlinePlayer.isOnline()) {
                Player online = offlinePlayer.getPlayer();
                if (online != null) {
                    online.sendMessage(ChatColor.GREEN + "Your shop has been successfully deleted");
                    online.getWorld().playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
            }

            // Broadcast availability
            Bukkit.broadcastMessage(ChatColor.GOLD + "A new shop is available for rent in the " + ChatColor.GREEN + "/shopworld");

            break; // Done processing the relevant region
        }

        // If none of the regions were owned
        if (!foundOwnedRegion) {
            // Send message if online
            if (offlinePlayer.isOnline()) {
                Player online = offlinePlayer.getPlayer();
                if (online != null) {
                    online.sendMessage(ChatColor.RED + "You can't unrent this area! Please contact an Admin!");
                }
            }

            Bukkit.getLogger().info("Failed to unrent shop: no region found for " + offlinePlayer.getName());

            return;
        }
    }*/
}
