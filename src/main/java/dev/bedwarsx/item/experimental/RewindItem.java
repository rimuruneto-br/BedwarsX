package dev.bedwarsx.item.experimental;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import dev.bedwarsx.util.ParticleUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 🔮 REBOBINAR - Rewind item
 * Allows the player to go back 3 seconds in time.
 */
public class RewindItem {

    private static final String ITEM_NAME = "&d&l🔮 REBOBINAR";
    private static final int SNAPSHOT_INTERVAL = 2; // ticks between snapshots
    private static final int SNAPSHOT_DURATION = 3; // seconds of history
    private static final int SNAPSHOTS_NEEDED = (20 / SNAPSHOT_INTERVAL) * SNAPSHOT_DURATION; // 30 snapshots
    private static final int COOLDOWN_MIN = 15;
    private static final int COOLDOWN_MAX = 25;

    // Per-player history: deque of snapshots (oldest first)
    private static final Map<UUID, Deque<PlayerSnapshot>> history = new HashMap<>();
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activeRewinds = new HashSet<>();

    public static ItemStack getItem() {
        return ItemUtil.create(Material.WATCH, 1, ITEM_NAME,
                "&7Volta &e3 segundos &7no tempo.",
                "",
                "&eEfeitos:",
                "  &f- Teleporta para posição anterior",
                "  &f- Restaura vida anterior",
                "  &f- Invisível e invulnerável durante uso",
                "  &f- Regeneração I por 2s após",
                "",
                "&eCooldown: &f15-25s",
                "&7Categoria: &5Experimental");
    }

    public static boolean isRewindItem(ItemStack item) {
        return ItemUtil.hasName(item, ITEM_NAME);
    }

    /**
     * Called every 2 ticks to record player state.
     */
    public static void recordSnapshot(Player player) {
        if (activeRewinds.contains(player.getUniqueId())) return;
        if (player.getLocation().getY() < 0) return; // Don't snapshot in void

        Deque<PlayerSnapshot> deque = history.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
        deque.addLast(new PlayerSnapshot(
                player.getLocation().clone(),
                player.getHealth(),
                player.getVelocity().clone()
        ));

        // Keep only last SNAPSHOTS_NEEDED snapshots
        while (deque.size() > SNAPSHOTS_NEEDED) {
            deque.removeFirst();
        }
    }

    public static void use(Player player, Game game) {
        UUID uuid = player.getUniqueId();

        // Check void
        if (player.getLocation().getY() < 0) {
            ChatUtil.send(player, "&cNão é possível usar no void!");
            return;
        }

        // Check cooldown
        if (cooldowns.containsKey(uuid)) {
            long remaining = (cooldowns.get(uuid) - System.currentTimeMillis()) / 1000;
            if (remaining > 0) {
                ChatUtil.send(player, "&cAguarde &e" + remaining + "s &cpara usar novamente.");
                return;
            }
        }

        // Check history
        Deque<PlayerSnapshot> deque = history.get(uuid);
        if (deque == null || deque.size() < 5) {
            ChatUtil.send(player, "&cNenhum estado seguro encontrado para rebobinar.");
            return;
        }

        // Get oldest snapshot (3 seconds ago)
        PlayerSnapshot target = deque.peekFirst();
        if (target == null) return;

        // Validate target location (not void)
        if (target.location.getY() < 0) {
            ChatUtil.send(player, "&cEstado anterior inválido (void).");
            return;
        }

        // Start rewind
        activeRewinds.add(uuid);

        // Apply invisibility and invulnerability
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 1, false, false));
        player.setNoDamageTicks(40);

        // Play effect
        player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 2f);
        spawnRewindParticles(player);

        // Teleport after short delay (0.25s for visual effect)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    activeRewinds.remove(uuid);
                    return;
                }

                // Teleport
                player.teleport(target.location);

                // Restore health
                double restoredHealth = Math.min(target.health, player.getMaxHealth());
                if (restoredHealth < 1.0) restoredHealth = 1.0;
                player.setHealth(restoredHealth);

                // Restore velocity
                player.setVelocity(target.velocity);

                // Post-rewind effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false));
                player.setNoDamageTicks(20);

                // Remove invisibility and activate
                player.removePotionEffect(PotionEffectType.INVISIBILITY);

                // Play landing sound
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 0.5f);
                spawnArrivalParticles(player);

                // Title feedback
                ChatUtil.sendTitle(player, "&d&l⏮ REBOBINADO!", "&7Voltou 3 segundos.", 5, 30, 10);

                // Clear history after use
                history.remove(uuid);
                activeRewinds.remove(uuid);

                // Apply cooldown (random between 15-25s)
                int cooldownSeconds = COOLDOWN_MIN + new Random().nextInt(COOLDOWN_MAX - COOLDOWN_MIN + 1);
                cooldowns.put(uuid, System.currentTimeMillis() + (cooldownSeconds * 1000L));

                // Show cooldown timer in action bar
                startCooldownDisplay(player, cooldownSeconds);
            }
        }.runTaskLater(BedWarsX.getInstance(), 5L);
    }

    private static void startCooldownDisplay(Player player, int seconds) {
        new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (!player.isOnline() || remaining <= 0) {
                    if (player.isOnline()) {
                        ChatUtil.sendActionBar(player, "&d🔮 Rebobinar: &aPronto!");
                    }
                    cancel();
                    return;
                }

                ChatUtil.sendActionBar(player, "&d🔮 Rebobinar: &e" + remaining + "s");
                remaining--;
            }
        }.runTaskTimer(BedWarsX.getInstance(), 0L, 20L);
    }

    private static void spawnRewindParticles(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        ParticleUtil.spawnPortal(loc, 30);
        ParticleUtil.spawnEnchant(loc, 20);
    }

    private static void spawnArrivalParticles(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        ParticleUtil.spawnPortal(loc, 30);
        ParticleUtil.spawnWitch(loc, 20);
    }

    public static void clearPlayer(UUID uuid) {
        history.remove(uuid);
        cooldowns.remove(uuid);
        activeRewinds.remove(uuid);
    }

    public static boolean isRewinding(UUID uuid) {
        return activeRewinds.contains(uuid);
    }

    // ─── Snapshot data class ──────────────────────────────────────────────────

    private static class PlayerSnapshot {
        final Location location;
        final double health;
        final org.bukkit.util.Vector velocity;

        PlayerSnapshot(Location location, double health, org.bukkit.util.Vector velocity) {
            this.location = location;
            this.health = health;
            this.velocity = velocity;
        }
    }
}
