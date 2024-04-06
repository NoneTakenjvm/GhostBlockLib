package me.nonetaken.ghostblocklib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.nonetaken.ghostblocklib.listener.BlockDigPacketListener;
import me.nonetaken.ghostblocklib.listener.BlockPlacePacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkBulkPacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkPacketListener;
import me.nonetaken.ghostblocklib.util.wrapper.ChunkMapWrapper;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.EnumSkyBlock;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import org.apache.logging.log4j.core.helpers.Assert;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;

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
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new BlockDigPacketListener());
        manager.addPacketListener(new BlockPlacePacketListener());
        manager.addPacketListener(new MapChunkBulkPacketListener());
        manager.addPacketListener(new MapChunkPacketListener());
    }

    public static void handleChunk(World world, int chunkX, int chunkZ, PacketPlayOutMapChunk.ChunkMap chunkMap) {
        // Ignore packets with a mask of 0
        if (chunkMap.b == 0) {
            return;
        }
        GhostBlockCuboid cuboid = getCuboidByLocation(world, chunkX * 16, chunkZ * 16);
        // Ignore all packets that are not in a ghost block cuboid
        if (cuboid == null) {
            return;
        }
        GhostBlockChunk chunk = cuboid.getGhostBlockChunk(chunkX * 16, chunkZ * 16);
        if (chunk == null) {
            return;
        }
        //byte skyLightData = getSkyLight(world.getChunkAt(chunkX * 16, chunkZ * 16), new BlockPosition(chunkX * 16, 255, chunkZ * 16));
        ChunkMapWrapper chunkMapWrapper = new ChunkMapWrapper(world.getEnvironment(), chunkMap.a, chunkMap.b);
        // Iterate over stored ghost blocks and edit data
        for (int x = 0; x < chunk.getBlocks().length; x++) {
            for (int y = 0; y < chunk.getBlocks()[x].length; y++) {
                for (int z = 0; z < chunk.getBlocks()[x][y].length; z++) {
                    GhostBlock block = chunk.getBlock(x, y, z);
                    if (block == null) {
                        continue;
                    }
                    chunkMapWrapper.setBlock(block.getX(), block.getY(), block.getZ(), block.getBlockRegistryID());
                }
            }
        }
        chunkMap.a = chunkMapWrapper.buildChunkMapData();
        chunkMap.b = chunkMapWrapper.getBitmask();
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
            if (cuboid.getChunks().containsKey(new ChunkCoordIntPair(x / 16, z / 16))) {
                return cuboid;
            }
        }
        return null;
    }

//    /**
//     * Return the sky-light level at the provided block position
//     *
//     * @param chunk the chunk the block position is in
//     * @param blockPosition the block position
//     * @return the sky-light
//     */
//    public static byte getSkyLight(Chunk chunk, BlockPosition blockPosition) {
//        return (byte) ((CraftChunk) chunk).getHandle().getBrightness(EnumSkyBlock.SKY, blockPosition);
//    }

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
    }
}
