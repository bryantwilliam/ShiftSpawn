package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ShiftSpawn extends JavaPlugin {
    public final static String MIN_PLAYERS_KEY = "Minimum players before game starts";
    public final static String TIME_BEFORE_START_KEY = "Time before games starts";
    public final static String GAME_TIME = "Game time";

    @Override
    public void onEnable() {
        getLogger().info("Starting up ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
        saveDefaultConfig();
        Game game = new Game(this);
        if (isEnoughPlayersToStart()) {
            beginStarting(game);
        }

        game.startTimer(false);
    }

    private boolean isEnoughPlayersToStart() {
        return Bukkit.getOnlinePlayers().size() > getConfig().getInt(MIN_PLAYERS_KEY);
    }
    private void beginStarting(Game game) {
        game.setGameState(GameState.STARTING);
        game.setTime(getConfig().getString(TIME_BEFORE_START_KEY));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
    }
}
