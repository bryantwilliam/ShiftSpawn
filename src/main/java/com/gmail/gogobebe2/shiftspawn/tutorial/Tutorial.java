package com.gmail.gogobebe2.shiftspawn.tutorial;

import com.gmail.gogobebe2.shiftspawn.Participant;
import com.gmail.gogobebe2.shiftspawn.ShiftSpawn;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tutorial {
    private static final ItemStack TUTORIAL_BUTTON;
    private static Set<Tutorial> tutorialSet = new HashSet<>();
    private static BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    private boolean hasBeenGivenTutorialButton = false;
    private Participant participant;
    private TutorialStage stage;
    private int taskID = -1;

    static {
        TUTORIAL_BUTTON = new ItemStack(Material.SKULL_ITEM, 1);

        SkullMeta skullMeta = (SkullMeta) TUTORIAL_BUTTON.getItemMeta();
        skullMeta.setOwner("MHF_Question");
        skullMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Tutorial");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "Left click to enter the tutorial or to skip a stage in the tutorial; Right click to leave it.");

        skullMeta.setLore(lore);
        TUTORIAL_BUTTON.setItemMeta(skullMeta);
    }

    private static Tutorial getTutorial(Player player) {
        for (Tutorial tutorial : tutorialSet) {
            if (tutorial.participant.getPlayer().getUniqueId().equals(player.getUniqueId())) return tutorial;
        }
        return null;
    }

    private static void addNewTutorialInstance(Participant participant) {
        tutorialSet.add(new Tutorial(participant));
    }

    private static void newTutorial(Player player) {
        Tutorial tutorial = getTutorial(player);
        if (tutorial != null && !tutorial.hasBeenGivenTutorialButton) tutorial.giveTutorialButton();
        else addNewTutorialInstance(ShiftSpawn.getInstance().getParticipant(player));
    }

    public static void setUpTutorials() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            newTutorial(player);
        }
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            private void onPlayerInteract(PlayerInteractEvent event) {
                if (event.getItem() != null) {
                    ItemStack itemStack = event.getItem().clone();
                    itemStack.setAmount(1);

                    if (itemStack.getItemMeta().equals(TUTORIAL_BUTTON.getItemMeta())) {
                        final Player player = event.getPlayer();
                        final Tutorial tutorial = getTutorial(event.getPlayer());
                        if (tutorial != null) {
                            Action action = event.getAction();
                            if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "Going to next stage in tutorial...");
                                final long INTERVAL = 20 * 5;
                                if (tutorial.taskID != -1) scheduler.cancelTask(tutorial.taskID);
                                tutorial.goToNextStage(player);
                                tutorial.taskID = scheduler.scheduleSyncRepeatingTask(ShiftSpawn.getInstance(), new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        tutorial.goToNextStage(player);
                                    }
                                }, INTERVAL, INTERVAL);
                            } else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "Exiting tutorial...");
                                scheduler.cancelTask(tutorial.taskID);
                                tutorial.stage = TutorialStage.NOT_IN_TUTORIAL;
                                tutorial.stage.doStage(player);
                            }
                        } else {
                            newTutorial(player);
                            player.sendMessage(ChatColor.RED + "An error occured while trying to enter the tutorial, please try again.");
                            Bukkit.getLogger().severe("Error! Check Tutorial.java for the reason why.");
                        }
                    }
                }
            }

            @EventHandler
            private void onPlayerJoin(PlayerJoinEvent event) {
                newTutorial(event.getPlayer());
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

    private void goToNextStage(Player player) {
        stage = TutorialStage.getByStageNumber((short) (stage.getStageNumber() + 1));
        if (stage == null) stage = TutorialStage.SELECT_KIT;
        stage.doStage(player);
    }
}
