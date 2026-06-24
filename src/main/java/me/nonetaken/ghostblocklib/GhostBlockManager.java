package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import me.nonetaken.ghostblocklib.listener.BlockDigPacketListener;
import me.nonetaken.ghostblocklib.listener.BlockPlacePacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkPacketListener;
import me.nonetaken.ghostblocklib.util.ChunkIntCoordinatePair;
import me.nonetaken.ghostblocklib.util.Utils;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.*;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:29
 */
public class GhostBlockManager extends PacketListenerAbstract {

    /**
     * Stores all cuboids by the chunk coordinates they have blocks in
     * <p>
     * Key: Map of World and Chunk X Coordinate
     * Value: Map of Chunk Z Coordinate to GhostBlockCuboid
     */
    private static final Map<World, CuboidCoordinateHandler> CUBOIDS = new HashMap<>();
    private static final Set<GhostBlockCuboid> REGISTERED = Collections.newSetFromMap(new IdentityHashMap<>());

    public static void init() {
        PacketEvents.getAPI().getEventManager().registerListeners(
                new BlockDigPacketListener(),
                new BlockPlacePacketListener(),
                new MapChunkPacketListener()
        );
    }

    static boolean isRegistered(GhostBlockCuboid cuboid) {
        return REGISTERED.contains(cuboid);
    }

    static void clearForTests() {
        REGISTERED.clear();
        CUBOIDS.clear();
    }

    static int registeredCountForTests() {
        return REGISTERED.size();
    }

    /**
     *
     * @param world the world
     * @return the handler
     */
    public static CuboidCoordinateHandler getCuboidCoordinateHandler(World world) {
        return CUBOIDS.computeIfAbsent(world, val -> new CuboidCoordinateHandler());
    }

    /**
     * Register the creation of a new {@link GhostBlockCuboid}
     *
     * @param cuboid the cuboid
     */
    public static void registerGhostBlockCuboid(GhostBlockCuboid cuboid) {
        if (REGISTERED.contains(cuboid)) {
            return;
        }
        cuboid.ensureChunkFootprint();
        REGISTERED.add(cuboid);
        CUBOIDS.computeIfAbsent(cuboid.getWorld(), val -> new CuboidCoordinateHandler()).addCuboid(cuboid);
    }

    /**
     * Cleanup and remove the provided {@link GhostBlockCuboid}
     *
     * @param cuboid the cuboid
     */
    public static void unregisterGhostBlockCuboid(GhostBlockCuboid cuboid) {
        REGISTERED.remove(cuboid);
        CuboidCoordinateHandler handler = CUBOIDS.get(cuboid.getWorld());
        if (handler != null) {
            handler.removeCuboid(cuboid);
        }
        cuboid.getChunks().clear();
    }

    static void patchCuboidIndex(GhostBlockCuboid cuboid, Collection<ChunkIntCoordinatePair> removedKeys, Collection<ChunkIntCoordinatePair> addedKeys) {
        if (!REGISTERED.contains(cuboid) || cuboid.getWorld() == null) {
            return;
        }
        CuboidCoordinateHandler handler = getCuboidCoordinateHandler(cuboid.getWorld());
        if (!removedKeys.isEmpty()) {
            handler.removeCuboidFromChunks(cuboid, removedKeys);
        }
        if (!addedKeys.isEmpty()) {
            handler.addCuboidToChunks(cuboid, addedKeys);
        }
    }

    public static class CuboidCoordinateHandler {

        private final Map<ChunkIntCoordinatePair, List<GhostBlockCuboid>> cuboids = new HashMap<>();

        /**
         * Add a cuboid to this handler
         *
         * @param cuboid the cuboid to add
         */
        public void addCuboid(GhostBlockCuboid cuboid) {
            this.addCuboidToChunks(cuboid, cuboid.getChunks().keySet());
        }

        void addCuboidToChunks(GhostBlockCuboid cuboid, Collection<ChunkIntCoordinatePair> keys) {
            for (ChunkIntCoordinatePair key : keys) {
                List<GhostBlockCuboid> cuboidList = this.cuboids.computeIfAbsent(key, val -> new ArrayList<>());
                if (!cuboidList.contains(cuboid)) {
                    cuboidList.add(cuboid);
                    cuboidList.sort(Comparator.comparingInt(GhostBlockCuboid::getPriority));
                }
            }
        }

        /**
         * Remove a cuboid from this handler
         *
         * @param cuboid the cuboid to remove
         */
        public void removeCuboid(GhostBlockCuboid cuboid) {
            this.removeCuboidFromChunks(cuboid, cuboid.getChunks().keySet());
        }

        void removeCuboidFromChunks(GhostBlockCuboid cuboid, Collection<ChunkIntCoordinatePair> keys) {
            for (ChunkIntCoordinatePair key : keys) {
                List<GhostBlockCuboid> cuboidList = this.cuboids.get(key);
                if (cuboidList == null || cuboidList.isEmpty()) {
                    continue;
                }
                cuboidList.remove(cuboid);
                if (cuboidList.isEmpty()) {
                    this.cuboids.remove(key);
                } else {
                    cuboidList.sort(Comparator.comparingInt(GhostBlockCuboid::getPriority));
                }
            }
        }

        /**
         * Return a list of all cuboids that contain the provided chunk x and z coordinates
         *
         * @param chunkX the chunk x coordinate
         * @param chunkZ the chunk z coordinate
         * @return the list of cuboids that contain the provided coordinates
         */
        public List<GhostBlockCuboid> getCuboidsAtChunk(int chunkX, int chunkZ) {
            return this.cuboids.getOrDefault(new ChunkIntCoordinatePair(chunkX, chunkZ), Collections.emptyList());
        }

        /**
         * Return a list of all cuboids that contain the provided world x, y and z coordinates
         *
         * @param x the world x coordinate
         * @param y the world y coordinate
         * @param z the world z coordinate
         * @return the list of cuboids that contain the provided coordinates
         */
        public List<GhostBlockCuboid> getCuboidsAtLocation(int x, int y, int z) {
            List<GhostBlockCuboid> cuboidsAtChunk = new ArrayList<>(this.getCuboidsAtChunk(Utils.toChunkCoordinate(x), Utils.toChunkCoordinate(z)));
            if (cuboidsAtChunk.isEmpty()) {
                return Collections.emptyList();
            }
            cuboidsAtChunk.removeIf(cuboid -> !cuboid.contains(x, y, z));
            return cuboidsAtChunk;
        }

        /**
         * Return the highest priority cuboid that contains the provided chunk coordinates
         *
         * @param x the chunk x coordinate
         * @param z the chunk z coordinate
         * @return the highest priority cuboid in this mesh that contains the provided coordinates
         */
        @Nullable
        public GhostBlockCuboid getHighestPriorityCuboidByChunk(int x, int z) {
            List<GhostBlockCuboid> cuboids = this.getCuboidsAtChunk(x, z);
            if (cuboids.isEmpty()) {
                return null;
            }
            return cuboids.getLast();
        }

        /**
         * Return the GhostBlockCuboid that the provided world x, y and z coordinates fall within
         * If the coordinates are not in any ghost block, null is returned
         *
         * @param x     the world x coordinate
         * @param y     the world y coordinate
         * @param z     the world z coordinate
         * @return the cuboid the coordinates fall within, or null if none
         */
        @Nullable
        public GhostBlockCuboid getHighestPriorityCuboidByLocation(int x, int y, int z) {
            List<GhostBlockCuboid> cuboids = this.getCuboidsAtLocation(x, y, z);
            if (cuboids.isEmpty()) {
                return null;
            }
            GhostBlockCuboid highestPriorityCuboid = null;
            for (GhostBlockCuboid cuboid : cuboids) {
                if (!cuboid.contains(x, y, z)) {
                    continue;
                }
                if (highestPriorityCuboid == null || highestPriorityCuboid.getPriority() < cuboid.getPriority()) {
                    highestPriorityCuboid = cuboid;
                }
            }
            return highestPriorityCuboid;
        }
    }
}
