package pandaraShop.manager.Admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListFiles {
    public static void check(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        File dir = new File(Main.getInstance().getDataFolder(), "shops");
        if (!dir.exists()) {
            player.sendMessage(ChatColor.RED + "Shops folder does not exist.");
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        List<String> names = new ArrayList<>();
        if (files != null) {
            for (File f : files) names.add(f.getName());
        }

        player.sendMessage(ChatColor.GREEN + "Files in: " + ChatColor.GOLD + dir.getAbsolutePath());
        if (names.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "(none)");
        } else {
            player.sendMessage(ChatColor.AQUA + String.join(", ", names));
        }
    }
}
