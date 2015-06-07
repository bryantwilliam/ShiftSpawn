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
    private Score subHeading = null;

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

        setLabel(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Scores:", participants.length + 2);

        if (subHeading == null) {
            subHeading = getObjective().getScore(getAlignedText(
                    ChatColor.DARK_GREEN + StringUtils.repeat(" ", 19) + ChatColor.ITALIC + "score",
                    ChatColor.DARK_RED + "" + ChatColor.ITALIC + "kills", 40));
            subHeading.setScore(participants.length + 1);
        }

        for (int pIndex = 0; pIndex < participants.length; pIndex++) {
            Participant participant = participants[pIndex];

            String s = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + participant.getScore();
            String p = ChatColor.DARK_PURPLE + " " + participant.getPlayer().getName() + ":";
            String prefix = getAlignedText(p, s, 19);
            String suffix = ChatColor.DARK_RED + "" + ChatColor.BOLD + participant.getKills();
            Score score = getObjective().getScore(getAlignedText(prefix, suffix, 40));
            score.setScore(pIndex + 1);
            scores.add(score);
        }
    }

    private String getAlignedText(String prefix, String suffix, int charLimit) {
        return prefix + StringUtils.repeat(" ", charLimit - prefix.length() - suffix.length()) + suffix;
    }
}
