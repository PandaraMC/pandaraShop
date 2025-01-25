package pandaraShop.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.sk89q.worldedit.WorldEdit.logger;

public class SetTP {

    public static void setTP(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), player.getUniqueId() + ".yml");
        Location loc = player.getLocation();

        if (!loc.getWorld().toString().equalsIgnoreCase("shop")) {
            player.sendMessage(ChatColor.RED + "You must be in the shop world to set a shop teleport!");
            return;
        }

        if (!file.exists()) {
            player.sendMessage(ChatColor.GOLD + "You don't own a shop.");
        }
        else {
            FileConfiguration editFile = YamlConfiguration.loadConfiguration(file);

            editFile.set("Shop.TP.x",loc.getX());
            editFile.set("Shop.TP.y",loc.getY());
            editFile.set("Shop.TP.z",loc.getZ());
            editFile.set("Shop.TP.yaw",loc.getYaw());
            editFile.set("Shop.TP.pitch",loc.getPitch());
            try {
                editFile.save(file);
            } catch (IOException e) {
                logger.error("Failed to save the file: {}", file.getName(), e);
            }
            player.sendMessage(ChatColor.GREEN+ "Your shop TP has been successfully updated");
            player.sendMessage(ChatColor.GREEN+ "Type /shop " +ChatColor.GOLD+player.getName()+ChatColor.GREEN+ " to teleport to it.");
        }
    }
}
