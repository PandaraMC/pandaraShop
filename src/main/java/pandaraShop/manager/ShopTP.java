package pandaraShop.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class ShopTP {

    public static void tpMe(UUID uuid, String target) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        World world = Bukkit.getWorld("shop");
        if (world == null) {
            player.sendMessage(ChatColor.RED + "The shop world is not loaded.");
            return;
        }

        File shopsDir = new File(Objects.requireNonNull(
                Bukkit.getPluginManager().getPlugin("pandaraShop")).getDataFolder(), "shops");
        File[] files = shopsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            player.sendMessage(ChatColor.RED + "No shops data found.");
            return;
        }

        for (File file : files) {
            FileConfiguration shopData = YamlConfiguration.loadConfiguration(file);
            String storedName = shopData.getString("Shop.Shopname");
            if (storedName != null && storedName.equalsIgnoreCase(target)) {
                double x = shopData.getDouble("Shop.TP.x");
                double y = shopData.getDouble("Shop.TP.y");
                double z = shopData.getDouble("Shop.TP.z");
                float yaw = (float) shopData.getDouble("Shop.TP.yaw");
                float pitch = (float) shopData.getDouble("Shop.TP.pitch");

                player.teleport(new Location(world, x, y, z, yaw, pitch));
                player.sendMessage(ChatColor.GREEN + "You've been teleported to shop: " + storedName);
                return;
            }
        }

        player.sendMessage(ChatColor.RED + "No shop found with the name '" + target + "'. Check spelling or use /shop list.");
    }
}
