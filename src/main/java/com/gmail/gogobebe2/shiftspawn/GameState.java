package com.gmail.gogobebe2.shiftspawn;

public enum GameState {
    WAITING(0),
    STARTING(1),
    STARTED(2),
    RESTARTING(3),
    ERROR(-1);

    private int code;

    GameState(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public GameState getByCode(int i) {
        for (GameState gs : GameState.values()) if (gs.getCode() == i) return gs;
        return null;
    }
}
