package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftspawn.scoreboard.OnlinePlayerSection;
import com.gmail.gogobebe2.shiftspawn.scoreboard.ScoreTagSection;
import com.gmail.gogobebe2.shiftspawn.scoreboard.StatusSection;
import com.gmail.gogobebe2.shiftspawn.scoreboard.TopScoresSection;
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

    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID) {
        this(plugin, PLAYER, spawnID, 0, 0);
    }

    private Objective getObjective(Scoreboard scoreboard, String name) {
        if (!scoreboard.getObjectives().isEmpty()) {
            for (Objective objective : scoreboard.getObjectives()) {
                if (objective.getName().equals(name)) {
                        return scoreboard.getObjective(name);
                }
            }
        }
        return scoreboard.registerNewObjective(name, "dummy");
    }
    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID, int score, int kills) {
        this.PLAYER = PLAYER;
        this.spawnID = spawnID;
        this.score = score;
        this.kills = kills;
        Scoreboard scoreboard = PLAYER.getScoreboard();
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        scoreboard.clearSlot(DisplaySlot.BELOW_NAME);
        
        String playerName = PLAYER.getName();

        Objective sideObjective = getObjective(scoreboard, getUniqueObjectiveName(playerName, 's'));
        sideObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        sideObjective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Shift");
        this.topScoresSection = new TopScoresSection(this, sideObjective, plugin);
        this.statusSection = new StatusSection(this, sideObjective, plugin);
        this.onlinePlayerSection = new OnlinePlayerSection(this, sideObjective, plugin);

        Objective nameObjective = getObjective(scoreboard, getUniqueObjectiveName(playerName, 'n'));
        nameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        nameObjective.setDisplayName(ChatColor.DARK_GREEN + "Points");
        this.scoreTagSection = new ScoreTagSection(this, nameObjective, plugin);
    }

    private String getUniqueObjectiveName(String name, char prefix) {
        // Incase of people with similar name.
        return prefix + "_" + new Random().nextInt(10) + (name.length() >= 13 ? name.substring(0, 12) : name) + new Random().nextInt(10);
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

    public TopScoresSection getTopScoresSection() {
        return topScoresSection;
    }

    public void setScore(int score) {
        this.score = score;
    }
    public StatusSection getStatusSection() {
        return statusSection;
    }

    public ScoreTagSection getScoreTagSection() {
        return scoreTagSection;
    }

    public OnlinePlayerSection getOnlinePlayerSection() {
        return onlinePlayerSection;
    }
}
