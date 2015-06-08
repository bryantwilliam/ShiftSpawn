package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TopScoresSection extends ScoreboardSection {
    private List<Score> scores = new ArrayList<>();

    public TopScoresSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
    }

    @Override
    public void arrangeSection() {
        if (!scores.isEmpty()) {
            for (Score score : scores) {
                getScoreboard().resetScores(score.getEntry());
            }
            scores.clear();
        }


        Participant[] participants = getPlugin().getParticipants().toArray(new Participant[getPlugin().getParticipants().size()]);
        Arrays.sort(participants);

        String pref = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Scores:";
        setLabel(pref + StringUtils.repeat(" ", 18 - pref.length())
                + ChatColor.DARK_GREEN + ChatColor.ITALIC + "score" + ChatColor.LIGHT_PURPLE + " : "
                + ChatColor.DARK_RED + ChatColor.ITALIC + "kills", participants.length + 1);


        for (int pIndex = 0; pIndex < participants.length; pIndex++) {
            Participant participant = participants[pIndex];
            String prefix = ChatColor.DARK_PURPLE + " " + participant.getPlayer().getName() + ": ";
            Score score = getObjective().getScore(prefix
                    + StringUtils.repeat(" ", 18 - (participant.getPlayer().getName().length() + getDigitsInString(participant.getPlayer().getName())))
                            + ChatColor.DARK_GREEN + ChatColor.BOLD + participant.getScore()
                            + ChatColor.DARK_PURPLE + " : "
                            + ChatColor.DARK_RED + ChatColor.BOLD + participant.getKills());
            score.setScore(pIndex + 1);
            scores.add(score);
        }
    }

    private int getDigitsInString(String string) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) if (Character.isDigit(string.charAt(i))) count++;
        return count;
    }
}
