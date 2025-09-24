package pandaraShop.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopTabCompleter implements TabCompleter {

    private List<String> getOnlinePlayerNames() {
        List<String> playerNames = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            playerNames.add(onlinePlayer.getName());
        }
        return playerNames;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player player)) {
            return Collections.emptyList(); // No suggestions for non-players
        }

        List<String> suggestions = new ArrayList<>();

        // Handle the first argument (subcommands or player names)
        if (args.length == 1) {
            // Collect all possible subcommands
            if (player.hasPermission("pandara.manager")) {
                suggestions.add("cleanfiles");
                suggestions.add("info");
                suggestions.add("restoreflags");
                suggestions.add("restore");
                suggestions.add("listfiles");
            }
            if (player.isOp()) {
                suggestions.add("create");
                suggestions.add("remove");
                suggestions.add("reloadschem");
            }
            if (player.hasPermission("pandara.staff")) {
                suggestions.add("checktime");
            }
            suggestions.add("rent");
            suggestions.add("unrent");
            suggestions.add("help");
            suggestions.add("available");
            suggestions.add("terms");

            // Combine subcommands with online player names
            suggestions.addAll(getOnlinePlayerNames());

            // Filter based on current input
            return filterSuggestions(args[0], suggestions);
        }

        // Handle the second argument (specific subcommands with player names)
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite":
                case "uninvite":
                case "checktime":
                    // Only suggest player names for these subcommands
                    suggestions.addAll(getOnlinePlayerNames());
                    return filterSuggestions(args[1], suggestions);
                default:
                    return Collections.emptyList(); // No suggestions for other subcommands
            }
        }

        return Collections.emptyList(); // No suggestions for other argument lengths
    }

    // Utility method to filter suggestions
    private List<String> filterSuggestions(String input, List<String> suggestions) {
        List<String> filtered = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }
}
