package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public abstract class MultipleScoreboardSection extends ScoreboardSection {
    private List<Score> scores = new ArrayList<>();

    public MultipleScoreboardSection(Scoreboard scoreboard, Objective objective, ShiftSpawn plugin) {
        super(scoreboard, objective, plugin);
    }

    @Override
    public void display(Player player) {
        if (!scores.isEmpty()) {
            for (Score score : scores) {
                getScoreboard().resetScores(score.getEntry());
            }
            scores.clear();
        }
        super.display(player);
    }

    protected List<Score> getScores() {
        return scores;
    }

    protected void addScore(String label, int index) {
        Score score = getObjective().getScore(label);
        score.setScore(index);
        scores.add(score);
    }

    @Deprecated
    protected void addScore(OfflinePlayer label, int index) {
        Score score = getObjective().getScore(label);
        score.setScore(index);
        scores.add(score);
    }
}
