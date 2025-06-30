package me.nonetaken.ghostblocklib.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Made by NoneTaken on 25/06/2025 at 13:21
 */
@Getter @EqualsAndHashCode
public class ChunkIntCoordinatePair {

    private final int chunkX;
    private final int chunkZ;

    public ChunkIntCoordinatePair(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
}
