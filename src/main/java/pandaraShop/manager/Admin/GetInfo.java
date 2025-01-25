package pandaraShop.manager.Admin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class GetInfo {

    private static File file;
    private static FileConfiguration editFile;

    public static void checkMe(UUID uuid, RegionManager regions) {

        ArrayList<String> rentedList = new ArrayList<>();
        ArrayList<String> unrentedList = new ArrayList<>();
        ArrayList<String> membersList = new ArrayList<>();
        Player player = Bukkit.getPlayer(uuid);
        int u = 0;

        if (player == null) {return;}

        for (Object names : regions.getRegions().keySet().toArray()) {
            if (names.toString().toLowerCase().contains("shop")) {
                ProtectedRegion rg = regions.getRegion(names.toString());
                if (rg.getId().toLowerCase().contains("shopa0") || rg.getId().toLowerCase().contains("shopb0") || rg.getId().toLowerCase().contains("shopc0") || rg.getId().toLowerCase().contains("shopd0")) {
                    u++;
                }
                if (rg != null && rg.hasMembersOrOwners()) {
                    rentedList.add(names.toString());
                }
                else {
                    unrentedList.add(names.toString());
                }
            }
        }

        rentedList.sort(String::compareToIgnoreCase);
        unrentedList.sort(String::compareToIgnoreCase);
        int rented = rentedList.size();
        int unrented = unrentedList.size();
        int r = (rented+unrented)-u;

        player.sendMessage(ChatColor.GOLD + ">> SHOP REGION LIST");
        player.sendMessage(ChatColor.GREEN + "There are currently ");
        player.sendMessage(ChatColor.GOLD + String.valueOf(rented) + ChatColor.AQUA + " rented shops and");
        player.sendMessage(ChatColor.GOLD + String.valueOf(unrented) + ChatColor.GRAY + " unrented shops.");
        player.sendMessage(ChatColor.GREEN + "For a total of "+ ChatColor.GOLD+u+ChatColor.GREEN+" ultra and "+ChatColor.GOLD+r+ChatColor.GREEN+" regular shops!");
        player.sendMessage(" ");

        file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), player.getUniqueId() + ".yml");
        if (file.exists()) {
            editFile = YamlConfiguration.loadConfiguration(file);
            int x = editFile.getInt("Shop.Center.x");
            int y = editFile.getInt("Shop.Center.y");
            int z = editFile.getInt("Shop.Center.z");
            ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(x,y,z));
            int count = 0;
            for (ProtectedRegion region : applicableRegionSet.getRegions()) {
                int size = region.getMembers().size();
                for (int c = 0; (c-size) <0; c++) {
                    String st = region.getMembers().toPlayersString().replace("uuid:","").split(",")[count];
                    Player pl = Bukkit.getPlayer(UUID.fromString(st));
                    if (pl != null && !pl.getName().equalsIgnoreCase(player.getName())) {
                        membersList.add(pl.getName());
                    }
                    count += 1;
                }
            }
            player.sendMessage(ChatColor.GOLD + "You currently own a shop!");
            player.sendMessage(ChatColor.GOLD + "To check your last activity type /shop checktime");
            if (membersList.size() >1) {
                player.sendMessage(ChatColor.GOLD + "The following players are members of your shop:");
                player.sendMessage(ChatColor.GREEN + membersList.toString().replace("[","").replace("]",""));
            }
            else {
                player.sendMessage(ChatColor.GOLD + "There are currently no additional members to your shop!");
            }
            membersList.clear();
            player.sendMessage(" ");
        }

        if (player.isOp() || player.hasPermission("pandara.manager")) {
            player.sendMessage(ChatColor.GOLD + "----- RENTED SHOP REGIONS -----");
            player.sendMessage(ChatColor.AQUA + " " + rentedList.toString().replace("[", "").replace("]",""));
            player.sendMessage(ChatColor.GOLD + "----- UNRENTED SHOP REGIONS -----");
            player.sendMessage(ChatColor.GRAY + " " + unrentedList.toString().replace("[", "").replace("]",""));
        }
    }
}
