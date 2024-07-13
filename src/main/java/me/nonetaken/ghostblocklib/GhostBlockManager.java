package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.nonetaken.ghostblocklib.listener.BlockDigPacketListener;
import me.nonetaken.ghostblocklib.listener.BlockPlacePacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkBulkPacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkPacketListener;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:29
 */
@UtilityClass
public class GhostBlockManager {

    @Getter
    private static final List<GhostBlockCuboid> cuboids = Collections.synchronizedList(new ArrayList<>());

    public static void init() {
        new BlockDigPacketListener();
        new BlockPlacePacketListener();
        new MapChunkBulkPacketListener();
        new MapChunkPacketListener();
    }

    public static void handleChunk(World world, int chunkX, int chunkZ, BaseChunk chunk) {
        // Ignore chunks that are empty
        if (chunk.isEmpty()) {
            return;
        }
        GhostBlockCuboid cuboid = getCuboidByLocation(world, chunkX * 16, chunkZ * 16);
        // Ignore all chunks that are not in a ghost block cuboid
        if (cuboid == null) {
            return;
        }
        GhostBlockChunk ghostChunk = cuboid.getGhostBlockChunk(chunkX * 16, chunkZ * 16);
        if (ghostChunk == null) {
            return;
        }
        for (int x = 0; x < ghostChunk.getBlocks().length; x++) {
            for (int y = 0; y < ghostChunk.getBlocks()[x].length; y++) {
                for (int z = 0; z < ghostChunk.getBlocks()[x][y].length; z++) {
                    GhostBlock block = ghostChunk.getBlock(x, y, z);
                    if (block == null) {
                        continue;
                    }
                    chunk.set(ClientVersion.UNKNOWN, x, y, z, block.getGlobalId());

                }
            }
        }
//        //byte skyLightData = getSkyLight(world.getChunkAt(chunkX * 16, chunkZ * 16), new BlockPosition(chunkX * 16, 255, chunkZ * 16));
//        ChunkMapWrapper chunkMapWrapper = new ChunkMapWrapper(world.getEnvironment(), ghostChunk.a, ghostChunk.b);
//        // Iterate over stored ghost blocks and edit data
//        for (int x = 0; x < ghostChunk.getBlocks().length; x++) {
//            for (int y = 0; y < ghostChunk.getBlocks()[x].length; y++) {
//                for (int z = 0; z < ghostChunk.getBlocks()[x][y].length; z++) {
//                    GhostBlock block = ghostChunk.getBlock(x, y, z);
//                    if (block == null) {
//                        continue;
//                    }
//                    chunkMapWrapper.setBlock(block.getX(), block.getY(), block.getZ(), block.getBlockRegistryID());
//                }
//            }
//        }
//        ghostChunk.a = chunkMapWrapper.buildChunkMapData();
//        ghostChunk.b = chunkMapWrapper.getBitmask();
    }

    /**
     * Return the provided {@link GhostBlockCuboid} that the provided coordinates fall within
     * <p>
     * If the coordinates are not in any ghost block, null is returned
     *
     * @param world the world
     * @param x     the x coordinate
     * @param z     the z coordinate
     * @return whether the coordinates are in a cuboid or not
     */
    public static GhostBlockCuboid getCuboidByLocation(World world, int x, int z) {
        for (GhostBlockCuboid cuboid : cuboids) {
            if (!world.equals(cuboid.getWorld())) {
                continue;
            }
            if (cuboid.containsChunk(x, z)) {
                return cuboid;
            }
        }
        return null;
    }

    /**
     * Register the creation of a new {@link GhostBlockCuboid}
     *
     * @param cuboid the cuboid
     */
    public static void registerGhostBlockCuboid(GhostBlockCuboid cuboid) {
        cuboids.add(cuboid);
    }

    /**
     * Cleanup and remove the provided {@link GhostBlockCuboid
     *
     * @param cuboid the cuboid
     */
    public static void unregisterGhostBlockCuboid(GhostBlockCuboid cuboid) {
        cuboids.remove(cuboid);
        cuboid.getChunks().clear();
    }
}
