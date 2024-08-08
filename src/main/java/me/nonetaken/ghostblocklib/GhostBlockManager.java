package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import lombok.Getter;
import me.nonetaken.ghostblocklib.listener.BlockDigPacketListener;
import me.nonetaken.ghostblocklib.listener.BlockPlacePacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkPacketListener;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:29
 */
public class GhostBlockManager extends PacketListenerAbstract {

    @Getter
    private static final List<GhostBlockCuboid> CUBOIDS = Collections.synchronizedList(new ArrayList<>());

    public static void init() {
        PacketEvents.getAPI().getEventManager().registerListeners(
                new BlockDigPacketListener(),
                new BlockPlacePacketListener(),
                new MapChunkPacketListener()
        );
    }

    /**
     * Return the GhostBlockCuboid that the provided chunk x and z coordinates fall within
     * If the coordinates are not in any ghost block, null is returned
     *
     * @param world the world
     * @param x the chunk x coordinate
     * @param z the chunk z coordinate
     * @return the cuboid the coordinates fall within, or null if none
     */
    public static GhostBlockCuboid getCuboidByChunkCoordinates(World world, int x, int z) {
        for (GhostBlockCuboid cuboid : CUBOIDS) {
            if (!world.equals(cuboid.getWorld())) {
                continue;
            }
            if (cuboid.containsChunk(x * 16, z * 16)) {
                return cuboid;
            }
        }
        return null;
    }

    /**
     * Return the GhostBlockCuboid that the provided world x, y and z coordinates fall within
     * If the coordinates are not in any ghost block, null is returned
     *
     * @param world the world
     * @param x     the world x coordinate
     * @param y     the world y coordinate
     * @param z     the world z coordinate
     * @return the cuboid the coordinates fall within, or null if none
     */
    public static GhostBlockCuboid getCuboidByLocation(World world, int x, int y, int z) {
        for (GhostBlockCuboid cuboid : CUBOIDS) {
            if (!world.equals(cuboid.getWorld())) {
                continue;
            }
            if (cuboid.contains(x, y, z)) {
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
        CUBOIDS.add(cuboid);
    }

    /**
     * Cleanup and remove the provided {@link GhostBlockCuboid
     *
     * @param cuboid the cuboid
     */
    public static void unregisterGhostBlockCuboid(GhostBlockCuboid cuboid) {
        CUBOIDS.remove(cuboid);
        cuboid.getChunks().clear();
    }
}
