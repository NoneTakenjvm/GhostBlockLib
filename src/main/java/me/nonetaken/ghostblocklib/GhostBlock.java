package me.nonetaken.ghostblocklib;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.Item;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
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

    public GhostBlock setType(Material material) {
        this.material = material;
        this.data = (byte) 0;
        return this;
    }

    public GhostBlock setTypeAndData(MaterialData materialData) {
        this.material = materialData.getItemType();
        this.data = materialData.getData();
        return this;
    }

    public Vector getVector() {
        return new Vector(this.x, this.y, this.z);
    }

    public Location getLocation(World world) {
        return this.getVector().toLocation(world);
    }

    public int getCombinedID() {
        return this.material.getId() + (this.data << 12);
    }

    public int getBlockRegistryID() {
        return Block.d.b(Block.getByCombinedId(this.getCombinedID()));
    }

    public MaterialData getMaterialData() {
        return new MaterialData(this.material, this.data);
    }

    public WrappedBlockData getWrappedBlockData() {
        return WrappedBlockData.createData(this.material, this.data);
    }
}
