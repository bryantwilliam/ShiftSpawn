package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {
    private ShiftSpawn plugin;

    public Listeners(ShiftSpawn plugin) {
        this.plugin = plugin;
    }

    public boolean tryBeginStarting() {
        boolean wasSuccessful = false;
        if (Bukkit.getOnlinePlayers().size() >= plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY) && plugin.getGame().getGameState().equals(GameState.WAITING)) {
            plugin.getGame().setGameState(GameState.STARTING);
            Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Game starting in " + plugin.getConfig().get(ShiftSpawn.TIME_BEFORE_START_KEY));
            plugin.getGame().setTime(plugin.getConfig().getString(ShiftSpawn.TIME_BEFORE_START_KEY));
            wasSuccessful = true;
        }
        return wasSuccessful;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.hasParticipantSet(player)) {
            plugin.getParticipants().add(new Participant(plugin, player, plugin.getNextSpawnID()));
        }
        plugin.spawn(player);
        String playerName = player.getName();
        event.setJoinMessage(ChatColor.DARK_PURPLE + playerName + " joined the game.");
        if (plugin.getGame().getGameState().equals(GameState.WAITING)) {
            if (!tryBeginStarting()) {
                event.setJoinMessage(ChatColor.DARK_PURPLE + playerName + " joined. We need "
                        + (plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY) - Bukkit.getOnlinePlayers().size())
                        + " more players to start.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        int minPlayers = plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY);
        Player player = event.getPlayer();
        String playerName = player.getName();
        event.setQuitMessage(ChatColor.DARK_PURPLE + playerName + " left the game.");
        if (minPlayers < Bukkit.getOnlinePlayers().size() && (plugin.getGame().getGameState().equals(GameState.STARTING) || plugin.getGame().getGameState().equals(GameState.WAITING))) {
            event.setQuitMessage(ChatColor.DARK_PURPLE + playerName + " left the game. We now need "
                    + (minPlayers - Bukkit.getOnlinePlayers().size())
                    + " more players to start. All " + playerName
                    + "'s fault. Blame them because now it'll take longer to start!!");
            plugin.getGame().setGameState(GameState.WAITING);
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (plugin.getGame().getGameState() == GameState.STARTED && killer != null) {
            Participant k = plugin.getParticipant(killer);
            k.setKills(k.getKills() + 1);
        }
        plugin.spawn(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (plugin.getGame().getGameState() == GameState.STARTED) {
            if (event.getBlock().getType() == Material.getMaterial(plugin.getConfig().getInt(ShiftSpawn.ALPHA_CORE_ID))) {
                Participant participant = plugin.getParticipant(event.getPlayer());
                participant.setScore(participant.getScore() + 1);
                event.setCancelled(true);
            }
        }
    }

}
