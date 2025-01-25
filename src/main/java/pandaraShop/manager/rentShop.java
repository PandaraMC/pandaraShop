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
import pandaraShop.manager.Admin.createFile;

import java.util.Arrays;
import java.util.UUID;

public class rentShop {

    public static void onRent(UUID uuid, RegionManager regions) {

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}

        ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getBlockX(),player.getLocation().getBlockY(),player.getLocation().getBlockZ()));
        String[] st = Main.getInstance().getDataFolder().list();

        if (Arrays.toString(st).contains(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "You already rent a shop. Unrent it first and try again.");
        }
        else {
            for (ProtectedRegion region : applicableRegionSet.getRegions()) {

                if (region.hasMembersOrOwners()) {
                    if (region.getMembers().contains(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You already rent this shop!");
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "You can't rent a rented shop!");
                    }

                }
                if (region.getId().toLowerCase().contains("shopa0") || region.getId().toLowerCase().contains("shopb0") || region.getId().toLowerCase().contains("shopc0") || region.getId().toLowerCase().contains("shopd0")) {
                    if (player.hasPermission("pandara.ultra")) {
                        // rent stuff
                        DefaultDomain members = region.getMembers();
                        members.addPlayer(player.getUniqueId());
                        region.setMembers(members);
                        String string = player.getName();
                        int x = ((region.getMaximumPoint().getBlockX()+region.getMinimumPoint().getBlockX())/2);
                        int y = ((region.getMaximumPoint().getBlockY()+region.getMinimumPoint().getBlockY())/2);
                        int z = ((region.getMaximumPoint().getBlockZ()+region.getMinimumPoint().getBlockZ())/2);
                        createFile.autoCreate(player.getUniqueId(),x,y,z,"large");
                        region.setFlag(Flags.GREET_MESSAGE,"&3Welcome to &4" + string + " &3's Shop'!");
                        player.sendMessage(ChatColor.GREEN + "You now rent this shop. Type /shop terms to read the terms of your rent!");
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "You must be rank ULTRA to rent this shop! Alternatively, please see a member of staff.");
                    }
                }
                else {
                    if (!player.hasPermission("pandara.ultra")) {
                        DefaultDomain members = region.getMembers();
                        members.addPlayer(player.getUniqueId());
                        region.setMembers(members);
                        String string = player.getName();
                        int x = ((region.getMaximumPoint().getBlockX()+region.getMinimumPoint().getBlockX())/2);
                        int y = ((region.getMaximumPoint().getBlockY()+region.getMinimumPoint().getBlockY())/2);
                        int z = ((region.getMaximumPoint().getBlockZ()+region.getMinimumPoint().getBlockZ())/2);
                        createFile.autoCreate(player.getUniqueId(),x,y,z,"small");
                        region.setFlag(Flags.GREET_MESSAGE,"&3Welcome to &4" + string + " &3's Shop'!");
                        player.sendMessage(ChatColor.GREEN + "You now rent this shop. Type /shop terms to read the terms of your rent!");
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "Your rank is too high to rent this shop! Please try again in an " + ChatColor.BOLD + "Ultra" + ChatColor.RESET + ChatColor.RED + " shop area or alternatively, please see a member of staff.");
                    }
                }
            }
        }

    }
}
