package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileListener implements Listener {

    private final BedWarsX plugin;

    public ProjectileListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // Visual feedback for arrows
    }
}
