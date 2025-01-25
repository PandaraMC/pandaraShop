package pandaraShop.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HelpManager {

    public static void onRequest(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}
        player.sendMessage(" ");
        player.sendMessage(ChatColor.GREEN + "SHOP HELP");
        player.sendMessage(ChatColor.GOLD+ "    - Rented shops will be removed after a period of \n       inactivity if owner doesn't visit the shop for \n       over 30 days.");
        player.sendMessage(ChatColor.GREEN + "============");
        player.sendMessage(" ");

        if (player.isOp() || player.hasPermission("pandara.manager")) {
            player.sendMessage(ChatColor.AQUA + "/shop info \n" + ChatColor.GREEN + "     - Shows rented/unrented shops info by shop region name.");
            player.sendMessage(ChatColor.AQUA + "/shop restoreflags \n" + ChatColor.GREEN + "     - restore flags for all unrented shops.");
            player.sendMessage(ChatColor.AQUA + "/shop create [NAME] \n" + ChatColor.GREEN + "     - Manually create a shop file for test purposes.");
            player.sendMessage(ChatColor.AQUA + "/shop remove [FILENAME] \n" + ChatColor.GREEN + "     - PERMANENTLY delete a shop file." + ChatColor.RED + " CAUTION IS ADVISED!");
            player.sendMessage(ChatColor.AQUA + "/shop listfiles \n" + ChatColor.GREEN + "     - Lists all shop files in the server.");
        }
        if (player.hasPermission("pandara.staff")) {
            player.sendMessage(ChatColor.AQUA + "/shop checktime [NAME] \n" + ChatColor.GREEN + "     - Shows the length of time since last owner activity.");
        }
        player.sendMessage(ChatColor.GOLD + "/shopworld \n" + ChatColor.GREEN + "     - Teleports you to shopworld.");
        player.sendMessage(ChatColor.AQUA + "/shop rent \n" + ChatColor.GREEN + "     - Rent an available shop. Large shop plots are reserved for " +ChatColor.RED+ "ULTRA RANK" +ChatColor.GREEN+ " only.");
        player.sendMessage(ChatColor.AQUA + "/shop unrent \n" + ChatColor.GREEN + "     - Unrent a shop." + ChatColor.RED + " CAUTION: Everything within the shop will be permanently deleted!");
        player.sendMessage(ChatColor.AQUA + "/shop settp \n" + ChatColor.GREEN + "     - Sets a teleport to your shop based on your location.");
        player.sendMessage(ChatColor.AQUA + "/shop [PLAYERNAME] \n" + ChatColor.GREEN + "     - Teleports you to that player shop should it exists.");
        player.sendMessage(ChatColor.AQUA + "/shop invite [PLAYERNAME] \n" + ChatColor.GREEN + "     - Adds a member to your shop.");
        player.sendMessage(ChatColor.AQUA + "/shop uninvite [PLAYERNAME] \n" + ChatColor.GREEN + "     - Removes a member from your shop.");
        player.sendMessage(ChatColor.AQUA + "/shop available \n" + ChatColor.GREEN + "     - Teleports you to a random available shop.");
        player.sendMessage(ChatColor.AQUA + "/shop checktime \n" + ChatColor.GREEN + "     - Shows the length of time since last owner activity.");

    }

    public static void onTerms(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}
        player.sendMessage(" ");
        player.sendMessage(ChatColor.GREEN + "SHOP TERMS");
        player.sendMessage(ChatColor.AQUA + "Those are the terms and rules that must be followed by every player.");
        player.sendMessage(ChatColor.GREEN + "============");
        player.sendMessage(ChatColor.GOLD+ "   1) "+ChatColor.GREEN+"Rented shops will be removed after a period of \n       inactivity if owner doesn't visit the shop for \n       over 30 days.");
        player.sendMessage(ChatColor.GOLD+ "   2) "+ChatColor.GREEN+"NO beacons allowed.");
        player.sendMessage(ChatColor.GOLD+ "   3) "+ChatColor.GREEN+"NO pistons allowed.");
        player.sendMessage(ChatColor.GOLD+ "   4) "+ChatColor.GREEN+"NO chunk hoppers allowed.");
        player.sendMessage(ChatColor.GOLD+ "   5) "+ChatColor.GREEN+"Lost items due to shop being unrented or \n       removed through inactivity cannot be \n       recovered.");
        player.sendMessage(ChatColor.GOLD+ "   4) "+ChatColor.GREEN+"You can add 2 additional members to your shop.");
        player.sendMessage(ChatColor.GOLD+ "   6) "+ChatColor.GREEN+"Aditional members of a shop cannot unrent it. \n       However, they will have all the same permissions \n       as you for everything else. \n       Add members at your own peril. ");
        player.sendMessage(ChatColor.GOLD+ "   7) "+ChatColor.GREEN+"Grief or lost items caused by additional shop \n       members cannot be recovered.");
        player.sendMessage(" ");

    }
}
