package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.entity.Player;

public class Participant {
    private final Player PLAYER;
    private int score;
    private String spawnID;

    public Participant(final Player PLAYER, String spawnID) {
        this(PLAYER, spawnID, 0);
    }

    public Participant(final Player PLAYER, String spawnID, int score) {
        this.PLAYER = PLAYER;
        this.spawnID = spawnID;
        this.score = score;
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

    public void setSpawnID(String spawnID) {
        this.spawnID = spawnID;
    }
}
