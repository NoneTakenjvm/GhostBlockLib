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
    private static final List<GhostBlockCuboid> cuboids = Collections.synchronizedList(new ArrayList<>());

    public static void init() {
        PacketEvents.getAPI().getEventManager().registerListeners(
                new BlockDigPacketListener(),
                new BlockPlacePacketListener(),
                new MapChunkPacketListener()
        );
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
