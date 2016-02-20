package com.gmail.gogobebe2.shiftspawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tutorial {
    private static final ItemStack TUTORIAL_BUTTON;
    private static Set<Tutorial> tutorialSet = new HashSet<>();

    private boolean hasBeenGivenTutorialButton = false;
    private Participant participant;

    private enum TutorialStage {
        SELECT_KIT((short) 1),
        MINING((short) 2),
        PLATFORMS((short) 3),
        ALPHA_CORE((short) 4),
        WIN((short) 5),
        ENCHANTMENT_TABLE((short) 6),
        VILLAGER((short) 7),
        END((short) 8);

        private short stageNumber;

        TutorialStage(short stageNumber) {
            this.stageNumber = stageNumber;
        }

        public int getStageNumber() {
            return this.stageNumber;
        }

        public GameState getByStageNumber(short i) {
            for (GameState gs : GameState.values()) if (gs.getCode() == i) return gs;
            return null;
        }
    }

    static {
        TUTORIAL_BUTTON = new ItemStack(Material.SKULL_ITEM, 1);

        SkullMeta skullMeta = (SkullMeta) TUTORIAL_BUTTON.getItemMeta();
        skullMeta.setOwner("MHF_Question");
        skullMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Tutorial");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "Left click to enter the tutorial, right click to leave it.");

        skullMeta.setLore(lore);
        TUTORIAL_BUTTON.setItemMeta(skullMeta);
    }

    private static Tutorial getTutorial(Player player) {
        for (Tutorial tutorial : tutorialSet) {
            if (tutorial.participant.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return tutorial;
            }
        }
        return null;
    }

    private static void addNewTutorialInstance(Participant participant) {
        tutorialSet.add(new Tutorial(participant));
    }

    protected static void setUpTutorials() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            private void onPlayerInteract(PlayerInteractEvent event) {
                ItemStack itemStack = event.getItem().clone();
                itemStack.setAmount(1);

                if (itemStack.equals(TUTORIAL_BUTTON)) {
                    Action action = event.getAction();
                    if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
                        // Enter.
                    } else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                        // Leave tutorial.
                    }
                }
            }

            @EventHandler
            private void onPlayerJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();
                Tutorial tutorial = getTutorial(player);
                if (tutorial != null && !tutorial.hasBeenGivenTutorialButton) tutorial.giveTutorialButton();
                else addNewTutorialInstance(ShiftSpawn.getInstance().getParticipant(player));
            }
        }, ShiftSpawn.getInstance());

        for (Participant participant : ShiftSpawn.getInstance().getParticipants()) {
            addNewTutorialInstance(participant);
        }
    }

    private Tutorial(Participant participant) {
        this.participant = participant;
        if (participant.getPlayer().isOnline()) giveTutorialButton();
    }

    private void giveTutorialButton() {
        participant.getPlayer().getInventory().addItem(TUTORIAL_BUTTON);
        hasBeenGivenTutorialButton = true;
    }

}