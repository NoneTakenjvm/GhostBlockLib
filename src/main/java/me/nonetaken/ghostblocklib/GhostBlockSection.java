package me.nonetaken.ghostblocklib;

import org.bukkit.Material;

import java.util.Arrays;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Compact 16x16x16 section storage with a per-section Material palette.
 */
class GhostBlockSection {

    private static final int SIZE = 4096;

    private final byte[] data = new byte[SIZE];
    private Material[] palette = new Material[]{Material.AIR};
    private int paletteSize = 1;

    Material getMaterial(int localX, int localY, int localZ) {
        return this.palette[this.data[index(localX, localY, localZ)] & 0xFF];
    }

    void setMaterial(int localX, int localY, int localZ, Material material) {
        this.data[index(localX, localY, localZ)] = this.getOrCreatePaletteIndex(material);
    }

    boolean isEmpty() {
        for (byte index : this.data) {
            if (index != 0) {
                return false;
            }
        }
        return true;
    }

    private byte getOrCreatePaletteIndex(Material material) {
        if (material == Material.AIR) {
            return 0;
        }
        for (byte i = 0; i < this.paletteSize; i++) {
            if (this.palette[i] == material) {
                return i;
            }
        }
        if (this.paletteSize >= 256) {
            throw new IllegalStateException("Section palette cannot exceed 256 block types");
        }
        if (this.paletteSize >= this.palette.length) {
            this.palette = Arrays.copyOf(this.palette, Math.min(this.palette.length * 2, 256));
        }
        this.palette[this.paletteSize] = material;
        return (byte) this.paletteSize++;
    }

    private static int index(int localX, int localY, int localZ) {
        return (localY << 8) | (localZ << 4) | localX;
    }
}
