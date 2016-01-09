package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class StatusSection extends SingleScoreboardSection {

    public StatusSection(Scoreboard scoreboard, Objective objective, ShiftSpawn plugin) {
        super(scoreboard, objective, plugin);
    }

    @Override
    public void display() {
        setLabel(getStatus(), 0);
    }

    private String getStatus() {
        String dots = (getPlugin().getGame().getSeconds() % 2 == 0 ? "." : "");
        switch (getPlugin().getGame().getGameState()) {
            case WAITING:
                return ChatColor.DARK_BLUE + "Waiting.." + dots;
            case STARTING:
                return ChatColor.AQUA + "Starting in: " + ChatColor.DARK_AQUA + getPlugin().getGame().getTime();
            case STARTED:
                return ChatColor.GOLD + "Time left: " + ChatColor.DARK_GREEN + getPlugin().getGame().getTime();
            case RESTARTING:
                return ChatColor.DARK_RED + getPlugin().getGame().getTime() + ChatColor.RED + " till server restarts.." + dots;
            default:
                return ChatColor.RED + "Error! ";
        }
    }
}
