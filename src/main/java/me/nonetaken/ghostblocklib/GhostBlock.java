package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.EnumMap;
import java.util.Map;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 15:24
 */
@Getter
public class GhostBlock {

    private static final Map<Material, WrappedBlockState> BLOCK_STATE_CACHE = new EnumMap<>(Material.class);

    private int x;
    private int y;
    private int z;
    private Material material = Material.AIR;

    public GhostBlock(Vector vector) {
        this(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public GhostBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void setPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set this ghost block's type to the provided {@link Material}
     *
     * @param material the material
     * @return this ghost block
     */
    public GhostBlock setType(Material material) {
        this.material = material;
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
     * @return the location
     */
    public Location getLocation(World world) {
        return this.getVector().toLocation(world);
    }

    public int getGlobalId() {
        return this.getBlockState().getGlobalId();
    }

    public WrappedBlockState getBlockState() {
        return getBlockState(this.material);
    }

    static WrappedBlockState getBlockState(Material material) {
        return BLOCK_STATE_CACHE.computeIfAbsent(material, value ->
                SpigotConversionUtil.fromBukkitBlockData(value.createBlockData()));
    }

    static int getGlobalId(Material material) {
        return material == Material.AIR ? 0 : getBlockState(material).getGlobalId();
    }
}
