package me.nonetaken.ghostblocklib;

import lombok.Getter;
import lombok.Setter;
import me.nonetaken.ghostblocklib.util.Utils;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:20
 */
@Getter
@SuppressWarnings("unused")
public class GhostBlockCuboid {

    private final Map<ChunkCoordIntPair, GhostBlockChunk> chunks = new ConcurrentHashMap<>();
    @Nullable @Setter private World world;
    private Vector min;
    private Vector max;
    private Vector[] allVectors;

    public GhostBlockCuboid(@Nullable World world, Vector min, Vector max) {
        min.setY(Math.max(0, min.getBlockY())); // avoid negative y values
        max.setY(Math.min(255, max.getBlockY())); // avoid y values above 255
        this.world = world;
        this.min = min;
        this.max = max;

        this.setAllPoints();

        // Find and cache all chunks the mine will contain
        this.findChunks();

        // Register the GhostBlockCuboid
        GhostBlockManager.registerGhostBlockCuboid(this);
    }

    private void findChunks() {
        int minX = this.min.getBlockX();
        int maxX = this.max.getBlockX();
        int minZ = this.min.getBlockZ();
        int maxZ = this.max.getBlockZ();
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                // Create a new GhostBlockChunk
                GhostBlockChunk chunk = new GhostBlockChunk(this, x, z);
                // Add the chunk to the map
                this.chunks.put(new ChunkCoordIntPair(x, z), chunk);
            }
        }
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
        GhostBlockChunk chunk = this.getGhostBlockChunk(x, z);
        return chunk == null ? null : chunk.getBlock(x, y, z);
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
        this.setBlocks(this.getAllVectors(), consumer);
    }

    /**
     * Set a row of {@link GhostBlock}s at the given y coordinate
     *
     * @param y the world y coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setHorizontalLayer(int y, Consumer<GhostBlock> consumer) {
        Vector min = this.min.clone().setY(y);
        Vector max = this.max.clone().setY(y);
        this.setBlocks(Utils.getVectorsBetween(min, max), consumer);
    }

    /**
     * Set a column of {@link GhostBlock}s at the given x coordinates
     *
     * @param x the world x coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setVerticalXColumn(int x, Consumer<GhostBlock> consumer) {
        Vector min = this.getMin().clone().setX(x);
        Vector max = this.getMax().clone().setX(x);
        this.setBlocks(Utils.getVectorsBetween(min, max), consumer);
    }

    /**
     * Set a column of {@link GhostBlock}s at the given z coordinates
     *
     * @param z the world z coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setVerticalZColumn(int z, Consumer<GhostBlock> consumer) {
        Vector min = this.getMin().clone().setZ(z);
        Vector max = this.getMax().clone().setZ(z);
        this.setBlocks(Utils.getVectorsBetween(min, max), consumer);
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

    /**
     * Repopulate {@link #allVectors} with every point in this cuboid
     */
    public void setAllPoints() {
        this.allVectors = Utils.getVectorsBetween(this.min, this.max);
    }

    /**
     * Return the volume of this cuboid
     *
     * @return the volume
     */
    public int getVolume() {
        int deltaX = 1 + Math.abs(this.max.getBlockX() - this.min.getBlockX());
        int deltaY = 1 + Math.abs(this.max.getBlockY() - this.min.getBlockY());
        int deltaZ = 1 + Math.abs(this.max.getBlockZ() - this.min.getBlockZ());
        return deltaX * deltaY * deltaZ;
    }

    /**
     * Set the minimum point of this cuboid
     *
     * @param min the new min point
     */
    public void setMin(Vector min) {
        this.min = min;
        this.setAllPoints();
        this.findChunks();
    }

    /**
     * Set the maximum point of this cuboid
     *
     * @param max the new max point
     */
    public void setMax(Vector max) {
        this.max = max;
        this.setAllPoints();
        this.findChunks();
    }

    private int getUpperX() {
        return this.getMax().getBlockX();
    }

    private int getUpperY() {
        return this.getMax().getBlockY();
    }

    private int getUpperZ() {
        return this.getMax().getBlockZ();
    }

    private int getLowerX() {
        return this.getMin().getBlockX();
    }

    private int getLowerY() {
        return this.getMin().getBlockY();
    }

    private int getLowerZ() {
        return this.getMin().getBlockZ();
    }

    /**
     * Return the corner points of this cuboid
     *
     * @return the cuboid
     */
    public Vector[] corners() {
        Vector[] res = new Vector[8];
        res[0] = new Vector(this.getLowerX(), this.getLowerY(), this.getLowerZ());
        res[1] = new Vector(this.getLowerX(), this.getLowerY(), this.getUpperZ());
        res[2] = new Vector(this.getLowerX(), this.getUpperY(), this.getLowerZ());
        res[3] = new Vector(this.getLowerX(), this.getUpperY(), this.getUpperZ());
        res[4] = new Vector(this.getUpperX(), this.getLowerY(), this.getLowerZ());
        res[5] = new Vector(this.getUpperX(), this.getLowerY(), this.getUpperZ());
        res[6] = new Vector(this.getUpperX(), this.getLowerY(), this.getLowerZ());
        res[7] = new Vector(this.getUpperX(), this.getLowerY(), this.getUpperZ());
        return res;
    }

    /**
     * Return whether this cuboid contains the provided {@link Location}
     *
     * @param location the location
     *
     * @return whether this cuboid contains the location
     */
    public boolean contains(Location location) {
        return this.contains(location.toVector());
    }

    /**
     * Return whether this cuboid contains the provided {@link Vector}
     *
     * @param vector the vector
     *
     * @return whether this cuboid contains the vector
     */
    public boolean contains(Vector vector) {
        return this.contains(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    /**
     * Return whether this cuboid contains the provided x, y and z coordinates
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     *
     * @return whether this cuboid contains the provided x, y and z coordinates
     */
    public boolean contains(int x, int y, int z) {
        return (y >= this.min.getBlockY() && y <= this.max.getBlockY()) && this.contains(x, z);

    }

    /**
     * Return whether this cuboid contains the provided x and z coordinates
     *
     * @param x the x coordinate
     * @param z the z coordinate
     *
     * @return whether this cuboid contains the provided x and z coordinates
     */
    public boolean contains(int x, int z) {
        return x >= this.min.getBlockX() && x <= this.max.getBlockX() && z >= this.min.getBlockZ() && z <= this.max.getBlockZ();
    }

    /**
     * Expand this cuboid in the provided direction by the provided amount
     *
     * @param dir the direction to expand in
     * @param amount the amount to expand by
     */
    public void expand(CuboidDirection dir, int amount) {
        switch (dir) {
            case UNKNOWN: {
                break;
            }
            case NORTH: {
                this.min.subtract(new Vector(0, 0, amount));
                this.setAllPoints();
                this.findChunks();
                break;
            }
            case EAST: {
                this.min.subtract(new Vector(amount, 0, 0));
                this.setAllPoints();
                this.findChunks();
                break;
            }
            case SOUTH: {
                this.max.add(new Vector(0, 0, amount));
                this.setAllPoints();
                this.findChunks();
                break;
            }
            case WEST: {
                this.max.add(new Vector(amount, 0, 0));
                this.setAllPoints();
                this.findChunks();
                break;
            }
            case UP: {
                this.max.add(new Vector(0, amount, 0));
                this.setAllPoints();
                this.findChunks();
                break;
            }
            case DOWN: {
                this.min.subtract(new Vector(0, amount, 0));
                this.setAllPoints();
                this.findChunks();
                break;
            }
            case DEFAULT: {
                for (CuboidDirection value : CuboidDirection.values()) {
                    if (value == CuboidDirection.DEFAULT || value == CuboidDirection.UP || value == CuboidDirection.ALL) {
                        continue;
                    }
                    this.expand(value, amount);
                }
                this.setAllPoints();
                this.findChunks();
                break;
            }
            case ALL: {
                for (CuboidDirection value : CuboidDirection.values()) {
                    if (value == CuboidDirection.DEFAULT || value == CuboidDirection.ALL) {
                        continue;
                    }
                    this.expand(value, amount);
                }
                this.setAllPoints();
                this.findChunks();
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid direction " + dir);
            }
        }
    }

    public enum CuboidDirection {

        NORTH,
        EAST,
        SOUTH,
        WEST,
        UP,
        DOWN,
        DEFAULT,
        ALL,
        UNKNOWN

    }
}
