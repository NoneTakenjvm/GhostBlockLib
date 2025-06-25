package me.nonetaken.ghostblocklib.util;

import lombok.Getter;

/**
 * Made by NoneTaken on 25/06/2025 at 13:21
 */
@Getter
public class ChunkIntCoordinatePair {

    private final int chunkX;
    private final int chunkZ;

    public ChunkIntCoordinatePair(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public int hashCode() {
        int i = 1664525 * this.chunkX + 1013904223;
        int j = 1664525 * (this.chunkZ ^ -559038737) + 1013904223;
        return i ^ j;
    }
}
