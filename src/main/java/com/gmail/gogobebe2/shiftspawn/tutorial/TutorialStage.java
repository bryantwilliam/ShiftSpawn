package com.gmail.gogobebe2.shiftspawn.tutorial;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

enum TutorialStage {
    NOT_IN_TUTORIAL((short) 0,
            "That's it! View the full tutorial at: ",
            ChatColor.UNDERLINE + "www.xpcraft.com/threads/shift-tutorial.16/",
            new PlayerRunnable() {
        @Override
        public void run(Player player) {
            // Kill the player so they respawn at the correct players with the onDeathEvent in Listeners class:
            player.setHealth(0);
        }
    }),

    SELECT_KIT((short) 1,
            "Select a kit!",
            "Right click the emerald to buy/choose a kit!",
            new PlayerRunnable() {
        @Override
        public void run(Player player) {

        }
    }),

    MINING((short) 2,
            "Mine Logs, Ores, and Podzol!",
            "Logs give sticks. Ores give ingots.",
            new PlayerRunnable() {
        @Override
        public void run(Player player) {

        }
    }),

    PLATFORMS((short) 3,
            "Use the platforms to move around!",
            "You can't break or place blocks.",
            new PlayerRunnable() {
        @Override
        public void run(Player player) {

        }
    }),

    ALPHA_CORE((short) 4,
            "Make your way to the middle!",
            "Mine the quartz repeatedly to score points!",
            new PlayerRunnable() {
        @Override
        public void run(Player player) {

        }
    }),

    WIN((short) 5,
            "Win the Game!",
            "The person with the most points when time runs out wins!",
            new PlayerRunnable() {
        @Override
        public void run(Player player) {

        }
    }),

    ENCHANTMENT_TABLE((short) 6,
            "Enchant tools & weapons with /ench!",
            "Enchantments cost gold ingots!",
            new PlayerRunnable() {
        @Override
        public void run(Player player) {

        }
    }),

    VILLAGER((short) 7,
            "Kill players to get kits!",
            "The only way to get potions & EPIC weapons!",
            new PlayerRunnable() {
        @Override
        public void run(Player player) {

        }
    });

    private short stageNumber;
    private PlayerRunnable runnable;
    private String title;
    private String subTitle;

    TutorialStage(short stageNumber, String title, String subTitle, PlayerRunnable runnable) {
        this.stageNumber = stageNumber;
        this.runnable = runnable;
        this.title = title;
        this.subTitle = subTitle;
    }

    protected short getStageNumber() {
        return this.stageNumber;
    }

    protected String getTitle() {
        return this.title;
    }

    protected String getSubTitle() {
        return this.subTitle;
    }

    protected void doStage(Player player) {
        this.runnable.run(player);
        player.sendMessage(ChatColor.GREEN + title);
        player.sendMessage(ChatColor.YELLOW + subTitle);
    }

    protected static TutorialStage getByStageNumber(short i) {
        for (TutorialStage gs : TutorialStage.values()) if (gs.getStageNumber() == i) return gs;
        return null;
    }
}
