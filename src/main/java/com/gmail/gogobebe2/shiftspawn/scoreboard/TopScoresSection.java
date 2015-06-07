package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
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
                    ChatColor.DARK_GREEN + "                   " + ChatColor.ITALIC + "score",
                    ChatColor.DARK_RED + "" + ChatColor.ITALIC + "kills", 40));
            subHeading.setScore(participants.length + 1);
        }

        for (int pIndex = 0; pIndex < participants.length; pIndex++) {
            Participant participant = participants[pIndex];

            String s = ChatColor.DARK_GREEN + "" + ChatColor.ITALIC + participant.getScore();
            String prefix = getAlignedText(ChatColor.DARK_PURPLE + "\u2605 " + participant.getPlayer().getName() + ":", s, 19 + s.length());
            String suffix = ChatColor.DARK_RED + "" + ChatColor.ITALIC + participant.getKills();
            Score score = getObjective().getScore(getAlignedText(prefix, suffix, 40));
            score.setScore(pIndex + 1);
            scores.add(score);
        }
    }

    private String getAlignedText(String prefix, String suffix, int charLimit) {
        StringBuilder text = new StringBuilder();
        text.append(prefix);
        for (int i = 0; i < charLimit - prefix.length() - suffix.length(); i++) {
            text.append(" ");
        }
        text.append(suffix);
        return text.toString();
    }
}
