package com.gmail.gogobebe2.shiftspawn;

import be.maximvdw.featherboard.api.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.regex.PatternSyntaxException;

public class Timer {
    private int minutes = 0;
    private int seconds = 0;
    private boolean isTimerRunning = false;
    private ShiftSpawn plugin;
    private BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    private int timerIncrementer;
    private GameState gameState;

    public Timer(ShiftSpawn plugin) {
        this(plugin, GameState.WAITING);
    }

    public Timer(ShiftSpawn plugin, GameState gameState) {
        this(plugin, gameState, "0:00");
    }

    public Timer(ShiftSpawn plugin, GameState gameState, String timeFormat) {
        this.plugin = plugin;
        this.gameState = gameState;
        setTime(timeFormat);
    }

    public void setTime(String timeFormat) throws NumberFormatException, PatternSyntaxException {
        String[] times = timeFormat.split(":");
        setMinutes(Integer.parseInt(times[0]));
        setSeconds(Integer.parseInt(times[1]));
    }

    public String getTime() {
        if (seconds < 10) {
            return getMinutes() + ":0" + getSeconds();
        }
        return getMinutes() + ":" + getSeconds();
    }

    public void startTimer(final boolean goUp) {
        if (!isTimerRunning()) {
            this.timerIncrementer = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    PlaceholderAPI.registerOfflinePlaceholder("shiftspawn", true,
                            new PlaceholderAPI.PlaceholderRequestEventHandler() {
                                @Override
                                public String onPlaceholderRequest(PlaceholderAPI.PlaceholderRequestEvent e) {
                                    String prefix;
                                    switch (gameState) {
                                        case WAITING:
                                            return ChatColor.BLUE + "" + ChatColor.BOLD + "Waiting." + (seconds % 2 == 0 ? ".." : ".");
                                        case STARTING:
                                            prefix = ChatColor.DARK_AQUA + "Starting in: " + ChatColor.AQUA;
                                            break;
                                        case STARTED:
                                            prefix = ChatColor.GREEN + "Time left: " + ChatColor.DARK_GREEN;
                                            break;
                                        case RESTARTING:
                                            return ChatColor.RED + "" + ChatColor.BOLD + "Restarting" + (seconds % 2 == 0 ? ".." : ".");
                                        default:
                                            prefix = ChatColor.RED + "Error! " + ChatColor.RESET;
                                            break;
                                    }
                                    return prefix + getTime();
                                }
                            });
                    if (goUp) {
                        seconds++;
                        if (seconds == 60) {
                            minutes++;
                            seconds = 0;
                        }
                    } else {
                        if (seconds != 0 || minutes != 0) {
                            seconds--;
                            if (seconds == -1) {
                                minutes--;
                                seconds = 59;
                            } else if (seconds == 0 && minutes == 0) {
                                stopTimer();
                            }
                        }
                    }
                }
            }, 0L, 20L);
            this.isTimerRunning = true;
        }
    }

    public void startTimer() {
        startTimer(true);
    }

    public void stopTimer() {
        if (isTimerRunning) {
            scheduler.cancelTask(this.timerIncrementer);
            this.isTimerRunning = false;
        }
        if (this.gameState.equals(GameState.WAITING) || this.gameState.equals(GameState.RESTARTING)) {
            // TODO: start timer again.
        }
        if (this.gameState.equals(GameState.STARTING)) {
            Bukkit.broadcastMessage("Game starting!!");
            this.gameState = GameState.STARTED;
            // TODO: Start game and start timer.
        }
        else if (this.gameState.equals(GameState.STARTED)) {
            Bukkit.broadcastMessage("Game over!!");
            this.gameState = GameState.RESTARTING;
            // TODO: End game and start short timer.
        }
        else if (this.gameState.equals(GameState.RESTARTING)) {
            // TODO: Restart server.
        }
    }

    public int getMinutes() {
        return this.minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public boolean isTimerRunning() {
        return this.isTimerRunning;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
