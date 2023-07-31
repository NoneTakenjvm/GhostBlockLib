package me.nonetaken.ghostblocklib.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunkBulk;

import java.lang.reflect.Field;

import static me.nonetaken.ghostblocklib.GhostBlockManager.handleChunk;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:51
 */
public class MapChunkBulkPacketListener extends PacketAdapter {

    @SuppressWarnings("deprecation")
    public MapChunkBulkPacketListener() {
        super(GhostBlockLib.getINSTANCE(), PacketType.Play.Server.MAP_CHUNK_BULK);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        PacketPlayOutMapChunkBulk nmsPacket = (PacketPlayOutMapChunkBulk) packet.getHandle();
        int[] chunkXArray;
        int[] chunkZArray;
        PacketPlayOutMapChunk.ChunkMap[] chunkMapArray;
        try {
            Field chunkXArrayField = nmsPacket.getClass().getDeclaredField("a");
            Field chunkZArrayField = nmsPacket.getClass().getDeclaredField("b");
            Field chunkMapArrayField = nmsPacket.getClass().getDeclaredField("c");
            chunkXArrayField.setAccessible(true);
            chunkZArrayField.setAccessible(true);
            chunkMapArrayField.setAccessible(true);
            chunkXArray = (int[]) chunkXArrayField.get(nmsPacket);
            chunkZArray = (int[]) chunkZArrayField.get(nmsPacket);
            chunkMapArray = (PacketPlayOutMapChunk.ChunkMap[]) chunkMapArrayField.get(nmsPacket);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        for (int i = 0; i < chunkXArray.length; i++) {
            handleChunk(event.getPlayer().getWorld(), chunkXArray[i], chunkZArray[i], chunkMapArray[i]);
        }
    }
}
