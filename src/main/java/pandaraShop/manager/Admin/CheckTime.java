package pandaraShop.manager.Admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CheckTime {

    public static void checkMe(UUID uuid, String string) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}

        String filename;
        if (Bukkit.getPlayerExact(string) != null) {
            filename = Objects.requireNonNull(Bukkit.getPlayerExact(string)).getUniqueId().toString();
        }
        else {
            filename = string;
        }
        File file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop")).getDataFolder(), filename + ".yml");
        if (file.exists()) {
            if (player.isOp() || player.hasPermission("pandara.manager") || player.getName().equalsIgnoreCase(filename)) {
                FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
                long now = System.currentTimeMillis();
                long then = (Long) fc.get("Shop.Date");
                long result = now-then;

                long d = TimeUnit.MILLISECONDS.toDays(result);
                result -= TimeUnit.DAYS.toMillis(d);
                long h = TimeUnit.MILLISECONDS.toHours(result);
                result -= TimeUnit.HOURS.toMillis(h);
                long m = TimeUnit.MILLISECONDS.toMinutes(result);

                player.sendMessage(ChatColor.GREEN + "Your last activity was recorded \n>> " + ChatColor.GOLD + d + ChatColor.GREEN + " days, " + ChatColor.GOLD + h + ChatColor.GREEN + " hours and " + ChatColor.GOLD + m + ChatColor.GREEN + " minutes ago.");
            }
            else {
                player.sendMessage(ChatColor.GOLD + "You can't retrieve information about this shop!");
            }
        }
        else {
            player.sendMessage(ChatColor.GOLD + "The shop information you require does not exist!");
        }
    }
}
