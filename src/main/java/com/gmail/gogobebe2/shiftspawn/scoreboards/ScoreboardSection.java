package com.gmail.gogobebe2.shiftspawn.scoreboards;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public abstract class ScoreboardSection {
    private Score heading = null;
    private Score score = null;
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
        arrangeSection();
        saveSection();
    }

    public abstract void arrangeSection();

    public boolean isHeadingSet() {
        return heading != null;
    }

    public boolean isScoreSet() {
        return score != null;
    }

    public void resestSection() {
        scoreboard.resetScores(heading.getEntry());
        scoreboard.resetScores(score.getEntry());
    }

    public void saveSection() {
        participant.getPlayer().setScoreboard(scoreboard);
    }

    public Score getScore() {
        return score;
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

    public void setScore(String score, int index) {
        this.score = objective.getScore(score);
        this.score.setScore(index);
    }
}
