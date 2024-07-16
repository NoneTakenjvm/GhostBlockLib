package me.nonetaken.ghostblocklib.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockChunk;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static me.nonetaken.ghostblocklib.GhostBlockManager.getCuboidByLocation;
import static me.nonetaken.ghostblocklib.GhostBlockManager.handleChunk;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:53
 */
public class MapChunkPacketListener extends SimplePacketListenerAbstract {

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.CHUNK_DATA) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (GhostBlockLib.isIgnoringGhostBlocks(player)) {
            return;
        }
        WrapperPlayServerChunkData packet = new WrapperPlayServerChunkData(event);
        GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), packet.getColumn().getX() * 16, packet.getColumn().getZ() * 16);
        // Ignore all chunks that are not in a ghost block cuboid
        if (cuboid == null) {
            return;
        }
        GhostBlockChunk ghostChunk = cuboid.getGhostBlockChunk(packet.getColumn().getX() * 16, packet.getColumn().getZ() * 16);
        if (ghostChunk == null) {
            return;
        }
        event.markForReEncode(true);
        for (int i = 0; i < packet.getColumn().getChunks().length; i++) {
            BaseChunk chunk = packet.getColumn().getChunks()[i];
            for (int x = 0; x < 16; x++) {
                int yLevel = -64 + (i * 16); // This is the Y level at the bottom of this base chunk
                for (int y = yLevel; y < yLevel + 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        GhostBlock block = ghostChunk.getBlock(x, y, z);
                        if (block == null) {
                            continue;
                        }
                        chunk.set(x, y % 16, z, block.getBlockState());
                    }
                }
            }
        }
    }
}
