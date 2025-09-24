package pandaraShop.manager.Admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class RemoveFile {

    /**
     * Manually remove a shop file by UUID (or custom string key).
     */
    public static void manualRemove(UUID uuid, String string) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        File file = new File(Main.getInstance().getDataFolder(), string + ".yml");
        if (!file.exists()) {
            player.sendMessage(ChatColor.GOLD + "File does not exist, please try again!");
            return;
        }

        if (file.delete()) {
            player.sendMessage(ChatColor.GREEN + "Your shop has been successfully deleted");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to delete the shop file. Please try again!");
        }
    }

    /**
     * Automatically remove expired shop files (>30 days).
     */
    public static void cleanFiles() {
        File shopDir = Main.getInstance().getDataFolder(); // ✅ root folder, no "shops/"
        if (!shopDir.exists() || !shopDir.isDirectory()) {
            Bukkit.getLogger().warning("No shop data folder found!");
            return;
        }

        long now = System.currentTimeMillis();
        long cutoff = 30L * 24 * 60 * 60 * 1000; // 30 days in ms

        int removedCount = 0;

        for (File file : Objects.requireNonNull(shopDir.listFiles())) {
            if (!file.isFile() || !file.getName().endsWith(".yml")) continue;

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            long shopDate = config.getLong("Shop.Date", -1);
            String ownerUuid = config.getString("Shop.Owner");
            String shopName = config.getString("Shop.Shopname");

            if (shopDate > 0 && (now - shopDate) > cutoff) {
                if (file.delete()) {
                    removedCount++;

                    String ownerName = "unknown";
                    if (ownerUuid != null) {
                        try {
                            OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUuid));
                            if (owner != null && owner.getName() != null) {
                                ownerName = owner.getName();
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }

                    Bukkit.broadcastMessage(ChatColor.GOLD + "[ShopCleaner] Removed expired shop file: "
                            + ChatColor.RED + file.getName()
                            + ChatColor.GRAY + " (Owner: " + ChatColor.YELLOW + ownerName
                            + ChatColor.GRAY + ", Shopname: " + ChatColor.AQUA + shopName + ")");
                }
            }
        }

        if (removedCount == 0) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "[ShopCleaner] No expired shop files were found.");
        } else {
            Bukkit.getLogger().info("Cleaned " + removedCount + " expired shop files.");
        }
    }
}
