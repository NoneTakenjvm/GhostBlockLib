package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class GhostBlockLib extends JavaPlugin {

    @Getter private static GhostBlockLib INSTANCE;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        PacketEvents.getAPI().init();
        GhostBlockManager.init();
    }

    @Override
    public void onDisable() {
        INSTANCE = null;
        PacketEvents.getAPI().terminate();
    }
}
