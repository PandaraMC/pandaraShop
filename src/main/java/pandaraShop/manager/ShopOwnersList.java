package pandaraShop.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pandaraShop.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class ShopOwnersList {

    static ArrayList<String> list = new ArrayList<>();
    private static File file;
    private static FileConfiguration editFile;

    public static void listMe(UUID uuid) {

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {return;}
        String[] st = Main.getInstance().getDataFolder().list();
        if (st == null) {return;}
        list.clear();
        int length = st.length;
        int count = 0;
        for (int x = 0; (x-length) <0; x++) {
            String filename = Arrays.stream(st).toList().toString().replace("[","").replace("]","").split(", ")[count];
            file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), filename);
            if (file.exists()) {
                editFile = YamlConfiguration.loadConfiguration(file);
                String owner = (String) editFile.get("Shop.Shopname");
                if (owner != null) {
                    list.add(owner);
                }
            }
            count += 1;
        }
        if (list.isEmpty()) {
            player.sendMessage(" ");
            player.sendMessage(ChatColor.AQUA+ "List of rented shops!");
            player.sendMessage("============");
            player.sendMessage(ChatColor.GOLD+ "There are currently no shops rented!");
        }
        else {
            player.sendMessage(" ");
            player.sendMessage(ChatColor.AQUA+ "List of rented shops!");
            player.sendMessage("============");
            player.sendMessage(ChatColor.GREEN+ list.toString().replace("[","").replace("]",""));
            player.sendMessage("============");
            player.sendMessage(ChatColor.GOLD+ "Type /shop [PLAYERNAME] to teleport to their shop.");
        }
        list.clear();
    }

}
