package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ShiftSpawn extends JavaPlugin {
    public final static String MIN_PLAYERS_KEY = "Minimum players before game starts";
    public final static String TIME_BEFORE_START_KEY = "Time before games starts";
    public final static String GAME_TIME = "Game time";
    private Game game;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public void onEnable() {
        getLogger().info("Starting up ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
        saveDefaultConfig();
        this.game = new Game(this);
        game.setGameState(GameState.WAITING);
        tryBeginStarting();
        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
    }

    public boolean tryBeginStarting() {
        boolean wasSuccessful = false;
        if (Bukkit.getOnlinePlayers().size() > getConfig().getInt(MIN_PLAYERS_KEY) && game.getGameState().equals(GameState.WAITING)) {
            game.setGameState(GameState.STARTING);
            game.setTime(getConfig().getString(TIME_BEFORE_START_KEY));
        }
        game.startTimer(false);
        return wasSuccessful;
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
    }

    public Game getGame() {
        return this.game;
    }
}
