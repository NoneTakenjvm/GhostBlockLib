package me.nonetaken.ghostblocklib.event;

import lombok.Getter;
import lombok.Setter;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockChunk;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import org.bukkit.entity.Player;

/*
 * Project: me.nonetaken.ghostblocklib.event | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:57
 */
@Getter
@Setter
public class GhostBlockBreakEvent extends WrappedBukkitEvent {

    private final GhostBlockCuboid ghostBlockCuboid;
    private final GhostBlock ghostBlock;
    private final Player player;

    public GhostBlockBreakEvent(GhostBlockCuboid ghostBlockCuboid, GhostBlock ghostBlock, Player player) {
        this.ghostBlockCuboid = ghostBlockCuboid;
        this.ghostBlock = ghostBlock;
        this.player = player;
    }
}
