package me.nonetaken.ghostblocklib.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import me.nonetaken.ghostblocklib.event.GhostBlockBreakEvent;
import me.nonetaken.ghostblocklib.util.BlockHardness;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static me.nonetaken.ghostblocklib.GhostBlockManager.getCuboidByLocation;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:45
 */
public class BlockDigPacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) {
            return;
        }
        WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
        Player player = (Player) event.getPlayer();
        DiggingAction action = packet.getAction();
        Vector3i vector = packet.getBlockPosition();
        GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), vector.getX(), vector.getZ());
        // If the player isn't breaking a block inside a ghost cuboid, return
        if (cuboid == null) {
            return;
        }
        // If the block is already broken or has a type of air, return
        GhostBlock block = cuboid.getBlock(vector.getX(), vector.getY(), vector.getZ());
        if (block == null || block.getMaterial() == Material.AIR) {
            return;
        }
        // Handle breaking for creative users
        if (player.getGameMode() == GameMode.CREATIVE) {
            if (action != DiggingAction.START_DIGGING) {
                event.setCancelled(true);
            }
            // Handle block breaking for survival users
        } else if (player.getGameMode() == GameMode.SURVIVAL) {
            if (action == DiggingAction.CANCELLED_DIGGING) {
                event.setCancelled(true);
                return;
            }
            boolean instant = BlockHardness.canInstantBreak(player, player.getInventory().getItemInMainHand(), block.getMaterial());
            if (instant && action != DiggingAction.START_DIGGING) {
                event.setCancelled(true);
            } else if (!instant && action != DiggingAction.FINISHED_DIGGING) {
                return;
            }
        }
        // Event must be called synchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                GhostBlockBreakEvent ghostBlockBreakEvent = new GhostBlockBreakEvent(cuboid, block, player);
                Bukkit.getPluginManager().callEvent(ghostBlockBreakEvent);
                // If the event was cancelled, tell the player the block wasn't broken
                if (ghostBlockBreakEvent.isCancelled()) {
                    WrapperPlayServerBlockChange changePacket = new WrapperPlayServerBlockChange(new Vector3i(vector.getX(), vector.getY(), vector.getZ()), block.getGlobalId());
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, changePacket);
                    event.setCancelled(true);
                    return;
                }
                cuboid.setBlock(new GhostBlock(vector.getX(), vector.getY(), vector.getZ()).setType(Material.AIR));
                // Notify nearby players of the block change
                WrapperPlayServerBlockChange changePacket = new WrapperPlayServerBlockChange(new Vector3i(vector.getX(), vector.getY(), vector.getZ()), 0);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getWorld() == player.getWorld()  // Player is in the same world
                            && onlinePlayer.getLocation().distance(player.getLocation()) < 64 // Player is within 64 blocks
                            && !GhostBlockLib.isIgnoringGhostBlocks(onlinePlayer)) { // Player is not ignoring GhostBlock data
                        PacketEvents.getAPI().getPlayerManager().sendPacket(onlinePlayer, changePacket);
                        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerAcknowledgeBlockChanges(packet.getSequence()));
                    }
                }
            }
        }.runTask(GhostBlockLib.getINSTANCE());
        event.setCancelled(true);
    }
}
