package pandaraShop.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class ShopTP {

    public static void tpMe(UUID uuid, UUID targ) {

        Player player = Bukkit.getPlayer(uuid);
        Player shop = Bukkit.getPlayer(targ);

        if (player != null && shop != null) {
            File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop")).getDataFolder(), shop.getUniqueId() + ".yml");

            if (file.exists()) {
                FileConfiguration editFile = YamlConfiguration.loadConfiguration(file);

                Location loc = new Location(Bukkit.getWorld("shop"), editFile.getInt("Shop.TP.x"), editFile.getInt("Shop.TP.y"), editFile.getInt("Shop.TP.z"), editFile.getInt("Shop.TP.yaw"), editFile.getInt("Shop.TP.pitch"));

                player.teleport(loc);

            }
            else {
                player.sendMessage(ChatColor.GOLD+ shop.getName() + " does not own a shop. Please check it via /shop list and try again.");
            }
        }
    }
}
