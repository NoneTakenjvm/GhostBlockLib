package me.nonetaken.ghostblocklib.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Braydon
 */
public class WrappedBukkitEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public WrappedBukkitEvent(boolean async) {
        super(async);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}