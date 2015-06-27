package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public abstract class ScoreboardSection {
    private Score label = null;
    private Scoreboard scoreboard;
    private Objective objective;
    private Participant participant;
    private ShiftSpawn plugin;

    public ScoreboardSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        this.participant = participant;
        this.scoreboard = participant.getPlayer().getScoreboard();
        this.objective = objective;
        this.plugin = plugin;
    }

    public void display() {
        if (isLabelSet()) {
            scoreboard.resetScores(label.getEntry());
        }
        arrangeSection();
        participant.getPlayer().setScoreboard(scoreboard);
    }

    public abstract void arrangeSection();

    public boolean isLabelSet() {
        return label != null;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Objective getObjective() {
        return objective;
    }

    public Participant getParticipant() {
        return participant;
    }

    public ShiftSpawn getPlugin() {
        return plugin;
    }

    public void setLabel(String label, int index) {
        this.label = objective.getScore(label);
        this.label.setScore(index);
    }

    @Deprecated
    public void setLabel(OfflinePlayer label, int index) {
        this.label = objective.getScore(label);
        this.label.setScore(index);
    }
}
