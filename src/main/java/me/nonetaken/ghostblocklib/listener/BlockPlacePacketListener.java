package me.nonetaken.ghostblocklib.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import me.nonetaken.ghostblocklib.event.BlockPlaceAgainstGhostBlockEvent;
import net.minecraft.server.v1_8_R3.PacketStatusOutServerInfo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import static me.nonetaken.ghostblocklib.GhostBlockManager.getCuboidByLocation;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:49
 */
public class BlockPlacePacketListener extends PacketAdapter {

    public BlockPlacePacketListener() {
        super(GhostBlockLib.getINSTANCE(), ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Client.BLOCK_PLACE), ListenerOptions.SYNC);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();
        Location position = packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld());
        GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), position.getBlockX(), position.getBlockZ());
        if (cuboid == null) {
            return;
        }
        GhostBlock block = cuboid.getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        if (block == null) {
            return;
        }
        ItemStack item = player.getItemInHand();
        if (item == null) {
            return;
        }
        event.setCancelled(true);
        if (!item.getType().isBlock() && !item.getType().isSolid()) {
            return;
        }
        int face = packet.getIntegers().read(0);
        Location newLocation = position.clone();
        if (face == 0) {
            newLocation.add(0, -1, 0);
        } else if (face == 1) {
            newLocation.add(0, 1, 0);
        } else if (face == 2) {
            newLocation.add(0, 0, -1);
        } else if (face == 3) {
            newLocation.add(0, 0, 1);
        } else if (face == 4) {
            newLocation.add(-1, 0, 0);
        } else if (face == 5) {
            newLocation.add(1, 0, 0);
        }
        BlockPlaceAgainstGhostBlockEvent placeEvent = new BlockPlaceAgainstGhostBlockEvent(player, cuboid, newLocation, item.getData());
        Bukkit.getPluginManager().callEvent(placeEvent);
        // Handle placing the block and deducting the item
        if (!placeEvent.isCancelled()) {
            newLocation.getBlock().setType(item.getType());
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (item.getAmount() == 0) {
                player.setItemInHand(null);
            } else {
                item.setAmount(item.getAmount() - 1);
                player.setItemInHand(item);
            }
        }
        // Tell the client the block was not placed
        else {
            WrapperPlayServerBlockChange changePacket = new WrapperPlayServerBlockChange(new Vector3i(newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ()), 0);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, changePacket);
        }
    }
}
