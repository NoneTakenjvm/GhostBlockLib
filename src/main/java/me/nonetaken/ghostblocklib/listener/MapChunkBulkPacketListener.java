package me.nonetaken.ghostblocklib.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkDataBulk;
import org.bukkit.entity.Player;

import static me.nonetaken.ghostblocklib.GhostBlockManager.handleChunk;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:51
 */
public class MapChunkBulkPacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.MAP_CHUNK_BULK) {
            return;
        }
        Player player = (Player) event.getPlayer();
        WrapperPlayServerChunkDataBulk packet = new WrapperPlayServerChunkDataBulk(event);
        for (int column = 0; column < packet.getChunks().length; column++) {
            int x = packet.getX()[column];
            int z = packet.getZ()[column];
            handleChunk(player.getWorld(), x, z, packet.getChunks()[x][z]);
        }
    }

    //    @Override
//    public void onPacketSending(PacketEvent event) {
//        Player player = event.getPlayer();
//        if (GhostBlockLib.isIgnoringGhostBlocks(player)) {
//            return;
//        }
//        PacketContainer packet = event.getPacket();
//        PacketPlayOutMapChunkBulk nmsPacket = (PacketPlayOutMapChunkBulk) packet.getHandle();
//        int[] chunkXArray;
//        int[] chunkZArray;
//        PacketPlayOutMapChunk.ChunkMap[] chunkMapArray;
//        try {
//            Field chunkXArrayField = nmsPacket.getClass().getDeclaredField("a");
//            Field chunkZArrayField = nmsPacket.getClass().getDeclaredField("b");
//            Field chunkMapArrayField = nmsPacket.getClass().getDeclaredField("c");
//            chunkXArrayField.setAccessible(true);
//            chunkZArrayField.setAccessible(true);
//            chunkMapArrayField.setAccessible(true);
//            chunkXArray = (int[]) chunkXArrayField.get(nmsPacket);
//            chunkZArray = (int[]) chunkZArrayField.get(nmsPacket);
//            chunkMapArray = (PacketPlayOutMapChunk.ChunkMap[]) chunkMapArrayField.get(nmsPacket);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return;
//        }
//        for (int i = 0; i < chunkXArray.length; i++) {
//            handleChunk(event.getPlayer().getWorld(), chunkXArray[i], chunkZArray[i], chunkMapArray[i]);
//        }
//    }
}
