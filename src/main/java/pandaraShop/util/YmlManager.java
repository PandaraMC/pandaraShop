package pandaraShop.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import pandaraShop.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class YmlManager {

    private Main main = Main.getInstance();
    private File file;
    private FileConfiguration config;

    public YmlManager(Plugin plugin, String path) {
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
        } catch (Exception e) {
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

    public YmlManager(Main main) {
        main.getConfig().options().copyDefaults();
        main.saveDefaultConfig();
    }

    public void backupWorldGuardRegions() {
        File backupFolder = new File(main.getDataFolder(), "region_backups");
        File regionsYml = new File("plugins/WorldGuard/worlds/shop/regions.yml");
        main.getLogger().info("Backup folder path: " + backupFolder.getAbsolutePath());

        // Check if the file exists
        if (!regionsYml.exists()) {
            main.getLogger().log(Level.WARNING, "WorldGuard regions.yml not found! Backup aborted.");
            return;
        }

        // Ensure backup folder exists
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            main.getLogger().log(Level.SEVERE, "Failed to create region_backups folder.");
            return;
        }

        // Create today's backup file
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File zipBackupFile = new File(backupFolder, "regionsBackup-" + date + ".zip");

        // Delete existing backup with today's date if it exists
        if (zipBackupFile.exists() && !zipBackupFile.delete()) {
            main.getLogger().log(Level.SEVERE, "Failed to delete existing region backup for today.");
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(zipBackupFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            zipSingleFile(regionsYml, "regions.yml", zos);
            main.getLogger().log(Level.INFO, "WorldGuard regions.yml backup created: " + zipBackupFile.getName());

        } catch (IOException e) {
            main.getLogger().log(Level.SEVERE, "Failed to back up regions.yml: " + e.getMessage(), e);
        }

        // Clean up old backups
        deleteOldRegionBackups(backupFolder);
    }

    private void deleteOldRegionBackups(File backupFolder) {
        File[] backups = backupFolder.listFiles((dir, name) -> name.startsWith("regionsBackup-") && name.endsWith(".zip"));

        if (backups != null && backups.length > 5) {
            Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified())); // oldest first
            for (int i = 0; i < backups.length - 5; i++) {
                if (backups[i].delete()) {
                    main.getLogger().log(Level.INFO, "Deleted old region backup: " + backups[i].getName());
                } else {
                    main.getLogger().log(Level.WARNING, "Failed to delete old region backup: " + backups[i].getName());
                }
            }
        }
    }

    // ✅ FIXED: Removed unnecessary FileOutputStream that was overwriting your source file
    private void zipSingleFile(File file, String fileName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }
    }

    public void ensureShopsFolderExists() {
        File shopsFolder = Main.getShopsDir(); // centralize path creation
        if (shopsFolder.exists()) {
            main.getLogger().info("'shops' folder already exists.");
        } else {
            main.getLogger().warning("Failed to create 'shops' folder.");
        }
    }

}
