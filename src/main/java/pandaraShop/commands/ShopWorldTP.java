package pandaraShop.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ShopWorldTP implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {
            Location loc = new Location(Bukkit.getWorld("shop"),0.001f,-19,0.001f,0,0);
            Random rand = new Random();
            int n = rand.nextInt(4) + 1;
            if (n == 1) {loc.setYaw(90);}
            if (n == 2) {loc.setYaw(180);}
            if (n == 3) {loc.setYaw(270);}
            player.teleport(loc);
        }

        return false;
    }
}
