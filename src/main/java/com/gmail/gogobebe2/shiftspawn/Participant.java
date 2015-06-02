package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftspawn.scoreboards.ScoreTagSection;
import com.gmail.gogobebe2.shiftspawn.scoreboards.StatusSection;
import com.gmail.gogobebe2.shiftspawn.scoreboards.TopScoresSection;
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
    private Scoreboard scoreboard;
    private Objective sideObjective;
    private Objective nameObjective;
    private TopScoresSection topScoresSection;
    private StatusSection statusSection;
    private ScoreTagSection scoreTagSection;
    private boolean online;

    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID) {
        this(plugin, PLAYER, spawnID, 0, 0);
    }

    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID, int score, int kills) {
        this.PLAYER = PLAYER;
        this.spawnID = spawnID;
        this.score = score;
        this.kills = kills;
        this.scoreboard = PLAYER.getScoreboard();
        this.sideObjective = scoreboard.registerNewObjective("shift_side", "dummy");
        this.sideObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.sideObjective.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shift Scores");
        this.nameObjective = scoreboard.registerNewObjective("shift_name", "dummy");
        this.nameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        this.nameObjective.setDisplayName(ChatColor.DARK_GREEN + "Points");
        PLAYER.setScoreboard(scoreboard);
        this.topScoresSection = new TopScoresSection(this, sideObjective, plugin);
        this.statusSection = new StatusSection(this, sideObjective, plugin);
        this.scoreTagSection = new ScoreTagSection(this, nameObjective, plugin);
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

    public Objective getSideObjective() {
        return sideObjective;
    }

    public TopScoresSection getTopScoresSection() {
        return topScoresSection;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setSpawnID(String spawnID) {
        this.spawnID = spawnID;
    }

    public void setTopScoresSection(TopScoresSection topScoresSection) {
        this.topScoresSection = topScoresSection;
    }

    public StatusSection getStatusSection() {
        return statusSection;
    }

    public void setStatusSection(StatusSection statusSection) {
        this.statusSection = statusSection;
    }

    public boolean isOnline() {
        return this.online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Objective getNameObjective() {
        return nameObjective;
    }

    public ScoreTagSection getScoreTagSection() {
        return scoreTagSection;
    }

    public void setScoreTagSection(ScoreTagSection scoreTagSection) {
        this.scoreTagSection = scoreTagSection;
    }
}
