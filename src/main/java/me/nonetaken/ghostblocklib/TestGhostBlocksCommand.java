package me.nonetaken.ghostblocklib;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Made by NoneTaken on 14/07/2024 at 19:18
 */
public class TestGhostBlocksCommand implements CommandExecutor {

    public TestGhostBlocksCommand(JavaPlugin plugin) {
        plugin.getCommand("testghostblocks").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (!player.isOp()) {
            return false;
        }
        GhostBlockCuboid cuboid = new GhostBlockCuboid(player.getWorld(), player.getLocation().subtract(17, 17, 17).toVector(), player.getLocation().add(17, 0, 17).toVector());
        GhostBlockManager.registerGhostBlockCuboid(cuboid);
        cuboid.setBlocks(cuboid.iterator(), block -> {
            Vector pos = block.getVector();
            block.setType(switch (pos.getBlockX() % 16) {
                case 0 -> Material.STONE;
                case 1 -> Material.DIRT;
                case 2 -> Material.GRASS_BLOCK;
                case 3 -> Material.COBBLESTONE;
                case 4 -> Material.OAK_PLANKS;
                case 5 -> Material.BEDROCK;
                case 6 -> Material.SAND;
                case 7 -> Material.GRAVEL;
                case 8 -> Material.GOLD_BLOCK;
                case 9 -> Material.IRON_BLOCK;
                case 10 -> Material.COAL_BLOCK;
                case 11 -> Material.LAPIS_BLOCK;
                case 12 -> Material.DIAMOND_BLOCK;
                case 13 -> Material.EMERALD_BLOCK;
                case 14 -> Material.REDSTONE_BLOCK;
                case 15 -> Material.NETHERITE_BLOCK;
                default -> Material.COAL_ORE;
            });
        });
        cuboid.refresh(player);
        return true;
    }
}
