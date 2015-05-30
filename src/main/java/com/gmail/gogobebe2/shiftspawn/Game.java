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

    private void showScoreTag(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = scoreboard.registerNewObjective(player.getName() + "score_tag", "dummy");
        o.setDisplaySlot(DisplaySlot.BELOW_NAME);
        o.setDisplayName(ChatColor.DARK_GREEN + "Points");
        player.setDisplayName(ChatColor.YELLOW + " [");
        Score score = o.getScore(player.getName());
        score.setScore(plugin.getParticipant(player).getScore());
        player.setScoreboard(scoreboard);
    }

    private void showStatus(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = scoreboard.registerNewObjective("status_tag", "dummy");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.setDisplayName(getStatus());
        Score score = o.getScore(ChatColor.BLACK + "" + ChatColor.MAGIC + "");
        // So it's always at the top of the scoreboard...
        score.setScore(Bukkit.getOnlinePlayers().size() + 2);
        player.setScoreboard(scoreboard);
    }


    private void showKillsTag() {
        for (Participant participant : plugin.getParticipants()) {
            participant.getPlayer().setDisplayName(ChatColor.YELLOW + "[" + participant.getKills() + "] " + ChatColor.AQUA + ChatColor.BOLD);
        }
    }

    private void showEveryoneScoreSide(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = scoreboard.registerNewObjective("allScore_side", "dummy");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "Everyone's scores");
        for (Participant participant : plugin.getParticipants()) {
            Score s = o.getScore(ChatColor.GREEN + participant.getPlayer().getName() + ":");
            s.setScore(participant.getScore());
        }
        player.setScoreboard(scoreboard);
    }

    private void showScoreSide(Scoreboard scoreboard) {
        Objective o = scoreboard.registerNewObjective("score_side", "dummy");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD);
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
