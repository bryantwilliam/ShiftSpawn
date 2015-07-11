package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreTagSection extends MultipleScoreboardSection {
    public ScoreTagSection(Scoreboard scoreboard, Objective objective, ShiftSpawn plugin) {
        super(scoreboard, objective, plugin);
    }

    @Override
    public void arrangeSection() {
        for (Participant participant : getPlugin().getParticipants()) {
            addScore(participant.getPlayer(), participant.getScore());
        }
    }
}
