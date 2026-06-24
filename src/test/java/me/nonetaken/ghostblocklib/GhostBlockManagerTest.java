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
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostBlockManagerTest {

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
    void registerIsIdempotentAndPreservesBlockData() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 64, 15));
        cuboid.setBlock(new GhostBlock(3, 64, 3).setType(Material.LAPIS_ORE));

        GhostBlockManager.registerGhostBlockCuboid(cuboid);
        GhostBlockManager.registerGhostBlockCuboid(cuboid);

        assertEquals(1, GhostBlockManager.registeredCountForTests());
        assertNotNull(cuboid.getBlock(3, 64, 3));
        assertEquals(Material.LAPIS_ORE, cuboid.getBlock(3, 64, 3).getMaterial());
    }

    @Test
    void registerDoesNotDuplicateSpatialIndexEntries() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 64, 15));
        GhostBlockManager.registerGhostBlockCuboid(cuboid);
        GhostBlockManager.registerGhostBlockCuboid(cuboid);

        assertEquals(1, GhostBlockManager.getCuboidCoordinateHandler(this.world).getCuboidsAtChunk(0, 0).size());
    }

    @Test
    void unregisterClearsChunksAndRegistration() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 64, 15));
        cuboid.setBlock(new GhostBlock(1, 64, 1).setType(Material.COBBLESTONE));
        GhostBlockManager.registerGhostBlockCuboid(cuboid);

        cuboid.unregister();

        assertFalse(GhostBlockManager.isRegistered(cuboid));
        assertTrue(cuboid.getChunks().isEmpty());
        assertNull(GhostBlockManager.getCuboidCoordinateHandler(this.world)
                .getHighestPriorityCuboidByLocation(1, 64, 1));
    }

    @Test
    void unregisteredCuboidIsNotReRegisteredByBoundsUpdate() {
        GhostBlockCuboid cuboid = new GhostBlockCuboid(this.world, new Vector(0, 64, 0), new Vector(15, 64, 15));
        GhostBlockManager.registerGhostBlockCuboid(cuboid);
        cuboid.unregister();

        cuboid.setMax(new Vector(31, 64, 31));

        assertFalse(GhostBlockManager.isRegistered(cuboid));
        assertTrue(GhostBlockManager.getCuboidCoordinateHandler(this.world).getCuboidsAtChunk(0, 0).isEmpty());
    }
}
