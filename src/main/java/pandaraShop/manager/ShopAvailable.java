package pandaraShop.manager;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class ShopAvailable {

    public static void rtp(UUID uuid, RegionManager regions) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        World world = Bukkit.getWorld("shop");
        if (world == null || regions == null) {
            player.sendMessage(ChatColor.RED + "The shop world is not ready.");
            return;
        }

        ArrayList<String> candidates = new ArrayList<>();

        for (String id : regions.getRegions().keySet()) {
            String lower = id.toLowerCase();
            if (!lower.contains("shop")) continue;

            ProtectedRegion rg = regions.getRegion(id);
            if (rg == null || rg.hasMembersOrOwners()) continue;

            boolean ultraPlot = lower.matches("shop[abcd]0.*"); // ultra only
            if (player.hasPermission("pandara.ultra") || player.isOp()) {
                candidates.add(id); // can see any
            } else if (!ultraPlot) {
                candidates.add(id); // regular only
            }
        }

        if (candidates.isEmpty()) {
            player.sendMessage(ChatColor.GOLD + "No available shops found right now.");
            return;
        }

        candidates.sort(String::compareToIgnoreCase);
        String pick = candidates.get(new Random().nextInt(candidates.size()));
        ProtectedRegion rg = regions.getRegion(pick);
        if (rg == null) {
            player.sendMessage(ChatColor.RED + "That shop could not be found, try again.");
            return;
        }

        int x = (rg.getMaximumPoint().getBlockX() + rg.getMinimumPoint().getBlockX()) / 2;
        int z = (rg.getMaximumPoint().getBlockZ() + rg.getMinimumPoint().getBlockZ()) / 2;
        Location loc = new Location(world, x, world.getHighestBlockYAt(x, z) + 1, z);

        player.teleport(loc);

        // Message based on whether the player already owns a shop file
        File shopFile = new File(Bukkit.getPluginManager().getPlugin("pandaraShop").getDataFolder(), "shops/" + uuid + ".yml");
        if (shopFile.exists()) {
            player.sendMessage(ChatColor.GREEN + "You already rent a shop, but here’s a random available one!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Here’s a random available shop! Use " + ChatColor.GOLD + "/shop rent");
        }
        player.sendMessage(ChatColor.GREEN + "There are currently " + ChatColor.GOLD + candidates.size() + ChatColor.GREEN + " available shops.");
    }
}
