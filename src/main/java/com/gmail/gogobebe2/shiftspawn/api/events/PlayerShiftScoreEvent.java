package com.gmail.gogobebe2.shiftspawn.api.events;

import org.bukkit.entity.Player;

public class PlayerShiftScoreEvent extends PlayerShiftEvent {
    public PlayerShiftScoreEvent(Player player) {
        super(player);
    }
}