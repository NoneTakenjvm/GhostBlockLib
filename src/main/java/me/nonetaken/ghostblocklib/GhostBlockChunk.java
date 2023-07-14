package me.nonetaken.ghostblocklib;

import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import lombok.Getter;
import me.nonetaken.ghostblocklib.util.IntTriple;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 15:24
 */
@Getter
public class GhostBlockChunk {

    private final GhostBlockCuboid parent;
    private final int chunkX;
    private final int chunkZ;
    private final GhostBlock[][][] blocks = new GhostBlock[16][256][16]; // x y z
    private final List<IntTriple> changes = Collections.synchronizedList(new ArrayList<>(1024));
    private int changeCount = 0;

    protected GhostBlockChunk(GhostBlockCuboid parent, int chunkX, int chunkZ) {
        this.parent = parent;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Return the {@link GhostBlock} at the provided world coordinates
     *
     * @param x the world x coordinate
     * @param y the world y coordinate
     * @param z the world z coordinate
     * @return the ghost block at the given coordinates
     */
    @Nullable
    public GhostBlock getBlock(int x, int y, int z) {
        return this.blocks[Math.abs(x % 16)][y][Math.abs(z % 16)];
    }

    /**
     * Set the {@link GhostBlock} in {@link #blocks}
     *
     * @param block the ghost block to set
     */
    public synchronized void setBlock(GhostBlock block) {
        this.blocks[Math.floorMod(block.getX(), 16)][block.getY()][Math.floorMod(block.getZ(), 16)] = block;
        if (1024 >= ++this.changeCount) {
            this.changes.add(new IntTriple(block.getX(), block.getY(), block.getZ()));
        }
    }

    /**
     * Refresh this chunk for the provided {@link Player}s
     * This function will reset {@link #changeCount} to 0, so all players that should see the changes should be provided
     *
     * @param players the players to refresh this chunk for
     */
    public synchronized void flushChanges(Player... players) {
        if (this.changeCount > 1024) {
            Chunk chunk = players[0].getWorld().getChunkAt(this.chunkX, this.chunkZ);
            net.minecraft.server.v1_8_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            for (Player player : players) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(nmsChunk, true, 0));
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(nmsChunk, true, 65535));
            }
        }
        else {
            short[] changeArray = new short[this.changeCount];
            for (int i = 0; i < this.changes.size(); i++) {
                IntTriple changeCoordinates = this.changes.get(i);
                GhostBlock changedBlock = this.getBlock(changeCoordinates.getX(), changeCoordinates.getY(), changeCoordinates.getZ());
                if (changedBlock == null) {
                    continue;
                }
                changeArray[i] = (short) ((changedBlock.getX() & 15) << 12 | (changedBlock.getZ() & 15) << 8 | changedBlock.getY());
            }
            PacketPlayOutMultiBlockChange multiBlockChange = new PacketPlayOutMultiBlockChange(this.changeCount, changeArray, ((CraftChunk) parent.getWorld().getChunkAt(this.chunkX, this.chunkZ)).getHandle());
            for (Player player : players) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(multiBlockChange);
            }
        }
        this.changeCount = 0;
        this.changes.clear();
    }
}
