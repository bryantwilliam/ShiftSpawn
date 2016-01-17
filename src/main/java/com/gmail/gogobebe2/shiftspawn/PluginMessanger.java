package com.gmail.gogobebe2.shiftspawn;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class PluginMessanger {
    private static final String CHANNEL_NAME = "XPCraftHub";

    private static PluginMessanger INSTANCE = new PluginMessanger();

    private PluginMessanger() {
    }

    protected static PluginMessanger getPluginMessanger() {
        return INSTANCE;
    }

    protected void sendAll() {
        sendMaxPlayers();
        sendPlayers();
        sendMapUUID();
        sendMinutes();
        sendSeconds();
        sendFormattedTime();
        sendGameState();
    }

    private void sendMaxPlayers() {

    }

    private void sendPlayers() {

    }

    private void sendMapUUID() {

    }

    private void sendMinutes() {

    }

    private void sendSeconds() {

    }

    private void sendFormattedTime() {

    }

    private void sendGameState() {

    }

    private void send(String subChannel, String... arguments) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

    }
}
