package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {

    private final BedWarsX plugin;

    public ItemDropListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        // Cancel drops outside game
        if (game == null || game.getState() != GameState.IN_GAME) {
            event.setCancelled(true);
        }
    }
}
