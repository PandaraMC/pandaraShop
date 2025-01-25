package pandaraShop.manager;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class shopAvailable {

    private static File file;
    private static FileConfiguration editFile;

    public static void rtp(UUID uuid, RegionManager regions) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}

        ArrayList<String> unrentedList = new ArrayList<>();
        int i = 0;

        file = file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), uuid + ".yml");

        for (Object names : regions.getRegions().keySet().toArray()) {
            if (names.toString().toLowerCase().contains("shop")) {
                ProtectedRegion rg = regions.getRegion(names.toString());
                if (rg != null && !rg.hasMembersOrOwners()) {
                    if (player.hasPermission("pandara.ultra")) {
                        if (names.toString().toLowerCase().contains("shopa0") || names.toString().toLowerCase().contains("shopb0") || names.toString().toLowerCase().contains("shopc0") || names.toString().toLowerCase().contains("shopd0")) {
                            i++;
                            unrentedList.add(names.toString());
                        }
                    }
                    else {
                        if (!names.toString().toLowerCase().contains("shopa0") || !names.toString().toLowerCase().contains("shopb0") || !names.toString().toLowerCase().contains("shopc0") || !names.toString().toLowerCase().contains("shopd0")) {
                            i++;
                            unrentedList.add(names.toString());
                        }
                    }
                }
            }
        }
        unrentedList.sort(String::compareToIgnoreCase);
        Random rand = new Random();
        int n = rand.nextInt(i) + 1;
        String string = unrentedList.stream().toList().get(n);
        ProtectedRegion rg = regions.getRegion(string);
        if (rg == null) {return;}
        int x = (rg.getMaximumPoint().getBlockX()+rg.getMinimumPoint().getBlockX())/2;
        int y = (rg.getMaximumPoint().getBlockY()+rg.getMinimumPoint().getBlockY())/2;
        int z = (rg.getMaximumPoint().getBlockZ()+rg.getMinimumPoint().getBlockZ())/2;
        World world = Bukkit.getWorld("shop");
        if (world == null) {return;}

        Location loc = new Location(world,x,world.getHighestBlockYAt(x,z)+1,z);
        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "There are currently a total of "+ChatColor.GOLD+i+ChatColor.GREEN+" available shops!" );
        if (file.exists()) {
            player.sendMessage(ChatColor.GREEN+"You already rent a shop, but here is a random one anyways!");
        }
        else {
            player.sendMessage(ChatColor.GREEN+"Here is a random available shop! Would you like to rent it?");
        }

    }
}
