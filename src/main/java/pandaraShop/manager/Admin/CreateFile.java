package pandaraShop.manager.Admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CreateFile {
    private static File file;
    private static FileConfiguration editFile;

    public static void manualCreate(UUID uuid, String name) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        File shopsDir = Main.getShopsDir();
        file = new File(shopsDir, name + ".yml");

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    player.sendMessage(ChatColor.GREEN + "New file created.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "Failed to create file.");
                return;
            }
            Main.getInstance().saveConfig();

            editFile = YamlConfiguration.loadConfiguration(file);
            long now = System.currentTimeMillis();
            Location loc = player.getLocation();
            editFile.set("Shop.Date", now);
            editFile.set("Shop.Owner", player.getUniqueId().toString());
            editFile.set("Shop.Shopname", player.getName());
            editFile.set("Shop.TP.x", loc.getX());
            editFile.set("Shop.TP.y", loc.getY());
            editFile.set("Shop.TP.z", loc.getZ());
            editFile.set("Shop.TP.yaw", loc.getYaw());
            editFile.set("Shop.TP.pitch", loc.getPitch());
            try {
                editFile.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "File already exists.");
        }
    }

    public static void autoCreate(UUID uuid, int x, int y, int z, String size) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        File shopsDir = Main.getShopsDir();
        file = new File(shopsDir, uuid + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Main.getInstance().saveConfig();
            editFile = YamlConfiguration.loadConfiguration(file);

            long now = System.currentTimeMillis();
            Location loc = player.getLocation();
            editFile.set("Shop.Date", now);
            editFile.set("Shop.Owner", uuid.toString());
            editFile.set("Shop.Shopname", player.getName());
            editFile.set("Shop.Size", size);
            editFile.set("Shop.Center.x", x);
            editFile.set("Shop.Center.y", y);
            editFile.set("Shop.Center.z", z);
            editFile.set("Shop.TP.x", loc.getBlockX());
            editFile.set("Shop.TP.y", loc.getBlockY());
            editFile.set("Shop.TP.z", loc.getBlockZ());
            editFile.set("Shop.TP.yaw", loc.getYaw());
            editFile.set("Shop.TP.pitch", loc.getPitch());
            try {
                editFile.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "Player already rents a shop, please unrent it and try again!");
        }
    }
}
