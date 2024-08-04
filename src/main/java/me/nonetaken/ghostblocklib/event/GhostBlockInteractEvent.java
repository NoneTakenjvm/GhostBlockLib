package me.nonetaken.ghostblocklib.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.nonetaken.ghostblocklib.GhostBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;

/**
 * Project: me.nonetaken.ghostblocklib.event | Author: NoneTaken#0001
 * Created: 23/03/2024 at 13:01
 */
@Getter
public class GhostBlockInteractEvent extends WrappedBukkitEvent implements Cancellable {

    private final Player player;
    private final GhostBlock ghostBlock;
    private final Action action;
    @Setter private boolean cancelled;

    public GhostBlockInteractEvent(Player player, GhostBlock ghostBlock, Action action) {
        super(false);
        this.player = player;
        this.ghostBlock = ghostBlock;
        this.action = action;
    }

}
