package com.gmail.gogobebe2.shiftspawn.tutorial;

import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

enum TutorialStage {
    NOT_IN_TUTORIAL((short) 0,
            "That's it! View the full tutorial at: ",
            ChatColor.UNDERLINE + "www.xpcraft.com/threads/shift-tutorial.16/",
            new PlayerRunnable() {
                @Override
                public void run(Player player) {
                    teleportPlayer("main", player);
                    player.setGameMode(GameMode.SURVIVAL);
                }
            }),

    SELECT_KIT((short) 1,
            "Select a kit!",
            "Right click the emerald to buy/choose a kit!",
            new PlayerRunnable() {
                @Override
                public void run(Player player) {
                    player.setGameMode(GameMode.ADVENTURE);
                    teleportPlayer(MAIN_SPAWN_POINT_ID, player);
                }
            }),

    MINING((short) 2,
            "Mine Logs, Ores, and Podzol!",
            "Logs give sticks. Ores give ingots.",
            new PlayerRunnable() {
                @Override
                public void run(Player player) {
                    teleportPlayer(ISLAND_SPAWN_POINT_ID, player);
                }
            }),

    PLATFORMS((short) 3,
            "Use the platforms to move around!",
            "You can't break or place blocks.",
            new PlayerRunnable() {
                @Override
                public void run(Player player) {
                    teleportPlayer(STATIC_PLATFORM_ID, player);
                }
            }),

    ALPHA_CORE((short) 4,
            "Make your way to the middle!",
            "Mine the quartz repeatedly to score points!",
            new PlayerRunnable() {
                @Override
                public void run(Player player) {
                    teleportPlayer(ALPHA_CORE_ID, player);
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
                    teleportPlayer(ENCHANTMENT_TABLE_ID, player);
                }
            }),

    VILLAGER((short) 7,
            "Kill players to get kits!",
            "The only way to get potions & EPIC weapons!",
            new PlayerRunnable() {
                @Override
                public void run(Player player) {
                    player.getWorld().spawnEntity(ShiftSpawn.getInstance().loadSpawn(VILLAGER_SPAWN_POINT_ID), EntityType.VILLAGER);
                    player.getNearbyEntities(30, 30, 30);
                }
            });

    static final String MAIN_SPAWN_POINT_ID = "tutorial_mainSpawnPoint";
    static final String ISLAND_SPAWN_POINT_ID = "tutorial_islandSpawnPoint";
    static final String STATIC_PLATFORM_ID = "tutorial_staticPlatform";
    static final String ALPHA_CORE_ID = "tutorial_alphacore";
    static final String ENCHANTMENT_TABLE_ID = "tutorial_enchantmentTable";
    static final String VILLAGER_SPAWN_POINT_ID = "tutorial_villagerSpawnPoint";

    protected static TutorialStage getByStageNumber(short i) {
        for (TutorialStage gs : TutorialStage.values()) if (gs.getStageNumber() == i) return gs;
        return null;
    }

    private static void teleportPlayer(String id, Player player) {
        player.teleport(ShiftSpawn.getInstance().loadSpawn(id));
    }

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

    protected void doStage(Player player) {
        this.runnable.run(player);
        player.sendMessage(ChatColor.DARK_GREEN + "[Shift Tutorial, stage " + getStageNumber() + "] " + ChatColor.GREEN + title);
        player.sendMessage(ChatColor.YELLOW + subTitle);
    }
}
