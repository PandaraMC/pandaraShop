package pandaraShop.handlers;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ActivityCounter implements Listener {

    private final Main plugin = Main.getInstance();
    private final File shopsDir;

    public ActivityCounter() {
        this.shopsDir = Main.getShopsDir();
        // Run on the main thread to remain Bukkit/WorldGuard safe
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    updateOwnerActivityIfInShop();
                    // NEW: also keep names in sync even if the owner isn't online/in the shop
                    syncAllOwnerNames();
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

            // NEW: when we see the owner in their shop, also ensure name syncs
            ensureYamlOwnerName(f, cfg, player.getUniqueId());
            refreshWGDomainsFor(player.getUniqueId(), regions);
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

    /**
     * NEW: Scan all shop files and keep names in sync even if the owner isn't present.
     * - Updates YAML Shop.OwnerName if changed
     * - Refreshes WG owners/members entries so regions.yml holds latest name for UUID
     */
    private void syncAllOwnerNames() {
        World shopWorld = Bukkit.getWorld("shop");
        if (shopWorld == null) return;

        RegionManager regions = WorldGuard.getInstance()
                .getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(shopWorld));
        if (regions == null) return;

        File[] files = shopsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) return;

        for (File f : files) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
            String ownerStr = fc.getString("Shop.Owner", null);
            if (ownerStr == null) continue;

            UUID ownerUuid;
            try {
                ownerUuid = UUID.fromString(ownerStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[namesync] Invalid UUID in " + f.getName() + " at Shop.Owner: " + ownerStr);
                continue;
            }

            // Keep YAML name fresh
            ensureYamlOwnerName(f, fc, ownerUuid);
            // Keep WG domains' displayed name fresh (owners & members)
            refreshWGDomainsFor(ownerUuid, regions);
        }
    }

    // === BEGIN: Name sync helpers (ADD-ONLY) ===

    /** Get RegionManager for a world name. If you already have one, you can reuse it and ignore this. */
    private RegionManager getRegionManager(String worldName) {
        var world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) return null;
        return WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(world));
    }

    /** Ensure YAML has the latest owner name; returns true if it wrote a change. */
    private boolean ensureYamlOwnerName(File yamlFile, org.bukkit.configuration.file.FileConfiguration fc, java.util.UUID ownerUuid) {
        OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(ownerUuid);
        String current = op.getName(); // may be null if not known
        if (current == null || current.isEmpty()) return false;

        String stored = fc.getString("Shop.OwnerName", "");
        if (current.equals(stored)) return false;

        fc.set("Shop.OwnerName", current);
        try {
            fc.save(yamlFile);
            Bukkit.getLogger().info("[namesync] Updated YAML owner name -> " + current + " in " + yamlFile.getName());
            return true;
        } catch (java.io.IOException ex) {
            Bukkit.getLogger().warning("[namesync] Failed to save owner name in " + yamlFile.getName() + ": " + ex.getMessage());
            return false;
        }
    }

    /**
     * Refresh WorldGuard domains so regions.yml stores the latest name for this UUID.
     * We remove+re-add the same UUID via OfflinePlayer for BOTH owners and members.
     * Saves RegionManager if anything changed.
     */
    private void refreshWGDomainsFor(java.util.UUID uuid, RegionManager rm) {
        if (rm == null || uuid == null) return;

        OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        String nameForLog = op.getName() != null ? op.getName() : "(unknown)";
        boolean changed = false;

        for (Map.Entry<String, ProtectedRegion> entry : rm.getRegions().entrySet()) {
            ProtectedRegion r = entry.getValue();

            DefaultDomain owners = r.getOwners();
            DefaultDomain members = r.getMembers();

            boolean touched = false;

            if (owners.getUniqueIds().contains(uuid)) {
                owners.removePlayer(uuid);
                owners.addPlayer(uuid); // re-add with latest name
                r.setOwners(owners);
                touched = true;
            }

            if (members.getUniqueIds().contains(uuid)) {
                members.removePlayer(uuid);
                members.addPlayer(uuid); // re-add with latest name
                r.setMembers(members);
                touched = true;
            }

            if (touched) {
                changed = true;
                Bukkit.getLogger().info("[namesync] Refreshed WG domain for " + nameForLog +
                        " (" + uuid + ") in region " + r.getId());
            }
        }

        if (changed) {
            try {
                rm.save();
            } catch (Exception ex) {
                Bukkit.getLogger().warning("[namesync] Failed to save RegionManager: " + ex.getMessage());
            }
        }
    }
    // === END: Name sync helpers ===
}
