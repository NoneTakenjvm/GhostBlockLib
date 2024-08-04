package me.nonetaken.ghostblocklib.event;

import lombok.Getter;
import lombok.Setter;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/*
 * Project: me.nonetaken.ghostblocklib.event | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:57
 */
@Getter
@Setter
public class AsyncGhostBlockBreakEvent extends WrappedBukkitEvent implements Cancellable {

    private final GhostBlockCuboid ghostBlockCuboid;
    private final GhostBlock ghostBlock;
    private final Player player;
    private boolean cancelled;

    public AsyncGhostBlockBreakEvent(GhostBlockCuboid ghostBlockCuboid, GhostBlock ghostBlock, Player player) {
        super(true);
        this.ghostBlockCuboid = ghostBlockCuboid;
        this.ghostBlock = ghostBlock;
        this.player = player;
    }
}
