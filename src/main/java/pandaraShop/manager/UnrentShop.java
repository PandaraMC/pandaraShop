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

    private static File file;
    private static FileConfiguration editFile;

    public static void onUnrent(UUID uuid, RegionManager regions) throws WorldEditException {

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}

        file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), player.getUniqueId() + ".yml");

        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "You are not currently renting a shop or the shop you are member of isn't primarily yours to unrent!");
            return;
        }

        editFile = YamlConfiguration.loadConfiguration(file);
        Location shoplocation = new Location(Bukkit.getWorld("shop"), editFile.getInt("Shop.Center.x"), editFile.getInt("Shop.Center.y"), editFile.getInt("Shop.Center.z"));


        ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(shoplocation.getBlockX(),shoplocation.getBlockY(),shoplocation.getBlockZ()));
        String[] st = Main.getInstance().getDataFolder().list();

        for (ProtectedRegion region : applicableRegionSet.getRegions()) {

            if (!region.getMembers().contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You can't unrent this area!");
            }
            else {

                DefaultDomain members = region.getMembers();
                members.removeAll();
                region.setMembers(members);
                //Location max = new Location(Bukkit.getWorld("shop"),region.getMaximumPoint().getBlockX(),region.getMaximumPoint().getBlockY(),region.getMaximumPoint().getBlockZ());
                Location min = new Location(Bukkit.getWorld("shop"),region.getMinimumPoint().getBlockX(),region.getMinimumPoint().getBlockY(),region.getMinimumPoint().getBlockZ());
                Chunk chunk = shoplocation.getChunk();
                for (Entity ent : chunk.getEntities()) {
                    if (ent instanceof Player) {
                        Location loc = new Location(Bukkit.getWorld("shop"),0.001f,-19,0.001f,0,0);
                        Random rand = new Random();
                        int n = rand.nextInt(4) + 1;
                        if (n == 1) {loc.setYaw(90);}
                        if (n == 2) {loc.setYaw(180);}
                        if (n == 3) {loc.setYaw(270);}
                        ent.teleport(loc);
                        ent.sendMessage(ChatColor.GOLD + "The shop you were at has been unrented!");
                    } else {
                        if (!ent.getType().equals(EntityType.DROPPED_ITEM)) {
                            ent.remove();
                        }
                    }
                }
                RestoreFlags.consoleRestore(regions);
                if (region.getId().toLowerCase().contains("shopa0") || region.getId().toLowerCase().contains("shopb0") || region.getId().toLowerCase().contains("shopc0") || region.getId().toLowerCase().contains("shopd0")) {
                    LoadSchematic.place(min,"large");
                } else {
                    LoadSchematic.place(min,"small");
                }
                file.delete();
                player.sendMessage(ChatColor.GREEN + "Your shop has been successfully deleted");
                Bukkit.broadcastMessage(ChatColor.GOLD+ "A new shop is available for rent in the "+ChatColor.GREEN+"/shopworld");
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,1,1);
            }
        }
    }
}
