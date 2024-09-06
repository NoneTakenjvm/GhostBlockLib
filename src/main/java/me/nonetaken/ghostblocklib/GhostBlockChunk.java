package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import io.papermc.paper.math.Position;
import lombok.Getter;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 15:24
 */
@Getter
public class GhostBlockChunk {

    private final GhostBlockCuboid parent;
    private final int chunkX;
    private final int chunkZ;
    private GhostBlock[][][] blocks = new GhostBlock[16][384][16]; // x y z
    private final List<Vector> changes = Collections.synchronizedList(new ArrayList<>());

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
        // Add 64 onto the y coordinate, so we can support negative y levels
        return this.blocks[Math.abs(x % 16)][y + 64][Math.abs(z % 16)];
    }

    /**
     * Set the {@link GhostBlock} in {@link #blocks}
     *
     * @param block the ghost block to set
     */
    public synchronized void setBlock(GhostBlock block) {
        // Add 64 onto the y coordinate, so we can support negative y levels
        this.blocks[Math.floorMod(block.getX(), 16)][block.getY() + 64][Math.floorMod(block.getZ(), 16)] = block;
        this.changes.add(block.getVector());
    }

    /**
     * Clear all changes to this chunk
     */
    public void clearChanges() {
        this.changes.clear();
    }

    /**
     * Group and return all changes to this chunk
     *
     * @return the changes to this chunk
     */
    @SuppressWarnings("UnstableApiUsage")
    public synchronized Map<Position, BlockData> groupChanges() {
        Map<Position, BlockData> changes = new HashMap<>();
        for (Vector change : this.changes) {
            GhostBlock block = this.getBlock(change.getBlockX(), change.getBlockY(), change.getBlockZ());
            if (block == null) {
                continue;
            }
            changes.put(Position.block(block.getX(), block.getY(), block.getZ()), block.getMaterial().createBlockData());
        }
        return changes;
    }
}