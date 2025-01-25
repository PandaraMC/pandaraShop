package pandaraShop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pandaraShop.commands.shopCommandManager;
import pandaraShop.util.SchematicManager;
import pandaraShop.util.shopTabCompleter;
import pandaraShop.commands.shopWorldTP;
import pandaraShop.handlers.activityCounter;
import pandaraShop.util.YmlManager;

public final class Main extends JavaPlugin {

    private static Main plugin;
    public static Main getInstance() {return plugin;}

    public static YmlManager ymlManager;

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
        SchematicManager schematicManager = new SchematicManager(this);
        schematicManager.ensureSchematicsExist();
    }

    private void registerCommands() {
        if (getCommand("shop") != null) {
            getCommand("shop").setExecutor(new shopCommandManager());
            getCommand("shop").setTabCompleter(new shopTabCompleter());
        } else {
            Bukkit.getLogger().severe("Command 'shop' not found in plugin.yml!");
        }
        if (getCommand("shopworld") != null) {
            getCommand("shopworld").setExecutor(new shopWorldTP());
        } else {
            Bukkit.getLogger().severe("Command 'shopworld' not found in plugin.yml!");
        }
    }

    private void registerEventListeners() {
        Bukkit.getPluginManager().registerEvents(new activityCounter(), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Disabling pandaraShop...");
        // Add cleanup logic here, if necessary
    }
}
