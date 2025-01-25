package pandaraShop.manager.Admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.UUID;

public class RemoveFile {

    private static File file;

    public static void manualRemove(UUID uuid, String string) {
        Player player = Bukkit.getPlayer(uuid);
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), string + ".yml"); //File to be removed
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
