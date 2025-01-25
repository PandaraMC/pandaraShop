package pandaraShop.manager.Admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.util.UUID;

public class listFiles {

    public static void check(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}

        String[] st = Main.getInstance().getDataFolder().list(); // Get a list of files (by name) in the folder

        player.sendMessage(ChatColor.GREEN + "Files:");
        player.sendMessage(ChatColor.GOLD + Main.getInstance().getDataFolder().getAbsoluteFile().toString());
        assert st != null;
        player.sendMessage(st);
    }
}
