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
        String name;
        if (player.getName().length() <= 12) {
            name = player.getName();
        } else {
            name = player.getName().substring(0, 12);
        }
        name = name + "_tag";

        Scoreboard scoreboard = player.getScoreboard();
        Objective o = scoreboard.getObjective(name);

        if (scoreboard.getObjectives().isEmpty() || o == null) {
            o = scoreboard.registerNewObjective(name, "dummy");
            o.setDisplaySlot(DisplaySlot.BELOW_NAME);
            o.setDisplayName(ChatColor.DARK_GREEN + "Points");
        }

        Score score = o.getScore(ChatColor.BLUE + name);
        score.setScore(plugin.getParticipant(player).getScore());
        player.setScoreboard(scoreboard);
    }

    private void showStatus(Player player) {
        String name = getObjectiveName(player);
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = scoreboard.getObjective(name);
        int onlineAmount = Bukkit.getOnlinePlayers().size();
        Score status = o.getScore(getStatus());
        status.setScore(onlineAmount + 1);
        Score online = o.getScore(ChatColor.AQUA + "Players online: ");
        online.setScore(onlineAmount);
        player.setScoreboard(scoreboard);
    }

    private void showEveryoneScoreSide(Player player) {
        String name = getObjectiveName(player);
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = scoreboard.getObjective(name);

        int highestScore = 0;
        for (Participant participant : plugin.getParticipants()) {
            Score s = o.getScore(ChatColor.GREEN + participant.getPlayer().getName() + ":");
            int score = participant.getScore();
            s.setScore(score);

            if (score > highestScore) {
                highestScore = participant.getScore();
            }
        }

        Score s = o.getScore(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "Everyone's scores");
        s.setScore(highestScore + 1);
        player.setScoreboard(scoreboard);
    }

    private void showScoreSide(Player player) {
        String name = "score_side";
        Scoreboard scoreboard = player.getScoreboard();
        Objective o = scoreboard.getObjective(name);
        player.setScoreboard(scoreboard);
    }

    private void showKillsTag() {
        Bukkit.broadcastMessage("debug 1...");
        for (Participant participant : plugin.getParticipants()) {
            Player player = participant.getPlayer();
            Bukkit.broadcastMessage("debug 2: " + player.getName() + ", " + participant.getKills());
            Scoreboard scoreboard = player.getScoreboard();

            String name;
            if (player.getName().length() <= 11) {
                name = player.getName();
            } else {
                name = player.getName().substring(0, 11);
            }
            name = name + "_team";
            Bukkit.broadcastMessage("debug 3: " + name);
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
            Bukkit.broadcastMessage("debug 4: " + foundTeam);
            if (!foundTeam) {
                team = scoreboard.registerNewTeam(name);
                Bukkit.broadcastMessage("debug 5");
            }
            team.setPrefix(ChatColor.YELLOW + "[" + participant.getKills() + "] " + ChatColor.AQUA + ChatColor.BOLD);
            team.addPlayer(player);
            Bukkit.broadcastMessage("debug 6");
        }
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

    private String getObjectiveName(Player player) {
        String name;
        if (player.getName().length() <= 10) {
            name = player.getName();
        } else {
            name = player.getName().substring(0, 10);
        }
        return "shift_" + name;
    }

    public void startTimer() {
        if (!isTimerRunning()) {
            this.isTimerRunning = true;
        }
        this.timerIncrementer = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("debug 1: gameState.name(): " + gameState.name() + ", getTime(): " + getTime());
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Scoreboard scoreboard = player.getScoreboard();
                    String name = getObjectiveName(player);
                    Objective o = scoreboard.getObjective(name);
                    if (scoreboard.getObjectives().isEmpty() || o == null) {
                        o = scoreboard.registerNewObjective(name, "dummy");
                        o.setDisplaySlot(DisplaySlot.SIDEBAR);
                        o.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shift Scores");
                        player.setScoreboard(scoreboard);
                    }
                    // TODO: remove all other player.setScoreboard(...); and see if it works if it's just here.
                    showKillsTag();
                    if (gameState.equals(GameState.STARTED)) {
                        showScoreTag(player);
                        showEveryoneScoreSide(player);
                    }
                    showStatus(player);
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
                // Just keep looping and use the timer to decide how to use ".." or "...".
                setMinutes(Integer.MAX_VALUE);
                startTimer();
                break;
            case STARTING:
                Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Game starting!");
                setTime(plugin.getConfig().getString(ShiftSpawn.GAME_TIME));
                this.gameState = GameState.STARTED;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.spawn(player);
                }
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
