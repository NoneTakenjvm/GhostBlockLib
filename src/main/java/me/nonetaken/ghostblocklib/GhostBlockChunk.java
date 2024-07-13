package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import lombok.Getter;
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
        List<WrapperPlayServerMultiBlockChange.EncodedBlock> encodedBlocks = new ArrayList<>(this.changes.size());
        for (Vector change : this.changes) {
            encodedBlocks.add(new WrapperPlayServerMultiBlockChange.EncodedBlock(this.getBlock(change.getBlockX(), change.getBlockY(), change.getBlockZ()).getGlobalId(), change.getBlockX(), change.getBlockY(), change.getBlockZ()));
        }
        WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(new Vector3i(this.chunkX >> 4, 0, this.chunkZ >> 4), true, encodedBlocks.toArray(new WrapperPlayServerMultiBlockChange.EncodedBlock[0]));
        for (Player player : players) {
            if (!GhostBlockLib.isIgnoringGhostBlocks(player)) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);
            }
        }
        this.changes.clear();
    }
}