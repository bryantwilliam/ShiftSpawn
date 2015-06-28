package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

public class ScoreTagSection extends ScoreboardSection {
    public ScoreTagSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
    }

    @Override
    public void arrangeSection() {
        setLabel(getParticipant().getPlayer(), getParticipant().getScore());
        showKillsTag();
    }

    private void showKillsTag() {
        for (Participant participant : getPlugin().getParticipants()) {
            Player player = participant.getPlayer();

            String name;
            if (player.getName().length() <= 11) {
                name = player.getName();
            } else {
                name = player.getName().substring(0, 11);
            }
            name = name + "_team";
            Team team = null;
            boolean foundTeam = false;
            if (!getScoreboard().getTeams().isEmpty()) {
                for (Team t : getScoreboard().getTeams()) {
                    if (t.getName().equals(name)) {
                        team = getScoreboard().getTeam(name);
                        foundTeam = true;
                        break;
                    }
                }
            }
            if (!foundTeam) {
                team = getScoreboard().registerNewTeam(name);
            }
            team.setPrefix(ChatColor.DARK_RED + "[" + participant.getKills() + "] " + ChatColor.AQUA + ChatColor.BOLD);
            team.addPlayer(player);
        }
    }
}
