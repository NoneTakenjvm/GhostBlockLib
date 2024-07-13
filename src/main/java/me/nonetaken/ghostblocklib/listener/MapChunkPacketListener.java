package me.nonetaken.ghostblocklib.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import org.bukkit.entity.Player;

import static me.nonetaken.ghostblocklib.GhostBlockManager.handleChunk;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:53
 */
public class MapChunkPacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.CHUNK_DATA) {
            return;
        }
        Player player = (Player) event.getPlayer();
        WrapperPlayServerChunkData packet = new WrapperPlayServerChunkData(event);
        for (BaseChunk chunk : packet.getColumn().getChunks()) {
            handleChunk(player.getWorld(), packet.getColumn().getX(), packet.getColumn().getZ(),  chunk);
        }
    }

//    @Override
//    public void onPacketSending(PacketEvent event) {
//        Player player = event.getPlayer();
//        if (GhostBlockLib.isIgnoringGhostBlocks(player)) {
//            return;
//        }
//        PacketContainer packet = event.getPacket();
//        PacketPlayOutMapChunk nmsPacket = (PacketPlayOutMapChunk) packet.getHandle();
//        PacketPlayOutMapChunk.ChunkMap chunkMap;
//        try {
//            Field chunkMapField = nmsPacket.getClass().getDeclaredField("c");
//            Field groundUp = nmsPacket.getClass().getDeclaredField("d");
//            chunkMapField.setAccessible(true);
//            groundUp.setAccessible(true);
//            chunkMap = (PacketPlayOutMapChunk.ChunkMap) chunkMapField.get(nmsPacket);
//            if (chunkMap.b == 0) {
//                return;
//            }
//            handleChunk(event.getPlayer().getWorld(), packet.getIntegers().read(0), packet.getIntegers().read(1), chunkMap);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}
