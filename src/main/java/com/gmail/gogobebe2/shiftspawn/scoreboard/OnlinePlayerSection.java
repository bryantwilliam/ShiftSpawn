package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;

public class OnlinePlayerSection extends ScoreboardSection {
    private ShiftSpawn plugin;
    public OnlinePlayerSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
        this.plugin = plugin;
    }

    @Override
    public void arrangeSection() {
        setHeading(ChatColor.AQUA + "" + ChatColor.ITALIC + "Players online: " + ChatColor.DARK_AQUA + Bukkit.getOnlinePlayers().size() + "/" + plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY), 0);
    }
}
