package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

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

    public void startTimer() {
        if (!isTimerRunning()) {
            this.isTimerRunning = true;
        }
        this.timerIncrementer = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Participant participant : plugin.getParticipants()) {
                    if (gameState.equals(GameState.STARTED)) {
                        participant.getScoreTagSection().display();
                        participant.getTopScoresSection().display();
                    }
                    participant.getStatusSection().display();
                    participant.getOnlinePlayerSection().display();
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
        }, 0L, 21L);
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
                break;
            case STARTING:
                Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Game starting!");
                this.gameState = GameState.STARTED;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.spawn(player);
                }
                setTime(plugin.getConfig().getString(ShiftSpawn.GAME_TIME));
                break;
            case STARTED:
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Game over!");

                List<Participant> participants = plugin.getParticipants();
                Collections.sort(participants);
                Set<Participant> winners = new HashSet<>();

                int highscore = 0;
                for (Participant participant : plugin.getParticipants()) {
                    if (participant.getScore() >= highscore) {
                        if (participant.getScore() > highscore) {
                            winners.clear();
                            highscore = participant.getScore();
                        }
                        winners.add(participant);
                    }
                    else {
                        break;
                    }
                }

                int mostKills = 0;
                if (winners.size() > 1) {
                    for (Participant participant : winners) {
                        if (participant.getKills() >= mostKills) {
                            if (participant.getKills() > mostKills) {
                                winners.clear();
                                mostKills = participant.getKills();
                            }
                            winners.add(participant);
                        }
                        else {
                            break;
                        }
                    }
                }

                StringBuilder broadcast = new StringBuilder();
                broadcast.append(ChatColor.DARK_GREEN + "" + ChatColor.ITALIC + "Winner");
                if (winners.size() > 1) {
                    broadcast.append("s");
                }
                broadcast.append(": ");
                for (Iterator<Participant> iterator = winners.iterator(); iterator.hasNext(); ) {
                    Participant participant = iterator.next();
                    broadcast.append(ChatColor.AQUA + "" + ChatColor.BOLD + participant.getPlayer().getName());
                    if (iterator.hasNext()) {
                        broadcast.append(", ");
                    }
                    else {
                        broadcast.append(".");
                    }
                }

                Bukkit.broadcastMessage(broadcast.toString());

                this.gameState = GameState.RESTARTING;
                // 1 minute before restart server and use the timer to decide how to use ".." or "...".
                setTime("1:00");
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
