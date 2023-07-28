package me.nonetaken.ghostblocklib.event;

import lombok.Getter;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.material.MaterialData;

/*
 * Project: me.nonetaken.ghostblocklib.event | Author: NoneTaken#0001
 * Created: 28/07/2023 at 18:41
 */
@Getter
public class BlockPlaceAgainstGhostBlockEvent extends WrappedBukkitEvent implements Cancellable {

    private final Player player;
    private final GhostBlockCuboid cuboid;
    private final Location location;
    private final MaterialData materialData;
    private boolean cancelled = false;

    public BlockPlaceAgainstGhostBlockEvent(Player player, GhostBlockCuboid cuboid, Location location, MaterialData materialData) {
        this.player = player;
        this.cuboid = cuboid;
        this.location = location;
        this.materialData = materialData;
        Bukkit.broadcastMessage("created, cancelled="+this.cancelled);
    }

    @Override
    public boolean isCancelled() {
        Bukkit.broadcastMessage("returning cancelled as " + this.cancelled);
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        Bukkit.broadcastMessage("set cancelled to "+b);
        this.cancelled = b;
    }
}
