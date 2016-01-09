package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
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

    public abstract void display();

    protected Scoreboard getScoreboard() {
        return scoreboard;
    }

    protected Objective getObjective() {
        return objective;
    }

    protected ShiftSpawn getPlugin() {
        return plugin;
    }
}
