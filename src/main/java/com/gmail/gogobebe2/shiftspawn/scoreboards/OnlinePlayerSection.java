package com.gmail.gogobebe2.shiftspawn.scoreboards;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;

public class OnlinePlayerSection extends ScoreboardSection {
    public OnlinePlayerSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
    }

    @Override
    public void displaySection() {
        int onlineAmount = Bukkit.getOnlinePlayers().size();
        setHeading(getObjective().getScore(ChatColor.AQUA + "Players online: "));
    }
}
