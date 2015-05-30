package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

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

    private Objective getObjective(String name, Scoreboard scoreboard) {
        for (Objective o : scoreboard.getObjectives()) {
            if (o.getName().equals(name)) {
                return o;
            }
        }
        return scoreboard.registerNewObjective(name, "dummy");
    }

    private void showScoreTag(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        String name;
        if (player.getName().length() <= 12) {
            name = player.getName();
        } else {
            name = player.getName().substring(0, 12);
        }
        Objective o = getObjective(name + "_tag", scoreboard);
        o.setDisplaySlot(DisplaySlot.BELOW_NAME);
        o.setDisplayName(ChatColor.DARK_GREEN + "Points");
        player.setDisplayName(ChatColor.YELLOW + " [");
        Score score = o.getScore(player.getName());
        score.setScore(plugin.getParticipant(player).getScore());
        player.setScoreboard(scoreboard);
    }

    private void showStatus(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = getObjective("status_tag", scoreboard);
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.setDisplayName(getStatus());
        player.setScoreboard(scoreboard);
    }


    private Team getTeam(String name, Scoreboard scoreboard) {
        for (Team t : scoreboard.getTeams()) {
            if (t.getName().equals(name)) {
                return t;
            }
        }
        return scoreboard.registerNewTeam(name);
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
            Team team = getTeam(name + "_team", scoreboard);
            team.setPrefix(ChatColor.YELLOW + "[" + participant.getKills() + "] " + ChatColor.AQUA + ChatColor.BOLD);
            team.addPlayer(player);
        }
    }

    private void showEveryoneScoreSide(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = getObjective("allScore_side", scoreboard);
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "Everyone's scores");
        for (Participant participant : plugin.getParticipants()) {
            Score s = o.getScore(ChatColor.GREEN + participant.getPlayer().getName() + ":");
            s.setScore(participant.getScore());
        }
        player.setScoreboard(scoreboard);
    }

    private void showScoreSide(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = getObjective("score_side", scoreboard);
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD);
        player.setScoreboard(scoreboard);
    }

    private String getStatus() {
        switch (gameState) {
            case WAITING:
                return ChatColor.BLUE + "" + ChatColor.BOLD + "Waiting.." + (seconds % 2 == 0 ? "." : "");
            case STARTING:
                return ChatColor.DARK_AQUA + "Starting in: " + ChatColor.AQUA + getTime();
            case STARTED:
                return ChatColor.GREEN + "Time left: " + ChatColor.DARK_GREEN + getTime();
            case RESTARTING:
                return ChatColor.RED + "" + ChatColor.BOLD + "Restarting.." + (seconds % 2 == 0 ? "." : "");
            default:
                return ChatColor.RED + "Error! " + ChatColor.RESET;
        }
    }

    public void startTimer() {
        if (!isTimerRunning()) {
            this.timerIncrementer = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Bukkit.broadcastMessage("debug 1: gameState.name(): " + gameState.name() + ", getTime(): " + getTime());
                    if (gameState.equals(GameState.STARTED)) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            showScoreTag(player);
                            showKillsTag();
                            showStatus(player);
                            showEveryoneScoreSide(player);
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
            }, 0L, 20L);
            this.isTimerRunning = true;
        }
    }

    private void endGame() {
        this.gameState = GameState.RESTARTING;
    }

    private void startGame() {
        this.gameState = GameState.STARTED;
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.spawn(player);
        }
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
                // Just keep looping and use the timer to decide how to use ".." or "...".
                setMinutes(Integer.MAX_VALUE);
                break;
            case STARTING:
                Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Game starting!");
                setTime(plugin.getConfig().getString(ShiftSpawn.GAME_TIME));
                startGame();
                break;
            case STARTED:
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Game over!");
                endGame();
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
