package pandaraShop.util;

import pandaraShop.Main;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SchematicManager {

    private final Main plugin;

    public SchematicManager(Main plugin) {
        this.plugin = plugin;
    }

    public void ensureSchematicsExist() {
        // The target folder where schematics should be
        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        // Ensure the schematics folder exists
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs(); // Create the folder if it doesn’t exist
        }

        // List of schematic files to check and restore
        String[] schematicFiles = {"large.schem", "small.schem"};

        for (String schematicFile : schematicFiles) {
            File targetFile = new File(schematicsFolder, schematicFile);

            // If the schematic file is missing, copy it from resources
            if (!targetFile.exists()) {
                plugin.getLogger().warning("Schematic file missing: " + schematicFile + ". Restoring...");
                copyResourceToFile("schematics/" + schematicFile, targetFile);
            }
        }
    }

    private void copyResourceToFile(String resourcePath, File targetFile) {
        try (InputStream in = plugin.getResource(resourcePath);
             FileOutputStream out = new FileOutputStream(targetFile)) {

            if (in == null) {
                plugin.getLogger().severe("Failed to find resource: " + resourcePath);
                return;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            plugin.getLogger().info("Successfully restored " + targetFile.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to restore schematic: " + resourcePath);
            logger.error("Failed to restore schematic: {}", file.getName(), e);
        }
    }
}
