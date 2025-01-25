package pandaraShop.commands;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;

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
            // Commands below are available for all players -----------------------------------------------------------
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                ShopTP.tpMe(player.getUniqueId(),target.getUniqueId());
            } else {
                switch (args[0].toLowerCase()) {
                    case "rent":
                        //Rent a shop
                        RentShop.onRent(player.getUniqueId(),regions);
                        break;
                    case "unrent":
                        //Unrent a shop
                        try {
                            UnrentShop.onUnrent(player.getUniqueId(),regions);
                        } catch (WorldEditException e) {
                            Bukkit.getLogger().severe("Error during shop unrent: " + e.getMessage());
                        }
                        break;
                    case "help":
                        //Display help for user
                        HelpManager.onRequest(player.getUniqueId());
                        break;
                    case "settp":
                        //Set shop tp (Player)
                        SetTP.setTP(player.getUniqueId());
                        break;
                    case "list":
                        //List names of shop owners
                        ShopOwnersList.listMe(player.getUniqueId());
                        break;
                    case "available":
                        //Random TP to an available shop
                        ShopAvailable.rtp(player.getUniqueId(),regions);
                        break;
                    case "terms":
                        //Shop terms
                        HelpManager.onTerms(player.getUniqueId());
                        break;
                    case "checktime":
                        //Check rented time of sender's shop
                        CheckTime.checkMe(player.getUniqueId(),player.getName());
                        break;
                    // Commands for Managers --------------------------------------------------------------------------
                    case "info":
                        // Count the shops and list which shops are rented and unrented.
                        if (player.hasPermission("pandara.manager")) {
                            GetInfo.checkMe(player.getUniqueId(),regions);
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't look like you have this permission...");
                            return true;
                        }
                        break;
                    case "restoreflags":
                        // Set flags to default for all un-rented shops.
                        if (player.hasPermission("pandara.manager")) {
                            RestoreFlags.restore(player.getUniqueId(), regions);
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't look like you have this permission...");
                            return true;
                        }
                        break;
                    case "listfiles":
                        //List all files in folder
                        if (player.hasPermission("pandara.manager")) {
                            ListFiles.check(player.getUniqueId());
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't look like you have this permission...");
                            return true;
                        }
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Unknown command. Type /shop help for a list of commands.");
                        break;
                }
            }
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
