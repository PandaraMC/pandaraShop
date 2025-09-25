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

    public static void manualRemove(UUID uuid, String name) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        File file = new File(Main.getShopsDir(), name + ".yml");

        if (!file.exists()) {
            player.sendMessage(ChatColor.GOLD + "File does not exist, please try again!");
            return;
        }

        boolean ok = file.delete();
        Main.getInstance().saveConfig();

        if (ok) {
            player.sendMessage(ChatColor.GREEN + "Shop file deleted.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to delete the shop file. Please try again!");
        }
    }

    public static void cleanFiles() {
        File shopDir = Main.getShopsDir();

        if (!shopDir.exists() || !shopDir.isDirectory()) {
            Bukkit.getLogger().warning("No shops directory found!");
            return;
        }

        long now = System.currentTimeMillis();
        long cutoff = 30L * 24 * 60 * 60 * 1000; // 30 days

        int removedCount = 0;
        for (File file : Objects.requireNonNull(shopDir.listFiles((d, n) -> n.endsWith(".yml")))) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            long last = config.getLong("Shop.Date", -1);
            String ownerUuid = config.getString("Shop.Owner");
            String shopName = config.getString("Shop.Shopname");

            if (last > 0 && (now - last) > cutoff) {
                if (file.delete()) {
                    removedCount++;
                    String ownerName = ownerUuid;
                    try {
                        OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUuid));
                        if (owner != null && owner.getName() != null) ownerName = owner.getName();
                    } catch (Exception ignored) {}

                    Bukkit.broadcastMessage(ChatColor.GOLD + "[ShopCleaner] Removed expired shop file: "
                            + ChatColor.RED + file.getName()
                            + ChatColor.GRAY + " (Owner: " + ChatColor.YELLOW + ownerName
                            + ChatColor.GRAY + ", Shopname: " + ChatColor.AQUA + shopName + ChatColor.GRAY + ")");
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
