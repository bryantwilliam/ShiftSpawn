package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftspawn.api.events.PlayerShiftWinEvent;
import com.gmail.gogobebe2.shiftstats.ShiftStats;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.util.*;

public class Game {
    private static Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
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
        try (Jedis jedis = ShiftSpawn.jedisPool.getResource()) {
            jedis.hset("shift", plugin.getConfig().getString("server-id"), String.valueOf(gameState.getCode()));
            // JEDIS auto-closes and returns to the pool.
        }
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
                if (gameState.equals(GameState.STARTED)) {
                    for (Participant participant : plugin.getParticipants()) showKillsTag(participant);
                    plugin.getScoreTagSection().display();
                    plugin.getTopScoresSection().display();
                }
                plugin.getStatusSection().display();
                plugin.getOnlinePlayerSection().display();
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
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF("Lobby");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        // Remove their scoreboard.
                        player.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
                        player.kickPlayer(ChatColor.AQUA + "You have been kicked while game restarts.");

                        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
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
                Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD
                        + "Other players will have up to 5 minutes to join.");

                this.gameState = GameState.STARTED;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.spawn(player);
                }
                setTime(plugin.getConfig().getString(ShiftSpawn.GAME_TIME));
                try (Jedis jedis = ShiftSpawn.jedisPool.getResource()) {
                    jedis.hset("shift", plugin.getConfig().getString("server-id"), String.valueOf(gameState.getCode()));
                    // JEDIS auto-closes and returns to the pool.
                }
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
                    Player player = participant.getPlayer();

                    broadcast.append(ChatColor.AQUA + "" + ChatColor.BOLD + player.getName());
                    try {
                        ShiftStats.getAPI().addWins(player.getUniqueId(), 1);
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "Error, can't save win to SQL database!");
                    }
                    if (iterator.hasNext()) {
                        broadcast.append(", ");
                    } else {
                        broadcast.append(".");
                    }
                }
                for (Participant participant : participants) {
                    Player player = participant.getPlayer();
                    UUID uuid = player.getUniqueId();
                    boolean won = false;
                    for (Participant winner : winners) {
                        if (winner.getPlayer().getUniqueId().equals(uuid)) {
                            won = true;
                            break;
                        }
                    }
                    if (!won) try {
                        ShiftStats.getAPI().addLosses(uuid, 1);
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "Error, can't save win to SQL database!");
                    }
                }

                Bukkit.broadcastMessage(broadcast.toString());

                this.gameState = GameState.RESTARTING;
                // 1 minute before restart server and use the timer to decide how to use ".." or "...".
                setTime("1:00");
                try (Jedis jedis = ShiftSpawn.jedisPool.getResource()) {
                    jedis.hset("shift", plugin.getConfig().getString("server-id"), String.valueOf(gameState.getCode()));
                    // JEDIS auto-closes and returns to the pool.
                }
                break;
            default:
                this.gameState = GameState.ERROR;
                try (Jedis jedis = ShiftSpawn.jedisPool.getResource()) {
                    jedis.hset("shift", plugin.getConfig().getString("server-id"), String.valueOf(gameState.getCode()));
                    // JEDIS auto-closes and returns to the pool.
                }
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error! Ask admin to fix immediately!!!");
                plugin.getLogger().severe("Internal error! No Game State set!?!?!? Ask willy to fix. Email him at: gogobebe2@gmail.com");
                return;
        }
        startTimer();
    }

    public int getMinutes() {
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

    public static Scoreboard getScoreboard() {
        return scoreboard;
    }
}
