package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public final class GhostBlockLib extends JavaPlugin implements Listener, CommandExecutor {

    @Getter private static GhostBlockLib INSTANCE;
    private static final Set<Player> IGNORE_GHOST_BLOCKS = new HashSet<>();

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("ignoreghostblocks").setExecutor(this);
        PacketEvents.getAPI().init();
        GhostBlockManager.init();
    }

    @Override
    public void onDisable() {
        INSTANCE = null;
        PacketEvents.getAPI().terminate();
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        // Prevent players being kicked for floating on ghost blocks
        if (!"Flying is not enabled on this server".equals(event.getReason())) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        try {
            // Reset the fly ticks
            Field flyTicks = connection.getClass().getDeclaredField("g");
            flyTicks.setAccessible(true);
            flyTicks.setInt(connection, 0);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command");
            return false;
        }
        Player player = (Player) sender;
        if (IGNORE_GHOST_BLOCKS.contains(player)) {
            IGNORE_GHOST_BLOCKS.remove(player);
            player.sendMessage("GhostBlocks are no longer ignored");
        } else {
            IGNORE_GHOST_BLOCKS.add(player);
            player.sendMessage("GhostBlocks are now ignored");
        }
        return false;
    }

    /**
     * Return whether the provided {@link Player} should be sent GhostBlock data or not
     *
     * @param player the player
     *
     * @return true if the player should not be sent ghost block data
     */
    public static boolean isIgnoringGhostBlocks(Player player) {
        return IGNORE_GHOST_BLOCKS.contains(player);
    }
}
