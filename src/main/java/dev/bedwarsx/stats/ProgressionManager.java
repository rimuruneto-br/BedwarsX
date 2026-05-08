package dev.bedwarsx.stats;

import dev.bedwarsx.main.BedWarsX;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class ProgressionManager {
    private final BedWarsX plugin;
    private final Map<UUID, Profile> profiles = new HashMap<>();
    private final SimpleDateFormat weekFormat = new SimpleDateFormat("YYYY-'W'ww");
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

    public ProgressionManager(BedWarsX plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        profiles.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("progression");
        ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                Profile profile = get(uuid);
                String path = "players." + key + ".";
                profile.name = config.getString(path + "name", "");
                profile.xp = config.getInt(path + "xp", 0);
                profile.kills = config.getInt(path + "kills", 0);
                profile.deaths = config.getInt(path + "deaths", 0);
                profile.finalKills = config.getInt(path + "finalKills", 0);
                profile.beds = config.getInt(path + "beds", 0);
                profile.wins = config.getInt(path + "wins", 0);
                profile.losses = config.getInt(path + "losses", 0);
                profile.games = config.getInt(path + "games", 0);
                loadPeriod(config, path + "weekly", profile.weekly);
                loadPeriod(config, path + "monthly", profile.monthly);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        FileConfiguration config = plugin.getConfigManager().getConfig("progression");
        config.set("players", null);
        for (Map.Entry<UUID, Profile> entry : profiles.entrySet()) {
            String path = "players." + entry.getKey() + ".";
            Profile profile = entry.getValue();
            config.set(path + "name", profile.name);
            config.set(path + "xp", profile.xp);
            config.set(path + "kills", profile.kills);
            config.set(path + "deaths", profile.deaths);
            config.set(path + "finalKills", profile.finalKills);
            config.set(path + "beds", profile.beds);
            config.set(path + "wins", profile.wins);
            config.set(path + "losses", profile.losses);
            config.set(path + "games", profile.games);
            savePeriod(config, path + "weekly", profile.weekly);
            savePeriod(config, path + "monthly", profile.monthly);
        }
        plugin.getConfigManager().saveConfig("progression");
    }

    public Profile get(Player player) {
        Profile profile = get(player.getUniqueId());
        profile.name = player.getName();
        return profile;
    }

    public Profile get(UUID uuid) {
        return profiles.computeIfAbsent(uuid, key -> new Profile());
    }

    public void recordKill(Player player, boolean finalKill) {
        Profile profile = get(player);
        profile.kills++;
        addPeriod(profile, "kills", 1);
        addXp(player, finalKill ? 40 : 20);
        if (finalKill) {
            profile.finalKills++;
            addPeriod(profile, "finalKills", 1);
        }
        save();
    }

    public void recordDeath(Player player) {
        Profile profile = get(player);
        profile.deaths++;
        addPeriod(profile, "deaths", 1);
        save();
    }

    public void recordBed(Player player) {
        Profile profile = get(player);
        profile.beds++;
        addPeriod(profile, "beds", 1);
        addXp(player, 35);
        save();
    }

    public void recordGame(Player player, boolean won) {
        Profile profile = get(player);
        profile.games++;
        if (won) {
            profile.wins++;
            addPeriod(profile, "wins", 1);
            addXp(player, 120);
        } else {
            profile.losses++;
            addXp(player, 35);
        }
        addPeriod(profile, "games", 1);
        save();
    }

    public void addXp(Player player, int amount) {
        if (player == null || amount <= 0) return;
        get(player).xp += amount;
    }

    public List<LeaderboardEntry> top(String period, String stat, int limit) {
        String key = "monthly".equalsIgnoreCase(period) ? currentMonth() : currentWeek();
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Profile profile : profiles.values()) {
            Map<String, Integer> stats = "monthly".equalsIgnoreCase(period) ? profile.monthly.get(key) : profile.weekly.get(key);
            int value = stats == null ? 0 : stats.getOrDefault(stat, 0);
            if (value > 0) entries.add(new LeaderboardEntry(profile.name, value));
        }
        entries.sort((a, b) -> Integer.compare(b.value, a.value));
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    private void addPeriod(Profile profile, String stat, int amount) {
        addToMap(profile.weekly, currentWeek(), stat, amount);
        addToMap(profile.monthly, currentMonth(), stat, amount);
    }

    private void addToMap(Map<String, Map<String, Integer>> map, String key, String stat, int amount) {
        Map<String, Integer> stats = map.computeIfAbsent(key, ignored -> new HashMap<>());
        stats.put(stat, stats.getOrDefault(stat, 0) + amount);
    }

    private String currentWeek() {
        return weekFormat.format(new Date());
    }

    private String currentMonth() {
        return monthFormat.format(new Date());
    }

    private void loadPeriod(FileConfiguration config, String path, Map<String, Map<String, Integer>> target) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) return;
        for (String period : section.getKeys(false)) {
            Map<String, Integer> stats = new HashMap<>();
            ConfigurationSection values = section.getConfigurationSection(period);
            if (values == null) continue;
            for (String stat : values.getKeys(false)) {
                stats.put(stat, values.getInt(stat));
            }
            target.put(period, stats);
        }
    }

    private void savePeriod(FileConfiguration config, String path, Map<String, Map<String, Integer>> source) {
        for (Map.Entry<String, Map<String, Integer>> period : source.entrySet()) {
            for (Map.Entry<String, Integer> stat : period.getValue().entrySet()) {
                config.set(path + "." + period.getKey() + "." + stat.getKey(), stat.getValue());
            }
        }
    }

    public static class Profile {
        private String name = "";
        private int xp;
        private int kills;
        private int deaths;
        private int finalKills;
        private int beds;
        private int wins;
        private int losses;
        private int games;
        private final Map<String, Map<String, Integer>> weekly = new HashMap<>();
        private final Map<String, Map<String, Integer>> monthly = new HashMap<>();

        public int getXp() { return xp; }
        public int getLevel() { return Math.max(1, xp / 5000 + 1); }
        public int getXpIntoLevel() { return xp % 5000; }
        public int getXpForNextLevel() { return 5000; }
        public int getLevelPercent() { return (int) Math.round((getXpIntoLevel() / 5000.0) * 100.0); }
        public int getKills() { return kills; }
        public int getDeaths() { return deaths; }
        public int getFinalKills() { return finalKills; }
        public int getBeds() { return beds; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getGames() { return games; }
    }

    public static class LeaderboardEntry {
        public final String name;
        public final int value;

        public LeaderboardEntry(String name, int value) {
            this.name = name == null || name.isEmpty() ? "Unknown" : name;
            this.value = value;
        }
    }
}
