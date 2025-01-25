package pandaraShop.handlers;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pandaraShop.Main;
import pandaraShop.manager.unrentShop;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class activityCounter implements Listener {

    private static File file;
    private static FileConfiguration editFile;

    //Counts & update shop owner's activities in days and remove unmaintained shops
    public activityCounter() {

        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != null) {
                        if (player.getLocation().getWorld() != null && player.getLocation().getWorld().getName().equalsIgnoreCase("shop")) {
                            file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), player.getUniqueId() + ".yml");
                            if (file.exists()) {
                                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld("shop")));
                                if (regions == null) continue;
                                ApplicableRegionSet ars = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getBlockX(),player.getLocation().getBlockY(),player.getLocation().getBlockZ()));
                                for (ProtectedRegion region : ars.getRegions()) {
                                    if (region.hasMembersOrOwners() && region.getMembers().contains(player.getUniqueId())) {
                                        editFile = YamlConfiguration.loadConfiguration(file);
                                        long time = System.currentTimeMillis();
                                        editFile.set("Shop.Date",time);
                                        try {
                                            editFile.save(file);
                                        } catch (IOException e)  {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                String[] st = Main.getInstance().getDataFolder().list();
                if (st == null) {return;}
                int length = st.length;
                int count = 0;
                for (int x = 0; (x-length) <0; x++) {
                    String filename = Arrays.stream(st).toList().toString().replace("[","").replace("]","").split(", ")[count];
                    file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), filename);
                    if (file.exists()) {
                        if (file.getName().contains(".yml")) {
                            editFile = YamlConfiguration.loadConfiguration(file);
                            if (editFile.contains("Shop.Shopname") && editFile.contains("Shop.Date")) {
                                String owner = (String) editFile.get("Shop.Owner");

                                long now = System.currentTimeMillis();
                                long then = (Long) editFile.get("Shop.Date");
                                long result = now-then;

                                long d = TimeUnit.MILLISECONDS.toDays(result);
                                result -= TimeUnit.DAYS.toMillis(d);
                                long h = TimeUnit.MILLISECONDS.toHours(result);
                                result -= TimeUnit.HOURS.toMillis(h);
                                long m = TimeUnit.MILLISECONDS.toMinutes(result);

                                if (d >= 30) {
                                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                    RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld("shop")));
                                    try {
                                        unrentShop.onUnrent(UUID.fromString(owner),regions);
                                    } catch (WorldEditException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                    }
                    count += 1;
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(),0,20L);
    }
}
