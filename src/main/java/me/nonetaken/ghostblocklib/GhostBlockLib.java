package me.nonetaken.ghostblocklib;

import lombok.Getter;
import me.nonetaken.ghostblocklib.event.GhostBlockBreakEvent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public final class GhostBlockLib extends JavaPlugin implements CommandExecutor {

    @Getter private static GhostBlockLib INSTANCE;

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Override
    public void onEnable() {
        INSTANCE = this;
        new GhostBlockManager(this);

        this.getCommand("test").setExecutor(this);
    }

    @Override
    public void onDisable() {
        INSTANCE = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Location origin = player.getLocation();
        GhostBlockCuboid cuboid = new GhostBlockCuboid(origin.getWorld(), origin.toVector().add(new Vector(-10, -10, -10)), origin.toVector().add(new Vector(10, 0, 10)));
        cuboid.fill(block -> block.setType(Material.BEDROCK));
        cuboid.refresh(player);
        player.sendMessage("Done");
        return true;
    }
}
