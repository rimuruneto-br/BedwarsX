package dev.bedwarsx.config;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final BedWarsX plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();
    private final File externalMirror;
    private final Object saveLock = new Object();

    public ConfigManager(BedWarsX plugin) {
        this.plugin = plugin;
        File pluginsFolder = plugin.getDataFolder().getParentFile();
        this.externalMirror = pluginsFolder == null
                ? new File(plugin.getDataFolder(), "_mirror")
                : new File(new File(pluginsFolder, "RN-DATA"), "BedWarsX");
        loadConfigs();
    }

    private void loadConfigs() {
        loadConfig("arenas", true);
        loadConfig("stats", true);
        loadConfig("npcs", true);
        loadConfig("shop", true);
        loadConfig("quickbuy", false);
        loadConfig("progression", false);
        loadConfig("missions", false);
        loadConfig("leaderboards", false);
        loadConfig("upgrades", true);
        loadConfig("visual", true);
        loadConfig("messages", true);
    }

    public void loadConfig(String name) {
        loadConfig(name, true);
    }

    public void loadConfig(String name, boolean mergeDefaults) {
        File file = new File(plugin.getDataFolder(), name + ".yml");
        restoreFromMirrorIfNeeded(name, file);

        if (!file.exists()) {
            copyResourceOrCreate(name, file);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (mergeDefaults) {
            mergeDefaults(name, config);
        }

        configs.put(name, config);
        files.put(name, file);
    }

    public FileConfiguration getConfig(String name) {
        if (name == null || name.equalsIgnoreCase("config")) {
            return plugin.getConfig();
        }
        FileConfiguration config = configs.get(name);
        if (config == null) {
            loadConfig(name, true);
            config = configs.get(name);
        }
        return config == null ? plugin.getConfig() : config;
    }

    public void saveConfig(String name) {
        if (name == null || name.equalsIgnoreCase("config")) {
            plugin.saveConfig();
            mirrorFile("config", new File(plugin.getDataFolder(), "config.yml"));
            return;
        }
        FileConfiguration config = configs.get(name);
        File file = files.get(name);
        if (config != null && file != null) {
            saveAtomic(name, file, config);
        }
    }

    public void reloadConfig(String name) {
        if (name == null || name.equalsIgnoreCase("config")) {
            plugin.reloadConfig();
            return;
        }
        File file = files.get(name);
        if (file != null) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            mergeDefaults(name, config);
            configs.put(name, config);
        }
    }

    public void saveAll() {
        plugin.saveConfig();
        mirrorFile("config", new File(plugin.getDataFolder(), "config.yml"));
        for (String name : configs.keySet()) {
            saveConfig(name);
        }
    }

    private void copyResourceOrCreate(String name, File file) {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (hasResource(name + ".yml")) {
            plugin.saveResource(name + ".yml", false);
            return;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().warning("Could not create " + name + ".yml: " + e.getMessage());
        }
    }

    private void mergeDefaults(String name, FileConfiguration config) {
        InputStream stream = plugin.getResource(name + ".yml");
        if (stream == null) return;
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        config.setDefaults(defaults);
        if (copyMissingDefaults(defaults, config)) {
            saveConfigObject(name, config);
            plugin.getLogger().info(name + ".yml recebeu novas chaves padrao sem sobrescrever ajustes existentes.");
        }
    }

    private boolean copyMissingDefaults(ConfigurationSection defaults, ConfigurationSection target) {
        boolean changed = false;
        for (String key : defaults.getKeys(false)) {
            if (defaults.isConfigurationSection(key)) {
                ConfigurationSection targetSection = target.getConfigurationSection(key);
                if (targetSection == null) {
                    targetSection = target.createSection(key);
                    changed = true;
                }
                changed = copyMissingDefaults(defaults.getConfigurationSection(key), targetSection) || changed;
            } else if (!target.contains(key)) {
                target.set(key, defaults.get(key));
                changed = true;
            }
        }
        return changed;
    }

    private boolean hasResource(String resource) {
        InputStream stream = plugin.getResource(resource);
        if (stream == null) return false;
        try {
            stream.close();
        } catch (IOException ignored) {
        }
        return true;
    }

    private void saveConfigObject(String name, FileConfiguration config) {
        File file = files.get(name);
        if (file == null) file = new File(plugin.getDataFolder(), name + ".yml");
        saveAtomic(name, file, config);
    }

    private void saveAtomic(String name, File file, FileConfiguration config) {
        synchronized (saveLock) {
            File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
            try {
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                config.save(tmp);
                if (file.exists()) {
                    File backup = new File(file.getParentFile(), file.getName() + ".bak");
                    try {
                        Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        plugin.getLogger().warning("Could not create backup for " + file.getName() + ": " + e.getMessage());
                    }
                }
                try {
                    Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicFailure) {
                    Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                mirrorFile(name, file);
            } catch (IOException e) {
                plugin.getLogger().warning("Could not save " + file.getName() + ": " + e.getMessage());
            } finally {
                if (tmp.exists() && !tmp.delete()) {
                    tmp.deleteOnExit();
                }
            }
        }
    }

    private void restoreFromMirrorIfNeeded(String name, File file) {
        if (file.exists() && file.length() > 0L) return;
        File mirrored = new File(externalMirror, name + ".yml");
        if (!mirrored.exists()) return;
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            Files.copy(mirrored.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Restored " + name + ".yml from external persistence mirror.");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not restore mirrored " + name + ".yml: " + e.getMessage());
        }
    }

    private void mirrorFile(String name, File source) {
        if (source == null || !source.exists()) return;
        try {
            if (!externalMirror.exists()) externalMirror.mkdirs();
            File target = new File(externalMirror, name + ".yml");
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not mirror " + name + ".yml: " + e.getMessage());
        }
    }

    public String getMessage(String key) {
        String prefix = plugin.getConfig().getString("prefix", "&8[&6BedWarsX&8] &r");
        String msg = plugin.getConfig().getString("messages." + key, null);
        if (msg == null) {
            msg = getConfig("messages").getString(key, "&cMessage not found: " + key);
        }
        return ChatUtil.color(prefix + msg);
    }

    public String getMessage(String key, Map<String, String> replacements) {
        String msg = getMessage(key);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return msg;
    }

    public Location getLobbyLocation() {
        FileConfiguration cfg = plugin.getConfig();
        String worldName = cfg.getString("lobby.world", "world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null && !plugin.getServer().getWorlds().isEmpty()) {
            world = plugin.getServer().getWorlds().get(0);
        }
        double x = cfg.getDouble("lobby.x", 0.5);
        double y = cfg.getDouble("lobby.y", 64.0);
        double z = cfg.getDouble("lobby.z", 0.5);
        float yaw = (float) cfg.getDouble("lobby.yaw", 0.0);
        float pitch = (float) cfg.getDouble("lobby.pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setLobbyLocation(Location loc) {
        plugin.getConfig().set("lobby.world", loc.getWorld().getName());
        plugin.getConfig().set("lobby.x", loc.getX());
        plugin.getConfig().set("lobby.y", loc.getY());
        plugin.getConfig().set("lobby.z", loc.getZ());
        plugin.getConfig().set("lobby.yaw", loc.getYaw());
        plugin.getConfig().set("lobby.pitch", loc.getPitch());
        saveConfig("config");
    }
}
