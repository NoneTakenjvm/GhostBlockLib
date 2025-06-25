package me.nonetaken.ghostblocklib;

import lombok.Getter;
import lombok.Setter;
import me.nonetaken.ghostblocklib.util.ChunkIntCoordinatePair;
import me.nonetaken.ghostblocklib.util.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:20
 */
@Getter
@SuppressWarnings("unused")
public class GhostBlockCuboid implements Iterable<Vector> {

    /**
     * Stores all chunks in this cuboid
     * Key: Chunk X coordinate
     * Value: Map of Chunk Z coordinate to GhostBlockChunk
     */
    private final Map<ChunkIntCoordinatePair, GhostBlockChunk> chunks = new ConcurrentHashMap<>();
    @Nullable @Setter private World world;

    private int x1, y1, z1, x2, y2, z2;
    private Vector chunkMin, chunkMax;
    private int changeCount = 0;
    private int priority = 0;

    public GhostBlockCuboid(@Nullable World world, Vector min, Vector max) {
        this(world, min, max, 0);
    }

    public GhostBlockCuboid(@Nullable World world, Vector min, Vector max, int priority) {
        this.world = world;
        this.x1 = Math.min(min.getBlockX(), max.getBlockX());
        this.y1 = Math.min(min.getBlockY(), max.getBlockY());
        this.z1 = Math.min(min.getBlockZ(), max.getBlockZ());
        this.x2 = Math.max(min.getBlockX(), max.getBlockX());
        this.y2 = Math.max(min.getBlockY(), max.getBlockY());
        this.z2 = Math.max(min.getBlockZ(), max.getBlockZ());

        // Set the chunk min and chunk max
        int chunkX = this.x1 - (this.x1 % 16);
        int chunkZ = this.z1 - (this.z1 % 16);
        this.chunkMin =  new Vector(chunkX, min.getBlockY(), chunkZ);

        chunkX = this.x2 + (16 - (this.x2 % 16));
        chunkZ = this.z2 + (16 - (this.z2 % 16));
        this.chunkMax = new Vector(chunkX, max.getBlockY(), chunkZ);

        // Set the priority
        this.priority = priority;

        // Register the GhostBlockCuboid
        GhostBlockManager.registerGhostBlockCuboid(this);
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
        if (!this.containsChunk(x, z)) {
            return null;
        }
        int chunkX = Utils.toChunkCoordinate(x);
        int chunkZ = Utils.toChunkCoordinate(z);
        // Fetch the chunk from the cache, or create a new one if it isn't in the cache
        return this.chunks.computeIfAbsent(new ChunkIntCoordinatePair(chunkX, chunkZ),
                val -> new GhostBlockChunk(this, chunkX, chunkZ));
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
        GhostBlockChunk chunk = this.getGhostBlockChunk(block.getX(), block.getZ());
        if (chunk != null) {
            chunk.setBlock(block);
            ++this.changeCount;
        }
    }

    /**
     * Fill this cuboid region with
     *
     * @param consumer the consumer to handle setting ghost block types
     */
    public synchronized void fill(Consumer<GhostBlock> consumer) {
        this.setBlocks(this.iterator(), consumer);
    }

    /**
     * Set a row of {@link GhostBlock}s at the given y coordinate
     *
     * @param y the world y coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setHorizontalLayer(int y, Consumer<GhostBlock> consumer) {
        this.setBlocks(Utils.getVectorsBetween(new Vector(this.x1, y, this.z1), new Vector(this.x2, y, this.z2)), consumer);
    }

    /**
     * Set a column of {@link GhostBlock}s at the given x coordinates
     *
     * @param x the world x coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setVerticalXColumn(int x, Consumer<GhostBlock> consumer) {
        this.setBlocks(Utils.getVectorsBetween(new Vector(x, this.y1, this.z1), new Vector(x, this.y2, this.z2)), consumer);
    }

    /**
     * Set a column of {@link GhostBlock}s at the given z coordinates
     *
     * @param z the world z coordinate
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setVerticalZColumn(int z, Consumer<GhostBlock> consumer) {
        this.setBlocks(Utils.getVectorsBetween(new Vector(this.x1, this.y1, z), new Vector(this.x2, this.y2, z)), consumer);
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
     * Set the provided {@link GhostBlock}s in this cuboid
     *
     * @param iterator the cuboid iterator to use
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setBlocks(GhostBlockCuboidIterator iterator, Consumer<GhostBlock> consumer) {
        while (iterator.hasNext()) {
            Vector next = iterator.next();
            GhostBlock block = new GhostBlock(next.getBlockX(), next.getBlockY(), next.getBlockZ());
            consumer.accept(block);
            this.setBlock(block);
        }
    }

    /**
     * Refresh this cuboid for the provided {@link Player}s
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
     * Return the volume of this cuboid
     *
     * @return the volume
     */
    public int getVolume() {
        int deltaX = 1 + Math.abs(this.x2 - this.x1);
        int deltaY = 1 + Math.abs(this.y2 - this.y1);
        int deltaZ = 1 + Math.abs(this.z2 - this.z1);
        return deltaX * deltaY * deltaZ;
    }

    /**
     * Set the minimum point of this cuboid
     *
     * @param min the new min point
     */
    public void setMin(Vector min) {
        this.x1 = min.getBlockX();
        this.y1 = min.getBlockY();
        this.z1 = min.getBlockZ();

        int chunkX = this.x1 - (this.x1 % 16);
        int chunkZ = this.z1 - (this.z1 % 16);
        this.chunkMin =  new Vector(chunkX, min.getBlockY(), chunkZ);
    }

    /**
     * Set the maximum point of this cuboid
     *
     * @param max the new max point
     */
    public void setMax(Vector max) {
        this.x2 = max.getBlockX();
        this.y2 = max.getBlockY();
        this.z2 = max.getBlockZ();

        int chunkX = this.x2 + (16 - (this.x2 % 16));
        int chunkZ = this.z2 + (16 - (this.z2 % 16));
        this.chunkMax = new Vector(chunkX, max.getBlockY(), chunkZ);
    }

    private int getUpperX() {
        return this.x2;
    }

    private int getUpperY() {
        return this.y2;
    }

    private int getUpperZ() {
        return this.z2;
    }

    private int getLowerX() {
        return this.x1;
    }

    private int getLowerY() {
        return this.y1;
    }

    private int getLowerZ() {
        return this.z1;
    }

    public Vector getMin() {
        return new Vector(this.getLowerX(), this.getLowerY(), this.getLowerZ());
    }

    public Vector getMax() {
        return new Vector(this.getUpperX(), this.getUpperY(), this.getUpperZ());
    }

    /**
     * Return the corner points of this cuboid
     *
     * @return the cuboid
     */
    public Vector[] corners() {
        Vector[] res = new Vector[8];
        res[0] = new Vector(this.getLowerX(), this.getLowerY(), this.getLowerZ()); // lower north west
        res[1] = new Vector(this.getLowerX(), this.getLowerY(), this.getUpperZ());
        res[2] = new Vector(this.getLowerX(), this.getUpperY(), this.getLowerZ());
        res[3] = new Vector(this.getLowerX(), this.getUpperY(), this.getUpperZ());
        res[4] = new Vector(this.getUpperX(), this.getLowerY(), this.getLowerZ());
        res[5] = new Vector(this.getUpperX(), this.getLowerY(), this.getUpperZ());
        res[6] = new Vector(this.getUpperX(), this.getUpperY(), this.getLowerZ());
        res[7] = new Vector(this.getUpperX(), this.getUpperY(), this.getUpperZ()); // upper south east
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
        return (y >= this.y1 && y <= this.y2) && this.contains(x, z);

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
        return x >= this.x1 && x <= this.x2 && z >= this.z1 && z <= this.z2;
    }

    /**
     * Return whether this cuboid is within the chunk at the provided world x and z coordinate
     *
     * @param x the chunk x coordinate
     * @param z the chunk z coordinate
     * @return whether this cuboid is in the chunk
     */
    public boolean containsChunk(int x, int z) {
        if (this.world == null) {
            return false;
        }
        return     x >= this.chunkMin.getBlockX()
                && x <= this.chunkMax.getBlockX()
                && z >= this.chunkMin.getBlockZ()
                && z <= this.chunkMax.getBlockZ();
    }

    /**
     * Return whether this cuboid contains the provided y coordinate
     *
     * @param y the y coordinate
     * @return whether this cuboid contains the provided y coordinate
     */
    public boolean containsY(int y) {
        return y >= this.y1 && y <= this.y2;
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
                this.z1 -= amount;
                break;
            }
            case EAST: {
                this.x1 -= amount;
                break;
            }
            case SOUTH: {
                this.z2 += amount;
                break;
            }
            case WEST: {
                this.x2 += amount;
                break;
            }
            case UP: {
                this.y2 += amount;
                break;
            }
            case DOWN: {
                this.y1 -= amount;
                break;
            }
            case DEFAULT: {
                for (CuboidDirection value : CuboidDirection.values()) {
                    if (value == CuboidDirection.DEFAULT || value == CuboidDirection.UP || value == CuboidDirection.ALL) {
                        continue;
                    }
                    this.expand(value, amount);
                }
                break;
            }
            case ALL: {
                for (CuboidDirection value : CuboidDirection.values()) {
                    if (value == CuboidDirection.DEFAULT || value == CuboidDirection.ALL) {
                        continue;
                    }
                    this.expand(value, amount);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid direction " + dir);
            }
        }
        // Update the chunk min and chunk max
        this.setMin(this.getMin());
        this.setMax(this.getMax());
    }

    @Override
    public @NotNull GhostBlockCuboidIterator iterator() {
        return new GhostBlockCuboidIterator(this);
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

    public static class GhostBlockCuboidIterator implements Iterator<Vector> {

        private final int sizeX, sizeY, sizeZ;
        private final Vector min;
        private int x, y, z;
        private int count = 0;
        private final int volume;

        private GhostBlockCuboidIterator(GhostBlockCuboid cuboid) {
            this.min = cuboid.getMin().clone();
            this.sizeX = Math.abs(cuboid.getUpperX() - cuboid.getLowerX()) + 1;
            this.sizeY = Math.abs(cuboid.getUpperY() - cuboid.getLowerY()) + 1;
            this.sizeZ = Math.abs(cuboid.getUpperZ() - cuboid.getLowerZ()) + 1;
            this.x = this.y = this.z = 0;
            this.volume = this.getVolume();
        }

        public int getVolume() {
            return this.sizeX * this.sizeY * this.sizeZ;
        }

        @Override
        public boolean hasNext() {
            return this.count <= this.volume;
        }

        @Override
        public Vector next() {
            if (++this.x >= this.sizeX) {
                this.x = 0;
                if (++this.y >= this.sizeY) {
                    this.y = 0;
                    if (++this.z >= this.sizeZ) {
                        this.z = 0;
                    }
                }
            }
            this.count++;
            return new Vector(this.min.getBlockX() + this.x, this.min.getBlockY() + this.y, this.min.getBlockZ() + this.z);
        }
    }
}
