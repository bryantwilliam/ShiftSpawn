package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.scheduler.BukkitRunnable;

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
        if (time == 10) {
            ShiftSpawn.broadcastTimeLeft(time);
        } else if (time <= 0) {
            ShiftSpawn.broadcastTimeLeft(time);
            plugin.start();
            this.cancel();
        } else if (time /* lol penis -----------> */ <=3) {
            ShiftSpawn.broadcastTimeLeft(time);
        }
        time--;
    }
}
