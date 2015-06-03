package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Game {
    private int minutes = 0;
    private int seconds = 0;
    private boolean isTimerRunning = false;
    private ShiftSpawn plugin;
    private BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    private int timerIncrementer;
    private GameState gameState;

    public Game(ShiftSpawn plugin, GameState gameState, String timeFormat) {
        this.plugin = plugin;
        this.gameState = gameState;
        setTime(timeFormat);
    }

    public void setTime(String timeFormat) {
        String[] times = timeFormat.split(":");
        if (times[0].equalsIgnoreCase("")) {
            setMinutes(0);
        } else {
            setMinutes(Integer.parseInt(times[0]));
        }

        if (times[1].equalsIgnoreCase("")) {
            setSeconds(0);
        } else {
            setSeconds(Integer.parseInt(times[1]));
        }

    }

    public String getTime() {
        if (seconds < 10) {
            return getMinutes() + ":0" + getSeconds();
        }
        return getMinutes() + ":" + getSeconds();
    }

    private void showKillsTag() {
        for (Participant participant : plugin.getParticipants()) {
            Player player = participant.getPlayer();
            Scoreboard scoreboard = player.getScoreboard();

            String name;
            if (player.getName().length() <= 11) {
                name = player.getName();
            } else {
                name = player.getName().substring(0, 11);
            }
            name = name + "_team";
            Team team = null;
            boolean foundTeam = false;
            if (!scoreboard.getTeams().isEmpty()) {
                for (Team t : scoreboard.getTeams()) {
                    if (t.getName().equals(name)) {
                        team = scoreboard.getTeam(name);
                        foundTeam = true;
                        break;
                    }
                }
            }
            if (!foundTeam) {
                team = scoreboard.registerNewTeam(name);
            }
            team.setPrefix(ChatColor.YELLOW + "[" + participant.getKills() + "] " + ChatColor.AQUA + ChatColor.BOLD);
            team.addPlayer(player);
        }
    }

    public void startTimer() {
        if (!isTimerRunning()) {
            this.isTimerRunning = true;
        }
        this.timerIncrementer = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("debug 1: gameState.name(): " + gameState.name() + ", getTime(): " + getTime());
                for (Participant participant : plugin.getParticipants()) {
                    showKillsTag();
                    if (gameState.equals(GameState.STARTED)) {
                        participant.getScoreTagSection().display();
                        participant.getTopScoresSection().display();
                    }
                    participant.getStatusSection().display();
                    participant.getOnlinePlayerSection().display();
                    Bukkit.broadcastMessage("debug 2: participant.getPlayer().getName(): " + participant.getPlayer().getName());
                }
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
        }, 0L, 20L);
    }

    public void stopTimer() {
        if (isTimerRunning) {
            scheduler.cancelTask(this.timerIncrementer);
            this.isTimerRunning = false;
        }
        switch (this.gameState) {
            case RESTARTING:
                Bukkit.getServer().shutdown();
            case WAITING:
                // Just keep looping and use the timer to decide how to use ".." or "..."
                setMinutes(Integer.MAX_VALUE);
                startTimer();
                break;
            case STARTING:
                Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Game starting!");
                this.gameState = GameState.STARTED;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.spawn(player);
                }
                setTime(plugin.getConfig().getString(ShiftSpawn.GAME_TIME));
                startTimer();
                break;
            case STARTED:
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Game over!");
                this.gameState = GameState.RESTARTING;
                // 1 minute before restart server and use the timer to decide how to use ".." or "...".
                setTime("1:00");
                startTimer();
                break;
            default:
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error! Ask admin to fix immediately!!!");
                plugin.getLogger().severe("Internal error! No Game State set!?!?!? Ask willy to fix. Email him at: gogobebe2@gmail.com");
                return;
        }
        startTimer();
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
