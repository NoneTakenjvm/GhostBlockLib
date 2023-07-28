package me.nonetaken.ghostblocklib.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/*
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 17/07/2023 at 20:45
 */
public class BlockHardness {
    public static float getHardness(Material material) {
        switch (material) {
            case DIRT:
            case PACKED_ICE:
            case ICE:
            case SLIME_BLOCK:
            case SOUL_SAND:
            case SAND:
                return 0.5f;
            case STONE:
            case PRISMARINE:
                return 1.5f;
            case COBBLESTONE:
            case BRICK:
            case MOSSY_COBBLESTONE:
            case WOOD:
            case NETHER_BRICK:
            case LOG:
                return 2f;
            case COAL_ORE:
            case ENDER_STONE:
            case QUARTZ_ORE:
            case EMERALD_ORE:
            case DIAMOND_ORE:
            case GOLD_ORE:
            case REDSTONE_ORE:
            case LAPIS_ORE:
            case IRON_ORE:
                return 3f;
            case GRAVEL:
            case SPONGE:
            case CLAY:
                return 0.6f;
            case STAINED_CLAY:
            case HARD_CLAY:
                return 1.25f;
            case COAL_BLOCK:
            case GOLD_BLOCK:
            case REDSTONE_BLOCK:
            case IRON_BLOCK:
            case EMERALD_BLOCK:
            case DIAMOND_BLOCK:
            case LAPIS_BLOCK:
                return 5f;
            case SEA_LANTERN:
            case GLOWSTONE:
            case STAINED_GLASS:
            case GLASS:
                return 0.3f;
            case NETHERRACK:
                return 0.4f;
            case SNOW_BLOCK:
                return 0.2f;
            case QUARTZ_BLOCK:
            case SANDSTONE:
            case RED_SANDSTONE:
            case QUARTZ:
            case WOOL:
                return 0.8f;
            case OBSIDIAN:
                return 50f;
            default:
                return 1f;
        }
    }

    /**
     * Checks if the player is using the best tool for the block
     *
     * @param material The material of the block
     * @param item The item the player is using
     *
     * @return Whether the player is using the best tool
     */
    public static boolean isBestTool(Material material, Material item) {
        switch (material) {
            // spade
            case DIRT:
            case SAND:
            case GRASS:
            case CLAY:
            case GRAVEL:
            case SOIL:
            case SOUL_SAND:
            case SNOW_BLOCK:
            case SNOW:
            case MYCEL:
                switch (item) {
                    case WOOD_SPADE:
                    case STONE_SPADE:
                    case IRON_SPADE:
                    case GOLD_SPADE:
                    case DIAMOND_SPADE:
                        return true;
                    default:
                        return false;
                }
                // axe
            case WOOD:
            case LOG:
            case BIRCH_DOOR:
            case DARK_OAK_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case TRAP_DOOR:
            case WOODEN_DOOR:
            case ACACIA_DOOR:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
            case FENCE:
            case FENCE_GATE:
            case SPRUCE_WOOD_STAIRS:
            case ACACIA_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case DARK_OAK_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case CHEST:
            case TRAPPED_CHEST:
            case SIGN:
            case WOOD_PLATE:
            case WORKBENCH:
            case JUKEBOX:
            case BOOKSHELF:
            case WOOD_STEP:
            case WOOD_DOUBLE_STEP:
            case SPRUCE_FENCE_GATE:
            case DARK_OAK_FENCE:
            case JUNGLE_FENCE:
            case BIRCH_FENCE:
            case DARK_OAK_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case SPRUCE_FENCE:
            case BED:
            case WOOD_STAIRS:
                switch (item) {
                    case WOOD_AXE:
                    case STONE_AXE:
                    case IRON_AXE:
                    case GOLD_AXE:
                    case DIAMOND_AXE:
                        return true;
                    default:
                        return false;
                }
                // shears
            case LEAVES:
            case LEAVES_2:
            case LONG_GRASS:
            case WEB:
            case WOOL:
                return Objects.requireNonNull(item) == Material.SHEARS;
            // no best tool
            case SEA_LANTERN:
            case SPONGE:
            case GLASS:
            case STAINED_GLASS:
            case STAINED_GLASS_PANE:
            case THIN_GLASS:
            case GLOWSTONE: {
                return false;
            }
        }
        // default for pickaxe true // may need changing
        return true;
    }

    /**
     * Gets the speed of the tool
     *
     * @param item The item to check
     *
     * @return The speed of the tool
     */
    public static int getToolSpeed(Material item) {
        switch (item) {
            case WOOD_SPADE:
            case WOOD_AXE:
            case WOOD_PICKAXE:
                return 2;
            case STONE_SPADE:
            case STONE_AXE:
            case STONE_PICKAXE:
                return 4;
            case IRON_SPADE:
            case IRON_AXE:
            case IRON_PICKAXE:
                return 6;
            case DIAMOND_SPADE:
            case DIAMOND_AXE:
            case DIAMOND_PICKAXE:
                return 8;
            case GOLD_SPADE:
            case GOLD_AXE:
            case GOLD_PICKAXE:
                return 12;
            case SHEARS:
                return 2;
            default:
                return 1;
        }
    }

    /**
     * Gets the length of time it takes to break a block
     *
     * @param player The player
     * @param itemStack The item the player is using
     * @param block The block the player is breaking
     *
     * @return The time it takes to break the block 0 if insta break
     */
    public static float getBreakDuration(Player player, ItemStack itemStack, Material block) {
        float speedMultiplier = 1f;
        if (isBestTool(block, itemStack.getType())) {
            speedMultiplier = getToolSpeed(itemStack.getType());
            if (itemStack.getEnchantmentLevel(Enchantment.DIG_SPEED) > 0) {
                speedMultiplier += Math.pow(itemStack.getEnchantmentLevel(Enchantment.DIG_SPEED), 2) + 1;
            }
        }
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (potionEffect.getType().equals(PotionEffectType.FAST_DIGGING)) {
                speedMultiplier *= 0.2 * (potionEffect.getAmplifier() + 1) + 1;
                break;
            }
        }
        if (player.isFlying()) {
            speedMultiplier /= 5f;
        }
        float damage = speedMultiplier / getHardness(block);
        if (isBestTool(block, itemStack.getType())) {
            damage /= 30f;
        } else {
            damage /= 100f;
        }
        // insta break
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
