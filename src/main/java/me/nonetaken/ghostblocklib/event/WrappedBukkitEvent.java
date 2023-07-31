package me.nonetaken.ghostblocklib.event;

import me.nonetaken.ghostblocklib.GhostBlockLib;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Braydon
 */
public class WrappedBukkitEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}