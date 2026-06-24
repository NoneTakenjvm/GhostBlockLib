package me.nonetaken.ghostblocklib;

import lombok.Getter;
import lombok.Setter;
import me.nonetaken.ghostblocklib.util.ChunkIntCoordinatePair;
import me.nonetaken.ghostblocklib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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

    public GhostBlockCuboid(@NotNull World world, @NotNull Vector min, @NotNull Vector max) {
        this(world, min, max, 0);
    }

    public GhostBlockCuboid(@NotNull World world, @NotNull Vector min, @NotNull Vector max, int priority) {
        this.world = world;
        this.x1 = Math.min(min.getBlockX(), max.getBlockX());
        this.y1 = Math.min(min.getBlockY(), max.getBlockY());
        this.z1 = Math.min(min.getBlockZ(), max.getBlockZ());
        this.x2 = Math.max(min.getBlockX(), max.getBlockX());
        this.y2 = Math.max(min.getBlockY(), max.getBlockY());
        this.z2 = Math.max(min.getBlockZ(), max.getBlockZ());
        this.priority = priority;
        this.updateBounds(false);
    }

    /**
     * Update the min and max bounds of this cuboid and sync its chunk footprint.
     * If this cuboid is registered, the spatial index is patched incrementally.
     *
     * @param register retained for API compatibility; registration is never implied by this call
     */
    @SuppressWarnings("unused")
    public void updateBounds(boolean register) {
        Set<ChunkIntCoordinatePair> oldKeys = new HashSet<>(this.chunks.keySet());
        Set<ChunkIntCoordinatePair> newKeys = this.computeChunkFootprint();
        this.applyChunkFootprintDiff(oldKeys, newKeys);
        if (GhostBlockManager.isRegistered(this)) {
            Set<ChunkIntCoordinatePair> removedKeys = new HashSet<>(oldKeys);
            removedKeys.removeAll(newKeys);
            Set<ChunkIntCoordinatePair> addedKeys = new HashSet<>(newKeys);
            addedKeys.removeAll(oldKeys);
            GhostBlockManager.patchCuboidIndex(this, removedKeys, addedKeys);
        }
    }

    void ensureChunkFootprint() {
        Set<ChunkIntCoordinatePair> oldKeys = new HashSet<>(this.chunks.keySet());
        Set<ChunkIntCoordinatePair> newKeys = this.computeChunkFootprint();
        this.applyChunkFootprintDiff(oldKeys, newKeys);
    }

    private Set<ChunkIntCoordinatePair> computeChunkFootprint() {
        int minChunkX = this.x1 - (this.x1 % 16);
        int minChunkZ = this.z1 - (this.z1 % 16);
        this.chunkMin = new Vector(minChunkX, this.y1, minChunkZ);

        int maxChunkX = this.x2 + (16 - (this.x2 % 16));
        int maxChunkZ = this.z2 + (16 - (this.z2 % 16));
        this.chunkMax = new Vector(maxChunkX, this.y2, maxChunkZ);

        Set<ChunkIntCoordinatePair> footprint = new HashSet<>();
        for (int x = minChunkX; x <= maxChunkX; x += 16) {
            int chunkX = Utils.toChunkCoordinate(x);
            for (int z = minChunkZ; z <= maxChunkZ; z += 16) {
                int chunkZ = Utils.toChunkCoordinate(z);
                footprint.add(new ChunkIntCoordinatePair(chunkX, chunkZ));
            }
        }
        return footprint;
    }

    private void applyChunkFootprintDiff(Set<ChunkIntCoordinatePair> oldKeys, Set<ChunkIntCoordinatePair> newKeys) {
        for (ChunkIntCoordinatePair key : oldKeys) {
            if (!newKeys.contains(key)) {
                this.chunks.remove(key);
            }
        }
        for (ChunkIntCoordinatePair key : newKeys) {
            this.chunks.computeIfAbsent(key, val -> new GhostBlockChunk(this, val.getChunkX(), val.getChunkZ()));
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
        if (!this.containsChunk(x, z)) {
            return null;
        }
        int chunkX = Utils.toChunkCoordinate(x);
        int chunkZ = Utils.toChunkCoordinate(z);
        // Fetch the chunk from the cache
        GhostBlockChunk chunk = this.chunks.get(new ChunkIntCoordinatePair(chunkX, chunkZ));
        // if the chunk is null, let's try to create it
        if (chunk == null) {
            if (!this.contains(x, z)) {
                throw new NullPointerException(String.format("Tried to fetch a chunk which was outside the cuboid, coordinates: %s,%s", x, z));
            }
            chunk = this.chunks.put(new ChunkIntCoordinatePair(chunkX, chunkZ), new GhostBlockChunk(this, chunkX, chunkZ));
        }
        return chunk;
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

    private synchronized void setMaterial(int x, int y, int z, Material material) {
        GhostBlockChunk chunk = this.getGhostBlockChunk(x, z);
        if (chunk != null) {
            chunk.setMaterial(x, y, z, material);
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
        GhostBlock scratch = new GhostBlock(0, 0, 0);
        for (Vector vector : vectors) {
            scratch.setPosition(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            consumer.accept(scratch);
            this.setMaterial(scratch.getX(), scratch.getY(), scratch.getZ(), scratch.getMaterial());
        }
    }


    /**
     * Set the provided {@link GhostBlock}s in this cuboid
     *
     * @param iterator the cuboid iterator to use
     * @param consumer the consumer to set the ghost block type, if required
     */
    public synchronized void setBlocks(GhostBlockCuboidIterator iterator, Consumer<GhostBlock> consumer) {
        GhostBlock scratch = new GhostBlock(0, 0, 0);
        while (iterator.hasNext()) {
            Vector next = iterator.next();
            scratch.setPosition(next.getBlockX(), next.getBlockY(), next.getBlockZ());
            consumer.accept(scratch);
            this.setMaterial(scratch.getX(), scratch.getY(), scratch.getZ(), scratch.getMaterial());
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
        this.updateBounds(true);
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
        this.updateBounds(true);
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
        this.updateBounds(true);
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
