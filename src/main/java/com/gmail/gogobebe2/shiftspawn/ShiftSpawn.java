package com.gmail.gogobebe2.shiftspawn;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftSpawn extends JavaPlugin implements Listener {
    private GameState gameState = GameState.WAITING;
    Map<Player, Location> playerSpawns = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Starting up ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
        if (!getConfig().isSet("spawns.main.world")) {
            for (int i = 0; i < 10; i++) {
                getLogger().severe("No main spawn set, to set it, type /shiftspawn main");
            }
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.teleport(getLocationConfig("main"));
        event.setJoinMessage(ChatColor.DARK_PURPLE + player.getName() + " left the game.");
        if (gameState.equals(GameState.WAITING)) {
            double time = getConfig().getDouble("time before games starts");
            if (Bukkit.getOnlinePlayers().size() >= getConfig().getInt("minimum players before game starts")) {
                gameState = GameState.STARTING;
                broadcastTimeLeft(time);
                BukkitTask task = new Timer(this, time).runTaskTimer(this, 0, 20);
            }
            event.setJoinMessage(ChatColor.DARK_PURPLE + event.getPlayer().getName() + " joined. We need "
                    + (time - Bukkit.getOnlinePlayers().size()) + " more players to start.");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(ChatColor.DARK_PURPLE + player.getName() + " left the game.");
        if (gameState.equals(GameState.STARTING)) {
            if (Bukkit.getOnlinePlayers().size() < getConfig().getInt("minimum players before game starts")) {
                gameState = GameState.WAITING;
                event.setQuitMessage(ChatColor.DARK_PURPLE + "Well, " + player.getName()
                        + " left, so there's not enough players to start. Blame them!");
            }
        }
        else if (gameState.equals(GameState.STARTED)) {
            if (playerSpawns.containsKey(player)) {
                playerSpawns.remove(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (playerSpawns.containsKey(player)) {
            event.setRespawnLocation(playerSpawns.get(player));
        }
        else {
            event.setRespawnLocation(getLocationConfig("main"));
        }
    }

    private Location getLocationConfig(String id) {
        ConfigurationSection spawnData = getConfig().getConfigurationSection("spawns." + id);

        World world = Bukkit.getWorld(spawnData.getString("world"));
        double x = spawnData.getDouble("x");
        double y = spawnData.getDouble("y");
        double z = spawnData.getDouble("z");
        float yaw = (float) spawnData.getDouble("yaw");
        float pitch = (float) spawnData.getDouble("pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void start() {
        List<String> spawnIDs = Lists.newArrayList(getConfig().getConfigurationSection("spawns").getKeys(false));
        spawnIDs.remove("main");
        int spawnIDIndex = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            String id = spawnIDs.get(spawnIDIndex);

            Location spawn = getLocationConfig(id);
            player.teleport(spawn);
            playerSpawns.put(player, spawn);

            if (spawnIDIndex < spawnIDs.size()) {
                spawnIDIndex++;
            } else {
                spawnIDIndex = 0;
            }

        }
    }

    public static void broadcastTimeLeft(double time) {
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Game starting in "
                + time + " seconds...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("shiftspawn") || label.equalsIgnoreCase("sp")) {
            if (!sender.hasPermission("shiftspawn.*")) {
                sender.sendMessage(ChatColor.RED + "You sneaky bum! You aren't allowed to use that command!!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Error! You have to be a player to use this command silly!");
                return true;
            }
            Player player = (Player) sender;
            if (args.length == 0) {
                player.sendMessage(ChatColor.GOLD + "Oops! No spawnpoint number specified. Type /shiftspawn <number>");
                return true;
            }
            String id = args[0];
            try {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(id);
            } catch (NumberFormatException e) {
                if (!id.equalsIgnoreCase("main")) {
                    player.sendMessage(ChatColor.RED + "Error! " + ChatColor.DARK_RED + args[0] + ChatColor.RED
                            + " is not a number!");
                    return true;
                }
            }
            Location loc = player.getLocation();
            String worldName = loc.getWorld().getName();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            float pitch = loc.getPitch();
            float yaw = loc.getYaw();

            ConfigurationSection spawnSettings = getConfig().getConfigurationSection("spawns");
            spawnSettings.set(id + ".world", worldName);
            spawnSettings.set(id + ".x", x);
            spawnSettings.set(id + ".y", y);
            spawnSettings.set(id + ".z", z);
            spawnSettings.set(id + ".pitch", pitch);
            spawnSettings.set(id + ".yaw", yaw);
            saveConfig();

            player.sendMessage(ChatColor.GREEN + "Spawn number " + ChatColor.BLUE + id + ChatColor.GREEN + " set in "
                    + ChatColor.BLUE + worldName + ChatColor.GREEN + " at x:" + ChatColor.BLUE + x + ChatColor.GREEN
                    + ", y:" + ChatColor.BLUE + y + ChatColor.GREEN + ", z:" + ChatColor.BLUE + z);
            return true;
        }
        return false;
    }

}
