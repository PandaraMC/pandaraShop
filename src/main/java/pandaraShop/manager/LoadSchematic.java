package pandaraShop.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LoadSchematic {

    private static File file;

    public static void place(Location loc, String size) throws WorldEditException {

        World world = Bukkit.getWorld("shop");
        if (world == null) {return;}

        file = new File(Bukkit.getServer().getPluginManager().getPlugin("pandaraShop").getDataFolder(), "/schematics/"+size+".schem");
        Clipboard clipboard = null;
        ClipboardFormat format = ClipboardFormats.findByFile(file);

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
            Bukkit.getLogger().info("Test01");
            Bukkit.getLogger().info("Name: " + format.getPrimaryFileExtension());
            Bukkit.getLogger().info("Clipboard min point: " + clipboard.getMinimumPoint());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (clipboard == null) {
            Bukkit.getLogger().info("The clipboard is null");
            return;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(x, y, z))
                    // configure here
                    .build();
            Operations.complete(operation);
        }
    }

    //Reload schematics after a shop is unrented or restored.
}
