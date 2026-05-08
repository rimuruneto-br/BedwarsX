package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final BedWarsX plugin;

    public BlockPlaceListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) {
            event.setCancelled(true);
            return;
        }

        if (game.getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        GamePlayer gp = game.getGamePlayer(player);
        if (gp == null || !gp.isAlive()) {
            event.setCancelled(true);
            return;
        }

        // Track placed block for map restoration
        game.trackPlacedBlock(event.getBlock().getLocation());
    }
}
