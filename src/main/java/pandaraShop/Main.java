package pandaraShop;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import pandaraShop.commands.ShopCommandManager;
import pandaraShop.commands.ShopWorldTP;
import pandaraShop.handlers.ActivityCounter;
import pandaraShop.util.SchematicManager;
import pandaraShop.util.ShopTabCompleter;
import pandaraShop.util.YmlManager;

import java.io.File;

public final class Main extends JavaPlugin {

    public static Main plugin;
    public static Main getInstance() {return plugin;}
    public static StateFlag ALLOW_SHOP;

    private YmlManager ymlManager;

    private void logStartupMessage() {
        Bukkit.getLogger().info("█ █");
        Bukkit.getLogger().info("█ █ █ █");
        Bukkit.getLogger().info("█ █ █ █ █ █");
        Bukkit.getLogger().info("█ █ █ █ █ █ █ █");
        Bukkit.getLogger().info(" ");
        Bukkit.getLogger().info("LOADING pandaraShop");
        Bukkit.getLogger().info(" ");
        Bukkit.getLogger().info("█ █ █ █ █ █ █ █");
        Bukkit.getLogger().info("█ █ █ █ █ █");
        Bukkit.getLogger().info("█ █ █ █");
        Bukkit.getLogger().info("█ █");
    }

    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        logStartupMessage();
        registerCommands();
        registerEventListeners();
        // Initialize SchematicManager and ensure schematics exist
        SchematicManager schematicManager = new SchematicManager(this);
        schematicManager.ensureSchematicsExist();

        ymlManager = new YmlManager(this);
        ymlManager.ensureShopsFolderExists();

        new BukkitRunnable() {
            @Override
            public void run() {
                ymlManager.backupWorldGuardRegions();
            }
        }.runTaskTimer(this, 0L, 20L * 60L * 60L * 24L);

        // Retrieve the allow-shop flag after Shopkeepers registers it
        Flag<?> rawFlag = WorldGuard.getInstance().getFlagRegistry().get("allow-shop");

        if (rawFlag instanceof StateFlag) {
            ALLOW_SHOP = (StateFlag) rawFlag;
            getLogger().info("Successfully hooked into the allow-shop flag.");
        } else {
            getLogger().warning("Could not find the allow-shop flag. Is Shopkeepers installed?");
        }
    }

    private void registerCommands() {
        if (getCommand("shop") != null) {
            getCommand("shop").setExecutor(new ShopCommandManager());
            getCommand("shop").setTabCompleter(new ShopTabCompleter());
        } else {
            Bukkit.getLogger().severe("Command 'shop' not found in plugin.yml!");
        }
        if (getCommand("shopworld") != null) {
            getCommand("shopworld").setExecutor(new ShopWorldTP());
        } else {
            Bukkit.getLogger().severe("Command 'shopworld' not found in plugin.yml!");
        }
    }

    private void registerEventListeners() {
        Bukkit.getPluginManager().registerEvents(new ActivityCounter(), this);
    }

    public static File getShopsDir() {
        File dir = new File(getInstance().getDataFolder(), "shops");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Disabling pandaraShop...");
        // Add cleanup logic here, if necessary
    }
}
