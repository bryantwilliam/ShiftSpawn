package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

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

    public void setKills(int kills) {
        this.kills = kills;
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

    protected void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(Participant p) {
        return this.score - p.getScore();
    }
}
