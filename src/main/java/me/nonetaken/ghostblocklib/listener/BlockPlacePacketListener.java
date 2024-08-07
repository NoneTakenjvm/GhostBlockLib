package me.nonetaken.ghostblocklib.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import me.nonetaken.ghostblocklib.event.BlockPlaceAgainstGhostBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static me.nonetaken.ghostblocklib.GhostBlockManager.getCuboidByLocation;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:49
 */
public class BlockPlacePacketListener extends PacketListenerAbstract {
    public BlockPlacePacketListener() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            return;
        }
        WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);
        Player player = (Player) event.getPlayer();
        Vector3i vector = packet.getBlockPosition();
        GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), vector.getX(), vector.getZ());
        if (cuboid == null) {
            return;
        }
        GhostBlock block = cuboid.getBlock(vector.getX(), vector.getY(), vector.getZ());
        if (block == null) {
            return;
        }
        event.setCancelled(true);
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null || item.getType().isAir()) {
            return;
        }
        if (!item.getType().isBlock() && !item.getType().isSolid()) {
            return;
        }
        short face = packet.getFace().getFaceValue();
        Location newLocation = new Location(player.getWorld(), vector.getX(), vector.getY(), vector.getZ());
        if (face == 0) { // Down
            newLocation.add(0, -1, 0);
        } else if (face == 1) { // Up
            newLocation.add(0, 1, 0);
        } else if (face == 2) { // North
            newLocation.add(0, 0, -1);
        } else if (face == 3) { // South
            newLocation.add(0, 0, 1);
        } else if (face == 4) { // West
            newLocation.add(-1, 0, 0);
        } else if (face == 5) { // East
            newLocation.add(1, 0, 0);
        }
        // Event must be called synchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                BlockPlaceAgainstGhostBlockEvent placeEvent = new BlockPlaceAgainstGhostBlockEvent(player, cuboid, newLocation, item.getType());
                Bukkit.getPluginManager().callEvent(placeEvent);
                // Handle placing the block and deducting the item
                if (!placeEvent.isCancelled()) {
                    newLocation.getBlock().setType(item.getType());
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        return;
                    }
                    if (item.getAmount() == 0) {
                        player.getInventory().setItem(slot, null);
                    } else {
                        item.setAmount(item.getAmount() - 1);
                        player.getInventory().setItem(slot, item);
                    }
                }
                // Tell the client the block was not placed
                else {
                    PacketEvents.getAPI().getPlayerManager().sendPacket(
                            player, new WrapperPlayServerAcknowledgeBlockChanges(packet.getSequence())
                    );
                }
            }
        }.runTaskLater(GhostBlockLib.getINSTANCE(), 0L);
    }
}
