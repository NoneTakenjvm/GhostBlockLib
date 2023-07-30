package me.nonetaken.ghostblocklib.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;

import java.util.ArrayList;
import java.util.List;

/*
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 30/07/2023 at 19:48
 */
public class MultiBlockChangeWrapper {
    private final ChunkCoordIntPair chunkCoordIntPair;
    private final List<MultiBlockChangeInfo> multiBlockChangeInfoList = new ArrayList<>();

    public MultiBlockChangeWrapper(ChunkCoordIntPair chunk) {
        this.chunkCoordIntPair = chunk;
    }

    /**
     * Adds a block change to the multi block change packet
     *
     * @param multiBlockChangeInfo The block change to add
     */
    public void addBlockChange(MultiBlockChangeInfo multiBlockChangeInfo) {
        this.multiBlockChangeInfoList.add(multiBlockChangeInfo);
    }

    public PacketContainer build() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
        packetContainer.getChunkCoordIntPairs().write(0, this.chunkCoordIntPair);
        packetContainer.getMultiBlockChangeInfoArrays().write(0, this.multiBlockChangeInfoList.toArray(new MultiBlockChangeInfo[0]));
        return packetContainer;
    }
}
