package me.nonetaken.ghostblocklib.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;

/*
 * Project: me.nonetaken.ghostblocklib.util | Author: NoneTaken#0001
 * Created: 26/07/2023 at 20:17
 */
public class Utils {

    public static String removeTrailingCharacters(String input, char character) {
        int index = input.length() - 1;
        while (index >= 0 && input.charAt(index) == character) {
            index--;
        }
        return input.substring(0, index + 1);
    }

    public static String removeBeginningCharacters(String input, char character) {
        int index = 0;
        while (index < input.length() && input.charAt(index) == character) {
            index++;
        }
        return input.substring(index);
    }

    /**
     * Credit: <a href="https://www.spigotmc.org/threads/getting-the-blockface-of-a-targeted-block.319181/#post-3002432">
     * <p>
     * Gets the BlockFace of the block the player is currently targeting
     *
     * @param player the player's whose targeted blocks BlockFace is to be checked
     * @return the BlockFace of the targeted block, or null if the targeted block is non-occluding
     */
    public static BlockFace getBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(new HashSet<Material>(){{add(Material.AIR);}}, 100);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding()) {
            return null;
        }
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }
}
