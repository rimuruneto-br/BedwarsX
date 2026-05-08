package dev.bedwarsx.main;

import dev.bedwarsx.arena.ArenaManager;
import dev.bedwarsx.command.BwAdmCommand;
import dev.bedwarsx.command.BwCommand;
import dev.bedwarsx.config.ConfigManager;
import dev.bedwarsx.database.DatabaseManager;
import dev.bedwarsx.game.GameManager;
import dev.bedwarsx.gui.ChatInputManager;
import dev.bedwarsx.gui.GuiManager;
import dev.bedwarsx.item.experimental.ExperimentalItemManager;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.listener.*;
import dev.bedwarsx.mission.MissionManager;
import dev.bedwarsx.npc.NPCManager;
import dev.bedwarsx.player.PlayerSessionManager;
import dev.bedwarsx.scoreboard.ScoreboardManager;
import dev.bedwarsx.shop.QuickBuyManager;
import dev.bedwarsx.stats.ProgressionManager;
import dev.bedwarsx.world.WorldInstanceManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BedWarsX extends JavaPlugin {

    private static BedWarsX instance;
    private ConfigManager     configManager;
    private ArenaManager      arenaManager;
    private GameManager       gameManager;
    private NPCManager        npcManager;
    private ScoreboardManager scoreboardManager;
    private DatabaseManager   databaseManager;
    private GuiManager        guiManager;
    private ChatInputManager  chatInputManager;
    private PlayerSessionManager playerSessionManager;
    private WorldInstanceManager worldInstanceManager;
    private ExperimentalItemManager experimentalItemManager;
    private ProgressionManager progressionManager;
    private MissionManager missionManager;
    private QuickBuyManager quickBuyManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Lang.init(this);

        configManager     = new ConfigManager(this);
        databaseManager   = new DatabaseManager(this);
        playerSessionManager = new PlayerSessionManager();
        worldInstanceManager = new WorldInstanceManager(this);
        worldInstanceManager.initialize();
        progressionManager = new ProgressionManager(this);
        missionManager = new MissionManager(this);
        quickBuyManager = new QuickBuyManager(this);
        arenaManager      = new ArenaManager(this);
        gameManager       = new GameManager(this);
        experimentalItemManager = new ExperimentalItemManager(this);
        npcManager        = new NPCManager(this);
        scoreboardManager = new ScoreboardManager(this);
        guiManager        = new GuiManager(this);
        chatInputManager  = new ChatInputManager(this);

        getCommand("bw").setExecutor(new BwCommand(this));
        getCommand("bwadm").setExecutor(new BwAdmCommand(this));

        registerListeners();
        scoreboardManager.startTask();
        arenaManager.loadArenas();
        npcManager.spawnLobbyNPCs();

        getLogger().info("BedWarsX v" + getDescription().getVersion() + " ativado!");
        getLogger().info("Arenas: " + arenaManager.getArenas().size() + " | DB: "
                + (databaseManager.isConnected() ? "OK" : "ERRO"));
    }

    @Override
    public void onDisable() {
        if (gameManager       != null) gameManager.shutdownAllGames();
        if (worldInstanceManager != null) worldInstanceManager.cleanupOrphanMatchWorlds();
        if (arenaManager      != null) arenaManager.saveArenas();
        if (progressionManager != null) progressionManager.save();
        if (npcManager        != null) npcManager.removeAllNPCs();
        if (databaseManager   != null) databaseManager.close();
        if (scoreboardManager != null) scoreboardManager.stopTask();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(guiManager,                      this);
        Bukkit.getPluginManager().registerEvents(chatInputManager,                this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this),    this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this),    this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this),   this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this),    this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(this),    this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(this),  this);
        Bukkit.getPluginManager().registerEvents(new FoodLevelListener(this),     this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this),this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this),this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this),    this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this),          this);
        Bukkit.getPluginManager().registerEvents(new NPCListener(this),           this);
        Bukkit.getPluginManager().registerEvents(new ItemDropListener(this),      this);
        Bukkit.getPluginManager().registerEvents(new ProjectileListener(this),    this);
        Bukkit.getPluginManager().registerEvents(new WeatherListener(this),       this);
    }

    public static BedWarsX getInstance()            { return instance; }
    public ConfigManager     getConfigManager()      { return configManager; }
    public ArenaManager      getArenaManager()       { return arenaManager; }
    public GameManager       getGameManager()        { return gameManager; }
    public NPCManager        getNpcManager()         { return npcManager; }
    public ScoreboardManager getScoreboardManager()  { return scoreboardManager; }
    public DatabaseManager   getDatabaseManager()    { return databaseManager; }
    public GuiManager        getGuiManager()         { return guiManager; }
    public ChatInputManager  getChatInputManager()   { return chatInputManager; }
    public PlayerSessionManager getPlayerSessionManager() { return playerSessionManager; }
    public WorldInstanceManager getWorldInstanceManager() { return worldInstanceManager; }
    public ExperimentalItemManager getExperimentalItemManager() { return experimentalItemManager; }
    public ProgressionManager getProgressionManager() { return progressionManager; }
    public MissionManager getMissionManager() { return missionManager; }
    public QuickBuyManager getQuickBuyManager() { return quickBuyManager; }
}
