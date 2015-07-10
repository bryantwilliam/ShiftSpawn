package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

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

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (!plugin.hasParticipantSet(player) && plugin.getGame().getGameState() == GameState.STARTED) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.AQUA + "Sorry, the game has already started. Come back later. There's "
                    + ChatColor.GOLD + plugin.getGame().getTime() + ChatColor.AQUA + " time left.");
        }
        else {
            event.allow();
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1.2F);
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

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        int minPlayers = plugin.getConfig().getInt(ShiftSpawn.MIN_PLAYERS_KEY);
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (plugin.getGame().getGameState() == GameState.STARTED && Bukkit.getOnlinePlayers().size() == 1) {
            plugin.getGame().setTime("0:10");
            event.setQuitMessage(ChatColor.DARK_RED + playerName + "Left. No more players alive, restarting game...");
        } else if (minPlayers >= Bukkit.getOnlinePlayers().size() && (plugin.getGame().getGameState() == GameState.STARTING || plugin.getGame().getGameState() == GameState.WAITING)) {
            event.setQuitMessage(ChatColor.DARK_PURPLE + playerName + " left the game. We now need "
                    + (minPlayers - (Bukkit.getOnlinePlayers().size() - 1))
                    + " more players to start. All " + playerName
                    + "'s fault. Blame them because now it'll take longer to start!!");
            plugin.getGame().setGameState(GameState.WAITING);
        } else {
            event.setQuitMessage(ChatColor.DARK_PURPLE + playerName + " left the game.");
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getY() <= 0.1) {
            onDeath(player, player.getKiller());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        onDeath(event.getEntity().getPlayer(), event.getEntity().getKiller());
    }

    @EventHandler
    public void onPlayerDamagedEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getFinalDamage() >= player.getHealth()) {
                event.setCancelled(true);
                onDeath(player, (Player) event.getDamager());
            }
        }
    }

    private void onDeath(Player player, Player killer) {
        if (plugin.getGame().getGameState() == GameState.STARTED && killer != null) {
            Participant k = plugin.getParticipant(killer);
            k.setKills(k.getKills() + 1);
            Bukkit.broadcastMessage("Death message - I'll do this shit later.");
            killer.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.4F, 1.6F);
        }
        plugin.spawn(player);
        player.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 0.9F, 1);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.getMaterial(plugin.getConfig().getInt(ShiftSpawn.ALPHA_CORE_ID))) {
            if (plugin.getGame().getGameState() == GameState.STARTED) {
                Player player = event.getPlayer();
                Participant participant = plugin.getParticipant(player);
                participant.setScore(participant.getScore() + 1);
                player.getWorld().playSound(player.getLocation(), Sound.ANVIL_LAND, 1.4F, 0.4F);
                plugin.getAlphaCores().add(block);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(event.toWeatherState());
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }
}
