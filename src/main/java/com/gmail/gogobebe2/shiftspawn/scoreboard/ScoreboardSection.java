package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public abstract class ScoreboardSection {
    private Score heading = null;
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
        if (isHeadingSet()) {
            resetHeading();
        }
        arrangeSection();
        saveSection();
    }

    public abstract void arrangeSection();

    public boolean isHeadingSet() {
        return heading != null;
    }

    public void resetHeading() {
        scoreboard.resetScores(heading.getEntry());
    }

    public void saveSection() {
        participant.getPlayer().setScoreboard(scoreboard);
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

    public void setHeading(String heading, int index) {
        this.heading = objective.getScore(heading);
        this.heading.setScore(index);
    }
}
