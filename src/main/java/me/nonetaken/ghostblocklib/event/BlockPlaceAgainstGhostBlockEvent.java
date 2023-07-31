package me.nonetaken.ghostblocklib.event;

import lombok.Getter;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.material.MaterialData;

/**
 * Project: me.nonetaken.ghostblocklib.event | Author: NoneTaken#0001
 * Created: 28/07/2023 at 18:41
 * <p>
 * Called when a player tries to place a block against a {@link me.nonetaken.ghostblocklib.GhostBlock}
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
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
