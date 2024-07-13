package me.nonetaken.ghostblocklib.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 17/07/2023 at 20:45
 *
 * @Author benwithjamin
 */
@UtilityClass
public class BlockHardness {

    /**
     * Checks if the player is using the best tool for the block
     *
     * @param material The material of the block
     * @param item     The item the player is using
     * @return Whether the player is using the best tool
     */
    public static boolean isBestTool(Material material, Material item) {
        // TODO: there are many material types missing from here, add as required
        switch (material) {
            // shovel
            case DIRT:
            case SAND:
            case GRASS_BLOCK:
            case CLAY:
            case GRAVEL:
            case SOUL_SAND:
            case SNOW_BLOCK:
            case SNOW:
            case MYCELIUM:
                switch (item) {
                    case WOODEN_SHOVEL:
                    case STONE_SHOVEL:
                    case IRON_SHOVEL:
                    case GOLDEN_SHOVEL:
                    case DIAMOND_SHOVEL:
                        return true;
                    default:
                        return false;
                }
            // axe
            case ACACIA_WOOD:
            case BIRCH_WOOD:
            case DARK_OAK_WOOD:
            case JUNGLE_WOOD:
            case OAK_WOOD:
            case SPRUCE_WOOD:
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
                switch (item) {
                    case WOODEN_AXE:
                    case STONE_AXE:
                    case IRON_AXE:
                    case GOLDEN_AXE:
                    case DIAMOND_AXE:
                        return true;
                    default:
                        return false;
                }
                // shears
            case WHITE_WOOL:
            case ORANGE_WOOL:
            case MAGENTA_WOOL:
            case LIGHT_BLUE_WOOL:
            case YELLOW_WOOL:
            case LIME_WOOL:
            case PINK_WOOL:
            case GRAY_WOOL:
            case LIGHT_GRAY_WOOL:
            case CYAN_WOOL:
            case PURPLE_WOOL:
            case BLUE_WOOL:
            case BROWN_WOOL:
            case GREEN_WOOL:
            case RED_WOOL:
                return item == Material.SHEARS;
            // no best tool
            case SEA_LANTERN:
            case SPONGE:
            case GLASS:
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
     * @return The speed of the tool
     */
    public static int getToolSpeed(Material item) {
        switch (item) {
            case WOODEN_SHOVEL:
            case WOODEN_AXE:
            case WOODEN_PICKAXE:
            case SHEARS:
                return 2;
            case STONE_SHOVEL:
            case STONE_AXE:
            case STONE_PICKAXE:
                return 4;
            case IRON_SHOVEL:
            case IRON_AXE:
            case IRON_PICKAXE:
                return 6;
            case DIAMOND_SHOVEL:
            case DIAMOND_AXE:
            case DIAMOND_PICKAXE:
                return 8;
            case GOLDEN_SHOVEL:
            case GOLDEN_AXE:
            case GOLDEN_PICKAXE:
                return 12;
            default:
                return 1;
        }
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
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
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
