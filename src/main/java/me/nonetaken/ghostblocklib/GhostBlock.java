package me.nonetaken.ghostblocklib;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 15:24
 *
 */
@SuppressWarnings("ALL")
@Getter
public class GhostBlock {

    private int x; // world x coordinatae
    private int y; // world y coordinate
    private int z; // world z coordinate
    private Material material = Material.AIR;
    private byte data;

    public GhostBlock(Vector vector) {
        this(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public GhostBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set this ghost block's type to the provided {@link Material}
     *
     * @param material the material
     *
     * @return this ghost block
     */
    public GhostBlock setType(Material material) {
        this.material = material;
        this.data = (byte) 0;
        return this;
    }

    /**
     * Set this ghost block's type to the provided {@link MaterialData}
     *
     * @param materialData the material data
     *
     * @return this ghost block
     */
    public GhostBlock setTypeAndData(MaterialData materialData) {
        this.material = materialData.getItemType();
        this.data = materialData.getData();
        return this;
    }

    /**
     * Return a {@link MaterialData} object based on this blocks {@link #material} and {@link #data}
     *
     * @return the material data
     */
    public MaterialData getMaterialData() {
        return new MaterialData(this.material, this.data);
    }

    /**
     * Return the NMS combinedID of this ghost block based on its {@link #material} and {@link #data}
     *
     * @return the combinedID
     */
    public int getCombinedID() {
        return this.material.getId() + (this.data << 12);
    }

    /**
     * Return the NMS BlockRegistryID of this ghost block based on its combinedID
     * @see #getCombinedID()
     *
     * @return the block registry ID
     */
    public int getBlockRegistryID() {
        return Block.d.b(Block.getByCombinedId(this.getCombinedID()));
    }

    /**
     * Return the {@link WrappedBlockData} of this ghost block based on its {@link #material} and {@link #data}
     *
     * @return the wrapped block data
     */
    public WrappedBlockData getWrappedBlockData() {
        return WrappedBlockData.createData(this.material, this.data);
    }

    /**
     * Return the position of this ghost block as a {@link Vector}
     *
     * @return the vector
     */
    public Vector getVector() {
        return new Vector(this.x, this.y, this.z);
    }

    /**
     * Return the position of this ghost block as a {@link Location} in the provided {@link World}
     *
     * @param world the world
     *
     * @return the location
     */
    public Location getLocation(World world) {
        return this.getVector().toLocation(world);
    }
}
