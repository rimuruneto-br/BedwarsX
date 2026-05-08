package dev.bedwarsx.util;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public final class PlayerUtil {

    private PlayerUtil() {
    }

    public static void clearPotionEffects(Player player) {
        if (player == null) return;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
}
