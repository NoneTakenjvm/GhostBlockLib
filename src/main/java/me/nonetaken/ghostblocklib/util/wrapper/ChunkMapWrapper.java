package me.nonetaken.ghostblocklib.util.wrapper;

import lombok.Getter;
import me.nonetaken.ghostblocklib.util.Utils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.World;

import java.util.Arrays;

/**
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 31/07/2023 at 16:40
 */
@Getter
public class ChunkMapWrapper {

    private final World.Environment environment; // Only overworld sends skylight
    private final byte[] biomeData = new byte[256]; // Copied from the original data array
    private final ChunkSectionWrapper[] chunkSections = new ChunkSectionWrapper[16];
    private int bitmask;

    private static final byte[] BLOCK_LIGHT_DATA = new byte[2048];
    private static final byte[] SKY_LIGHT_DATA = new byte[2048];

    static {
        Arrays.fill(BLOCK_LIGHT_DATA, (byte) 0); // Block-light we will assume to be 0 (dark)
        Arrays.fill(SKY_LIGHT_DATA, (byte) 255); // Sky-light we will assume to be 255 (full bright)
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
     * @return the new data array, ready to be sent to the client in a MapChunkPacket
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

        private final int sectionY; // the y coordinate of the chunk section (0-15)
        private final byte[] blockData; // the data array representing the blocks in this chunk section

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

        /**
         * Set the block at the provided world coordinates to the provided block registry id
         *
         * @param x the world x coordinate
         * @param y the world y coordinate
         * @param z the world z coordinate
         * @param blockRegistryID the block registry ID
         */
        public void setBlock(int x, int y, int z, int blockRegistryID) {
            int index = getIndex(x % 16, y % 16, z % 16); // Assure all coordinates are relative to the chunk section
            this.blockData[index] = (byte) (blockRegistryID & 255);
            this.blockData[index + 1] = (byte) (blockRegistryID >> 8 & 255);
        }
    }
}