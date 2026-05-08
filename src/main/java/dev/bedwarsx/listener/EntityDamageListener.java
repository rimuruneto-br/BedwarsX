package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.item.experimental.RewindItem;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final BedWarsX plugin;

    public EntityDamageListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Cancel damage if rewinding
        if (RewindItem.isRewinding(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game == null) return;

        GamePlayer gp = game.getGamePlayer(player);
        if (gp == null) return;

        // Cancel damage if dead/eliminated/spectating
        if (!gp.isAlive()) {
            event.setCancelled(true);
            return;
        }

        // Void damage - cancel (handled separately)
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            Player killer = null;
            java.util.UUID killerId = game.getArena().findRecentDamager(player.getUniqueId(), 15000L);
            if (killerId != null) killer = Bukkit.getPlayer(killerId);
            game.handlePlayerDeath(player, killer);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        // Get attacker
        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }

        Game game = plugin.getGameManager().getPlayerGame(victim);
        if (game == null) return;

        // Cancel friendly fire
        if (attacker != null && game.areSameTeam(victim, attacker)) {
            event.setCancelled(true);
            return;
        }

        if (attacker != null) {
            game.getArena().registerLastDamage(victim.getUniqueId(), attacker.getUniqueId());
        }

        // Cancel damage to dead players
        GamePlayer gp = game.getGamePlayer(victim);
        if (gp != null && !gp.isAlive()) {
            event.setCancelled(true);
            return;
        }

        // Check if victim would die from this hit
        if (victim.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            game.handlePlayerDeath(victim, attacker);
        }
    }
}
