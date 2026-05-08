package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final BedWarsX plugin;

    public PlayerRespawnListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null) {
            // Set respawn to arena lobby to prevent world spawn
            if (game.getArena().getLobby() != null) {
                event.setRespawnLocation(game.getArena().getLobby());
            }
        } else {
            // Lobby respawn
            event.setRespawnLocation(plugin.getConfigManager().getLobbyLocation());
        }
    }
}
