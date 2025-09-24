package pandaraShop.manager;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pandaraShop.Main;
import pandaraShop.manager.Admin.CreateFile;

import java.io.File;
import java.util.UUID;

public class RentShop {

    public static void onRent(UUID uuid, RegionManager regions) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        // Check if this player already has a shop file
        File file = new File(Main.getInstance().getDataFolder(), uuid + ".yml");
        if (file.exists()) {
            player.sendMessage(ChatColor.RED + "You already rent a shop. Unrent it first and try again.");
            return;
        }

        ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(
                BlockVector3.at(player.getLocation().getBlockX(),
                        player.getLocation().getBlockY(),
                        player.getLocation().getBlockZ()));

        for (ProtectedRegion region : applicableRegionSet.getRegions()) {
            if (region.hasMembersOrOwners()) {
                if (region.getMembers().contains(player.getUniqueId()) || region.getOwners().contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You already rent this shop!");
                } else {
                    player.sendMessage(ChatColor.RED + "You can't rent a rented shop!");
                }
                continue;
            }

            boolean isUltraRegion = region.getId().toLowerCase().matches("shop[abcd]0.*");
            if (isUltraRegion) {
                if (player.hasPermission("pandara.ultra") || player.isOp()) {
                    rentShop(player, region, "large", ChatColor.GREEN);
                } else {
                    player.sendMessage(ChatColor.RED + "You must be rank ULTRA to rent this shop!");
                }
            } else {
                if (!player.hasPermission("pandara.ultra") || player.isOp()) {
                    rentShop(player, region, "small", ChatColor.RED);
                } else {
                    player.sendMessage(ChatColor.RED + "Your rank is too high to rent this shop! Please use an ULTRA shop area.");
                }
            }
        }
    }

    private static void rentShop(Player player, ProtectedRegion region, String size, ChatColor color) {
        // Owners and members should be separate
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(player.getUniqueId());
        region.setOwners(owners);

        DefaultDomain members = region.getMembers();
        members.addPlayer(player.getUniqueId());
        region.setMembers(members);

        // Calculate center
        int x = (region.getMaximumPoint().getBlockX() + region.getMinimumPoint().getBlockX()) / 2;
        int y = (region.getMaximumPoint().getBlockY() + region.getMinimumPoint().getBlockY()) / 2;
        int z = (region.getMaximumPoint().getBlockZ() + region.getMinimumPoint().getBlockZ()) / 2;

        // Create shop file
        CreateFile.autoCreate(player.getUniqueId(), x, y, z, size);

        // Set greeting message
        region.setFlag(Flags.GREET_MESSAGE, "&3Welcome to " + color + player.getName() + "&3's Shop!");

        player.sendMessage(ChatColor.GREEN + "You now rent this shop. Type /shop terms to read the terms of your rent!");
    }
}
