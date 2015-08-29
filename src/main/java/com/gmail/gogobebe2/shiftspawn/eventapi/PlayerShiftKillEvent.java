package com.gmail.gogobebe2.shiftspawn.eventapi;

import org.bukkit.entity.Player;

public class PlayerShiftKillEvent extends PlayerShiftEvent {
    private Player killer;

    public PlayerShiftKillEvent(Player player, Player killer) {
        super(player);
        this.killer = killer;
    }

    public Player getKiller() {
        return killer;
    }
}