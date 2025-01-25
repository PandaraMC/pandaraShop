package pandaraShop.manager.Admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class RemoveFile {

    public static void manualRemove(UUID uuid, String string) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop")).getDataFolder(), string + ".yml"); //File to be removed
        if (file.exists()) {
            file.delete();
            Main.getInstance().saveConfig();
            if (file.delete()) { // Check the result of file.delete()
                player.sendMessage(ChatColor.GREEN + "Your shop has been successfully deleted");
                Main.getInstance().saveConfig();
            } else {
                player.sendMessage(ChatColor.RED + "Failed to delete the shop file. Please try again!");
            }
        }
        else {
            player.sendMessage(ChatColor.GOLD + "File does not exist, please try again!");
        }
    }
}
