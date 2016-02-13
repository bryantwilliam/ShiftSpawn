package com.gmail.gogobebe2.shiftspawn;

import co.insou.xcore.utils.PlayerInfo;
import com.gmail.gogobebe2.shiftspawn.api.events.PlayerShiftKilledEvent;
import com.gmail.gogobebe2.shiftspawn.api.events.PlayerShiftScoreEvent;
import com.gmail.gogobebe2.shiftspawn.api.events.PlayerShiftWinEvent;
import com.gmail.gogobebe2.shiftstats.ShiftStats;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class Listeners implements Listener {
    private ShiftSpawn plugin;

    protected Listeners(ShiftSpawn plugin) {
        this.plugin = plugin;
    }

    protected boolean tryBeginStarting() {
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
        if (!plugin.hasParticipantSet(player) && plugin.getGame().getGameState() == GameState.STARTED
                && plugin.getGame().getMinutes() < Integer.parseInt(plugin.getConfig().getString(ShiftSpawn.GAME_TIME).split(":")[0]) - 5) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.AQUA + "Sorry, the game has already started. Come back later. There's "
                    + ChatColor.GOLD + plugin.getGame().getTime() + ChatColor.AQUA + " time left.");
        } else {
            event.allow();
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1.2F);
        if (!plugin.hasParticipantSet(player)) {
            // Clearing players inventory to fix weird bug:
            player.getInventory().clear();

            player.getInventory().setArmorContents(new ItemStack[4]);
            player.setExp(0);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setGameMode(GameMode.SURVIVAL);
            plugin.getParticipants().add(new Participant(player, plugin.getNextSpawnID()));
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
            event.setQuitMessage(ChatColor.DARK_RED + "No more players alive, congratulations" + playerName + ", you win by default...");
            plugin.getGame().setTime("0:10");
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
        event.setKeepLevel(true);
        event.setDeathMessage(null);
        onDeath(event.getEntity(), event.getEntity().getKiller());
    }

    private void onDeath(Player player, Player killer) {
        if (killer != null) {
            PlayerShiftKilledEvent playerShiftKilledEvent = new PlayerShiftKilledEvent(player, killer);
            Bukkit.getServer().getPluginManager().callEvent(playerShiftKilledEvent);
            if (!playerShiftKilledEvent.isCancelled()) {
                Participant k = plugin.getParticipant(killer);
                k.addKills(1);
                killer.playSound(killer.getLocation(), Sound.NOTE_PIANO, 1.4F, 1.6F);
                killer.giveExpLevels(1);
            }
        }
        plugin.spawn(player);
        player.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 0.9F, 1);

        String randomDeathMessage = getRandomDeathMessage(player, killer);
        if (randomDeathMessage != null) {
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + randomDeathMessage);
        }
        try {
            ShiftStats.getAPI().addDeaths(player.getUniqueId(), 1);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDamagedEvent(EntityDamageByEntityEvent event) {
        if (plugin.getGame().getGameState() != GameState.STARTED) {
            event.setCancelled(true);
            event.getDamager().sendMessage(ChatColor.AQUA + "You can't hurt anyone, the game hasn't started yet!");
        }
    }

    private String getRandomDeathMessage(Player player, Player killer) {
        if (!plugin.getConfig().isSet(ShiftSpawn.DEATH_MESSAGES)) {
            return null;
        }
        List<String> deathMessages = plugin.getConfig().getStringList(ShiftSpawn.DEATH_MESSAGES);
        if (deathMessages.size() == 0) {
            return null;
        }
        int index = new Random().nextInt(deathMessages.size());
        String deathMessage = deathMessages.get(index);

        String playerVariable = "PLAYER";
        String killerVariable = "KILLER";
        if (deathMessage.contains(killerVariable)) {
            if (killer == null) {
                boolean NoKillerMessage = true;
                for (String message : deathMessages) {
                    if (message.contains(killerVariable)) {
                        NoKillerMessage = false;
                    }
                }
                return NoKillerMessage ? player.getName() + " died." : getRandomDeathMessage(player, null);
            } else {
                deathMessage = deathMessage.replaceAll(killerVariable, killer.getName());
            }
        }
        if (deathMessage.contains(playerVariable)) {
            deathMessage = deathMessage.replaceAll(playerVariable, player.getName());
        }
        return deathMessage;
    }


    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();
        Player player = event.getPlayer();
        final Material alphaCoreMaterial = Material.getMaterial(plugin.getConfig().getInt(ShiftSpawn.ALPHA_CORE_ID));
        if (blockType == alphaCoreMaterial) {
            if (plugin.getGame().getGameState() == GameState.STARTED) {
                PlayerShiftScoreEvent playerShiftScoreEvent = new PlayerShiftScoreEvent(player);
                Bukkit.getServer().getPluginManager().callEvent(playerShiftScoreEvent);
                if (!playerShiftScoreEvent.isCancelled()) {
                    Participant participant = plugin.getParticipant(player);
                    participant.addPoints(1);
                    player.getWorld().playSound(player.getLocation(), Sound.ANVIL_LAND, 1.2F, 0.4F);
                }
            }
            plugin.getAlphaCores().add(block);
            event.setCancelled(true);
        }
        Material[] oreTypes = new Material[]{Material.DIAMOND_ORE, Material.COAL_ORE, Material.EMERALD_ORE,
                Material.GLOWING_REDSTONE_ORE, Material.GOLD_ORE, Material.IRON_ORE, Material.LAPIS_ORE,
                Material.REDSTONE_ORE, Material.DIAMOND_BLOCK, Material.COAL_BLOCK,
                Material.EMERALD_BLOCK, Material.GLOWING_REDSTONE_ORE, Material.GOLD_BLOCK, Material.IRON_BLOCK,
                Material.LAPIS_BLOCK, Material.REDSTONE_BLOCK};

        for (Material oreType : oreTypes) {
            if (oreType == blockType) {
                try {
                    ShiftStats.getAPI().addOresMined(player.getUniqueId(), 1);
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
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

    @EventHandler
    public void onPlayerShiftKilled(PlayerShiftKilledEvent event) {
        giveXP(2, event.getKiller());
    }

    @EventHandler
    public void onPlayerShiftScore(PlayerShiftScoreEvent event) {
        giveXP(1, event.getPlayer());
    }

    @EventHandler
    public void onPlayerShiftWinEvent(PlayerShiftWinEvent event) {
        giveXP(50, event.getPlayer());
    }

    private void giveXP(int xp, Player player) {
        PlayerInfo playerInfo =  PlayerInfo.getPlayerInfo(player);
        int initialAmount = playerInfo.getXP();
        PlayerInfo.getPlayerInfo(player).addXP(xp);
        player.sendMessage(ChatColor.GREEN + "You just earned " + (playerInfo.getXP() - initialAmount) + " xp!");
    }
}
