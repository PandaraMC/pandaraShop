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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.sk89q.worldedit.WorldEdit.logger;

public class SetTP {

    private static final World world = Bukkit.getWorld("shop");

    public static void setTP(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), player.getUniqueId() + ".yml");
        Location loc = player.getLocation();


        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getBlockX(),player.getLocation().getBlockY(),player.getLocation().getBlockZ()));

        if (!loc.getWorld().toString().contains("shop")) {
            player.sendMessage(ChatColor.RED + "You must be in the shop world to set a shop teleport!");
            return;
        }

        if (!file.exists()) {
            player.sendMessage(ChatColor.GOLD + "You don't own a shop.");
            return;
        }

        for (ProtectedRegion region : applicableRegionSet.getRegions()) {
            if (region.hasMembersOrOwners()) {
                if (!region.getMembers().contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You must be within your own shop!");
                    return;
                }
                else {
                    FileConfiguration editFile = YamlConfiguration.loadConfiguration(file);

                    editFile.set("Shop.TP.x",loc.getX());
                    editFile.set("Shop.TP.y",loc.getY());
                    editFile.set("Shop.TP.z",loc.getZ());
                    editFile.set("Shop.TP.yaw",loc.getYaw());
                    editFile.set("Shop.TP.pitch",loc.getPitch());
                    try {
                        editFile.save(file);
                    } catch (IOException e) {
                        logger.error("Failed to save the file: {}", file.getName(), e);
                    }
                    player.sendMessage(ChatColor.GREEN+ "Your shop TP has been successfully updated");
                    player.sendMessage(ChatColor.GREEN+ "Type /shop " +ChatColor.GOLD+player.getName()+ChatColor.GREEN+ " to teleport to it.");
                }

            }
        }
    }
}
