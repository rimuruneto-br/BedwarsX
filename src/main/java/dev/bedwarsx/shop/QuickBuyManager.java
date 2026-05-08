package dev.bedwarsx.shop;

import dev.bedwarsx.main.BedWarsX;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuickBuyManager {
    private final BedWarsX plugin;

    public QuickBuyManager(BedWarsX plugin) {
        this.plugin = plugin;
    }

    public List<String> getFavorites(Player player) {
        return new ArrayList<>(plugin.getConfigManager().getConfig("quickbuy")
                .getStringList("players." + player.getUniqueId() + ".favorites"));
    }

    public boolean toggleFavorite(Player player, String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) return false;
        FileConfiguration config = plugin.getConfigManager().getConfig("quickbuy");
        String path = "players." + player.getUniqueId() + ".favorites";
        List<String> favorites = new ArrayList<>(config.getStringList(path));
        String clean = ShopItem.normalizeId(itemId);
        boolean added;
        if (favorites.contains(clean)) {
            favorites.remove(clean);
            added = false;
        } else {
            favorites.add(clean);
            added = true;
        }
        config.set(path, favorites);
        plugin.getConfigManager().saveConfig("quickbuy");
        return added;
    }
}
