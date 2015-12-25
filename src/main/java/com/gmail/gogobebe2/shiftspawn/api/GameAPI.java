package com.gmail.gogobebe2.shiftspawn.api;

import com.gmail.gogobebe2.shiftspawn.GameState;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("unused") // It's an API so is not locally used.
public final class GameAPI {
    public static GameState getGameState() {
        return ShiftSpawn.getInstance().getGame().getGameState();
    }

    /**
     * @return formatted time like "0:00".
     */
    public static String getFormattedTime() {
        return ShiftSpawn.getInstance().getGame().getTime();
    }

    public static int getSeconds() {
        return ShiftSpawn.getInstance().getGame().getSeconds();
    }

    public static int getMinutes() {
        return ShiftSpawn.getInstance().getGame().getMinutes();
    }

    public static UUID getMapUUID() {
        ShiftSpawn plugin = ShiftSpawn.getInstance();
        World world = null;
        for (String idStr : plugin.getConfig().getConfigurationSection("Spawns").getKeys(false)) {
            try {
                world = Bukkit.getWorld(plugin.getConfig().getString("Spawns." + Integer.parseInt(idStr) + ".World"));
                break;
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        assert world != null;
        return world.getUID();
    }

    public static int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    public static Collection<? extends Player> getPlayers() {
        return Bukkit.getOnlinePlayers();
    }
}
