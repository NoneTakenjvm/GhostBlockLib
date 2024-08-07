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
        // The position of the block that was placed
        Location placedAgainstPosition = getLocation(packet, player);
        // Check if the block that was placed against is in a ghost block cuboid
        GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), placedAgainstPosition.getBlockX(), placedAgainstPosition.getBlockZ());
        if (cuboid == null) {
            return;
        }
        event.setCancelled(true);
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType().isAir()) {
            return;
        }
        if (!item.getType().isBlock() && !item.getType().isSolid()) {
            return;
        }
        // Event must be called synchronously
        Bukkit.getScheduler().runTask(GhostBlockLib.getINSTANCE(), () -> {
            Vector3i v = packet.getBlockPosition();
            BlockPlaceAgainstGhostBlockEvent placeEvent = new BlockPlaceAgainstGhostBlockEvent(player, cuboid, new Location(player.getWorld(), v.getX(), v.getY(), v.getZ()), item.getType());
            Bukkit.getPluginManager().callEvent(placeEvent);
            // Handle placing the block and deducting the item
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
                // It doesn't matter whether this is here or not, the item is still removed and a ghost block placed on the client!
                PacketEvents.getAPI().getPlayerManager().sendPacket(
                        player, new WrapperPlayServerAcknowledgeBlockChanges(packet.getSequence())
                );
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
    private static Location getLocation(WrapperPlayClientPlayerBlockPlacement packet, Player player) {
        Vector3i placedBlockPosition = packet.getBlockPosition();
        short face = packet.getFace().getFaceValue();
        // The position of the block that was placed against
        Location placedAgainstPosition = new Location(player.getWorld(), placedBlockPosition.getX(), placedBlockPosition.getY(), placedBlockPosition.getZ());
        if (face == 0) { // Down
            placedAgainstPosition.add(0, -1, 0);
        } else if (face == 1) { // Up
            placedAgainstPosition.add(0, 1, 0);
        } else if (face == 2) { // North
            placedAgainstPosition.add(0, 0, -1);
        } else if (face == 3) { // South
            placedAgainstPosition.add(0, 0, 1);
        } else if (face == 4) { // West
            placedAgainstPosition.add(-1, 0, 0);
        } else if (face == 5) { // East
            placedAgainstPosition.add(1, 0, 0);
        }
        return placedAgainstPosition;
    }
}
