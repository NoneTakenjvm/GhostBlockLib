package me.nonetaken.ghostblocklib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import lombok.Getter;
import me.nonetaken.ghostblocklib.util.wrapper.MultiBlockChangeWrapper;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 15:24
 */
@Getter
public class GhostBlockChunk {

    private final GhostBlockCuboid parent;
    private final int chunkX;
    private final int chunkZ;
    private GhostBlock[][][] blocks = new GhostBlock[16][256][16]; // x y z
    private List<Vector> changes = Collections.synchronizedList(new ArrayList<>());

    protected GhostBlockChunk(GhostBlockCuboid parent, int chunkX, int chunkZ) {
        this.parent = parent;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Return the {@link GhostBlock} at the provided world coordinates
     *
     * @param x the world x coordinate
     * @param y the world y coordinate
     * @param z the world z coordinate
     * @return the ghost block at the given coordinates
     */
    @Nullable
    public GhostBlock getBlock(int x, int y, int z) {
        return this.blocks[Math.abs(x % 16)][y][Math.abs(z % 16)];
    }

    /**
     * Set the {@link GhostBlock} in {@link #blocks}
     *
     * @param block the ghost block to set
     */
    public synchronized void setBlock(GhostBlock block) {
        this.blocks[Math.floorMod(block.getX(), 16)][block.getY()][Math.floorMod(block.getZ(), 16)] = block;
        this.changes.add(block.getVector());

    }

    /**
     * Clear the changes and blocks stored in this chunk
     */
    public void cleanup() {
        this.changes.clear();
        this.blocks = new GhostBlock[16][256][16];
    }

    /**
     * Refresh this chunk for the provided {@link Player}s
     * All players that should see the changes should be provided
     *
     * @param players the players to refresh this chunk for
     */
    public synchronized void refresh(Player... players) {
        MultiBlockChangeWrapper wrapper = new MultiBlockChangeWrapper(new ChunkCoordIntPair(this.chunkX, this.chunkZ));
        for (Vector change : this.changes) {
            GhostBlock block = this.getBlock(change.getBlockX(), change.getBlockY(), change.getBlockZ());
            if (block != null) {
                wrapper.addBlockChange(new MultiBlockChangeInfo(block.getLocation(this.parent.getWorld()), block.getWrappedBlockData()));
            }
        }
        PacketContainer packet = wrapper.build();
        for (Player player : players) {
            if (!GhostBlockLib.isIgnoringGhostBlocks(player)) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            }
        }
        wrapper.getMultiBlockChangeInfoList().clear();
        this.changes.clear();
    }
}