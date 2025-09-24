package pandaraShop.manager;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class ShopAvailable {

    public static void rtp(UUID uuid, RegionManager regions) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        ArrayList<String> unrentedList = new ArrayList<>();

        File file = new File(Main.getInstance().getDataFolder(), uuid + ".yml");

        for (String name : regions.getRegions().keySet()) {
            if (!name.toLowerCase().contains("shop")) continue;

            ProtectedRegion rg = regions.getRegion(name);
            if (rg == null || rg.hasMembersOrOwners()) continue;

            boolean isUltraRegion = name.toLowerCase().matches("shop[abcd]0.*");

            if (player.hasPermission("pandara.ultra")) {
                if (isUltraRegion) {
                    unrentedList.add(name);
                }
            } else {
                if (!isUltraRegion) {
                    unrentedList.add(name);
                }
            }
        }

        if (unrentedList.isEmpty()) {
            player.sendMessage(ChatColor.RED + "There are no available shops right now!");
            return;
        }

        unrentedList.sort(String::compareToIgnoreCase);

        Random rand = new Random();
        String chosen = unrentedList.get(rand.nextInt(unrentedList.size()));
        ProtectedRegion rg = regions.getRegion(chosen);
        if (rg == null) return;

        int x = (rg.getMaximumPoint().getBlockX() + rg.getMinimumPoint().getBlockX()) / 2;
        int z = (rg.getMaximumPoint().getBlockZ() + rg.getMinimumPoint().getBlockZ()) / 2;

        World world = Bukkit.getWorld("shop");
        if (world == null) return;

        Location loc = new Location(world, x, world.getHighestBlockYAt(x, z) + 1, z);
        player.teleport(loc);

        player.sendMessage(ChatColor.GREEN + "There are currently a total of "
                + ChatColor.GOLD + unrentedList.size() + ChatColor.GREEN + " available shops!");

        if (file.exists()) {
            player.sendMessage(ChatColor.GREEN + "You already rent a shop, but here is a random one anyways!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Here is a random available shop! Would you like to rent it?");
        }
    }
}
