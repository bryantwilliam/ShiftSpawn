package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftspawn.api.events.PlayerShiftWinEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Game {
    private static Scoreboard scoreboard;
    private int minutes = 0;
    private int seconds = 0;
    private boolean isTimerRunning = false;
    private ShiftSpawn plugin;
    private BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    private int timerIncrementer;
    private GameState gameState;

    protected Game(ShiftSpawn plugin, GameState gameState, String timeFormat) {
        this.plugin = plugin;
        this.gameState = gameState;
        setTime(timeFormat);
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    protected void setTime(String timeFormat) {
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

    protected void startTimer() {
        if (!isTimerRunning()) {
            this.isTimerRunning = true;
        }
        this.timerIncrementer = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Participant participant : plugin.getParticipants()) {
                    if (gameState.equals(GameState.STARTED)) {
                        showKillsTag(participant);
                        plugin.getScoreTagSection().display(participant.getPlayer());
                        plugin.getTopScoresSection().display(participant.getPlayer());
                    }
                    plugin.getStatusSection().display(participant.getPlayer());
                    plugin.getOnlinePlayerSection().display(participant.getPlayer());
                }
                if (!plugin.getAlphaCores().isEmpty()) {
                    for (Block alphaCore : plugin.getAlphaCores()) {
                        alphaCore.getWorld().playEffect(alphaCore.getLocation().clone().subtract(0, 1, 0), Effect.LAVADRIP, 5);
                    }
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

    private void showKillsTag(Participant participant) {
        Player player = participant.getPlayer();

        String name;
        if (player.getName().length() <= 11) {
            name = player.getName();
        } else {
            name = player.getName().substring(0, 11);
        }
        name = name + "_team";
        Team team = null;
        if (!scoreboard.getTeams().isEmpty()) {
            for (Team t : scoreboard.getTeams()) {
                if (t.getName().equals(name)) {
                    team = scoreboard.getTeam(name);
                    break;
                }
            }
        }
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
        }
        team.setPrefix(ChatColor.DARK_RED + "[" + participant.getKills() + "] " + ChatColor.AQUA + ChatColor.BOLD);
        team.addPlayer(player);
    }

    protected void stopTimer() {
        if (isTimerRunning) {
            scheduler.cancelTask(this.timerIncrementer);
            this.isTimerRunning = false;
        }
        switch (this.gameState) {
            case RESTARTING:
                if (!Bukkit.getOnlinePlayers().isEmpty()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        // Remove their scoreboard.
                        player.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
                        player.kickPlayer(ChatColor.AQUA + "You have been kicked while game restarts.");
                    }
                }
                // Bukkit.getServer().shutdown(); Doesn't work for his server.
                // So trying this out:
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "stop");

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
                }

                int mostKills = 0;

                Set<Participant> tempWinners = new HashSet<>();
                if (winners.size() > 1) {
                    for (Participant participant : winners) {
                        if (participant.getKills() >= mostKills) {
                            if (participant.getKills() > mostKills) {
                                tempWinners.clear();
                                mostKills = participant.getKills();
                            }
                            tempWinners.add(participant);
                        }
                    }
                }

                for (Iterator<Participant> iterator = winners.iterator(); iterator.hasNext(); ) {
                    Participant winner = iterator.next();
                    PlayerShiftWinEvent playerShiftWinEvent = new PlayerShiftWinEvent(winner.getPlayer());
                    Bukkit.getServer().getPluginManager().callEvent(playerShiftWinEvent);
                    if (playerShiftWinEvent.isCancelled()) {
                        iterator.remove();
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
                    } else {
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

    protected int getMinutes() {
        return this.minutes;
    }

    protected void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return this.seconds;
    }

    protected void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    protected boolean isTimerRunning() {
        return this.isTimerRunning;
    }

    public GameState getGameState() {
        return gameState;
    }

    protected void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    protected static Scoreboard getScoreboard() {
        return scoreboard;
    }
}
