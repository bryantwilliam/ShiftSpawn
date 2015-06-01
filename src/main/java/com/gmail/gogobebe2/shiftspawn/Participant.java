package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftspawn.scoreboards.TopScores;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Participant {
    private final Player PLAYER;
    private int score;
    private int kills;
    private String spawnID;
    private ShiftSpawn plugin;
    private Scoreboard scoreboard;
    private Objective objective;
    private TopScores topScores;

    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID) {
        this(plugin, PLAYER, spawnID, 0, 0);
    }

    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID, int score, int kills) {
        this.PLAYER = PLAYER;
        this.spawnID = spawnID;
        this.score = score;
        this.kills = kills;
        this.plugin = plugin;
        this.scoreboard = PLAYER.getScoreboard();
        this.objective = scoreboard.registerNewObjective("shift_obj", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shift Scores");
        PLAYER.setScoreboard(scoreboard);
        this.topScores = new TopScores(this, objective, plugin);
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getScore() {
        return score;
    }

    public Player getPlayer() {
        return PLAYER;
    }

    public String getSpawnID() {
        return spawnID;
    }

    public Player getPLAYER() {
        return PLAYER;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Objective getObjective() {
        return objective;
    }

    public TopScores getTopScores() {
        return topScores;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setSpawnID(String spawnID) {
        this.spawnID = spawnID;
    }

    public void setTopScores(TopScores topScores) {
        this.topScores = topScores;
    }
}
