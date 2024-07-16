package me.nonetaken.ghostblocklib.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import org.bukkit.Location;
import org.bukkit.Material;
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
@RequiredArgsConstructor
public class BlockPlaceAgainstGhostBlockEvent extends WrappedBukkitEvent implements Cancellable {

    private final Player player;
    private final GhostBlockCuboid cuboid;
    private final Location location;
    private final Material material;
    @Setter private boolean cancelled = false;

}
