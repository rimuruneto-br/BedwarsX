package dev.bedwarsx.arena;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.generator.ArenaGenerator;
import dev.bedwarsx.generator.GeneratorType;
import dev.bedwarsx.team.BedWarsTeam;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ArenaManager {

    private final BedWarsX plugin;
    private final Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(BedWarsX plugin) {
        this.plugin = plugin;
    }

    public void loadArenas() {
        FileConfiguration config = plugin.getConfigManager().getConfig("arenas");
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            Arena arena = new Arena(id);
            arena.loadFromConfig(section.getConfigurationSection(id));
            arenas.put(id, arena);
            plugin.getLogger().info("Loaded arena: " + id);
        }
    }

    public void saveArenas() {
        FileConfiguration config = plugin.getConfigManager().getConfig("arenas");
        config.set("arenas", null);
        for (Arena arena : arenas.values()) {
            arena.saveToConfig(config.createSection("arenas." + arena.getId()));
        }
        plugin.getConfigManager().saveConfig("arenas");
    }

    public Arena createArena(String id) {
        if (arenas.containsKey(id)) return null;
        Arena arena = new Arena(id);
        arenas.put(id, arena);
        saveArenas();
        return arena;
    }

    public boolean deleteArena(String id) {
        if (!arenas.containsKey(id)) return false;
        arenas.remove(id);
        saveArenas();
        return true;
    }

    public Arena getArena(String id) {
        return arenas.get(id);
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public List<Arena> getAvailableArenas() {
        List<Arena> available = new ArrayList<>();
        for (Arena arena : arenas.values()) {
            if (arena.isSetup() && (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING)) {
                available.add(arena);
            }
        }
        return available;
    }

    public Arena getArenaByPlayer(org.bukkit.entity.Player player) {
        for (Arena arena : arenas.values()) {
            if (plugin.getGameManager().isInGame(player, arena)) {
                return arena;
            }
        }
        return null;
    }

    // Setup helpers
    public boolean setTeamSpawn(String arenaId, BedWarsTeam team, Location loc) {
        Arena arena = arenas.get(arenaId);
        if (arena == null) return false;
        arena.getTeamSpawns().put(team, loc);
        if (!arena.getEnabledTeams().contains(team)) {
            arena.getEnabledTeams().add(team);
        }
        saveArenas();
        return true;
    }

    public boolean setTeamBed(String arenaId, BedWarsTeam team, Location loc) {
        Arena arena = arenas.get(arenaId);
        if (arena == null) return false;
        arena.getTeamBeds().put(team, loc);
        if (!arena.getEnabledTeams().contains(team)) {
            arena.getEnabledTeams().add(team);
        }
        saveArenas();
        return true;
    }

    public boolean addGenerator(String arenaId, String type, Location loc) {
        Arena arena = arenas.get(arenaId);
        if (arena == null) return false;
        GeneratorType generatorType = GeneratorType.fromString(type);
        switch (type.toLowerCase()) {
            case "iron": arena.getIronGenerators().add(loc); break;
            case "gold": arena.getGoldGenerators().add(loc); break;
            case "diamond": arena.getDiamondGenerators().add(loc); break;
            case "emerald": arena.getEmeraldGenerators().add(loc); break;
            default: return false;
        }
        if (generatorType != null) {
            arena.getGenerators().add(new ArenaGenerator(generatorType, loc, null));
        }
        saveArenas();
        return true;
    }

    public boolean setTeamGenerator(String arenaId, BedWarsTeam team, Location loc) {
        Arena arena = arenas.get(arenaId);
        if (arena == null || team == null) return false;
        arena.getTeamGenerators().put(team, loc);
        if (!arena.getEnabledTeams().contains(team)) {
            arena.getEnabledTeams().add(team);
        }
        saveArenas();
        return true;
    }

    public boolean setTeamShop(String arenaId, BedWarsTeam team, Location loc) {
        Arena arena = arenas.get(arenaId);
        if (arena == null || team == null) return false;
        arena.getTeamShopLocations().put(team, loc);
        if (!arena.getEnabledTeams().contains(team)) {
            arena.getEnabledTeams().add(team);
        }
        saveArenas();
        return true;
    }

    public boolean setTeamUpgradeShop(String arenaId, BedWarsTeam team, Location loc) {
        Arena arena = arenas.get(arenaId);
        if (arena == null || team == null) return false;
        arena.getTeamUpgradeShopLocations().put(team, loc);
        if (!arena.getEnabledTeams().contains(team)) {
            arena.getEnabledTeams().add(team);
        }
        saveArenas();
        return true;
    }

    public boolean isArenaComplete(Arena arena) {
        if (arena.getLobby() == null) return false;
        if (arena.getActiveTeams().size() < 2) return false;
        for (BedWarsTeam team : arena.getActiveTeams()) {
            if (!arena.getTeamSpawns().containsKey(team)) return false;
            if (!arena.getTeamBeds().containsKey(team)) return false;
        }
        return true;
    }
}
