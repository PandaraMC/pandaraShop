package pandaraShop.commands;

import com.sk89q.worldedit.WorldEditException;
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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pandaraShop.manager.Admin.*;
import pandaraShop.manager.*;

public class ShopCommandManager implements CommandExecutor {

    private static final World world = Bukkit.getWorld("shop");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Check if player is a real player.
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        // Check if world is valid and if the world has a region as per World edit.
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "The shop world is not loaded.");
            return true;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) {
            player.sendMessage(ChatColor.RED + "No region manager found for the shop world.");
            return true;
        }

        // Handle subcommands
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Please specify a subcommand. Type /shop help for help.");
            return true;
        }

        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            switch (arg) {
                case "rent":
                    if (!player.hasPermission("pandara.settler")) {
                        player.sendMessage(ChatColor.RED + "You must be rank SETTLER to rent a shop.");
                        return true;
                    }
                    RentShop.onRent(player.getUniqueId(), regions);
                    break;
                case "unrent":
                    try {
                        UnrentShop.onUnrent(player.getUniqueId(), regions);
                    } catch (WorldEditException e) {
                        Bukkit.getLogger().severe("Error during shop unrent: " + e.getMessage());
                    }
                    break;
                case "help":
                    HelpManager.onRequest(player.getUniqueId());
                    break;
                case "settp":
                    SetTP.setTP(player.getUniqueId());
                    break;
                case "list":
                    ShopOwnersList.listMe(player.getUniqueId());
                    break;
                case "available":
                    ShopAvailable.rtp(player.getUniqueId(), regions);
                    break;
                case "terms":
                    HelpManager.onTerms(player.getUniqueId());
                    break;
                case "checktime":
                    CheckTime.checkMe(player.getUniqueId(), player.getName());
                    break;
                case "info":
                    GetInfo.checkMe(player.getUniqueId(), regions);
                    break;
                case "restoreflags":
                    if (player.hasPermission("pandara.manager")) {
                        RestoreFlags.restore(player.getUniqueId(), regions);
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't look like you have this permission...");
                    }
                    break;
                case "listfiles":
                    if (player.hasPermission("pandara.manager")) {
                        ListFiles.check(player.getUniqueId());
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't look like you have this permission...");
                    }
                    break;
                case "restore":
                    Location loc = player.getLocation();
                    ApplicableRegionSet regionSet = regions.getApplicableRegions(
                            BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
                    );

                    ProtectedRegion standingRegion = null;
                    for (ProtectedRegion region : regionSet) {
                        if (region.getId().toLowerCase().startsWith("shop")) {
                            standingRegion = region;
                            break;
                        }
                    }

                    if (standingRegion == null) {
                        player.sendMessage(ChatColor.RED + "You are not standing inside a shop region.");
                        return true; // stop execution
                    }

                    if (player.hasPermission("pandara.manager")) {

                        try {
                            UnrentShop.onAdminUnrent(player.getUniqueId(),regions, standingRegion);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        }

                        break;

                    } else {
                        player.sendMessage(ChatColor.RED + "You don't look like you have this permission...");
                        break;
                    }
                case "cleanfiles":
                    if (player.hasPermission("pandara.manager")) {
                        RemoveFile.cleanFiles();
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't look like you have this permission...");
                    }
                    break;
                default:
                    // Only try teleport if it's not a known subcommand
                    ShopTP.tpMe(player.getUniqueId(), arg);
                    break;
            }
            return true;
        }
        if (args.length == 2) {
            String string = args[1];
            Player target = Bukkit.getPlayer(string);
            // Commands below are available for all players -----------------------------------------------------------
            switch (args[0].toLowerCase()) {
                case "invite":
                    //Add a friend as a shopmember
                    if (target != null) {
                        MembersManager.onAdding(player.getUniqueId(),regions,string);
                    } else {
                        player.sendMessage(ChatColor.RED + string + " isn't a player. Please try again with the correct player name.");
                    }
                    break;
                case "uninvite":
                    //Remove a friend as a shopmember
                    if (target != null) {
                        MembersManager.onRemoving(player.getUniqueId(),regions,string);
                    } else {
                        player.sendMessage(ChatColor.RED + string + " isn't a player or isn't in your list. Please try again with the correct player name.");
                    }
                    break;
                // Commands for all staff -----------------------------------------------------------------------------
                case "checktime":
                    //Check rented time of player's shop
                    if (player.hasPermission("pandara.staff")) {
                        CheckTime.checkMe(player.getUniqueId(),string);
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't look like you have this permission...");
                        return true;
                    }
                    break;
                // Commands for OP only! ------------------------------------------------------------------------------
                case "create":
                    //Manually create a test file, /shop create [NAME]
                    if (player.isOp()) {
                        CreateFile.manualCreate(player.getUniqueId(),string + "-mock");
                        player.sendMessage(ChatColor.GREEN + "Mock file created successfully.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Woa champion, only the most chubby of them all can do that.");
                        return true;
                    }
                    break;
                case "remove":
                    //Manually remove a file /shop remove [NAME]
                    if (player.isOp()) {
                        RemoveFile.manualRemove(player.getUniqueId(), string);
                        player.sendMessage(ChatColor.GREEN + "Mock file removed successfully.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Woa champion, only the most chubby of them all can do that.");
                        return true;
                    }
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown command. Type /shop help for a list of commands.");
                    break;
            }

        }
        return false;
    }
}
