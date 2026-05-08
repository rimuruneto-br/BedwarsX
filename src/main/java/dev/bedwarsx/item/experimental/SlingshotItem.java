package dev.bedwarsx.item.experimental;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import dev.bedwarsx.util.ParticleUtil;
import org.bukkit.Material;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 🪃 ESTILINGUE - Slingshot mobility item
 * Launches player forward and upward with a 5-second cooldown.
 */
public class SlingshotItem {

    private static final String ITEM_NAME = "&a&l🪃 ESTILINGUE";
    private static final int COOLDOWN_SECONDS = 5;

    // Launch forces - consistent and predictable
    private static final double FORWARD_FORCE = 1.6;
    private static final double VERTICAL_FORCE = 0.7;

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> cooldownDisplayTasks = new HashMap<>();

    public static ItemStack getItem() {
        return ItemUtil.create(Material.LEASH, 1, ITEM_NAME,
                "&7Impulso controlado para frente.",
                "",
                "&eEfeitos:",
                "  &f- Lança para frente e para cima",
                "  &f- Impulso consistente e previsível",
                "  &f- Cooldown: 5 segundos",
                "",
                "&eFoco: &fMobilidade rápida",
                "&7Categoria: &5Experimental");
    }

    public static boolean isSlingshotItem(ItemStack item) {
        return ItemUtil.hasName(item, ITEM_NAME);
    }

    public static void use(Player player, Game game) {
        UUID uuid = player.getUniqueId();

        // Check cooldown
        if (cooldowns.containsKey(uuid)) {
            long remaining = (cooldowns.get(uuid) - System.currentTimeMillis());
            if (remaining > 0) {
                double remainingSeconds = remaining / 1000.0;
                ChatUtil.sendActionBar(player, "&a🪃 Estilingue: &e" + String.format("%.1f", remainingSeconds) + "s");
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1f, 0.5f);
                return;
            }
        }

        // Calculate launch vector
        Vector direction = player.getLocation().getDirection();

        // Forward component (horizontal only from look direction)
        Vector forward = new Vector(direction.getX(), 0, direction.getZ()).normalize();
        forward.multiply(FORWARD_FORCE);

        // Add upward force
        forward.setY(VERTICAL_FORCE);

        // Apply velocity
        player.setVelocity(forward);
        player.setFallDistance(0);

        // Sound and particles
        player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1f, 1.5f);
        ParticleUtil.spawnFirework(player.getLocation().clone().add(0, 0.5, 0), 15);

        // Apply cooldown
        cooldowns.put(uuid, System.currentTimeMillis() + (COOLDOWN_SECONDS * 1000L));

        // Cancel existing display task
        BukkitRunnable existing = cooldownDisplayTasks.get(uuid);
        if (existing != null) {
            existing.cancel();
        }

        // Show countdown in action bar
        BukkitRunnable displayTask = new BukkitRunnable() {
            double remaining = COOLDOWN_SECONDS;

            @Override
            public void run() {
                if (!player.isOnline() || remaining <= 0) {
                    if (player.isOnline()) {
                        ChatUtil.sendActionBar(player, "&a🪃 Estilingue: &aPronto!");
                    }
                    cooldownDisplayTasks.remove(uuid);
                    cancel();
                    return;
                }

                ChatUtil.sendActionBar(player, "&a🪃 Estilingue: &e" + String.format("%.1f", remaining) + "s");
                remaining -= 0.5;
            }
        };
        displayTask.runTaskTimer(BedWarsX.getInstance(), 0L, 10L); // every 0.5s
        cooldownDisplayTasks.put(uuid, displayTask);
    }

    public static boolean isOnCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return false;
        return cooldowns.get(uuid) > System.currentTimeMillis();
    }

    public static double getRemainingCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return 0;
        long remaining = cooldowns.get(uuid) - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000.0 : 0;
    }

    public static void clearPlayer(UUID uuid) {
        cooldowns.remove(uuid);
        BukkitRunnable task = cooldownDisplayTasks.remove(uuid);
        if (task != null) task.cancel();
    }
}
