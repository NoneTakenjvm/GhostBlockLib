package me.nonetaken.ghostblocklib;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Project: me.nonetaken.ghostblocklib | Author: NoneTaken#0001
 * Created: 01/07/2023 at 15:24
 */
@Getter
public class GhostBlockChunk {

    private final GhostBlockCuboid parent;
    private final int chunkX;
    private final int chunkZ;
    private final Map<Integer, GhostBlockSection> sections = new HashMap<>();
    private final List<Vector3i> changes = Collections.synchronizedList(new ArrayList<>());

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
        GhostBlockSection section = this.sections.get(y >> 4);
        if (section == null) {
            return null;
        }
        Material material = section.getMaterial(Math.floorMod(x, 16), y & 15, Math.floorMod(z, 16));
        if (material == Material.AIR) {
            return null;
        }
        return new GhostBlock(x, y, z).setType(material);
    }

    /**
     * Set the {@link GhostBlock} in this chunk
     *
     * @param block the ghost block to set
     */
    public synchronized void setBlock(GhostBlock block) {
        this.setMaterial(block.getX(), block.getY(), block.getZ(), block.getMaterial());
    }

    synchronized void setMaterial(int x, int y, int z, Material material) {
        int sectionY = y >> 4;
        GhostBlockSection section = this.sections.get(sectionY);
        if (material == Material.AIR) {
            if (section == null) {
                return;
            }
            section.setMaterial(Math.floorMod(x, 16), y & 15, Math.floorMod(z, 16), material);
            if (section.isEmpty()) {
                this.sections.remove(sectionY);
            }
        } else {
            if (section == null) {
                section = new GhostBlockSection();
                this.sections.put(sectionY, section);
            }
            section.setMaterial(Math.floorMod(x, 16), y & 15, Math.floorMod(z, 16), material);
        }
        this.changes.add(new Vector3i(x, y, z));
    }

    Material getMaterial(int x, int y, int z) {
        GhostBlockSection section = this.sections.get(y >> 4);
        if (section == null) {
            return Material.AIR;
        }
        return section.getMaterial(Math.floorMod(x, 16), y & 15, Math.floorMod(z, 16));
    }

    /**
     * Refresh this chunk for the provided {@link Player}s
     * All players that should see the changes should be provided
     *
     * @param players the players to refresh this chunk for
     */
    public synchronized void refresh(Player... players) {
        if (this.changes.isEmpty()) {
            return;
        }
        Map<Integer, List<WrapperPlayServerMultiBlockChange.EncodedBlock>> encodedBlocks = new HashMap<>();
        for (Vector3i change : this.changes) {
            Material material = this.getMaterial(change.getX(), change.getY(), change.getZ());
            int globalId = GhostBlock.getGlobalId(material);
            encodedBlocks.computeIfAbsent(change.getY() >> 4, k -> new ArrayList<>())
                    .add(new WrapperPlayServerMultiBlockChange.EncodedBlock(globalId, change.getX(), change.getY(), change.getZ()));
        }
        for (Map.Entry<Integer, List<WrapperPlayServerMultiBlockChange.EncodedBlock>> entry : encodedBlocks.entrySet()) {
            WrapperPlayServerMultiBlockChange blockChangePacket = new WrapperPlayServerMultiBlockChange(
                    new Vector3i(this.chunkX, entry.getKey(), this.chunkZ),
                    true,
                    entry.getValue().toArray(new WrapperPlayServerMultiBlockChange.EncodedBlock[0])
            );
            for (Player player : players) {
                if (!GhostBlockLib.isIgnoringGhostBlocks(player)) {
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, blockChangePacket);
                }
            }
        }
        this.changes.clear();
    }
}
