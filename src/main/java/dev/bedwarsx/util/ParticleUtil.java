package dev.bedwarsx.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Compatibility wrapper for particle effects in 1.8.9.
 * In 1.8.9, particles are spawned via Effect enum and World#spigot().
 */
public class ParticleUtil {

    // Particle names for 1.8.9 (Spigot API Effect enum)
    public enum ParticleType {
        PORTAL("PORTAL"),
        FLAME("FLAME"),
        SMOKE("LARGE_SMOKE"),
        EXPLOSION("LARGE_EXPLOSION"),
        ENCHANT("ENCHANTMENT_TABLE"),
        WITCH("WITCH_MAGIC"),
        FIREWORK("FIREWORKS_SPARK"),
        VILLAGER("HAPPY_VILLAGER"),
        HEART("HEART"),
        CRIT("CRIT"),
        MAGIC_CRIT("MAGIC_CRIT");

        private final String effectName;

        ParticleType(String effectName) {
            this.effectName = effectName;
        }

        public String getEffectName() {
            return effectName;
        }
    }

    public static void spawn(Location loc, ParticleType type, int count,
                              float offsetX, float offsetY, float offsetZ, float speed) {
        if (loc.getWorld() == null) return;
        try {
            // Use World spigot API for 1.8.9
            Object spigot = loc.getWorld().getClass().getMethod("spigot").invoke(loc.getWorld());
            Class<?> effectClass = Class.forName("org.bukkit.Effect");
            Object effect = null;
            for (Object e : (Object[]) effectClass.getMethod("values").invoke(null)) {
                if (e.toString().equals(type.getEffectName())) {
                    effect = e;
                    break;
                }
            }
            if (effect == null) return;

            Method playEffect = spigot.getClass().getMethod("playEffect",
                    Location.class, effectClass, int.class, int.class,
                    float.class, float.class, float.class,
                    float.class, int.class, int.class);
            playEffect.invoke(spigot, loc, effect, 0, 0,
                    offsetX, offsetY, offsetZ, speed, count, 256);
        } catch (Exception e) {
            // Fallback - silently fail
        }
    }

    public static void spawnForPlayer(Player player, Location loc, ParticleType type,
                                       int count, float offsetX, float offsetY, float offsetZ, float speed) {
        try {
            Object spigot = player.getClass().getMethod("spigot").invoke(player);
            Class<?> effectClass = Class.forName("org.bukkit.Effect");
            Object effect = null;
            for (Object e : (Object[]) effectClass.getMethod("values").invoke(null)) {
                if (e.toString().equals(type.getEffectName())) {
                    effect = e;
                    break;
                }
            }
            if (effect == null) return;

            Method playEffect = spigot.getClass().getMethod("playEffect",
                    Location.class, effectClass, int.class, int.class,
                    float.class, float.class, float.class,
                    float.class, int.class, int.class);
            playEffect.invoke(spigot, loc, effect, 0, 0,
                    offsetX, offsetY, offsetZ, speed, count, 32);
        } catch (Exception e) {
            // Silently fail
        }
    }

    public static void spawnPortal(Location loc, int count) {
        spawn(loc, ParticleType.PORTAL, count, 0.5f, 0.5f, 0.5f, 0.1f);
    }

    public static void spawnFlame(Location loc, int count) {
        spawn(loc, ParticleType.FLAME, count, 0.4f, 0.4f, 0.4f, 0.05f);
    }

    public static void spawnExplosion(Location loc) {
        spawn(loc, ParticleType.EXPLOSION, 3, 0.3f, 0.3f, 0.3f, 0f);
    }

    public static void spawnFirework(Location loc, int count) {
        spawn(loc, ParticleType.FIREWORK, count, 0.5f, 0.5f, 0.5f, 0.1f);
    }

    public static void spawnEnchant(Location loc, int count) {
        spawn(loc, ParticleType.ENCHANT, count, 0.5f, 0.5f, 0.5f, 0.1f);
    }

    public static void spawnWitch(Location loc, int count) {
        spawn(loc, ParticleType.WITCH, count, 0.5f, 0.5f, 0.5f, 0.1f);
    }
}
