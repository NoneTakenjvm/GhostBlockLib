package me.nonetaken.ghostblocklib.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import me.nonetaken.ghostblocklib.event.GhostBlockBreakEvent;
import me.nonetaken.ghostblocklib.util.BlockHardness;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;

import static me.nonetaken.ghostblocklib.GhostBlockManager.getCuboidByLocation;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:45
 */
public class BlockDigPacketListener extends PacketAdapter {

    public BlockDigPacketListener() {
        super(GhostBlockLib.getINSTANCE(), ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Client.BLOCK_DIG), ListenerOptions.SYNC);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();
        StructureModifier<BlockPosition> positions = packet.getBlockPositionModifier();
        PacketPlayInBlockDig.EnumPlayerDigType digType = packet.getEnumModifier(PacketPlayInBlockDig.EnumPlayerDigType.class, 2).read(0);
        BlockPosition position = positions.read(0);
        GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), position.getX(), position.getZ());
        if (cuboid == null) {
            return;
        }
        GhostBlock block = cuboid.getBlock(position.getX(), position.getY(), position.getZ());
        if (block == null || block.getMaterial() == Material.AIR) {
            return;
        }
        // Handle breaking for creative users
        if (player.getGameMode() == GameMode.CREATIVE) {
            if (digType != PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                event.setCancelled(true);
                return;
            }
            // Handle block breaking for survival users
        } else if (player.getGameMode() == GameMode.SURVIVAL) {
            if (digType == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                event.setCancelled(true);
                return;
            }
            boolean instant = BlockHardness.canInstantBreak(player, player.getItemInHand(), block.getMaterial());
            if (instant && digType != PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                event.setCancelled(true);
                return;
            } else if (!instant && digType != PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                event.setCancelled(true);
                return;
            }
        }
        GhostBlockBreakEvent ghostBlockBreakEvent = new GhostBlockBreakEvent(cuboid, block, player);
        Bukkit.getPluginManager().callEvent(ghostBlockBreakEvent);
        // If the event was cancelled, tell the player the block wasn't broken
        if (ghostBlockBreakEvent.isCancelled()) {
            WrapperPlayServerBlockChange changePacket = new WrapperPlayServerBlockChange(new Vector3i(position.getX(), position.getY(), position.getZ()), block.getBlockRegistryID());
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, changePacket);
            event.setCancelled(true);
            return;
        }
        cuboid.setBlock(new GhostBlock(position.getX(), position.getY(), position.getZ()).setType(Material.AIR));
        // Notify nearby players of the block change
        WrapperPlayServerBlockChange changePacket = new WrapperPlayServerBlockChange(new Vector3i(position.getX(), position.getY(), position.getZ()), WrappedBlockData.createData(Material.AIR).getData());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getWorld() == player.getWorld() && onlinePlayer.getLocation().distance(player.getLocation()) < 64) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(onlinePlayer, changePacket);
            }
        }
        event.setCancelled(true);
    }
}
