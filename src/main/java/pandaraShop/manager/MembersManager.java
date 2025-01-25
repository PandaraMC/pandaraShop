package pandaraShop.manager;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class MembersManager {

    private static File file;
    private static FileConfiguration editFile;

    public static void onAdding(UUID uuid, RegionManager regions, String string) {
        Player player = Bukkit.getPlayer(uuid);
        Player member = Bukkit.getPlayerExact(string);
        if (player != null) {
            if (player.getLocation().getWorld().getName().equalsIgnoreCase("shop")) {
                file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), player.getUniqueId() + ".yml");
                if (file.exists()) {
                    if (member != null) {
                        if (!member.getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
                            if (member.isOnline()) {
                                ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getBlockX(),player.getLocation().getBlockY(),player.getLocation().getBlockZ()));
                                for (ProtectedRegion region : applicableRegionSet.getRegions()) {
                                    if (region.hasMembersOrOwners() && region.getMembers().contains(player.getUniqueId())) {
                                        editFile = YamlConfiguration.loadConfiguration(file);
                                        String owner = (String) editFile.get("Shop.Owner");
                                        assert owner != null;
                                        if (region.getMembers().contains(UUID.fromString(owner))) {
                                            if (region.getMembers().size() <= 3) {
                                                if (!region.getMembers().contains(member.getUniqueId())) {
                                                    DefaultDomain members = region.getMembers();
                                                    members.addPlayer(member.getUniqueId());
                                                    region.setMembers(members);
                                                    player.sendMessage(ChatColor.GREEN + "Additional member added!");
                                                    member.sendMessage(ChatColor.GREEN + "You have been added as a member of " + player.getName() + "'s shop!");
                                                }
                                                else {
                                                    player.sendMessage(ChatColor.GOLD + "This player is already member of your shop!");
                                                }
                                            }
                                            else {
                                                player.sendMessage(ChatColor.GOLD + "You already have an additional 2 members, remove a member first to add another!");
                                            }
                                        }
                                        else {
                                            player.sendMessage(ChatColor.GOLD + "Only the shop owner can add members!");
                                        }

                                    }
                                    else {
                                        player.sendMessage(ChatColor.GOLD + "You must stand in your shop to use this command!");
                                    }
                                }
                            }
                            else {
                                player.sendMessage(ChatColor.GOLD + "The member you are trying to add must be online!");
                            }
                        }
                        else {
                            player.sendMessage(ChatColor.RED + "You can't add yourself, you own this shop...");
                        }
                    }
                    else {
                        player.sendMessage(ChatColor.GOLD + string + " is not a valid player name or player not online. Please try again!");
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + "You don't own a shop!");
                }
            }else {
                player.sendMessage("You must stand in your shop to use this command!");
            }
        }
    }

    public static void onRemoving(UUID uuid, RegionManager regions, String string) {
        Player player = Bukkit.getPlayer(uuid);
        Player member = Bukkit.getPlayerExact(string);
        if (player != null) {
            if (player.getLocation().getWorld().getName().equalsIgnoreCase("shop")) {
                file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), player.getUniqueId() + ".yml");
                if (file.exists()) {
                    if (member != null) {
                        if (!member.getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
                            ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getBlockX(),player.getLocation().getBlockY(),player.getLocation().getBlockZ()));
                            for (ProtectedRegion region : applicableRegionSet.getRegions()) {
                                if (region.hasMembersOrOwners() && region.getMembers().contains(player.getUniqueId())) {
                                    editFile = YamlConfiguration.loadConfiguration(file);
                                    String owner = (String) editFile.get("Shop.Owner");
                                    assert owner != null;
                                    if (region.getMembers().contains(UUID.fromString(owner))) {
                                        if (region.getMembers().size() > 1) {
                                            if (region.getMembers().contains(member.getUniqueId())) {
                                                DefaultDomain members = region.getMembers();
                                                members.removePlayer(member.getUniqueId());
                                                region.setMembers(members);
                                                player.sendMessage(ChatColor.GREEN + "Additional member removed!");
                                                if (member.isOnline()) {
                                                    member.sendMessage(ChatColor.GREEN + "You have been removed as a member of " + player.getName() + "'s shop!");
                                                }
                                            }
                                            else {
                                                player.sendMessage(ChatColor.GOLD + "This player is not a member of your shop!");
                                            }
                                        }
                                        else {
                                            player.sendMessage(ChatColor.GOLD + "There are currently no additional members to your shop!");
                                        }
                                    }
                                    else {
                                        player.sendMessage(ChatColor.GOLD + "Only the shop owner can remove members!");
                                    }

                                }
                                else {
                                    player.sendMessage(ChatColor.GOLD + "You must stand in your shop to use this command!");
                                }
                            }
                        }
                        else {
                            player.sendMessage(ChatColor.RED + "You can't remove yourself!");
                        }
                    }
                    else {
                        player.sendMessage(ChatColor.GOLD + string + " is not a valid player name or player not online. Please try again!");
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + "You don't own a shop!");
                }
            }else {
                player.sendMessage("You must stand in your shop to use this command!");
            }
        }
    }
}
