package me.nonetaken.ghostblocklib.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.nonetaken.ghostblocklib.GhostBlockLib;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

import static me.nonetaken.ghostblocklib.GhostBlockManager.handleChunk;

/**
 * Project: me.nonetaken.ghostblocklib.listener | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:53
 */
public class MapChunkPacketListener extends PacketAdapter {

    public MapChunkPacketListener() {
        super(GhostBlockLib.getINSTANCE(), PacketType.Play.Server.MAP_CHUNK);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        PacketPlayOutMapChunk nmsPacket = (PacketPlayOutMapChunk) packet.getHandle();
        PacketPlayOutMapChunk.ChunkMap chunkMap;
        try {
            Field chunkMapField = nmsPacket.getClass().getDeclaredField("c");
            Field groundUp = nmsPacket.getClass().getDeclaredField("d");
            chunkMapField.setAccessible(true);
            groundUp.setAccessible(true);
            chunkMap = (PacketPlayOutMapChunk.ChunkMap) chunkMapField.get(nmsPacket);
            if (chunkMap.b == 0) {
                return;
            }
            handleChunk(event.getPlayer().getWorld(), packet.getIntegers().read(0), packet.getIntegers().read(1), chunkMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
