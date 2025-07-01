package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import me.nonetaken.ghostblocklib.listener.BlockDigPacketListener;
import me.nonetaken.ghostblocklib.listener.BlockPlacePacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkPacketListener;
import me.nonetaken.ghostblocklib.util.ChunkIntCoordinatePair;
import me.nonetaken.ghostblocklib.util.Utils;
import org.bukkit.Bukkit;
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

    public static void init() {
        PacketEvents.getAPI().getEventManager().registerListeners(
                new BlockDigPacketListener(),
                new BlockPlacePacketListener(),
                new MapChunkPacketListener()
        );
    }

    /**
     * Return the cuboid coordinate handler for the provided world#
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
        CUBOIDS.computeIfAbsent(cuboid.getWorld(), val -> new CuboidCoordinateHandler()).addCuboid(cuboid);
    }

    /**
     * Cleanup and remove the provided {@link GhostBlockCuboid
     *
     * @param cuboid the cuboid
     */
    public static void unregisterGhostBlockCuboid(GhostBlockCuboid cuboid) {
        CuboidCoordinateHandler handler = CUBOIDS.get(cuboid.getWorld());
        if (handler != null) {
            handler.removeCuboid(cuboid);
        }
        cuboid.getChunks().clear();
    }

    public static class CuboidCoordinateHandler {

        private final Map<ChunkIntCoordinatePair, List<GhostBlockCuboid>> cuboids = new HashMap<>();

        /**
         * Add a cuboid to this handler
         *
         * @param cuboid the cuboid to add
         */
        public void addCuboid(GhostBlockCuboid cuboid) {
            for (Map.Entry<ChunkIntCoordinatePair, GhostBlockChunk> entry : cuboid.getChunks().entrySet()) {
                List<GhostBlockCuboid> cuboidList = this.cuboids.computeIfAbsent(entry.getKey(), val -> new ArrayList<>());
                cuboidList.add(cuboid);
                cuboidList.sort(Comparator.comparingInt(GhostBlockCuboid::getPriority));
            }
        }

        /**
         * Remove a cuboid from this handler
         *
         * @param cuboid the cuboid to remove
         */
        public void removeCuboid(GhostBlockCuboid cuboid) {
            for (Map.Entry<ChunkIntCoordinatePair, GhostBlockChunk> entry : cuboid.getChunks().entrySet()) {
                List<GhostBlockCuboid> cuboidList = this.cuboids.get(entry.getKey());
                if (cuboidList == null || cuboidList.isEmpty()) {
                    continue;
                }
                cuboidList.remove(cuboid);
                cuboidList.sort(Comparator.comparingInt(GhostBlockCuboid::getPriority));
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
            if (cuboids == null) {
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
