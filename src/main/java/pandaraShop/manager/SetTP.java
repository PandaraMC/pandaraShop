package pandaraShop.manager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SetTP {

    public static void setTP(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        World world = Bukkit.getWorld("shop");
        if (world == null) {
            player.sendMessage(ChatColor.RED + "The shop world is not loaded.");
            return;
        }
        if (!"shop".equalsIgnoreCase(player.getWorld().getName())) {
            player.sendMessage(ChatColor.RED + "You must be in the shop world to set a shop teleport!");
            return;
        }

        File file = new File(Main.getShopsDir(), player.getUniqueId() + ".yml");

        if (!file.exists()) {
            player.sendMessage(ChatColor.GOLD + "You don't own a shop.");
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) {
            player.sendMessage(ChatColor.RED + "Region manager not found.");
            return;
        }

        ApplicableRegionSet set = regions.getApplicableRegions(BlockVector3.at(
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()));

        boolean insideOwnShop = false;
        for (ProtectedRegion region : set) {
            if (region.hasMembersOrOwners() && region.getOwners().contains(player.getUniqueId())) {
                insideOwnShop = true;
                break;
            }
        }
        if (!insideOwnShop) {
            player.sendMessage(ChatColor.RED + "You must be within your own shop!");
            return;
        }

        Location loc = player.getLocation();
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("Shop.TP.x", loc.getX());
        cfg.set("Shop.TP.y", loc.getY());
        cfg.set("Shop.TP.z", loc.getZ());
        cfg.set("Shop.TP.yaw", loc.getYaw());
        cfg.set("Shop.TP.pitch", loc.getPitch());
        try {
            cfg.save(file);
            player.sendMessage(ChatColor.GREEN + "Your shop TP has been successfully updated");
            player.sendMessage(ChatColor.GREEN + "Type /shop " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + " to teleport to it.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to save your shop TP; contact staff.");
        }
    }
}
