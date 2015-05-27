package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class Game {
    private int minutes = 0;
    private int seconds = 0;
    private boolean isTimerRunning = false;
    private ShiftSpawn plugin;
    private BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    private int timerIncrementer;
    private GameState gameState;
    private List<Team> individualScores = new ArrayList<>();

    public Game(ShiftSpawn plugin) {
        this(plugin, GameState.WAITING);
    }

    public Game(ShiftSpawn plugin, GameState gameState) {
        this(plugin, gameState, "0:00");
    }

    public Game(ShiftSpawn plugin, GameState gameState, String timeFormat) {
        this.plugin = plugin;
        this.gameState = gameState;
        setTime(timeFormat);
    }

    public void setTime(String timeFormat) throws NumberFormatException, PatternSyntaxException {
        String[] times = timeFormat.split(":");
        if (times[0].equalsIgnoreCase("")) {
            setMinutes(0);
        }
        else {
            setMinutes(Integer.parseInt(times[0]));
        }

        if (times[1].equalsIgnoreCase("")) {
            setSeconds(0);
        }
        else {
            setSeconds(Integer.parseInt(times[1]));
        }

    }

    public String getTime() {
        if (seconds < 10) {
            return getMinutes() + ":0" + getSeconds();
        }
        return getMinutes() + ":" + getSeconds();
    }

    public void startTimer(final boolean goUp) {
        // goUp decides whether the countdown counts up or down.
        if (!isTimerRunning()) {
            this.timerIncrementer = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    ScoreboardManager manager = Bukkit.getScoreboardManager();
                    if (!gameState.equals(GameState.WAITING)) {
                        Scoreboard boardUnderPlayer = manager.getNewScoreboard();

                        Objective objectiveScore = boardUnderPlayer.registerNewObjective("score", "dummy");
                        objectiveScore.setDisplaySlot(DisplaySlot.BELOW_NAME);
                        objectiveScore.setDisplayName("score");

                        for(Player online : Bukkit.getOnlinePlayers()){
                            Score score = objectiveScore.getScore(online);
                            score.setScore(plugin.getParticipant(online).getScore());
                        }

                        for(Player online : Bukkit.getOnlinePlayers()){
                            online.setScoreboard(boardUnderPlayer);
                        }
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            online.setScoreboard(boardUnderPlayer);
                        }
                    }

                    Scoreboard board = manager.getNewScoreboard();
                    Objective statusObj = board.registerNewObjective("status", "dummy");
                    statusObj.setDisplaySlot(DisplaySlot.SIDEBAR);


                    String msg;
                    switch (gameState) {
                        case WAITING:
                            msg = ChatColor.BLUE + "" + ChatColor.BOLD + "Waiting.." + (seconds % 2 == 0 ? "." : "");
                            break;
                        case STARTING:
                            msg = ChatColor.DARK_AQUA + "Starting in: " + ChatColor.AQUA + getTime();
                            break;
                        case STARTED:
                            msg = ChatColor.GREEN + "Time left: " + ChatColor.DARK_GREEN + getTime();
                            break;
                        case RESTARTING:
                            msg = ChatColor.RED + "" + ChatColor.BOLD + "Restarting.." + (seconds % 2 == 0 ? "." : "");
                            break;
                        default:
                            msg = ChatColor.RED + "Error! " + ChatColor.RESET;
                            break;
                    }
                    Objective allObj = board.registerNewObjective("all_score", "dummy");
                    allObj.setDisplaySlot(DisplaySlot.SIDEBAR);
                    allObj.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "Everyone's scores");
                    for (Participant participant : plugin.getParticipants()) {
                        Score s = allObj.getScore(ChatColor.GREEN + participant.getPlayer().getName() + ":");
                        s.setScore(participant.getScore());
                    }

                    statusObj.setDisplayName(msg);
                    for (Participant participant : plugin.getParticipants()) {
                        Team individual = board.registerNewTeam("individual");
                        individual.addPlayer(participant.getPlayer());
                        individualScores.add(individual);
                        individual.setSuffix(ChatColor.YELLOW + "[" + participant.getScore() + "]");
                        individual.setDisplayName(ChatColor.LIGHT_PURPLE + "Your score: " + ChatColor.WHITE + participant.getScore());
                        participant.getPlayer().setScoreboard(board);
                    }


                    for (Player online : Bukkit.getOnlinePlayers()) {

                    }

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

    private void endGame() {
        this.gameState = GameState.RESTARTING;
    }

    private void startGame() {
        this.gameState = GameState.STARTED;
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
        startTimer(false);

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
