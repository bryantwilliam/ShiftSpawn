package com.gmail.gogobebe2.shiftspawn;

import be.maximvdw.featherboard.api.PlaceholderAPI;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Timer extends BukkitRunnable {

    private final ShiftSpawn plugin;

    private double time;

    public Timer(ShiftSpawn plugin, double time) {
        this.plugin = plugin;
        if (time < 0.11) {
            throw new IllegalArgumentException("counter must be greater than 0.11");
        } else {
            this.time = time;
        }
    }

    @Override
    public void run() {
        PlaceholderAPI.registerPlaceholder("timer", new PlaceholderAPI.PlaceholderRequestEventHandler() {
            @Override
            public String onPlaceholderRequest(PlaceholderAPI.PlaceholderRequestEvent placeholderRequestEvent) {
                if (plugin.getGameState().equals(GameState.WAITING)) {
                    return ChatColor.BLUE + "" + ChatColor.BOLD + "Waiting...";
                }
                else if (plugin.getGameState().equals(GameState.RESTARTING)) {
                    return ChatColor.RED + "" + ChatColor.BOLD + "Restarting";
                }
                else if (plugin.getGameState().equals(GameState.STARTING)) {
                    return ChatColor.BLUE + "Game starting in: " + ChatColor.AQUA + ChatColor.BOLD + time;
                }
                else if (plugin.getGameState().equals(GameState.STARTED)) {
                    return ChatColor.BLUE + "Time left: " + ChatColor.AQUA + ChatColor.BOLD + time;
                }
                return ChatColor.RED + "" + ChatColor.BOLD + "ERROR! ShiftSpawn is broken!";
            }
        });
        if (plugin.getGameState().equals(GameState.STARTING)) {
            if (time == plugin.getConfig().getDouble("time before games starts") || time == 0.10 || (time <= 0.03 && time > 0)) {
                Bukkit.broadcastMessage(ChatColor.RED + "Game starting in " + ChatColor.BOLD + time + ChatColor.RED
                        + " minutes...");
            } else if (time <= 0) {
                Bukkit.broadcastMessage(ChatColor.RED + "Game starting " + ChatColor.BOLD + "NOW" + ChatColor.RED
                        + "!!!!");
                start();
            }
        }
        if (plugin.getGameState().equals(GameState.STARTED)) {
            if (time == plugin.getConfig().getDouble("game time (in minutes)")) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "You have " + ChatColor.BOLD + time + ChatColor.GREEN
                        + " minutes to mine the alpha core. Go!");
            }
            else if (time == 0.10) {
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Hurry! You only have " + ChatColor.BOLD + (time * 100)
                        + ChatColor.DARK_PURPLE + " seconds left!");
            }
            else if (time <= 0.03 && time > 0) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + (time * 100) + ChatColor.RED + " seconds left!");
            }
            else if (time <= 0) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + ChatColor.BOLD + "Game over!");
                stop();
            }
        }
        time = time - 0.01;
    }

    private void stop() {
        // TODO: Restart server and find winner.
    }

    private void start() {
        List<String> spawnIDs = Lists.newArrayList(plugin.getConfig().getConfigurationSection("spawns").getKeys(false));
        spawnIDs.remove("main");
        int spawnIDIndex = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            String id = spawnIDs.get(spawnIDIndex);

            Location spawn = plugin.getLocationConfig(id);

            plugin.getPlayerSpawns().put(player, spawn);
            plugin.spawn(player, spawn);

            if (spawnIDIndex < spawnIDs.size()) {
                spawnIDIndex++;
            } else {
                spawnIDIndex = 0;
            }
        }
        this.time = plugin.getConfig().getDouble("game time (in minutes)");
        plugin.setGameState(GameState.STARTED);
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTime() {
        return this.time;
    }
}
