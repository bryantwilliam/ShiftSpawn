package com.gmail.gogobebe2.shiftspawn.scoreboards;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class TopScoresSection extends ScoreboardSection {

    public TopScoresSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
    }

    @Override
    public void displaySection() {
        deleteSection();
        int highestScore = 0;
        for (Participant participant : getPlugin().getOnlineParticipants()) {
            setScore(getObjective().getScore(ChatColor.GREEN + participant.getPlayer().getName() + ": "));
            int pScore = participant.getScore();
            setSectionIndex(pScore);

            if (pScore > highestScore) {
                highestScore = participant.getScore();
            }
        }

        Score s = getObjective().getScore(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "Everyone's scores");
        s.setScore(highestScore + 1);
        saveSection();
    }
}
