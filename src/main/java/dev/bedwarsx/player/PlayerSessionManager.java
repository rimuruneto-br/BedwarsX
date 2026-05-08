package dev.bedwarsx.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSessionManager {
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Snapshot> snapshots = new ConcurrentHashMap<>();
    private final Set<UUID> restoring = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    public PlayerSession getOrCreate(Player player) {
        PlayerSession session = sessions.computeIfAbsent(player.getUniqueId(), PlayerSession::new);
        session.setPlayerName(player.getName());
        return session;
    }

    public void snapshot(Player player) {
        if (player == null || snapshots.containsKey(player.getUniqueId())) return;
        snapshots.put(player.getUniqueId(), new Snapshot(player));
    }

    public boolean restore(Player player, boolean teleport) {
        if (player == null || !restoring.add(player.getUniqueId())) return false;
        Snapshot snapshot = snapshots.remove(player.getUniqueId());
        if (snapshot == null) {
            restoring.remove(player.getUniqueId());
            return false;
        }
        try {
            snapshot.restore(player, teleport);
            return true;
        } finally {
            restoring.remove(player.getUniqueId());
        }
    }

    public void clear(Player player) {
        if (player == null) return;
        sessions.remove(player.getUniqueId());
        snapshots.remove(player.getUniqueId());
        restoring.remove(player.getUniqueId());
    }

    private static class Snapshot {
        private final Location location;
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final GameMode gameMode;
        private final float exp;
        private final int level;
        private final double health;
        private final int food;
        private final boolean allowFlight;
        private final boolean flying;
        private final float walkSpeed;
        private final Collection<PotionEffect> effects;

        Snapshot(Player player) {
            this.location = player.getLocation().clone();
            this.contents = player.getInventory().getContents().clone();
            this.armor = player.getInventory().getArmorContents().clone();
            this.gameMode = player.getGameMode();
            this.exp = player.getExp();
            this.level = player.getLevel();
            this.health = player.getHealth();
            this.food = player.getFoodLevel();
            this.allowFlight = player.getAllowFlight();
            this.flying = player.isFlying();
            this.walkSpeed = player.getWalkSpeed();
            this.effects = new ArrayList<>(player.getActivePotionEffects());
        }

        void restore(Player player, boolean teleport) {
            player.getInventory().setContents(contents);
            player.getInventory().setArmorContents(armor);
            player.setGameMode(gameMode);
            player.setExp(exp);
            player.setLevel(level);
            player.setHealth(Math.min(player.getMaxHealth(), Math.max(1.0, health)));
            player.setFoodLevel(food);
            player.setAllowFlight(allowFlight);
            player.setFlying(flying && allowFlight);
            player.setWalkSpeed(walkSpeed);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            for (PotionEffect effect : effects) {
                player.addPotionEffect(effect);
            }
            if (teleport && location != null && location.getWorld() != null) {
                player.teleport(location);
            }
            player.updateInventory();
        }
    }
}
