package me.nonetaken.ghostblocklib.listener;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import me.nonetaken.ghostblocklib.GhostBlock;
import me.nonetaken.ghostblocklib.GhostBlockCuboid;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import me.nonetaken.ghostblocklib.GhostBlockManager;
import org.bukkit.entity.Player;

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
        GhostBlockManager.CuboidCoordinateHandler handler = GhostBlockManager.getCuboidCoordinateHandler(player.getWorld());
        // There's no ghost block cuboids at this chunk, so we don't need to do anything
        if (handler.getCuboidsAtChunk(packet.getColumn().getX(), packet.getColumn().getZ()).isEmpty()) {
            return;
        }
        event.markForReEncode(true);
        for (int i = 0; i < packet.getColumn().getChunks().length; i++) {
            Chunk_v1_18 chunk = (Chunk_v1_18) packet.getColumn().getChunks()[i];
            for (int x = 0; x < 16; x++) {
                int yLevel = -64 + (i * 16); // This is the Y level at the bottom of this base chunk
                for (int y = yLevel; y < yLevel + 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        // Find the highest priority cuboid which has a block at this location
                        GhostBlockCuboid cuboid = handler.getHighestPriorityCuboidByLocation(x, y, z);
                        if (cuboid == null) {
                            continue;
                        }
                        // Get the block at the location
                        GhostBlock block = cuboid.getBlock(x, y, z);
                        if (block == null) {
                            continue;
                        }
                        // Set the block
                        chunk.set(x, y % 16, z, block.getBlockState());
                    }
                }
            }
            // We need to update the block count to avoid weird issues on the client
            chunk.setBlockCount(4096);
        }
    }
}
