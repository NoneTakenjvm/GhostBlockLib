package me.nonetaken.ghostblocklib;

import lombok.Getter;
import me.nonetaken.ghostblocklib.util.Cuboid;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:20
 */
@Getter
public class GhostBlockCuboid extends Cuboid {

    private final Map<ChunkCoordIntPair, GhostBlockChunk> chunks = new ConcurrentHashMap<>();

    public GhostBlockCuboid(World world, Vector min, Vector max) {
        super(world, min, max);

        // Find and store all chunks in this cuboid
        int minX = super.getMin().getBlockX();
        int maxX = super.getMax().getBlockX();
        int minZ = super.getMin().getBlockZ();
        int maxZ = super.getMax().getBlockZ();
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                // Create a new GhostBlockChunk
                GhostBlockChunk chunk = new GhostBlockChunk(this, x, z);
                // Add the chunk to the map
                this.chunks.put(new ChunkCoordIntPair(x, z), chunk);
            }
        }

        // Register the GhostBlockCuboid
        GhostBlockManager.registerGhostBlockCuboid(this);
    }

    /**
     * @return the world this cuboid is in
     */
    public World getWorld() {
        return super.getWorld();
    }

    /**
     * Return the {@link GhostBlockChunk} at the given world coordinates
     *
     * @param x the world x coordinate
     * @param z the world z coordinate
     *
     * @return the ghost block chunk
     */
    public GhostBlockChunk getGhostBlockChunk(int x, int z) {
        return Objects.requireNonNull(this.chunks.get(new ChunkCoordIntPair(x >> 4, z >> 4)));
    }

    /**
     * Return the {@link GhostBlock} at the provided world coordinates
     *
     * @param x the world x coordinate
     * @param y the world y coordinate
     * @param z the world z coordinate
     *
     * @return the ghost block
     */
    public GhostBlock getBlock(int x, int y, int z) {
        return this.getGhostBlockChunk(x, z).getBlock(x, y, z);
    }

    /**
     * Place the provided {@link GhostBlock} into this cuboid
     *
     * @param block the block to place
     */
    public synchronized void setBlock(GhostBlock block) {
        this.getGhostBlockChunk(block.getX(), block.getZ()).setBlock(block);
    }

    /**
     * Fill this cuboid region with
     *
     * @param consumer the consumer to handle setting ghost block types
     */
    public synchronized void fill(Consumer<GhostBlock> consumer) {
        this.setBlocks(super.getAllVectors(), consumer);
    }

    /**
     * Set a row of {@link GhostBlock}s at the given y coordinate
     *
     * @param y the world y coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setHorizontalLayer(int y, Consumer<GhostBlock> consumer) {
        Vector min = super.getMin().clone().setY(y);
        Vector max = super.getMax().clone().setY(y);
        Cuboid row = new Cuboid(super.getWorld(), min, max);
        this.setBlocks(row.getAllVectors(), consumer);
    }

    /**
     * Set a column of {@link GhostBlock}s at the given x coordinates
     *
     * @param x the world x coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setVerticalXColumn(int x, Consumer<GhostBlock> consumer) {
        Vector min = super.getMin().clone().setX(x);
        Vector max = super.getMax().clone().setX(x);
        Cuboid column = new Cuboid(super.getWorld(), min, max);
        this.setBlocks(column.getAllVectors(), consumer);
    }

    /**
     * Set a column of {@link GhostBlock}s at the given z coordinates
     *
     * @param z the world z coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setVerticalZColumn(int z, Consumer<GhostBlock> consumer) {
        Vector min = super.getMin().clone().setZ(z);
        Vector max = super.getMax().clone().setZ(z);
        Cuboid column = new Cuboid(super.getWorld(), min, max);
        this.setBlocks(column.getAllVectors(), consumer);
    }

    /**
     * Set the provided {@link GhostBlock}s in this cuboid
     *
     * @param vectors the list of vectors to be set
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setBlocks(Vector[] vectors, Consumer<GhostBlock> consumer) {
        for (Vector vector : vectors) {
            GhostBlock block = new GhostBlock(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            consumer.accept(block);
            this.setBlock(block);
        }
    }

    /**
     * Refresh this cuboid for the provided {@link Player}s
     *
     * @see GhostBlockChunk#refresh(Player...)
     */
    public synchronized void refresh(Player... players) {
        for (GhostBlockChunk chunk : this.chunks.values()) {
            chunk.refresh(players);
        }
    }

    /**
     * This function must be called if the region is no longer going to be used
     */
    public void unregister() {
        GhostBlockManager.unregisterGhostBlockCuboid(this);
    }
}
