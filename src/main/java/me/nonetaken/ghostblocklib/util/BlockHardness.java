package me.nonetaken.ghostblocklib.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.EnumSet;

import static org.bukkit.Material.*;

/**
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 17/07/2023 at 20:45
 *
 * @Author benwithjamin
 */
@UtilityClass
public class BlockHardness {

    private static final EnumSet<Material> SHOVEL_MATERIALS = EnumSet.of(
            DIRT,
            SAND,
            GRASS_BLOCK,
            CLAY,
            GRAVEL,
            SOUL_SAND,
            SNOW_BLOCK,
            SNOW,
            MYCELIUM
    );

    private static final EnumSet<Material> AXE_MATERIALS = EnumSet.of(
            ACACIA_WOOD,
            BIRCH_WOOD,
            DARK_OAK_WOOD,
            JUNGLE_WOOD,
            OAK_WOOD,
            SPRUCE_WOOD,
            OAK_LOG,
            SPRUCE_LOG,
            BIRCH_LOG,
            JUNGLE_LOG,
            ACACIA_LOG,
            DARK_OAK_LOG
    );

    private static final EnumSet<Material> NO_BEST_TOOL_MATERIALS = EnumSet.of(
            SEA_LANTERN,
            SPONGE,
            GLASS,
            GLOWSTONE
    );

    /**
     * Checks if the player is using the best tool for the block
     *
     * @param material The material of the block
     * @param item     The item the player is using
     * @return Whether the player is using the best tool
     */
    public static boolean isBestTool(Material material, Material item) {
        if (SHOVEL_MATERIALS.contains(material)) {
            return switch (item) {
                case WOODEN_SHOVEL, STONE_SHOVEL, IRON_SHOVEL, GOLDEN_SHOVEL, DIAMOND_SHOVEL -> true;
                default -> false;
            };
        }
        else if (AXE_MATERIALS.contains(material)) {
            return switch (item) {
                case WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE -> true;
                default -> false;
            };
        }
        return !NO_BEST_TOOL_MATERIALS.contains(material);
    }

    /**
     * Gets the speed of the tool
     *
     * @param item The item to check
     * @return The speed of the tool
     */
    public static int getToolSpeed(Material item) {
        if (item == null) {
            return 1;
        }
        else if (item == WOODEN_SHOVEL || item == WOODEN_AXE || item == WOODEN_PICKAXE || item == SHEARS) {
            return 2;
        }
        else if (item == GOLDEN_SHOVEL || item == GOLDEN_AXE || item == GOLDEN_PICKAXE || item == STONE_SHOVEL || item == STONE_AXE || item == STONE_PICKAXE) {
            return 4;
        }
        else if (item == IRON_SHOVEL || item == IRON_AXE || item == IRON_PICKAXE) {
            return 6;
        }
        else if (item == DIAMOND_SHOVEL || item == DIAMOND_AXE || item == DIAMOND_PICKAXE) {
            return 8;
        }
        return 1;
    }

    /**
     * Gets the length of time it takes to break a block
     *
     * @param player    The player
     * @param itemStack The item the player is using
     * @param block     The block the player is breaking
     * @return The time it takes to break the block 0 if insta break
     */
    public static float getBreakDuration(Player player, ItemStack itemStack, Material block) {
        float speedMultiplier = 1f;
        if (isBestTool(block, itemStack.getType())) {
            speedMultiplier = getToolSpeed(itemStack.getType());
            if (itemStack.getEnchantmentLevel(Enchantment.EFFICIENCY) > 0) {
                speedMultiplier += (float) (Math.pow(itemStack.getEnchantmentLevel(Enchantment.EFFICIENCY), 2) + 1);
            }
        }
        for (PotionEffect potionEffect : new ArrayList<>(player.getActivePotionEffects())) {
            if (potionEffect.getType().equals(PotionEffectType.HASTE)) {
                speedMultiplier *= (float) (0.2 * (potionEffect.getAmplifier() + 1) + 1);
                break;
            }
        }
        if (player.isFlying()) {
            speedMultiplier /= 5f;
        }
        float damage = speedMultiplier / block.getHardness();
        if (isBestTool(block, itemStack.getType())) {
            damage /= 30f;
        } else {
            damage /= 100f;
        }
        // damage above 1 is an instant break
        if (damage > 1) {
            return 0;
        }
        int ticks = (int) Math.ceil(1f / damage);
        return ticks / 20f;
    }

    public static boolean canInstantBreak(Player player, ItemStack item, Material block) {
        return getBreakDuration(player, item, block) == 0;
    }
}
