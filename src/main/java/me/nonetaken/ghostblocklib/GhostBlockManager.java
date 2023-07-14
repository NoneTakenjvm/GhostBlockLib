package me.nonetaken.ghostblocklib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import lombok.Getter;
import me.nonetaken.ghostblocklib.event.GhostBlockBreakEvent;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:29
 */
public class GhostBlockManager {

    @Getter
    private static final List<GhostBlockCuboid> cuboids = Collections.synchronizedList(new ArrayList<>());

    @SuppressWarnings("deprecation")
    public GhostBlockManager(GhostBlockLib plugin) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        // Handle breaking of ghost blocks
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Client.BLOCK_DIG), ListenerOptions.SYNC, ListenerOptions.SKIP_PLUGIN_VERIFIER) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                StructureModifier<BlockPosition> positions = packet.getBlockPositionModifier();
                StructureModifier<PacketPlayInBlockDig.EnumPlayerDigType> enums = packet.getEnumModifier(PacketPlayInBlockDig.EnumPlayerDigType.class, 2);
                if (player.getGameMode() == GameMode.CREATIVE) {
                    if (enums.read(0) != PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                        return;
                    }
                } else {
                    if (enums.read(0) != PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                        return;
                    }
                }
                BlockPosition position = positions.read(0);
                GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), position.getX(), position.getZ());
                if (cuboid == null) {
                    return;
                }
                GhostBlock block = cuboid.getBlock(position.getX(), position.getY(), position.getZ());
                if (block == null) {
                    return;
                }
                GhostBlockBreakEvent ghostBlockBreakEvent = new GhostBlockBreakEvent(cuboid, block, player);
                plugin.getServer().getPluginManager().callEvent(ghostBlockBreakEvent);
                cuboid.setBlock(new GhostBlock(position.getX(), position.getY(), position.getZ()).setType(Material.AIR));
                event.setCancelled(true);
            }
        });
        // Handle interactions with ghost blocks
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Client.BLOCK_PLACE), ListenerOptions.SYNC, ListenerOptions.SKIP_PLUGIN_VERIFIER) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                StructureModifier<BlockPosition> positions = packet.getBlockPositionModifier();
                BlockPosition position = positions.read(0);
                GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), position.getX(), position.getZ());
                if (cuboid == null) {
                    return;
                }
                GhostBlock block = cuboid.getBlock(position.getX(), position.getY(), position.getZ());
                if (block == null) {
                    return;
                }
                event.setCancelled(true);
            }
        });
        // Handle placement of bulk map chunk packets
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Server.MAP_CHUNK_BULK), ListenerOptions.ASYNC, ListenerOptions.SKIP_PLUGIN_VERIFIER) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                PacketPlayOutMapChunkBulk nmsPacket = (PacketPlayOutMapChunkBulk) packet.getHandle();
                int[] chunkXArray;
                int[] chunkZArray;
                PacketPlayOutMapChunk.ChunkMap[] chunkMapArray;
                try {
                    Field chunkXArrayField = nmsPacket.getClass().getDeclaredField("a");
                    Field chunkZArrayField = nmsPacket.getClass().getDeclaredField("b");
                    Field chunkMapArrayField = nmsPacket.getClass().getDeclaredField("c");
                    chunkXArrayField.setAccessible(true);
                    chunkZArrayField.setAccessible(true);
                    chunkMapArrayField.setAccessible(true);
                    chunkXArray = (int[]) chunkXArrayField.get(nmsPacket);
                    chunkZArray = (int[]) chunkZArrayField.get(nmsPacket);
                    chunkMapArray = (PacketPlayOutMapChunk.ChunkMap[]) chunkMapArrayField.get(nmsPacket);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                for (int i = 0; i < chunkXArray.length; i++) {
                    handleChunk(event.getPlayer().getWorld(), chunkXArray[i], chunkZArray[i], chunkMapArray[i]);
                }
            }
        });
        // Handle altering of map chunk packets
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Server.MAP_CHUNK), ListenerOptions.ASYNC, ListenerOptions.SKIP_PLUGIN_VERIFIER) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                PacketPlayOutMapChunk nmsPacket = (PacketPlayOutMapChunk) packet.getHandle();
                PacketPlayOutMapChunk.ChunkMap chunkMap;
                try {
                    Field chunkMapField = nmsPacket.getClass().getDeclaredField("c");
                    Field groundUp = nmsPacket.getClass().getDeclaredField("d");
                    chunkMapField.setAccessible(true);
                    groundUp.setAccessible(true);
                    chunkMap = (PacketPlayOutMapChunk.ChunkMap) chunkMapField.get(nmsPacket);
                    if (chunkMap.b == 0) {
                        return;
                    }
                    if (handleChunk(event.getPlayer().getWorld(), packet.getIntegers().read(0), packet.getIntegers().read(1), chunkMap)) {
                        groundUp.set(nmsPacket, true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static boolean handleChunk(World world, int chunkX, int chunkZ, PacketPlayOutMapChunk.ChunkMap chunkMap) {
        // Ignore packets with a mask of 0
        if (chunkMap.b == 0) {
            return false;
        }
        GhostBlockCuboid cuboid = getCuboidByLocation(world, chunkX * 16, chunkZ * 16);
        // Ignore all packets that are not in a ghost block cuboid
        if (cuboid == null) {
            return false;
        }
        GhostBlockChunk chunk = cuboid.getGhostBlockChunk(chunkX * 16, chunkZ * 16);
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
        chunkMap.b = 65535;
        return true;
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
            if (!cuboid.getWorld().equals(world)) {
                continue;
            }
            if (cuboid.getChunks().containsKey(new ChunkCoordIntPair(x / 16, z / 16))) {
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
    public static void register(GhostBlockCuboid cuboid) {
        cuboids.add(cuboid);
    }

    /**
     * Cleanup and remove the provided {@link GhostBlockCuboid
     *
     * @param cuboid the cuboid
     */
    public static void unregister(GhostBlockCuboid cuboid) {
        cuboids.remove(cuboid);
    }

    @Getter
    public static class ChunkMapWrapper {

        private final World.Environment environment; // Only overworld sends skylight
        private final byte[] blockData; // Copied from the original data array, then edited as needed
        private final byte[] biomeData = new byte[256]; // Copied from the original data array

        private static final byte[] BLOCK_LIGHT_DATA = new byte[16 * 2048];
        private static final byte[] SKY_LIGHT_DATA = new byte[16 * 2048];

        static {
            Arrays.fill(BLOCK_LIGHT_DATA, (byte) 0);
            Arrays.fill(SKY_LIGHT_DATA, (byte) 255);
        }

        public ChunkMapWrapper(World.Environment environment, byte[] originalData, int bitmask) {
            this.environment = environment;
            this.blockData = new byte[16 * 8192];
            System.arraycopy(originalData, originalData.length - 256, this.biomeData, 0, 256);

            // Copy the original data array into the new block data array
            char[] chars = Integer.toBinaryString(bitmask).toCharArray();
            // The array must be reversed so we read from bottom up rather than top down
            ArrayUtils.reverse(chars);
            int counter = 0;
            for (int i = 0; i < chars.length; i++) {
                // Chunk sections with a mask of 0 have no data for us to copy over
                if (chars[i] != '1') {
                    continue;
                }
                System.arraycopy(originalData, counter * 8192, this.blockData, i * 8192, 8192);
                counter++;
            }
        }

        /**
         * Convert the provided x, y, and z world coordinates into their respective block's index in the data array
         *
         * @param x the world x coordinate
         * @param y the world y coordinate
         * @param z the world z coordinate
         * @return the index
         */
        public static int getIndex(int x, int y, int z) {
            return ((((y) << 8) | ((z & 0xF) << 4) | (x & 0xF)) * 2);
        }

        /**
         * Convert the provided world coordinate to a chunk coordinate
         *
         * @param worldCoordinate the world coordinate
         * @return the chunk coordinate
         */
        private int toChunkCoordinate(int worldCoordinate) {
            return worldCoordinate % 16;
        }

        /**
         * Set the block at the provided x, y, and z coordinate to the provided block registry ID
         * The provided coordinates should be world coordinates, not chunk coordinates
         *
         * @param x               the x coordinate
         * @param y               the y coordinate
         * @param z               the z coordinate
         * @param blockRegistryID the block registry ID
         */
        public void setBlock(int x, int y, int z, int blockRegistryID) {
            int index = getIndex(this.toChunkCoordinate(x), y, this.toChunkCoordinate(z));
            this.blockData[index] = (byte) (blockRegistryID & 255);
            this.blockData[index + 1] = (byte) (blockRegistryID >> 8 & 255);
        }

        /**
         * Build the new chunk map data array valid for a bitmask of 65535
         *
         * @return the new data array
         */
        public byte[] buildChunkMapData() {
            // Build the new data array
            byte[] data = ArrayUtils.addAll(this.blockData, BLOCK_LIGHT_DATA);
            // Only the Overworld sends skylight data in chunk packets
            if (this.environment == World.Environment.NORMAL) {
                data = ArrayUtils.addAll(data, SKY_LIGHT_DATA);
            }
            return ArrayUtils.addAll(data, this.biomeData);
        }
    }

}
