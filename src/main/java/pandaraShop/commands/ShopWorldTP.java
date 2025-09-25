package pandaraShop.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class ShopWorldTP implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (sender instanceof Player player) {
            World world = Bukkit.getWorld("shop");
            if (world == null) return true;

            Location loc = new Location(world, 0.001, -19, 0.001, 0, 0);
            int n = new Random().nextInt(4);
            if (n == 0) loc.setYaw(0);
            if (n == 1) loc.setYaw(90);
            if (n == 2) loc.setYaw(180);
            if (n == 3) loc.setYaw(270);
            player.teleport(loc);
        }
        return true; // handled
    }
}
