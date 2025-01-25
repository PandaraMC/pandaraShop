package pandaraShop.util;

import pandaraShop.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class YmlManager {

    private Main main = Main.getInstance();
    private File file;
    private FileConfiguration config;

    public YmlManager (Plugin plugin, String path) {
        this(plugin.getDataFolder().getAbsolutePath() + "/" + path);
    }

    public YmlManager(String path) {
        this.file = new File(path);
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public boolean save() {
        try {
            this.config.save(this.file);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public File getFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public YmlManager (Main main) {
        main.getConfig().options().copyDefaults();
        main.saveDefaultConfig();
    }



}
