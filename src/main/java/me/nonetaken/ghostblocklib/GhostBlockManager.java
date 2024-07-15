package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.nonetaken.ghostblocklib.listener.BlockDigPacketListener;
import me.nonetaken.ghostblocklib.listener.BlockPlacePacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkBulkPacketListener;
import me.nonetaken.ghostblocklib.listener.MapChunkPacketListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

    @Override
    public void onPacketSend(PacketSendEvent event) {
        String name = event.getPacketType().getName();
        if (name.toLowerCase().contains("chunk") || name.toLowerCase().contains("block")) {
            Bukkit.broadcastMessage("Sending packet " + name);
        }
    }

    public static void init() {
        PacketEvents.getAPI().getEventManager().registerListeners(
                new GhostBlockManager(),
                new BlockDigPacketListener(),
                new BlockPlacePacketListener(),
                new MapChunkBulkPacketListener(),
                new MapChunkPacketListener()
        );
    }

    public static void handleChunk(World world, int chunkX, int chunkZ, Column chunk) {
//        GhostBlockCuboid cuboid = getCuboidByLocation(world, chunkX * 16, chunkZ * 16);
//        // Ignore all chunks that are not in a ghost block cuboid
//        if (cuboid == null) {
//            return;
//        }
//        GhostBlockChunk ghostChunk = cuboid.getGhostBlockChunk(chunkX * 16, chunkZ * 16);
//        if (ghostChunk == null) {
//            return;
//        }
        for (int i = 0; i < chunk.getChunks().length; i++) {
            BaseChunk chunkSection = chunk.getChunks()[i];
            WrappedBlockState state = SpigotConversionUtil.fromBukkitBlockData(switch (i) {
                case 0 -> Material.STONE.createBlockData();
                case 1 -> Material.DIRT.createBlockData();
                case 2 -> Material.GRASS_BLOCK.createBlockData();
                case 3 -> Material.COBBLESTONE.createBlockData();
                case 4 -> Material.OAK_PLANKS.createBlockData();
                case 5 -> Material.BEDROCK.createBlockData();
                case 6 -> Material.SAND.createBlockData();
                case 7 -> Material.GRAVEL.createBlockData();
                case 8 -> Material.GOLD_BLOCK.createBlockData();
                case 9 -> Material.IRON_BLOCK.createBlockData();
                case 10 -> Material.COAL_BLOCK.createBlockData();
                case 11 -> Material.LAPIS_BLOCK.createBlockData();
                case 12 -> Material.DIAMOND_BLOCK.createBlockData();
                case 13 -> Material.EMERALD_BLOCK.createBlockData();
                case 14 -> Material.REDSTONE_BLOCK.createBlockData();
                case 15 -> Material.NETHERITE_BLOCK.createBlockData();
                default -> Material.COAL_ORE.createBlockData();
            });
            for (int y = -64; y < 256; y++) {
                chunkSection.set((chunkX << 4) + 1, y, (chunkZ << 4) + 1, state);
            }
        }
//        for (int x = 0; x < ghostChunk.getBlocks().length; x++) {
//            for (int y = 0; y < ghostChunk.getBlocks()[x].length; y++) {
//                for (int z = 0; z < ghostChunk.getBlocks()[x][y].length; z++) {
//                    GhostBlock block = ghostChunk.getBlock(x, y, z);
//                    if (block == null) {
//                        continue;
//                    }
//                    chunk.set(x, y, z, block.getGlobalId());
//                }
//            }
//        }
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
