package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import lombok.Getter;
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
     * Clear the changes and blocks stored in this chunk
     */
    public void cleanup() {
        this.changes.clear();
        this.blocks = new GhostBlock[16][384][16];
    }

    /**
     * Refresh this chunk for the provided {@link Player}s
     * All players that should see the changes should be provided
     *
     * @param players the players to refresh this chunk for
     */
    public synchronized void refresh(Player... players) {
        // K: chunk section Y coordinate, V: encoded block
        Map<Integer, List<WrapperPlayServerMultiBlockChange.EncodedBlock>> encodedBlocks = new HashMap<>();
        for (Vector change : this.changes) {
            GhostBlock block = this.getBlock(change.getBlockX(), change.getBlockY(), change.getBlockZ());
            // If the changed block is null, skip it
            if (block == null) {
                continue;
            }
            // Add the encoded block to the list of encoded blocks for the chunk section the block is in
            encodedBlocks.computeIfAbsent(change.getBlockY() >> 4, k -> new ArrayList<>())
                    .add(new WrapperPlayServerMultiBlockChange.EncodedBlock(block.getGlobalId(), change.getBlockX(), change.getBlockY(), change.getBlockZ()));
        }
        // Send the changed chunk sections to the client
        for (Map.Entry<Integer, List<WrapperPlayServerMultiBlockChange.EncodedBlock>> entry : encodedBlocks.entrySet()) {
            // Unsure what trustEdges does, but I'm a glass half full guy so true it is
            WrapperPlayServerMultiBlockChange blockChangePacket = new WrapperPlayServerMultiBlockChange(new Vector3i(this.chunkX, entry.getKey(), this.chunkZ), true, entry.getValue().toArray(new WrapperPlayServerMultiBlockChange.EncodedBlock[0]));
            for (Player player : players) {
                // Some players might be ignoring ghost blocks
                if (!GhostBlockLib.isIgnoringGhostBlocks(player)) {
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, blockChangePacket);
                }
            }
        }
        // Clear the change list
        this.changes.clear();
    }
}