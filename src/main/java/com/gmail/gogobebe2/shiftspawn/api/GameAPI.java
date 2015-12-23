package com.gmail.gogobebe2.shiftspawn.api;

import com.gmail.gogobebe2.shiftspawn.GameState;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;

public class GameAPI {
    public static GameState getGameState() {
        return ShiftSpawn.getInstance().getGame().getGameState();
    }

    public static String getFormattedTime() {
        return ShiftSpawn.getInstance().getGame().getTime();
    }

    public static int getSeconds() {
        return ShiftSpawn.getInstance().getGame().getSeconds();
    }
}
