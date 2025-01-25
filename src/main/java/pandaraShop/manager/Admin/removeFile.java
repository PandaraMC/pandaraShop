package pandaraShop.manager.Admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.UUID;

public class removeFile {

    private static File file;
    private static FileConfiguration editFile;

    public static void manualRemove(UUID uuid, String string) {
        Player player = Bukkit.getPlayer(uuid);
        String name = string;
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), name + ".yml"); //File to be removed
        if (file.exists()) {
            file.delete();
            Main.getInstance().saveConfig();
            player.sendMessage(ChatColor.GREEN + "File " + file.getName() + " successfully removed.");
        }
        else {
            player.sendMessage(ChatColor.GOLD + "File does not exist, please try again!");
        }
    }

    //Remove an existing file!!!
    public static void autoRemove(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), player.getUniqueId() + ".yml"); //File to be removed
        if (file.exists()) {
            file.delete();
            Main.getInstance().saveConfig();
            player.sendMessage(ChatColor.GREEN + "File " + file.getName() + " successfully removed.");
        }
        else {
            player.sendMessage(ChatColor.GOLD + "File does not exist, please try again!");
        }
    }
}
