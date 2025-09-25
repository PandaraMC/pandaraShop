package pandaraShop.handlers;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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

    private final Main plugin = Main.getInstance();
    private final File shopsDir;

    public ActivityCounter() {
        this.shopsDir = new File(plugin.getDataFolder(), "shops");
        // Run on the main thread to remain Bukkit/WorldGuard safe
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    updateOwnerActivityIfInShop();
                    unrentExpiredShops();
                } catch (Exception ex) {
                    plugin.getLogger().severe("ActivityCounter task error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.runTaskTimer(plugin, 20L * 60L, 20L * 60L); // every 60s
    }

    private void updateOwnerActivityIfInShop() {
        World shopWorld = Bukkit.getWorld("shop");
        if (shopWorld == null) return;

        RegionManager regions = WorldGuard.getInstance()
                .getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(shopWorld));
        if (regions == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOnline() || player.getWorld() != shopWorld) continue;

            ApplicableRegionSet set = regions.getApplicableRegions(BlockVector3.at(
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(),
                    player.getLocation().getBlockZ()));

            boolean isOwnerHere = false;
            for (ProtectedRegion region : set) {
                if (region.getOwners().contains(player.getUniqueId())) {
                    isOwnerHere = true;
                    break;
                }
            }
            if (!isOwnerHere) continue;

            File f = new File(shopsDir, player.getUniqueId() + ".yml");
            if (!f.exists()) continue;

            FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            cfg.set("Shop.Date", System.currentTimeMillis());
            try {
                cfg.save(f);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save shop file: " + f.getName());
            }
        }
    }

    private void unrentExpiredShops() {
        World shopWorld = Bukkit.getWorld("shop");
        if (shopWorld == null) return;

        RegionManager regions = WorldGuard.getInstance()
                .getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(shopWorld));
        if (regions == null) return;

        File[] files = shopsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        long now = System.currentTimeMillis();
        long cutoff = TimeUnit.DAYS.toMillis(30);

        for (File f : files) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            if (!cfg.contains("Shop.Date") || !cfg.contains("Shop.Owner")) continue;

            long then = cfg.getLong("Shop.Date", 0L);
            if (then <= 0L) continue;

            if ((now - then) >= cutoff) {
                String ownerStr = cfg.getString("Shop.Owner");
                if (ownerStr == null) continue;
                try {
                    UUID owner = UUID.fromString(ownerStr);
                    UnrentShop.onUnrent(owner, regions); // runs on main thread
                } catch (WorldEditException | IllegalArgumentException ex) {
                    plugin.getLogger().warning("Failed to auto-unrent " + f.getName() + ": " + ex.getMessage());
                }
            }
        }
    }
}
