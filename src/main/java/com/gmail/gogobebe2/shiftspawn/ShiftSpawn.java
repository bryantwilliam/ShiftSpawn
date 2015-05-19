package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class ShiftSpawn extends JavaPlugin implements Listener {
    private GameState gameState = GameState.WAITING;
    Map<Player, Location> playerSpawns = new HashMap<>();
    Timer timer;
    BukkitTask task;

    @Override
    public void onEnable() {
        getLogger().info("Starting up ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        if (!getConfig().isSet("spawns.main.world")) {
            for (int i = 0; i < 10; i++) {
                getLogger().severe("No main spawn set, to set it, type /shiftspawn main");
            }
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        double time = getConfig().getDouble("time before games starts (in minutes)");
        timer = new Timer(this, time);
        task = timer.runTaskTimer(this, 0, 20);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFall(PlayerMoveEvent event) {
        if (event.getTo().getY() < 0) {
            event.setCancelled(true);
            event.getPlayer().setHealth(0);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.teleport(getLocationConfig("main"));
        event.setJoinMessage(ChatColor.DARK_PURPLE + player.getName() + " left the game.");
        if (gameState.equals(GameState.WAITING)) {
            double time = getConfig().getDouble("time before games starts (in minutes)");
            int minPlayers = getConfig().getInt("minimum players before game starts");
            if (Bukkit.getOnlinePlayers().size() >= minPlayers) {
                gameState = GameState.STARTING;
            }
            else {
                event.setJoinMessage(ChatColor.DARK_PURPLE + event.getPlayer().getName() + " joined. We need "
                        + (minPlayers - Bukkit.getOnlinePlayers().size()) + " more players to start.");
            }
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
        } else if (playerSpawns.containsKey(player)) {
            playerSpawns.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (playerSpawns.containsKey(player)) {
            spawn(player, playerSpawns.get(player));
        } else {
            event.setRespawnLocation(getLocationConfig("main"));
        }
    }

    public void spawn(Player player, Location spawn) {
        player.teleport(spawn);
        Inventory inventory = player.getInventory();
        inventory.addItem(new ItemStack(Material.WOOD_PICKAXE, 1));
        inventory.addItem(new ItemStack(Material.WOOD_SWORD, 1));
        player.updateInventory();
    }

    public Location getLocationConfig(String id) {
        ConfigurationSection spawnData = getConfig().getConfigurationSection("spawns." + id);

        World world = Bukkit.getWorld(spawnData.getString("world"));
        double x = spawnData.getDouble("x");
        double y = spawnData.getDouble("y");
        double z = spawnData.getDouble("z");
        float yaw = (float) spawnData.getDouble("yaw");
        float pitch = (float) spawnData.getDouble("pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("shiftspawn")) {
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

            getConfig().set("spawns." + id + ".world", worldName);
            getConfig().set("spawns." + id + ".x", x);
            getConfig().set("spawns." + id + ".y", y);
            getConfig().set("spawns." + id + ".z", z);
            getConfig().set("spawns." + id + ".pitch", pitch);
            getConfig().set("spawns." + id + ".yaw", yaw);
            saveConfig();

            player.sendMessage(ChatColor.GREEN + "Spawn number " + ChatColor.BLUE + id + ChatColor.GREEN + " set in "
                    + ChatColor.BLUE + worldName + ChatColor.GREEN + " at x:" + ChatColor.BLUE + x + ChatColor.GREEN
                    + ", y:" + ChatColor.BLUE + y + ChatColor.GREEN + ", z:" + ChatColor.BLUE + z);
            return true;
        }
        return false;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Map<Player, Location> getPlayerSpawns() {
        return playerSpawns;
    }
}
