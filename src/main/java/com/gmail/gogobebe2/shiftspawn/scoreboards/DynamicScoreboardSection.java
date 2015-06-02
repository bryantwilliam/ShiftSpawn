package com.gmail.gogobebe2.shiftspawn.scoreboards;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.scoreboard.Objective;

public abstract class DynamicScoreboardSection extends ScoreboardSection {
    public DynamicScoreboardSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
    }

    @Override
    public void display() {
        if (isHeadingSet() || isScoreSet()) {
            resestSection();
        }
        super.display();
    }
}
