package me.nonetaken.ghostblocklib.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
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
    private Vector[] allVectors;

    public Cuboid(World world, Vector min, Vector max) {
        min.setY(Math.max(0, min.getBlockY())); // avoid negative y values
        max.setY(Math.min(255, max.getBlockY())); // avoid y values above 255
        this.world = world;
        this.min = min;
        this.max = max;

        this.setAllPoints();
    }

    /**
     * Repopulate {@link #allVectors} with every point in this cuboid
     */
    private void setAllPoints() {
        this.allVectors = new Vector[this.getVolume()];
        int index = 0;
        for (int x = this.min.getBlockX(); x <= this.max.getBlockX(); x++) {
            for (int y = this.min.getBlockY(); y <= this.max.getBlockY(); y++) {
                for (int z = this.min.getBlockZ(); z <= this.max.getBlockZ(); z++) {
                    this.allVectors[index++] = new Vector(x, y, z);
                }
            }
        }
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

    /**
     * Set the minimum point of this cuboid
     *
     * @param min the new min point
     */
    public void setMin(Vector min) {
        this.min = min;
        this.setAllPoints();
    }

    /**
     * Set the maximum point of this cuboid
     *
     * @param max the new max point
     */
    public void setMax(Vector max) {
        this.max = max;
        this.setAllPoints();
    }

    /**
     * Return whether this cuboid contains the provided {@link Location}
     *
     * @param location the location
     *
     * @return whether this cuboid contains the location
     */
    public boolean contains(Location location) {
        return this.contains(location.toVector());
    }

    /**
     * Return whether this cuboid contains the provided {@link Vector}
     *
     * @param vector the vector
     *
     * @return whether this cuboid contains the vector
     */
    public boolean contains(Vector vector) {
        return this.contains(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    /**
     * Return whether this cuboid contains the provided x, y and z coordinates
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     *
     * @return whether this cuboid contains the provided x, y and z coordinates
     */
    public boolean contains(int x, int y, int z) {
        return (y >= this.min.getBlockY() && y <= this.max.getBlockY()) && this.contains(x, z);

    }

    /**
     * Return whether this cuboid contains the provided x and z coordinates
     *
     * @param x the x coordinate
     * @param z the z coordinate
     *
     * @return whether this cuboid contains the provided x and z coordinates
     */
    public boolean contains(int x, int z) {
        return x >= this.min.getBlockX() && x <= this.max.getBlockX() && z >= this.min.getBlockZ() && z <= this.max.getBlockZ();
    }
}
