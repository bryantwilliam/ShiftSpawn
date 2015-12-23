package com.gmail.gogobebe2.shiftspawn.api;

import com.gmail.gogobebe2.shiftspawn.GameState;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;

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
}
