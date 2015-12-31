package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftstats.ShiftStats;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import java.sql.SQLException;

public class Participant implements Comparable<Participant> {
    private final Player PLAYER;
    private int score;
    private int kills;
    private String spawnID;

    protected Participant(final Player PLAYER, String spawnID) {
        this.PLAYER = PLAYER;
        this.spawnID = spawnID;
        this.score = 0;
        this.kills = 0;
        PLAYER.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        PLAYER.getScoreboard().clearSlot(DisplaySlot.BELOW_NAME);
    }

    public int getKills() {
        return kills;
    }

    public void addKills(int kills) {
        this.kills += kills;
        try {
            ShiftStats.getAPI().addKills(PLAYER.getUniqueId(), kills);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getScore() {
        return score;
    }

    public Player getPlayer() {
        return PLAYER;
    }

    protected String getSpawnID() {
        return spawnID;
    }

    protected void addPoints(int points) {
        this.score += points;
        try {
            ShiftStats.getAPI().addPoints(PLAYER.getUniqueId(), points);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(Participant p) {
        return this.score - p.getScore();
    }
}
