package com.gmail.gogobebe2.shiftspawn.scoreboard;

import com.gmail.gogobebe2.shiftspawn.Game;
import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;

public class StatusSection extends ScoreboardSection {
    private Game game;

    public StatusSection(Participant participant, Objective objective, ShiftSpawn plugin) {
        super(participant, objective, plugin);
        this.game = plugin.getGame();
    }

    @Override
    public void arrangeSection() {
        setHeading(getStatus(), 0);
    }

    private String getStatus() {
        String dots = (game.getSeconds() % 2 == 0 ? "." : "");
        switch (game.getGameState()) {
            case WAITING:
                return ChatColor.BLUE + "Waiting.." + dots;
            case STARTING:
                return ChatColor.DARK_AQUA + "Starting in: " + ChatColor.AQUA + game.getTime();
            case STARTED:
                return ChatColor.GREEN + "Time left: " + ChatColor.DARK_GREEN + game.getTime();
            case RESTARTING:
                return ChatColor.RED + "" + ChatColor.BOLD + "Restarting.." + dots;
            default:
                return ChatColor.RED + "Error! ";
        }
    }
}
