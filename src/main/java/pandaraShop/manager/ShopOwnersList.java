package pandaraShop.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopOwnersList {
    public static void listMe(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        File shopDir = new File(Main.getInstance().getDataFolder(), "shops");
        File[] files = shopDir.listFiles((dir, name) -> name.endsWith(".yml"));

        List<String> owners = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                String shopName = cfg.getString("Shop.Shopname");
                if (shopName != null && !shopName.isBlank()) owners.add(shopName);
            }
        }

        player.sendMessage(" ");
        player.sendMessage(ChatColor.AQUA + "List of rented shops!");
        player.sendMessage("============");
        if (owners.isEmpty()) {
            player.sendMessage(ChatColor.GOLD + "There are currently no shops rented!");
        } else {
            player.sendMessage(ChatColor.GREEN + String.join(", ", owners));
            player.sendMessage("============");
            player.sendMessage(ChatColor.GOLD + "Type /shop [PLAYERNAME] to teleport to their shop.");
        }
    }
}
