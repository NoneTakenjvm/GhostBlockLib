package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
    private WrappedBlockState blockState;

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
        this.blockState = SpigotConversionUtil.fromBukkitBlockData(this.material.createBlockData());
        return this;
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

    public int getGlobalId() {
        return this.blockState.getGlobalId();
    }

    public WrappedBlockState getBlockState() {
        return this.blockState;
    }
}
