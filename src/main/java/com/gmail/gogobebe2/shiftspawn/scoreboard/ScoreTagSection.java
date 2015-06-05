package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.scoreboard.Objective;

public class ScoreTagSection extends ScoreboardSection {
    public ScoreTagSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
    }

    @Override
    public void arrangeSection() {
        setLabel(getParticipant().getPlayer().getName(), getParticipant().getScore());
    }
}
