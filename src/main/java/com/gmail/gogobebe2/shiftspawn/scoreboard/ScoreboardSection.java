package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public abstract class ScoreboardSection {

    private Scoreboard scoreboard;
    private Objective objective;
    private ShiftSpawn plugin;

    public ScoreboardSection(Scoreboard scoreboard, Objective objective, ShiftSpawn plugin) {
        this.scoreboard = scoreboard;
        this.objective = objective;
        this.plugin = plugin;
    }

    public void display(Player player) {
        arrangeSection();
        player.setScoreboard(scoreboard);
    }

    public abstract void arrangeSection();

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Objective getObjective() {
        return objective;
    }

    public ShiftSpawn getPlugin() {
        return plugin;
    }
}
