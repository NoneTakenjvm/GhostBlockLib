package me.nonetaken.ghostblocklib;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostBlockChunkTest {

    private World world;
    private GhostBlockCuboid cuboid;

    @BeforeEach
    void setUp() {
        GhostBlockManager.clearForTests();
        this.world = Mockito.mock(World.class);
        this.cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 79, 15));
    }

    @AfterEach
    void tearDown() {
        GhostBlockManager.clearForTests();
    }

    @Test
    void lazyAllocatesSectionsOnWrite() {
        GhostBlockChunk chunk = this.cuboid.getGhostBlockChunk(0, 0);
        assertTrue(chunk.getSections().isEmpty());

        chunk.setMaterial(0, 64, 0, Material.STONE);
        assertEquals(1, chunk.getSections().size());
        assertNotNull(chunk.getSections().get(4));
    }

    @Test
    void getBlockReturnsNullForUnsetAndAir() {
        GhostBlockChunk chunk = this.cuboid.getGhostBlockChunk(0, 0);
        assertNull(chunk.getBlock(0, 64, 0));

        chunk.setMaterial(0, 64, 0, Material.STONE);
        assertNotNull(chunk.getBlock(0, 64, 0));

        chunk.setMaterial(0, 64, 0, Material.AIR);
        assertNull(chunk.getBlock(0, 64, 0));
        assertTrue(chunk.getSections().isEmpty());
    }

    @Test
    void negativeCoordinatesUseFloorMod() {
        GhostBlockCuboid negativeCuboid = new GhostBlockCuboid(this.world, new Vector(-16, 64, -16), new Vector(-1, 64, -1));
        GhostBlockChunk chunk = negativeCuboid.getGhostBlockChunk(-1, -1);
        chunk.setMaterial(-1, 64, -1, Material.GOLD_ORE);

        GhostBlock block = chunk.getBlock(-1, 64, -1);
        assertNotNull(block);
        assertEquals(Material.GOLD_ORE, block.getMaterial());
    }

    @Test
    void tracksChangesOnWrite() {
        GhostBlockChunk chunk = this.cuboid.getGhostBlockChunk(0, 0);
        chunk.setMaterial(1, 65, 2, Material.DIAMOND_ORE);
        assertEquals(1, chunk.getChanges().size());
        assertEquals(1, chunk.getChanges().getFirst().getX());
        assertEquals(65, chunk.getChanges().getFirst().getY());
        assertEquals(2, chunk.getChanges().getFirst().getZ());
    }
}
