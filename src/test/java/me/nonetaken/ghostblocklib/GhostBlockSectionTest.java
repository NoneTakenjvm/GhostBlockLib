package me.nonetaken.ghostblocklib;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostBlockSectionTest {

    @Test
    void unsetCellsAreAir() {
        GhostBlockSection section = new GhostBlockSection();
        assertTrue(section.isEmpty());
        assertEquals(Material.AIR, section.getMaterial(0, 0, 0));
        assertEquals(Material.AIR, section.getMaterial(15, 15, 15));
    }

    @Test
    void storesAndRetrievesMaterials() {
        GhostBlockSection section = new GhostBlockSection();
        section.setMaterial(3, 7, 11, Material.STONE);
        section.setMaterial(0, 0, 0, Material.DIRT);
        section.setMaterial(15, 15, 15, Material.COAL_ORE);

        assertEquals(Material.STONE, section.getMaterial(3, 7, 11));
        assertEquals(Material.DIRT, section.getMaterial(0, 0, 0));
        assertEquals(Material.COAL_ORE, section.getMaterial(15, 15, 15));
        assertFalse(section.isEmpty());
    }

    @Test
    void clearingOnlyBlockMakesSectionEmpty() {
        GhostBlockSection section = new GhostBlockSection();
        section.setMaterial(4, 4, 4, Material.IRON_ORE);
        section.setMaterial(4, 4, 4, Material.AIR);
        assertTrue(section.isEmpty());
    }

    @Test
    void reusesPaletteIndexForSameMaterial() {
        GhostBlockSection section = new GhostBlockSection();
        section.setMaterial(0, 0, 0, Material.STONE);
        section.setMaterial(1, 0, 0, Material.STONE);
        section.setMaterial(0, 1, 0, Material.DIRT);
        section.setMaterial(1, 1, 0, Material.DIRT);

        assertEquals(Material.STONE, section.getMaterial(0, 0, 0));
        assertEquals(Material.STONE, section.getMaterial(1, 0, 0));
        assertEquals(Material.DIRT, section.getMaterial(0, 1, 0));
        assertEquals(Material.DIRT, section.getMaterial(1, 1, 0));
    }
}
