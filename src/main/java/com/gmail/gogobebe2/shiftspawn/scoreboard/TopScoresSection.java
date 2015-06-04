package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
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
        }
        Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
        setLabel(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Everyone's scores " + ChatColor.GOLD + "score"
                + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "/" + ChatColor.DARK_RED + "kills"
                + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + ":", onlinePlayers.length + 2);
        for (int pIndex = 0; pIndex < onlinePlayers.length; pIndex++) {
            Player player = onlinePlayers[pIndex];
            Score score = getObjective().getScore(ChatColor.DARK_PURPLE + "- " + player.getName() + ": "
                    + ChatColor.GOLD + ChatColor.BOLD + getPlugin().getParticipant(player).getScore()
                    + ChatColor.DARK_PURPLE + "/"
                    + ChatColor.DARK_RED + getPlugin().getParticipant(player).getKills());
            score.setScore(pIndex + 1);
            scores.add(score);
        }
    }
}
