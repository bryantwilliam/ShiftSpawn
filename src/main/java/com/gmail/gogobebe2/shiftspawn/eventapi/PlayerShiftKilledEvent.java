package com.gmail.gogobebe2.shiftspawn.eventapi;

import org.bukkit.entity.Player;

public class PlayerShiftKilledEvent extends PlayerShiftEvent {
    private Player killer;

    public PlayerShiftKilledEvent(Player player, Player killer) {
        super(player);
        this.killer = killer;
    }

    public Player getKiller() {
        return killer;
    }
}