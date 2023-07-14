package me.nonetaken.ghostblocklib.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.util.Vector;

/*
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 01/07/2023 at 12:20
 */
@Getter
@Setter
public class Cuboid {

    private World world;

    private Vector min;
    private Vector max;

    public Cuboid(World world, Vector min, Vector max) {
        min.setY(Math.max(0, min.getBlockY())); // avoid negative y values
        max.setY(Math.min(255, max.getBlockY())); // avoid y values above 255
        this.world = world;
        this.min = min;
        this.max = max;
    }

    /**
     * Return an array of all points in this cuboid
     *
     * @return the array of all points
     */
    public Vector[] getAllVectors() {
        Vector[] points = new Vector[this.getVolume()];
        int index = 0;
        for (int x = this.min.getBlockX(); x <= this.max.getBlockX(); x++) {
            for (int y = this.min.getBlockY(); y <= this.max.getBlockY(); y++) {
                for (int z = this.min.getBlockZ(); z <= this.max.getBlockZ(); z++) {
                    points[index++] = new Vector(x, y, z);
                }
            }
        }
        return points;
    }

    /**
     * Return the volume of this cuboid
     *
     * @return the volume
     */
    public int getVolume() {
        int deltaX = 1 + Math.abs(this.max.getBlockX() - this.min.getBlockX());
        int deltaY = 1 + Math.abs(this.max.getBlockY() - this.min.getBlockY());
        int deltaZ = 1 + Math.abs(this.max.getBlockZ() - this.min.getBlockZ());
        return deltaX * deltaY * deltaZ;
    }

    public boolean contains(int x, int y, int z) {
        return (y >= this.min.getBlockY() && y <= this.max.getBlockY()) && this.contains(x, z);

    }

    public boolean contains(int x, int z) {
        return x >= this.min.getBlockX() && x <= this.max.getBlockX() && z >= this.min.getBlockZ() && z <= this.max.getBlockZ();
    }
}
