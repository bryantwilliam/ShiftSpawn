package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftspawn.scoreboard.ScoreTagSection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

public class Participant implements Comparable<Participant> {
    private final Player PLAYER;
    private int score;
    private int kills;
    private String spawnID;

    private ScoreTagSection scoreTagSection;

    public Participant(ShiftSpawn plugin, final Player PLAYER, String spawnID) {
        this.PLAYER = PLAYER;
        this.spawnID = spawnID;
        this.score = 0;
        this.kills = 0;
        PLAYER.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        PLAYER.getScoreboard().clearSlot(DisplaySlot.BELOW_NAME);

        Objective nameObjective = plugin.getObjective(Game.getScoreboard(), "name_obj");
        nameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        nameObjective.setDisplayName(ChatColor.DARK_GREEN + "Points");
        this.scoreTagSection = new ScoreTagSection(Game.getScoreboard(), nameObjective, plugin);
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

    public void setScore(int score) {
        this.score = score;
    }

    public ScoreTagSection getScoreTagSection() {
        return scoreTagSection;
    }

    @Override
    public int compareTo(Participant p) {
        return this.score - p.getScore();
    }
}
