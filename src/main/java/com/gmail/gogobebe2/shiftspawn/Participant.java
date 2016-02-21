package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftstats.ShiftStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import java.sql.SQLException;
import java.util.UUID;

public class Participant implements Comparable<Participant> {
    private final UUID uuid;
    private int score;
    private int kills;
    private String spawnID;

    protected Participant(final UUID uuid, String spawnID) {
        this.uuid = uuid;
        this.spawnID = spawnID;
        this.score = 0;
        this.kills = 0;
        Player player = getPlayer();
        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        player.getScoreboard().clearSlot(DisplaySlot.BELOW_NAME);
        player.setScoreboard(Game.getScoreboard());
    }

    public int getKills() {
        return kills;
    }

    public void addKills(int kills) {
        this.kills += kills;
        try {
            ShiftStats.getAPI().addKills(uuid, kills);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getScore() {
        return score;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public String getSpawnID() {
        return spawnID;
    }

    protected void addPoints(int points) {
        this.score += points;
        try {
            ShiftStats.getAPI().addPoints(uuid, points);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(Participant p) {
        return this.score - p.getScore();
    }
}
