package com.gmail.gogobebe2.shiftspawn.eventapi;

import org.bukkit.entity.Player;

public class PlayerShiftKillEvent extends PlayerShiftEvent {
    public PlayerShiftKillEvent(Player player) {
        super(player);
    }
}