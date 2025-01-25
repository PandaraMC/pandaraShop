package pandaraShop.manager.Admin;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RestoreFlags {

    public static void restore(UUID uuid, RegionManager regions) {

        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {return;}

        for (Object names : regions.getRegions().keySet().toArray()) {
            if (names.toString().toLowerCase().contains("shop")) {
                ProtectedRegion rg = regions.getRegion(names.toString());
                if (rg != null) {

                    rg.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.MEMBERS);

                    rg.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.BLOCK_BREAK.getRegionGroupFlag(),RegionGroup.MEMBERS);

                    rg.setFlag(Flags.BLOCK_PLACE, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.BLOCK_PLACE.getRegionGroupFlag(),RegionGroup.MEMBERS);

                    rg.setFlag(Flags.USE, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.USE.getRegionGroupFlag(),RegionGroup.MEMBERS);

                    rg.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.CHEST_ACCESS.getRegionGroupFlag(),RegionGroup.MEMBERS);

                    if (names.toString().toLowerCase().contains("shopa0") || names.toString().toLowerCase().contains("shopb0") || names.toString().toLowerCase().contains("shopc0") || names.toString().toLowerCase().contains("shopd0")) {
                        if (!rg.hasMembersOrOwners()) {
                            rg.setFlag(Flags.GREET_MESSAGE,"&3This shop is available for &4ULTRA &3ranks only. Type &6/shop rent &3to rent it!");
                        }
                    }
                    else {
                        if (!rg.hasMembersOrOwners()) {
                            rg.setFlag(Flags.GREET_MESSAGE,"&aThis shop is available. Type &6/shop rent &ato rent it!");
                        }
                    }
                    rg.setPriority(20);
                }
            }
        }

        player.sendMessage(ChatColor.GOLD + "All shop flags restored!");
    }

    public static void consoleRestore(RegionManager regions) {

        for (Object names : regions.getRegions().keySet().toArray()) {
            if (names.toString().toLowerCase().contains("shop")) {
                ProtectedRegion rg = regions.getRegion(names.toString());
                if (rg != null) {

                    rg.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.MEMBERS);

                    rg.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.BLOCK_BREAK.getRegionGroupFlag(),RegionGroup.MEMBERS);

                    rg.setFlag(Flags.BLOCK_PLACE, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.BLOCK_PLACE.getRegionGroupFlag(),RegionGroup.MEMBERS);

                    rg.setFlag(Flags.USE, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.USE.getRegionGroupFlag(),RegionGroup.MEMBERS);

                    rg.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
                    rg.setFlag(Flags.CHEST_ACCESS.getRegionGroupFlag(),RegionGroup.MEMBERS);

                    if (names.toString().toLowerCase().contains("shopa0") || names.toString().toLowerCase().contains("shopb0") || names.toString().toLowerCase().contains("shopc0")) {
                        if (!rg.hasMembersOrOwners()) {
                            rg.setFlag(Flags.GREET_MESSAGE,"&3This shop is available for &4ULTRA &3ranks only. Type /shop rent to rent it!");
                        }
                    }
                    else {
                        if (!rg.hasMembersOrOwners()) {
                            rg.setFlag(Flags.GREET_MESSAGE,"&aThis shop is available. Type /shop rent to rent it!");
                        }
                    }
                    rg.setPriority(20);
                }
            }
        }
    }
}
