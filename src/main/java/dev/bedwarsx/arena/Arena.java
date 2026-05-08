package dev.bedwarsx.arena;

import dev.bedwarsx.generator.ArenaGenerator;
import dev.bedwarsx.generator.GeneratorType;
import dev.bedwarsx.team.BedWarsTeam;
import dev.bedwarsx.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class Arena {

    private final String id;
    private String displayName;
    private ArenaMode mode = ArenaMode.SOLO;
    private ArenaState state = ArenaState.WAITING;
    private int maxPlayers;
    private int minPlayers;
    private int maxPlayersPerTeam;
    private Location lobby;
    private Location spectatorSpawn;
    private Location center;
    private Location deathmatchCenter;
    private Location boundsPos1;
    private Location boundsPos2;
    private final Map<BedWarsTeam, Location> teamSpawns = new HashMap<>();
    private final Map<BedWarsTeam, Location> teamBeds = new HashMap<>();
    private final Map<BedWarsTeam, Location> teamGenerators = new HashMap<>();
    private final Map<BedWarsTeam, Location> teamShopLocations = new HashMap<>();
    private final Map<BedWarsTeam, Location> teamUpgradeShopLocations = new HashMap<>();
    private final Map<BedWarsTeam, Location> teamIndicators = new HashMap<>();
    private final Map<BedWarsTeam, Location> teamCenters = new HashMap<>();
    private final Map<BedWarsTeam, Integer> teamIslandRadius = new HashMap<>();
    private final List<BedWarsTeam> enabledTeams = new ArrayList<>();
    private final List<Location> ironGenerators = new ArrayList<>();
    private final List<Location> goldGenerators = new ArrayList<>();
    private final List<Location> diamondGenerators = new ArrayList<>();
    private final List<Location> emeraldGenerators = new ArrayList<>();
    private final List<Location> teamIronGenerators = new ArrayList<>();
    private final List<ArenaGenerator> generators = new ArrayList<>();
    private final Map<BedWarsTeam, List<Location>> teamShops = new HashMap<>();
    private String worldName;
    private String mapName;
    private String matchWorldName;
    private String templateBaseName;
    private boolean isSetup = false;
    private boolean customRoom = false;
    private boolean dynamicInstance = false;
    private boolean autoStartPaused = false;
    private UUID customOwner;
    private int customMaxPlayers = 0;
    private int customTeamLimit = 0;
    private int matchTime = 0;
    private int endingTime = 0;
    private int diamondLevel = 1;
    private int emeraldLevel = 1;
    private double forgeMultiplier = 1.0;
    private final Set<String> placedBlockKeys = new HashSet<>();
    private final Set<Integer> diamondMilestones = new HashSet<>();
    private final Set<Integer> emeraldMilestones = new HashSet<>();
    private final Map<UUID, UUID> lastDamager = new HashMap<>();
    private final Map<UUID, Long> lastDamageTime = new HashMap<>();
    private final Map<UUID, BedWarsTeam> playerTeams = new HashMap<>();
    private final Map<UUID, Integer> matchKills = new HashMap<>();
    private final Map<UUID, Integer> matchDeaths = new HashMap<>();
    private final Map<UUID, Integer> matchFinalKills = new HashMap<>();

    public Arena(String id) {
        this.id = id;
        this.displayName = id;
        this.maxPlayers = mode.getDefaultCapacity();
        this.minPlayers = 2;
        this.maxPlayersPerTeam = mode.getTeamSize();
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("displayName", displayName);
        section.set("mode", mode.name());
        section.set("maxPlayers", maxPlayers);
        section.set("minPlayers", minPlayers);
        section.set("maxPlayersPerTeam", maxPlayersPerTeam);
        section.set("worldName", worldName);
        section.set("mapName", mapName);
        section.set("matchWorldName", matchWorldName);
        section.set("templateBaseName", templateBaseName);
        section.set("isSetup", isSetup);
        section.set("customRoom", customRoom);
        section.set("dynamicInstance", dynamicInstance);
        section.set("autoStartPaused", autoStartPaused);
        section.set("customOwner", customOwner == null ? null : customOwner.toString());
        section.set("customMaxPlayers", customMaxPlayers);
        section.set("customTeamLimit", customTeamLimit);
        section.set("forgeMultiplier", forgeMultiplier);

        if (lobby != null) LocationUtil.saveToConfig(section, "lobby", lobby);
        if (spectatorSpawn != null) LocationUtil.saveToConfig(section, "spectator", spectatorSpawn);
        if (center != null) LocationUtil.saveToConfig(section, "center", center);
        if (deathmatchCenter != null) LocationUtil.saveToConfig(section, "deathmatchCenter", deathmatchCenter);
        if (boundsPos1 != null) LocationUtil.saveToConfig(section, "bounds.pos1", boundsPos1);
        if (boundsPos2 != null) LocationUtil.saveToConfig(section, "bounds.pos2", boundsPos2);

        List<String> teamList = new ArrayList<>();
        for (BedWarsTeam team : enabledTeams) {
            teamList.add(team.name());
        }
        section.set("teams", teamList);

        for (BedWarsTeam team : enabledTeams) {
            String path = "teamData." + team.name() + ".";
            saveLocation(section, "spawns." + team.name(), teamSpawns.get(team));
            saveLocation(section, "beds." + team.name(), teamBeds.get(team));
            saveLocation(section, path + "generator", teamGenerators.get(team));
            saveLocation(section, path + "shop", teamShopLocations.get(team));
            saveLocation(section, path + "upgradeShop", teamUpgradeShopLocations.get(team));
            saveLocation(section, path + "indicator", teamIndicators.get(team));
            saveLocation(section, path + "center", teamCenters.get(team));
            section.set(path + "radius", getTeamIslandRadius(team));
        }

        section.set("generators.iron", serializeLocations(ironGenerators));
        section.set("generators.gold", serializeLocations(goldGenerators));
        section.set("generators.diamond", serializeLocations(diamondGenerators));
        section.set("generators.emerald", serializeLocations(emeraldGenerators));
        section.set("generators.teamIron", serializeLocations(teamIronGenerators));
        section.set("generators.rich", serializeGenerators());
    }

    public void loadFromConfig(ConfigurationSection section) {
        if (section == null) return;
        displayName = section.getString("displayName", id);
        mode = ArenaMode.fromString(section.getString("mode", mode.name()));
        maxPlayers = section.getInt("maxPlayers", mode.getDefaultCapacity());
        minPlayers = section.getInt("minPlayers", 2);
        maxPlayersPerTeam = section.getInt("maxPlayersPerTeam", mode.getTeamSize());
        worldName = section.getString("worldName", "world");
        mapName = section.getString("mapName", null);
        matchWorldName = section.getString("matchWorldName", null);
        templateBaseName = section.getString("templateBaseName", null);
        isSetup = section.getBoolean("isSetup", false);
        customRoom = section.getBoolean("customRoom", false);
        dynamicInstance = section.getBoolean("dynamicInstance", false);
        autoStartPaused = section.getBoolean("autoStartPaused", false);
        customMaxPlayers = section.getInt("customMaxPlayers", 0);
        customTeamLimit = section.getInt("customTeamLimit", 0);
        forgeMultiplier = section.getDouble("forgeMultiplier", 1.0);
        String owner = section.getString("customOwner", null);
        if (owner != null && !owner.trim().isEmpty()) {
            try {
                customOwner = UUID.fromString(owner);
            } catch (IllegalArgumentException ignored) {
                customOwner = null;
            }
        }

        lobby = LocationUtil.loadFromConfig(section, "lobby");
        spectatorSpawn = LocationUtil.loadFromConfig(section, "spectator");
        center = LocationUtil.loadFromConfig(section, "center");
        deathmatchCenter = LocationUtil.loadFromConfig(section, "deathmatchCenter");
        boundsPos1 = LocationUtil.loadFromConfig(section, "bounds.pos1");
        boundsPos2 = LocationUtil.loadFromConfig(section, "bounds.pos2");

        enabledTeams.clear();
        for (String teamName : section.getStringList("teams")) {
            BedWarsTeam team = BedWarsTeam.fromString(teamName);
            if (team != null && !enabledTeams.contains(team)) enabledTeams.add(team);
        }
        if (enabledTeams.isEmpty()) {
            migrateLegacyTeams(section.getConfigurationSection("spawns"));
            migrateLegacyTeams(section.getConfigurationSection("beds"));
        }

        for (BedWarsTeam team : enabledTeams) {
            String path = "teamData." + team.name() + ".";
            Location spawn = LocationUtil.loadFromConfig(section, "spawns." + team.name());
            Location bed = LocationUtil.loadFromConfig(section, "beds." + team.name());
            if (spawn != null) teamSpawns.put(team, spawn);
            if (bed != null) teamBeds.put(team, bed);
            putIfNotNull(teamGenerators, team, LocationUtil.loadFromConfig(section, path + "generator"));
            putIfNotNull(teamShopLocations, team, LocationUtil.loadFromConfig(section, path + "shop"));
            putIfNotNull(teamUpgradeShopLocations, team, LocationUtil.loadFromConfig(section, path + "upgradeShop"));
            putIfNotNull(teamIndicators, team, LocationUtil.loadFromConfig(section, path + "indicator"));
            putIfNotNull(teamCenters, team, LocationUtil.loadFromConfig(section, path + "center"));
            teamIslandRadius.put(team, section.getInt(path + "radius", 18));
        }

        loadLocations(section, "generators.iron", ironGenerators);
        loadLocations(section, "generators.gold", goldGenerators);
        loadLocations(section, "generators.diamond", diamondGenerators);
        loadLocations(section, "generators.emerald", emeraldGenerators);
        loadLocations(section, "generators.teamIron", teamIronGenerators);
        loadGenerators(section);
        ensureRichGeneratorsFromLegacy();
    }

    public void resetMatchState() {
        matchTime = 0;
        endingTime = 0;
        diamondLevel = 1;
        emeraldLevel = 1;
        diamondMilestones.clear();
        emeraldMilestones.clear();
        placedBlockKeys.clear();
        lastDamager.clear();
        lastDamageTime.clear();
        playerTeams.clear();
        matchKills.clear();
        matchDeaths.clear();
        matchFinalKills.clear();
        for (ArenaGenerator generator : generators) {
            generator.reset();
        }
    }

    public void registerLastDamage(UUID victim, UUID attacker) {
        if (victim == null || attacker == null || victim.equals(attacker)) return;
        lastDamager.put(victim, attacker);
        lastDamageTime.put(victim, System.currentTimeMillis());
    }

    public UUID findRecentDamager(UUID victim, long validMs) {
        if (victim == null) return null;
        UUID attacker = lastDamager.get(victim);
        Long time = lastDamageTime.get(victim);
        if (attacker == null || time == null) return null;
        if (validMs > 0 && System.currentTimeMillis() - time > validMs) {
            clearLastDamage(victim);
            return null;
        }
        return attacker;
    }

    public void clearLastDamage(UUID victim) {
        if (victim == null) return;
        lastDamager.remove(victim);
        lastDamageTime.remove(victim);
    }

    public void addPlacedBlock(Location location) {
        String key = blockKey(location);
        if (key != null) placedBlockKeys.add(key);
    }

    public void removePlacedBlock(Location location) {
        String key = blockKey(location);
        if (key != null) placedBlockKeys.remove(key);
    }

    public boolean isPlacedBlock(Location location) {
        String key = blockKey(location);
        return key != null && placedBlockKeys.contains(key);
    }

    public Location toMatchLocation(Location location) {
        if (location == null || matchWorldName == null || matchWorldName.trim().isEmpty()) return location;
        World world = Bukkit.getWorld(matchWorldName);
        if (world == null) return location;
        return new Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public int getEffectiveTeamSize() {
        return customRoom && maxPlayersPerTeam > 0 ? maxPlayersPerTeam : Math.max(1, mode.getTeamSize());
    }

    public int getEffectiveTeamLimit() {
        int modeLimit = mode.getMaxTeams();
        int enabled = enabledTeams.isEmpty() ? modeLimit : enabledTeams.size();
        int limit = customTeamLimit > 0 ? customTeamLimit : Math.min(modeLimit, enabled);
        return Math.max(2, Math.min(BedWarsTeam.values().length, limit));
    }

    public int getEffectiveMaxPlayers() {
        if (customMaxPlayers > 0) return Math.max(2, Math.min(80, customMaxPlayers));
        return Math.max(2, getEffectiveTeamLimit() * getEffectiveTeamSize());
    }

    public int getMinimumToStart() {
        return Math.max(2, Math.min(getEffectiveMaxPlayers(), getEffectiveTeamSize() * 2));
    }

    public List<BedWarsTeam> getActiveTeams() {
        List<BedWarsTeam> teams = new ArrayList<>();
        for (BedWarsTeam team : enabledTeams) {
            if (teams.size() >= getEffectiveTeamLimit()) break;
            teams.add(team);
        }
        if (teams.isEmpty()) {
            for (BedWarsTeam team : BedWarsTeam.values()) {
                if (teams.size() >= getEffectiveTeamLimit()) break;
                teams.add(team);
            }
        }
        return teams;
    }

    public int getTeamIslandRadius(BedWarsTeam team) {
        Integer radius = teamIslandRadius.get(team);
        return radius == null || radius <= 0 ? 18 : radius;
    }

    private static String blockKey(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private void saveLocation(ConfigurationSection section, String path, Location location) {
        if (location != null) LocationUtil.saveToConfig(section, path, location);
    }

    private List<String> serializeLocations(List<Location> locations) {
        List<String> values = new ArrayList<>();
        for (Location location : locations) values.add(LocationUtil.serialize(location));
        return values;
    }

    private List<String> serializeGenerators() {
        List<String> values = new ArrayList<>();
        for (ArenaGenerator generator : generators) {
            String owner = generator.getOwner() == null ? "" : generator.getOwner().name();
            values.add(generator.getType().name() + "|" + owner + "|" + LocationUtil.serialize(generator.getLocation()));
        }
        return values;
    }

    private void loadLocations(ConfigurationSection section, String path, List<Location> target) {
        target.clear();
        for (String value : section.getStringList(path)) {
            Location location = LocationUtil.deserialize(value);
            if (location != null) target.add(location);
        }
    }

    private void loadGenerators(ConfigurationSection section) {
        generators.clear();
        for (String value : section.getStringList("generators.rich")) {
            String[] parts = value.split("\\|", 3);
            if (parts.length < 3) continue;
            GeneratorType type = GeneratorType.fromString(parts[0]);
            BedWarsTeam owner = parts[1].isEmpty() ? null : BedWarsTeam.fromString(parts[1]);
            Location location = LocationUtil.deserialize(parts[2]);
            if (type != null && location != null) generators.add(new ArenaGenerator(type, location, owner));
        }
    }

    private void ensureRichGeneratorsFromLegacy() {
        if (!generators.isEmpty()) return;
        addLegacyGenerators(GeneratorType.IRON, ironGenerators);
        addLegacyGenerators(GeneratorType.GOLD, goldGenerators);
        addLegacyGenerators(GeneratorType.DIAMOND, diamondGenerators);
        addLegacyGenerators(GeneratorType.EMERALD, emeraldGenerators);
    }

    private void addLegacyGenerators(GeneratorType type, List<Location> locations) {
        for (Location location : locations) {
            generators.add(new ArenaGenerator(type, location, null));
        }
    }

    private void migrateLegacyTeams(ConfigurationSection section) {
        if (section == null) return;
        for (String teamName : section.getKeys(false)) {
            BedWarsTeam team = BedWarsTeam.fromString(teamName);
            if (team != null && !enabledTeams.contains(team)) enabledTeams.add(team);
        }
    }

    private <T> void putIfNotNull(Map<BedWarsTeam, T> map, BedWarsTeam team, T value) {
        if (value != null) map.put(team, value);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public ArenaMode getMode() { return mode; }
    public void setMode(ArenaMode mode) {
        if (mode == null) return;
        this.mode = mode;
        this.maxPlayers = mode.getDefaultCapacity();
        this.maxPlayersPerTeam = mode.getTeamSize();
        this.minPlayers = Math.max(2, mode.getTeamSize() * 2);
    }
    public ArenaState getState() { return state; }
    public void setState(ArenaState state) { this.state = state; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }
    public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
    public void setMaxPlayersPerTeam(int maxPlayersPerTeam) { this.maxPlayersPerTeam = maxPlayersPerTeam; }
    public Location getLobby() { return lobby; }
    public void setLobby(Location lobby) { this.lobby = lobby; }
    public Location getSpectatorSpawn() { return spectatorSpawn; }
    public void setSpectatorSpawn(Location spectatorSpawn) { this.spectatorSpawn = spectatorSpawn; }
    public Location getCenter() { return center; }
    public void setCenter(Location center) { this.center = center; }
    public Location getDeathmatchCenter() { return deathmatchCenter; }
    public void setDeathmatchCenter(Location deathmatchCenter) { this.deathmatchCenter = deathmatchCenter; }
    public Location getBoundsPos1() { return boundsPos1; }
    public void setBoundsPos1(Location boundsPos1) { this.boundsPos1 = boundsPos1; }
    public Location getBoundsPos2() { return boundsPos2; }
    public void setBoundsPos2(Location boundsPos2) { this.boundsPos2 = boundsPos2; }
    public Map<BedWarsTeam, Location> getTeamSpawns() { return teamSpawns; }
    public Map<BedWarsTeam, Location> getTeamBeds() { return teamBeds; }
    public Map<BedWarsTeam, Location> getTeamGenerators() { return teamGenerators; }
    public Map<BedWarsTeam, Location> getTeamShopLocations() { return teamShopLocations; }
    public Map<BedWarsTeam, Location> getTeamUpgradeShopLocations() { return teamUpgradeShopLocations; }
    public Map<BedWarsTeam, Location> getTeamIndicators() { return teamIndicators; }
    public Map<BedWarsTeam, Location> getTeamCenters() { return teamCenters; }
    public Map<BedWarsTeam, Integer> getTeamIslandRadiusMap() { return teamIslandRadius; }
    public List<BedWarsTeam> getEnabledTeams() { return enabledTeams; }
    public List<Location> getIronGenerators() { return ironGenerators; }
    public List<Location> getGoldGenerators() { return goldGenerators; }
    public List<Location> getDiamondGenerators() { return diamondGenerators; }
    public List<Location> getEmeraldGenerators() { return emeraldGenerators; }
    public List<Location> getTeamIronGenerators() { return teamIronGenerators; }
    public List<ArenaGenerator> getGenerators() { return generators; }
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    public String getMatchWorldName() { return matchWorldName; }
    public void setMatchWorldName(String matchWorldName) { this.matchWorldName = matchWorldName; }
    public String getTemplateBaseName() { return templateBaseName; }
    public void setTemplateBaseName(String templateBaseName) { this.templateBaseName = templateBaseName; }
    public boolean isSetup() { return isSetup; }
    public void setSetup(boolean setup) { isSetup = setup; }
    public boolean isCustomRoom() { return customRoom; }
    public void setCustomRoom(boolean customRoom) { this.customRoom = customRoom; }
    public boolean isDynamicInstance() { return dynamicInstance; }
    public void setDynamicInstance(boolean dynamicInstance) { this.dynamicInstance = dynamicInstance; }
    public boolean isAutoStartPaused() { return autoStartPaused; }
    public void setAutoStartPaused(boolean autoStartPaused) { this.autoStartPaused = autoStartPaused; }
    public UUID getCustomOwner() { return customOwner; }
    public void setCustomOwner(UUID customOwner) { this.customOwner = customOwner; }
    public int getCustomMaxPlayers() { return customMaxPlayers; }
    public void setCustomMaxPlayers(int customMaxPlayers) { this.customMaxPlayers = customMaxPlayers; }
    public int getCustomTeamLimit() { return customTeamLimit; }
    public void setCustomTeamLimit(int customTeamLimit) { this.customTeamLimit = customTeamLimit; }
    public int getMatchTime() { return matchTime; }
    public void setMatchTime(int matchTime) { this.matchTime = matchTime; }
    public int getEndingTime() { return endingTime; }
    public void setEndingTime(int endingTime) { this.endingTime = endingTime; }
    public int getDiamondLevel() { return diamondLevel; }
    public void setDiamondLevel(int diamondLevel) { this.diamondLevel = diamondLevel; }
    public int getEmeraldLevel() { return emeraldLevel; }
    public void setEmeraldLevel(int emeraldLevel) { this.emeraldLevel = emeraldLevel; }
    public double getForgeMultiplier() { return forgeMultiplier; }
    public void setForgeMultiplier(double forgeMultiplier) {
        this.forgeMultiplier = Double.isNaN(forgeMultiplier) || Double.isInfinite(forgeMultiplier) ? 1.0 : Math.max(0.1, forgeMultiplier);
    }
    public Set<String> getPlacedBlockKeys() { return placedBlockKeys; }
    public Set<Integer> getDiamondMilestones() { return diamondMilestones; }
    public Set<Integer> getEmeraldMilestones() { return emeraldMilestones; }
    public Map<UUID, BedWarsTeam> getPlayerTeams() { return playerTeams; }
    public Map<UUID, Integer> getMatchKills() { return matchKills; }
    public Map<UUID, Integer> getMatchDeaths() { return matchDeaths; }
    public Map<UUID, Integer> getMatchFinalKills() { return matchFinalKills; }
    public Map<BedWarsTeam, List<Location>> getTeamShops() { return teamShops; }
}
