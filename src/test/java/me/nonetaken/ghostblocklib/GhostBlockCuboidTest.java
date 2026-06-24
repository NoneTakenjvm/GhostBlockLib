package me.nonetaken.ghostblocklib;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostBlockCuboidTest {

    private World world;

    @BeforeEach
    void setUp() {
        GhostBlockManager.clearForTests();
        this.world = Mockito.mock(World.class);
    }

    @AfterEach
    void tearDown() {
        GhostBlockManager.clearForTests();
    }

    @Test
    void fillStoresBlocksAcrossVolume() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(2, 64, 2));
        cuboid.fill(block -> block.setType(Material.STONE));

        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) {
                GhostBlock block = cuboid.getBlock(x, 64, z);
                assertNotNull(block);
                assertEquals(Material.STONE, block.getMaterial());
            }
        }
        assertTrue(cuboid.getChangeCount() >= cuboid.getVolume());
    }

    @Test
    void constructorCreatesChunkShellsWithoutSections() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(31, 70, 31));
        assertFalse(cuboid.getChunks().isEmpty());
        for (GhostBlockChunk chunk : cuboid.getChunks().values()) {
            assertTrue(chunk.getSections().isEmpty());
        }
    }

    @Test
    void updateBoundsExpandPreservesOverlappingBlockData() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 64, 15));
        cuboid.setBlock(new GhostBlock(5, 64, 5).setType(Material.EMERALD_ORE));

        cuboid.setMax(new Vector(31, 64, 31));

        GhostBlock preserved = cuboid.getBlock(5, 64, 5);
        assertNotNull(preserved);
        assertEquals(Material.EMERALD_ORE, preserved.getMaterial());
        assertTrue(cuboid.getChunks().size() > 1);
    }

    @Test
    void updateBoundsShrinkDropsOutOfFootprintChunks() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(31, 64, 31));
        int initialChunks = cuboid.getChunks().size();
        cuboid.setBlock(new GhostBlock(30, 64, 30).setType(Material.REDSTONE_ORE));

        cuboid.setMax(new Vector(15, 64, 15));

        assertTrue(cuboid.getChunks().size() < initialChunks);
        assertNull(cuboid.getBlock(30, 64, 30));
    }

    @Test
    void expandWhileRegisteredStaysRegisteredAndIndexed() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 64, 15));
        GhostBlockManager.registerGhostBlockCuboid(cuboid);
        int initialChunkKeys = cuboid.getChunks().size();

        cuboid.expand(GhostBlockCuboid.CuboidDirection.SOUTH, 16);

        assertTrue(GhostBlockManager.isRegistered(cuboid));
        assertTrue(cuboid.getChunks().size() > initialChunkKeys);
        assertSame(cuboid, GhostBlockManager.getCuboidCoordinateHandler(this.world)
                .getHighestPriorityCuboidByLocation(0, 64, 20));
    }

    @Test
    void expandWhileUnregisteredDoesNotEnterSpatialIndex() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 64, 15));
        cuboid.expand(GhostBlockCuboid.CuboidDirection.EAST, 16);

        assertFalse(GhostBlockManager.isRegistered(cuboid));
        assertNull(GhostBlockManager.getCuboidCoordinateHandler(this.world)
                .getHighestPriorityCuboidByLocation(5, 64, 5));
    }

    @Test
    void registeredExpandPatchesSpatialIndexForNewChunksOnly() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 64, 15));
        GhostBlockManager.registerGhostBlockCuboid(cuboid);
        GhostBlockManager.CuboidCoordinateHandler handler = GhostBlockManager.getCuboidCoordinateHandler(this.world);

        cuboid.expand(GhostBlockCuboid.CuboidDirection.SOUTH, 16);

        assertFalse(handler.getCuboidsAtChunk(0, 0).isEmpty());
        assertFalse(handler.getCuboidsAtChunk(0, 1).isEmpty());
        assertEquals(1, handler.getCuboidsAtChunk(0, 0).size());
        assertEquals(1, handler.getCuboidsAtChunk(0, 1).size());
    }
}
