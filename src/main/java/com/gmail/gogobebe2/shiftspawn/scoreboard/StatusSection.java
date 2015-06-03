package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;

public class StatusSection extends ScoreboardSection {

    public StatusSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
    }

    @Override
    public void arrangeSection() {
        setHeading(getStatus(), 0);
    }

    private String getStatus() {
        String dots = (getPlugin().getGame().getSeconds() % 2 == 0 ? "." : "");
        switch (getPlugin().getGame().getGameState()) {
            case WAITING:
                return ChatColor.BLUE + "Waiting.." + dots;
            case STARTING:
                return ChatColor.AQUA + "Starting in: " + ChatColor.DARK_AQUA + getPlugin().getGame().getTime();
            case STARTED:
                return ChatColor.GOLD + "Time left: " + ChatColor.DARK_GREEN + getPlugin().getGame().getTime();
            case RESTARTING:
                return ChatColor.RED + "" + ChatColor.BOLD + "Restarting.." + dots;
            default:
                return ChatColor.RED + "Error! ";
        }
    }
}
