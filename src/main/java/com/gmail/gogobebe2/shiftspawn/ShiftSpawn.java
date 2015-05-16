package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ShiftSpawn extends JavaPlugin implements Listener {
    private GameState gameState = GameState.WAITING;

    @Override
    public void onEnable() {
        getLogger().info("Starting up ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (gameState.equals(GameState.WAITING)) {
            if (Bukkit.getOnlinePlayers().size() >= 6) {
                gameState = GameState.STARTING;
                event.setJoinMessage(ChatColor.DARK_PURPLE + event.getPlayer().getName()
                        + " joined");
            }
            event.setJoinMessage(ChatColor.DARK_PURPLE + event.getPlayer().getName() + " joined. We need "
                    + (6 - Bukkit.getOnlinePlayers().size()) + " more players to start.");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (gameState.equals(GameState.STARTING)) {
            if (Bukkit.getOnlinePlayers().size() < 6) {
                gameState = GameState.WAITING;
                event.setQuitMessage(ChatColor.DARK_PURPLE + "Well, " + event.getPlayer().getName()
                        + " left, so there's not enough players to start. Blame them!");
            }
        }

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
            int num;
            try {
                num = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Error! " + ChatColor.DARK_RED + args[0] + ChatColor.RED
                        + " is not a number!");
                return true;
            }
            Location loc = player.getLocation();
            String worldName = loc.getWorld().getName();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            float pitch = loc.getPitch();
            float yaw = loc.getYaw();

            getConfig().set("spawns." + num + ".world", worldName);
            getConfig().set("spawns." + num + ".x", x);
            getConfig().set("spawns." + num + ".y", y);
            getConfig().set("spawns." + num + ".z", z);
            getConfig().set("spawns." + num + ".pitch", pitch);
            getConfig().set("spawns." + num + ".yaw", yaw);
            saveConfig();

            player.sendMessage(ChatColor.GREEN + "Spawn number " + ChatColor.BLUE + num + ChatColor.GREEN +  " set in "
                    + ChatColor.BLUE + worldName + ChatColor.GREEN + " at x:" + ChatColor.BLUE + x + ChatColor.GREEN
                    + ", y:" + ChatColor.BLUE + y + ChatColor.GREEN + ", z:" + ChatColor.BLUE + z);
            return true;
        }
        return false;
    }
}
