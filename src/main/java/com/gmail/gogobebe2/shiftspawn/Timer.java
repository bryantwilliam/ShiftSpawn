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
        if (time < 1) {
            throw new IllegalArgumentException("counter must be greater than 1");
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
                else {
                    return ChatColor.BLUE + "Timer: " + ChatColor.AQUA + ChatColor.BOLD + time;
                }
            }
        });
        if (plugin.getGameState().equals(GameState.STARTING)) {
            if (time == 10) {
                ShiftSpawn.broadcastTimeBeforeStart(time);
            } else if (time <= 0) {
                ShiftSpawn.broadcastTimeBeforeStart(time);
                start();
                this.cancel();
            } else if (time /* lol penis -----------> */ <=3) {
                ShiftSpawn.broadcastTimeBeforeStart(time);
            }
            time--;
        }
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
        this.time = plugin.getConfig().getDouble("game time");
        plugin.setGameState(GameState.STARTED);
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTime() {
        return this.time;
    }
}
