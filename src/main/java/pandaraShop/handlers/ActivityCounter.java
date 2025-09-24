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
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pandaraShop.Main;
import pandaraShop.manager.UnrentShop;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ActivityCounter implements Listener {

    private final Main plugin;

    public ActivityCounter(Main plugin) {
        this.plugin = plugin;
    }

    // Start repeating task
    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                World shopWorld = Bukkit.getWorld("shop");
                if (shopWorld == null) return;

                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(BukkitAdapter.adapt(shopWorld));
                if (regions == null) return;

                long now = System.currentTimeMillis();

                // ✅ Part 1: Update active players' shop date
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().equals(shopWorld)) continue;

                    ApplicableRegionSet ars = regions.getApplicableRegions(
                            BlockVector3.at(player.getLocation().getBlockX(),
                                    player.getLocation().getBlockY(),
                                    player.getLocation().getBlockZ()));

                    for (ProtectedRegion region : ars.getRegions()) {
                        if (region.hasMembersOrOwners() && region.getMembers().contains(player.getUniqueId())) {
                            File file = new File(plugin.getDataFolder(), player.getUniqueId() + ".yml");
                            if (!file.exists()) continue;

                            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                            cfg.set("Shop.Date", now);

                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    cfg.save(file);
                                } catch (IOException e) {
                                    plugin.getLogger().warning("Failed to save shop file " + file.getName() + ": " + e.getMessage());
                                }
                            });
                        }
                    }
                }

                // ✅ Part 2: Check for expired shops
                File[] files = plugin.getDataFolder().listFiles((dir, name) -> name.endsWith(".yml"));
                if (files == null) return;

                for (File file : files) {
                    FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

                    if (!cfg.contains("Shop.Shopname") || !cfg.contains("Shop.Date") || !cfg.contains("Shop.Owner"))
                        continue;

                    long then = cfg.getLong("Shop.Date", now);
                    long days = TimeUnit.MILLISECONDS.toDays(now - then);

                    if (days >= 30) {
                        String ownerStr = cfg.getString("Shop.Owner");
                        if (ownerStr == null) continue;

                        try {
                            UUID owner = UUID.fromString(ownerStr);
                            UnrentShop.onUnrent(owner, regions);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid UUID in " + file.getName());
                        } catch (WorldEditException e) {
                            plugin.getLogger().severe("WorldEdit error while unrenting " + file.getName());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L * 30, 20L * 30); // run every 30s
    }
}
