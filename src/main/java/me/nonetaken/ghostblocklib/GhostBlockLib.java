package me.nonetaken.ghostblocklib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Collections;

public final class GhostBlockLib extends JavaPlugin implements CommandExecutor {

    @Getter private static GhostBlockLib INSTANCE;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Override
    public void onEnable() {
        INSTANCE = this;
        PacketEvents.getAPI().init();
        new GhostBlockManager(this);
        this.getCommand("test").setExecutor(this);
    }

    @Override
    public void onDisable() {
        INSTANCE = null;
        PacketEvents.getAPI().terminate();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Location origin = player.getLocation();
        GhostBlockCuboid cuboid = new GhostBlockCuboid(origin.getWorld(), origin.toVector().add(new Vector(-10, -1, -10)), origin.toVector().add(new Vector(10, 0, 10)));
        cuboid.fill(block -> block.setType(Material.GOLD_BLOCK));
        cuboid.refresh(player);
        player.sendMessage("Done");
        return true;
    }
}
