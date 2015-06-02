package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftspawn.scoreboards.OnlinePlayerSection;
import com.gmail.gogobebe2.shiftspawn.scoreboards.ScoreTagSection;
import com.gmail.gogobebe2.shiftspawn.scoreboards.StatusSection;
import com.gmail.gogobebe2.shiftspawn.scoreboards.TopScoresSection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Random;

public class Participant {
    private final Player PLAYER;
    private int score;
    private int kills;
    private String spawnID;
    private TopScoresSection topScoresSection;
    private StatusSection statusSection;
    private ScoreTagSection scoreTagSection;
    private OnlinePlayerSection onlinePlayerSection;
    private boolean online;

    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID) {
        this(plugin, PLAYER, spawnID, 0, 0);
    }

    private String getUniqueObjectiveName(String name, String prefix) {
        return prefix + "_" + new Random().nextInt(9 + 1) + (name.length() >= 12 ? name.substring(0, 132) : name) + new Random().nextInt(9 + 1);
    }

    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID, int score, int kills) {
        this.PLAYER = PLAYER;
        this.spawnID = spawnID;
        this.score = score;
        this.kills = kills;
        Scoreboard scoreboard = PLAYER.getScoreboard();
        String playerName = PLAYER.getName();

        Objective sideObjective = scoreboard.registerNewObjective(getUniqueObjectiveName(playerName, "s"), "dummy");
        sideObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        sideObjective.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Shift Scores");
        this.topScoresSection = new TopScoresSection(this, sideObjective, plugin);
        this.statusSection = new StatusSection(this, sideObjective, plugin);
        this.onlinePlayerSection = new OnlinePlayerSection(this, sideObjective, plugin);

        Objective nameObjective = scoreboard.registerNewObjective(getUniqueObjectiveName(playerName, "n"), "dummy");
        nameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        nameObjective.setDisplayName(ChatColor.DARK_GREEN + "Points");
        this.scoreTagSection = new ScoreTagSection(this, nameObjective, plugin);
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        /*
         TODO:
         Use this when a player gets a kill.
         */
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

    public TopScoresSection getTopScoresSection() {
        return topScoresSection;
    }

    public void setScore(int score) {
        /*
        TODO:
        Use this when a player mines alpha core.
        */
        this.score = score;
    }
    public StatusSection getStatusSection() {
        return statusSection;
    }

    public boolean isOnline() {
        return this.online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public ScoreTagSection getScoreTagSection() {
        return scoreTagSection;
    }

    public OnlinePlayerSection getOnlinePlayerSection() {
        return onlinePlayerSection;
    }
}
