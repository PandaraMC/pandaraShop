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
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
        return names;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
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

            suggestions.addAll(getOnlinePlayerNames());
            return filterSuggestions(args[0], suggestions);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite":
                case "uninvite":
                case "checktime":
                    suggestions.addAll(getOnlinePlayerNames());
                    return filterSuggestions(args[1], suggestions);
                default:
                    return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterSuggestions(String input, List<String> suggestions) {
        List<String> out = new ArrayList<>();
        String lower = input.toLowerCase();
        for (String s : suggestions) {
            if (s.toLowerCase().startsWith(lower)) out.add(s);
        }
        return out;
    }
}
