package me.nonetaken.ghostblocklib.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import me.nonetaken.ghostblocklib.event.BlockPlaceAgainstGhostBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
        // The position of the block that was placed against
        Location placedAgainstPosition = getBlockPlacedAgainst(packet, player);
        // Get the ghost block cuboid the block placed against is within
        GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), placedAgainstPosition.getBlockX(), placedAgainstPosition.getBlockY(), placedAgainstPosition.getBlockZ());
        // Return if the player is not placing within a ghost block cuboid or the block placed against isn't a ghost block
        if (cuboid == null) {
            return;
        }
        // Cancel the packet
        event.setCancelled(true);
        // If the player is holding air or a non-solid block, we don't need to do anything more
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack item = player.getInventory().getItem(slot);
        if ((item == null || item.getType().isAir()) || (!item.getType().isBlock() && !item.getType().isSolid())) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(
                    player, new WrapperPlayServerAcknowledgeBlockChanges(packet.getSequence())
            );
            return;
        }
        // BlockPlaceAgainstGhostBlockEvent must be called synchronously
        Bukkit.getScheduler().runTask(GhostBlockLib.getINSTANCE(), () -> {
            Vector3i v = packet.getBlockPosition();
            BlockPlaceAgainstGhostBlockEvent placeEvent = new BlockPlaceAgainstGhostBlockEvent(player, cuboid, new Location(player.getWorld(), v.getX(), v.getY(), v.getZ()), item.getType());
            Bukkit.getPluginManager().callEvent(placeEvent);
            // Handle placing the block and deducting the item from the player's hand
            if (!placeEvent.isCancelled()) {
                placedAgainstPosition.getBlock().setType(item.getType());
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
                // Force an update of the player's inv to prevent any ghost items
                player.updateInventory();
            }
        });
    }

    /**
     * Returns the location of the block that was placed against
     *
     * @param packet the block place packet
     * @param player the player placing the block
     * @return the location of the block that was placed against
     */
    @NotNull
    private static Location getBlockPlacedAgainst(WrapperPlayClientPlayerBlockPlacement packet, Player player) {
        Vector3i placedBlockPosition = packet.getBlockPosition();
        // The position of the block that was placed against
        Location placedAgainstPosition = new Location(player.getWorld(), placedBlockPosition.getX(), placedBlockPosition.getY(), placedBlockPosition.getZ());
        switch (packet.getFace()) {
            case UP -> placedAgainstPosition.add(0, -1, 0);
            case DOWN -> placedAgainstPosition.add(0, 1, 0);
            case NORTH -> placedAgainstPosition.add(0, 0, 1);
            case SOUTH -> placedAgainstPosition.add(0, 0, -1);
            case WEST -> placedAgainstPosition.add(1, 0, 0);
            case EAST -> placedAgainstPosition.add(-1, 0, 0);
        }
        return placedAgainstPosition;
    }
}
