package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final BedWarsX plugin;

    public PlayerMoveListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game == null || game.getState() != GameState.IN_GAME) return;

        GamePlayer gp = game.getGamePlayer(player);
        if (gp == null || !gp.isAlive()) return;

        // Void detection
        if (event.getTo().getY() < -5) {
            game.handlePlayerDeath(player, null);
        }

        // Lock movement in STARTING phase
        if (game.getState() == GameState.STARTING) {
            // Allow movement in lobby area - no lock needed
        }
    }
}
