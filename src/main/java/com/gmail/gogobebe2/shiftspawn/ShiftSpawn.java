package com.gmail.gogobebe2.shiftspawn;

import com.gmail.gogobebe2.shiftspawn.scoreboard.OnlinePlayerSection;
import com.gmail.gogobebe2.shiftspawn.scoreboard.ScoreTagSection;
import com.gmail.gogobebe2.shiftspawn.scoreboard.StatusSection;
import com.gmail.gogobebe2.shiftspawn.scoreboard.TopScoresSection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;

public class ShiftSpawn extends JavaPlugin {
    protected static ShiftSpawn instance;
    private Game game;
    private String spawnID = "-1";
    private List<Participant> participants = new ArrayList<>();
    private ArrayList<Block> alphaCores = new ArrayList<>();
    private TopScoresSection topScoresSection;
    private StatusSection statusSection;
    private OnlinePlayerSection onlinePlayerSection;
    private ScoreTagSection scoreTagSection;

    // Settings from config.yml:
    protected final static String MIN_PLAYERS_KEY = "Minimum players before game starts";
    protected final static String TIME_BEFORE_START_KEY = "Time before games starts";
    protected final static String GAME_TIME = "Game time";
    protected final static String DEATH_MESSAGES = "Death messages";
    protected final static String ALPHA_CORE_ID = "Alpha Core block ID";
    /*
    * GAME STATUS UPDATER
     */

    public static JedisPool jedisPool;

    public static ShiftSpawn getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("shiftspawn")) {
            if (!sender.hasPermission("shiftspawn.*")) {
                sender.sendMessage(ChatColor.RED + "You sneaky kid. You aren't allowed to use this command silly!");
                return true;
            } else if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Now how the hell would that work? Idiot, you have to be a player to use this command!!");
                return true;
            }
            Player player = (Player) sender;
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Enter a spawn id /shiftspawn <'main':number>");
                return true;
            }
            String id = args[0].toLowerCase();
            if (!id.equalsIgnoreCase("main")) {
                try {
                    if (Integer.parseInt(args[0]) < 0) {
                        throw new IllegalArgumentException();
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.DARK_RED + args[0] + ChatColor.RED + " is not an appropriate number or is not main!");
                    return true;
                }
            }
            final Location LOCATION = player.getLocation();
            final String WORLD = LOCATION.getWorld().getName();
            final double X = LOCATION.getX();
            final double Y = LOCATION.getY();
            final double Z = LOCATION.getZ();
            final float PITCH = LOCATION.getPitch();
            final float YAW = LOCATION.getYaw();

            getConfig().set("Spawns." + id + ".World", WORLD);
            getConfig().set("Spawns." + id + ".X", X);
            getConfig().set("Spawns." + id + ".Y", Y);
            getConfig().set("Spawns." + id + ".Z", Z);
            getConfig().set("Spawns." + id + ".Pitch", PITCH);
            getConfig().set("Spawns." + id + ".Yaw", YAW);
            saveConfig();
            player.sendMessage(ChatColor.GREEN + "Set " + (id.equalsIgnoreCase("main") ? "the main spawn." : "the spawn id " + id));
            return true;
        }
        return false;
    }

    private boolean hasSpecialItem(PlayerInventory inventory, ItemStack itemStack) {
        Material material = itemStack.getType();
        if (inventory.contains(material)) {
            for (ItemStack item : inventory.all(material).values()) {
                if (item != null && item.getType() != Material.AIR && item.getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void spawn(final Player PLAYER) {
        String id;
        PLAYER.setFireTicks(0);
        PLAYER.setHealth(20);
        PlayerInventory inventory = PLAYER.getInventory();
        if (game.getGameState().equals(GameState.STARTED)) {
            id = getParticipant(PLAYER).getSpawnID();

            ItemStack pickaxe = new ItemStack(Material.WOOD_PICKAXE, 1);
            ItemMeta pickaxeMeta = pickaxe.getItemMeta();
            pickaxeMeta.setDisplayName(ChatColor.AQUA + "Chipped pickaxe");
            pickaxe.setDurability((short) 58);
            pickaxe.setItemMeta(pickaxeMeta);

            if (!hasSpecialItem(inventory, pickaxe)) {
                inventory.addItem(pickaxe);
            }
        } else {
            id = "main";
        }
        PLAYER.setGameMode(GameMode.CREATIVE);
        PLAYER.teleport(loadSpawn(id));
        PLAYER.setGameMode(GameMode.SURVIVAL);
        PLAYER.updateInventory();
    }

    protected Location loadSpawn(String id) {
        final World WORLD = Bukkit.getWorld(getConfig().getString("Spawns." + id + ".World"));
        final double X = getConfig().getDouble("Spawns." + id + ".X");
        final double Y = getConfig().getDouble("Spawns." + id + ".Y");
        final double Z = getConfig().getDouble("Spawns." + id + ".Z");
        final float PITCH = (float) getConfig().getDouble("Spawns." + id + ".Pitch");
        final float YAW = (float) getConfig().getDouble("Spawns." + id + ".Yaw");
        return new Location(WORLD, X, Y, Z, YAW, PITCH);
    }

    protected String getNextSpawnID() {
        List<String> ids = new ArrayList<>();
        ids.addAll(getConfig().getConfigurationSection("Spawns").getKeys(false));
        if (ids.contains("main")) {
            ids.remove("main");
        }
        if (this.spawnID.equals("-1")) {
            this.spawnID = ids.get(new Random().nextInt(ids.size()));
        }
        else {
            Collections.sort(ids);
            Iterator<String> iterator = ids.listIterator(ids.indexOf(this.spawnID) + 1);
            if (iterator.hasNext()) {
                this.spawnID = iterator.next();
            } else {
                this.spawnID = ids.get(0);
            }
        }
        return this.spawnID;
    }

    protected Objective getObjective(Scoreboard scoreboard, String name) {
        if (!scoreboard.getObjectives().isEmpty()) {
            for (Objective objective : scoreboard.getObjectives()) {
                if (objective.getName().equals(name)) {
                    return scoreboard.getObjective(name);
                }
            }
        }
        return scoreboard.registerNewObjective(name, "dummy");
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Starting up ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
        /*
        * INITIALIZE THE JEDIS POOL FOR THREAD-SAFE POOLING.
         */
        jedisPool = new JedisPool(new JedisPoolConfig(), getConfig().getString("redis.host"), getConfig().getInt("redis.port"), 10*1000, getConfig().getString("redis.auth"));
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            // TODO Should probably use send to hub instead...
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer(ChatColor.AQUA + "You have been kicked while the game gets setup.");
            }
        }
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    world.setTime(6000);
                }
            }
        }, 0L, 1000L);
        this.game = new Game(this, GameState.WAITING, Integer.MAX_VALUE + ":00");
        game.startTimer();
        Objective sideObjective = getObjective(Game.getScoreboard(), "side_obj");
        sideObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        sideObjective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Shift");
        this.topScoresSection = new TopScoresSection(Game.getScoreboard(), sideObjective, this);
        this.statusSection = new StatusSection(Game.getScoreboard(), sideObjective, this);
        this.onlinePlayerSection = new OnlinePlayerSection(Game.getScoreboard(), sideObjective, this);

        Objective nameObjective = getObjective(Game.getScoreboard(), "name_obj");
        nameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        nameObjective.setDisplayName(ChatColor.DARK_GREEN + "Points");
        this.scoreTagSection = new ScoreTagSection(Game.getScoreboard(), nameObjective, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ShiftSpawn. If you need me to update this plugin, email at gogobebe2@gmail.com");
        jedisPool.close();
    }

    public Game getGame() {
        return this.game;
    }

    public List<Participant> getParticipants() {
        return this.participants;
    }

    protected boolean hasParticipantSet(Player player) {
        try {
            getParticipant(player);
            return true;
        }
        catch (NullPointerException exc) {
            return false;
        }
    }

    public Participant getParticipant(Player player) throws NullPointerException {
        for (Participant participant : participants) {
            if (participant.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return participant;
            }
        }
        throw new NullPointerException(player.getName() + " has not had his participant set yet");
    }

    protected ArrayList<Block> getAlphaCores() {
        return alphaCores;
    }

    protected TopScoresSection getTopScoresSection() {
        return topScoresSection;
    }

    protected StatusSection getStatusSection() {
        return statusSection;
    }

    protected OnlinePlayerSection getOnlinePlayerSection() {
        return onlinePlayerSection;
    }

    protected ScoreTagSection getScoreTagSection() {
        return scoreTagSection;
    }
}
