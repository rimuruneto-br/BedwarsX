package dev.bedwarsx.mission;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class MissionManager {
    private final BedWarsX plugin;
    private final Map<String, Mission> missions = new LinkedHashMap<>();
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat weekFormat = new SimpleDateFormat("YYYY-'W'ww");

    public MissionManager(BedWarsX plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfigManager().getConfig("missions");
        seedDefaults(config);
        missions.clear();
        ConfigurationSection section = config.getConfigurationSection("missions");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            String path = "missions." + id + ".";
            MissionEvent event;
            try {
                event = MissionEvent.valueOf(config.getString(path + "event", "GAME").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                continue;
            }
            missions.put(id, new Mission(
                    id,
                    config.getString(path + "name", id),
                    event,
                    Math.max(1, config.getInt(path + "target", 1)),
                    Math.max(0, config.getInt(path + "xp", 100)),
                    config.getString(path + "period", "lifetime")
            ));
        }
    }

    public Collection<Mission> getMissions() {
        return missions.values();
    }

    public void record(Player player, MissionEvent event, int amount) {
        if (player == null || event == null || amount <= 0) return;
        FileConfiguration config = plugin.getConfigManager().getConfig("missions");
        for (Mission mission : missions.values()) {
            if (mission.getEvent() != event) continue;
            String base = "progress." + player.getUniqueId() + "." + periodKey(mission) + "." + mission.getId();
            if (config.getBoolean(base + ".claimed", false)) continue;
            int progress = Math.min(mission.getTarget(), config.getInt(base + ".value", 0) + amount);
            config.set(base + ".value", progress);
            if (progress >= mission.getTarget()) {
                config.set(base + ".claimed", true);
                plugin.getProgressionManager().addXp(player, mission.getXpReward());
                plugin.getProgressionManager().save();
                ChatUtil.send(player, "&aMission completed: &f" + mission.getName() + " &7(+" + mission.getXpReward() + " XP)");
            }
        }
        plugin.getConfigManager().saveConfig("missions");
    }

    private void seedDefaults(FileConfiguration config) {
        if (config.getConfigurationSection("missions") != null) return;
        config.set("missions.daily_kills.name", "Daily Hunter");
        config.set("missions.daily_kills.period", "daily");
        config.set("missions.daily_kills.event", "KILL");
        config.set("missions.daily_kills.target", 10);
        config.set("missions.daily_kills.xp", 300);
        config.set("missions.bed_breaker.name", "Bed Breaker");
        config.set("missions.bed_breaker.period", "weekly");
        config.set("missions.bed_breaker.event", "BED");
        config.set("missions.bed_breaker.target", 3);
        config.set("missions.bed_breaker.xp", 450);
        config.set("missions.closer.name", "Closer");
        config.set("missions.closer.period", "weekly");
        config.set("missions.closer.event", "WIN");
        config.set("missions.closer.target", 2);
        config.set("missions.closer.xp", 600);
        plugin.getConfigManager().saveConfig("missions");
    }

    private String periodKey(Mission mission) {
        String period = mission.getPeriod();
        if ("daily".equals(period)) return "daily-" + dayFormat.format(new Date());
        if ("weekly".equals(period)) return "weekly-" + weekFormat.format(new Date());
        return "lifetime";
    }
}
