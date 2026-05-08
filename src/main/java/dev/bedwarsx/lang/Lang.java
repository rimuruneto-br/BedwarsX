package dev.bedwarsx.lang;

import dev.bedwarsx.util.ChatUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Lang {

    private static FileConfiguration messages;
    private static JavaPlugin plugin;

    public static void init(JavaPlugin pl) {
        plugin = pl;
        File file = new File(pl.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            pl.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);

        // Merge defaults
        InputStream defStream = pl.getResource("messages.yml");
        if (defStream != null) {
            YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
            messages.setDefaults(def);
        }
    }

    public static String get(String key) {
        String raw = messages.getString(key, "&c[MSG AUSENTE: " + key + "]");
        return ChatUtil.color(raw);
    }

    public static String get(String key, Object... replacements) {
        String msg = get(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
        }
        return msg;
    }

    public static String prefix() {
        return get("prefix");
    }

    public static String p(String key, Object... replacements) {
        return prefix() + get(key, replacements);
    }

    public static void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }
}
