package me.nonetaken.ghostblocklib;

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import lombok.Getter;
import me.nonetaken.ghostblocklib.event.BlockPlaceAgainstGhostBlockEvent;
import me.nonetaken.ghostblocklib.event.GhostBlockBreakEvent;
import me.nonetaken.ghostblocklib.util.BlockHardness;
import me.nonetaken.ghostblocklib.util.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

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
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Client.BLOCK_DIG), ListenerOptions.SYNC) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                StructureModifier<BlockPosition> positions = packet.getBlockPositionModifier();
                PacketPlayInBlockDig.EnumPlayerDigType digType = packet.getEnumModifier(PacketPlayInBlockDig.EnumPlayerDigType.class, 2).read(0);
                BlockPosition position = positions.read(0);
                GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), position.getX(), position.getZ());
                if (cuboid == null) {
                    return;
                }
                GhostBlock block = cuboid.getBlock(position.getX(), position.getY(), position.getZ());
                if (block == null) {
                    return;
                }
                // Handle breaking for creative users
                if (player.getGameMode() == GameMode.CREATIVE) {
                    if (digType != PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                        event.setCancelled(true);
                        return;
                    }
                    // Handle block breaking for survival users
                } else if (player.getGameMode() == GameMode.SURVIVAL) {
                    if (digType == PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK) {
                        event.setCancelled(true);
                        return;
                    }
                    boolean instant = BlockHardness.canInstantBreak(player, player.getItemInHand(), block.getMaterial());
                    if (instant && digType != PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                        event.setCancelled(true);
                        return;
                    } else if (!instant && digType != PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                        event.setCancelled(true);
                        return;
                    }
                }
                GhostBlockBreakEvent ghostBlockBreakEvent = new GhostBlockBreakEvent(cuboid, block, player);
                plugin.getServer().getPluginManager().callEvent(ghostBlockBreakEvent);
                if (ghostBlockBreakEvent.isCancelled()) {
                    return;
                }
                cuboid.setBlock(new GhostBlock(position.getX(), position.getY(), position.getZ()).setType(Material.AIR));
                // Notify nearby players of the block change
                WrapperPlayServerBlockChange changePacket = new WrapperPlayServerBlockChange();
                changePacket.setLocation(position);
                changePacket.setBlockData(WrappedBlockData.createData(Material.AIR));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getWorld() == player.getWorld() && onlinePlayer.getLocation().distance(player.getLocation()) < 64) {
                        changePacket.sendPacket(onlinePlayer);
                    }
                }
                event.setCancelled(true);
            }
        });
        // Handle interactions with ghost blocks
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Client.BLOCK_PLACE), ListenerOptions.SYNC) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                Location position = packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld());
                GhostBlockCuboid cuboid = getCuboidByLocation(player.getWorld(), position.getBlockX(), position.getBlockZ());
                if (cuboid == null) {
                    return;
                }
                GhostBlock block = cuboid.getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ());
                if (block == null) {
                    return;
                }
                ItemStack item = player.getItemInHand();
                if (item == null) {
                    return;
                }
                event.setCancelled(true);
                if (!item.getType().isBlock() && !item.getType().isSolid()) {
                    return;
                }
                int face = packet.getIntegers().read(0);
                Location newLocation = position.clone();
                if (face == 0) {
                    newLocation.add(0, -1, 0);
                } else if (face == 1) {
                    newLocation.add(0, 1, 0);
                } else if (face == 2) {
                    newLocation.add(0, 0, -1);
                } else if (face == 3) {
                    newLocation.add(0, 0, 1);
                } else if (face == 4) {
                    newLocation.add(-1, 0, 0);
                } else if (face == 5) {
                    newLocation.add(1, 0, 0);
                }
                BlockPlaceAgainstGhostBlockEvent placeEvent = new BlockPlaceAgainstGhostBlockEvent(player, cuboid, newLocation, item.getData());
                Bukkit.getPluginManager().callEvent(placeEvent);
                if (!placeEvent.isCancelled()) {
                    newLocation.getBlock().setType(item.getType());
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        return;
                    }
                    if (item.getAmount() == 0) {
                        player.setItemInHand(null);
                    } else {
                        item.setAmount(item.getAmount() - 1);
                        player.setItemInHand(item);
                    }
                }
            }
        });
        // Handle placement of bulk map chunk packets
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Server.MAP_CHUNK_BULK)) {
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
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Collections.singleton(PacketType.Play.Server.MAP_CHUNK)) {
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
        chunkMap.b = chunkMapWrapper.bitmask;
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
    private static class ChunkMapWrapper {

        private final World.Environment environment; // Only overworld sends skylight
        private final byte[] biomeData = new byte[256]; // Copied from the original data array
        private final ChunkSectionWrapper[] chunkSections = new ChunkSectionWrapper[16];
        private int bitmask;

        private static final byte[] BLOCK_LIGHT_DATA = new byte[2048];
        private static final byte[] SKY_LIGHT_DATA = new byte[2048];

        static {
            Arrays.fill(BLOCK_LIGHT_DATA, (byte) 0);
            Arrays.fill(SKY_LIGHT_DATA, (byte) 255);
        }

        public ChunkMapWrapper(World.Environment environment, byte[] originalData, int bitmask) {
            this.environment = environment;
            this.bitmask = bitmask;
            System.arraycopy(originalData, originalData.length - 256, this.biomeData, 0, 256);

            // Copy the original data array into the new block data array
            char[] chars = Integer.toBinaryString(bitmask).toCharArray();
            // The array must be reversed, so we read from bottom up rather than top down
            ArrayUtils.reverse(chars);
            int counter = 0;
            for (int i = 0; i < chars.length; i++) {
                // Chunk sections with a mask of 0 have no data for us to copy over
                if (chars[i] != '1') {
                    continue;
                }
                this.chunkSections[i] = new ChunkSectionWrapper(i);
                System.arraycopy(originalData, counter * 8192, this.chunkSections[i].blockData, 0, 8192);
                counter++;
            }
        }

        /**
         * Set the block at the provided world coordinates to the provided block registry id
         *
         * @param x               the x coordinate
         * @param y               the y coordinate
         * @param z               the z coordinate
         * @param blockRegistryID the block registry ID
         */
        public void setBlock(int x, int y, int z, int blockRegistryID) {
            int sectionY = y >> 4;
            ChunkSectionWrapper chunkSectionWrapper = this.chunkSections[sectionY];
            if (chunkSectionWrapper == null) {
                chunkSectionWrapper = new ChunkSectionWrapper(sectionY);
                this.chunkSections[sectionY] = chunkSectionWrapper;
            }
            chunkSectionWrapper.setBlock(x, y, z, blockRegistryID);
        }

        /**
         * Build the new chunk map data array and set the bitmask to the appropriate value
         *
         * @return the new data array
         */
        public byte[] buildChunkMapData() {
            int populatedSections = 0;
            byte[] data = new byte[0];
            StringBuilder bitmask = new StringBuilder();
            for (ChunkSectionWrapper chunkSection : this.chunkSections) {
                if (chunkSection == null) {
                    bitmask.append("0");
                    continue;
                }
                bitmask.append("1");
                ++populatedSections;
                data = ArrayUtils.addAll(data, chunkSection.blockData);
            }
            // Add block light data
            for (int i = 0; i < populatedSections; i++) {
                data = ArrayUtils.addAll(data, BLOCK_LIGHT_DATA);
            }
            // Add sky-light data for the over-world
            if (this.environment == World.Environment.NORMAL) {
                for (int i = 0; i < populatedSections; i++) {
                    data = ArrayUtils.addAll(data, SKY_LIGHT_DATA);
                }
            }
            // Set bitmask and add biome data
            String bitmaskBinary = Utils.removeBeginningCharacters(StringUtils.reverse(bitmask.toString()), '0');
            this.bitmask = Integer.parseInt(bitmaskBinary, 2);
            return ArrayUtils.addAll(data, this.biomeData);
        }

        @Getter
        private static class ChunkSectionWrapper {

            private final int sectionY;
            private final byte[] blockData;

            public ChunkSectionWrapper(int sectionY) {
                this(sectionY, new byte[8192]);
            }

            public ChunkSectionWrapper(int sectionY, byte[] blockData) {
                this.sectionY = sectionY;
                this.blockData = blockData;
            }

            /**
             * Convert the provided x, y, and z world coordinates into their respective block's index in the data array
             *
             * @param x the world x coordinate
             * @param y the world y coordinate
             * @param z the world z coordinate
             * @return the index
             */
            public int getIndex(int x, int y, int z) {
                return ((((y) << 8) | ((z & 0xF) << 4) | (x & 0xF)) * 2);
            }

            public void setBlock(int x, int y, int z, int blockRegistryID) {
                int index = getIndex(x % 16, y % 16, z % 16);
                this.blockData[index] = (byte) (blockRegistryID & 255);
                this.blockData[index + 1] = (byte) (blockRegistryID >> 8 & 255);
            }
        }
    }
}
