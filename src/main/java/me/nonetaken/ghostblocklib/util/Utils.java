package me.nonetaken.ghostblocklib.util;

import com.github.retrooper.packetevents.util.Vector3i;
import lombok.experimental.UtilityClass;
import org.bukkit.util.Vector;


/*
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 26/07/2023 at 20:17
 */
@UtilityClass
public class Utils {

    /**
     * Create an array of all vector points between the provided minimum and maximum points
     *
     * @param min the min point
     * @param max the max point
     * @return all vectors between
     */
    public static Vector[] getVectorsBetween(Vector min, Vector max) {
        // Calculate the size of the array
        int deltaX = 1 + Math.abs(max.getBlockX() - min.getBlockX());
        int deltaY = 1 + Math.abs(max.getBlockY() - min.getBlockY());
        int deltaZ = 1 + Math.abs(max.getBlockZ() - min.getBlockZ());
        int volume = deltaX * deltaY * deltaZ;
        Vector[] vectors = new Vector[volume];

        // Fill the array
        int index = 0;
        for (int x = 0; x < deltaX; x++) {
            for (int y = 0; y < deltaY; y++) {
                for (int z = 0; z < deltaZ; z++) {
                    vectors[index++] = new Vector(min.getBlockX() + x, min.getBlockY() + y, min.getBlockZ() + z);
                }
            }
        }
        return vectors;
    }

    public int toChunkCoordinate(int worldCoordinate) {
        return (worldCoordinate - (worldCoordinate % 16)) / 16;
    }

    public int toWorldCoordinate(int chunkCoordinate) {
        return chunkCoordinate * 16;
    }

    public Vector3i toVector3i(Vector vector) {
        return new Vector3i(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
}
