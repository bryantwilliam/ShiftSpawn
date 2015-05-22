package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {
    private ShiftSpawn plugin;

    public Listeners(ShiftSpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        if (plugin.getGame().getGameState().equals(GameState.WAITING) && !plugin.tryBeginStarting()) {
            event.setJoinMessage(ChatColor.DARK_PURPLE + playerName + " joined. We need "
                    + (plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY) - Bukkit.getOnlinePlayers().size())
                    + " more players to start.");
        } else {
            event.setJoinMessage(ChatColor.DARK_PURPLE + playerName + " joined the game.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getY() < 0 && player.getHealth() > 0) {
            player.setHealth(0);
            player.spigot().respawn();
        }
    }
}
