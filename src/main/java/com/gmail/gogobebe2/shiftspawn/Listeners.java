package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {
    private ShiftSpawn plugin;
    private Game game;

    public Listeners(ShiftSpawn plugin) {
        this.plugin = plugin;
        this.game = plugin.getGame();
    }

    public boolean tryBeginStarting() {
        boolean wasSuccessful = false;
        if (Bukkit.getOnlinePlayers().size() >= plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY) && game.getGameState().equals(GameState.WAITING)) {
            game.setGameState(GameState.STARTING);
            Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Game starting in " + plugin.getConfig().get(ShiftSpawn.TIME_BEFORE_START_KEY));
            game.setTime(plugin.getConfig().getString(ShiftSpawn.TIME_BEFORE_START_KEY));
            wasSuccessful = true;
        }
        return wasSuccessful;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        event.setJoinMessage(ChatColor.DARK_PURPLE + playerName + " joined the game.");
        if (game.getGameState().equals(GameState.WAITING)) {
            if (!tryBeginStarting()) {
                event.setJoinMessage(ChatColor.DARK_PURPLE + playerName + " joined. We need "
                        + (plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY) - Bukkit.getOnlinePlayers().size())
                        + " more players to start.");
            }
        }
        plugin.spawn(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        int minPlayers = plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY);
        Player player = event.getPlayer();
        String playerName = player.getName();
        event.setQuitMessage(ChatColor.DARK_PURPLE + playerName + " left the game.");
        if (minPlayers < Bukkit.getOnlinePlayers().size() && (game.getGameState().equals(GameState.STARTING) || game.getGameState().equals(GameState.WAITING))) {
            event.setQuitMessage(ChatColor.DARK_PURPLE + playerName + " left the game. We now need "
                    + (minPlayers - Bukkit.getOnlinePlayers().size())
                    + " more players to start. All " + playerName
                    + "'s fault. Blame them because now it'll take longer to start!!");
            game.setGameState(GameState.WAITING);
            if (plugin.containsPlayer(player)) {
                plugin.getParticipants().remove(plugin.getParticipant(player));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDamageEvent(BlockDamageEvent event) {
        //noinspection deprecation
        if (event.getBlock().getTypeId() == plugin.getConfig().getInt(ShiftSpawn.ALPHA_CORE_ID)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getY() < 0 && player.getHealth() > 0) {
            player.setHealth(0);
            plugin.spawn(player);
        }
    }
}
