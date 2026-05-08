package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final BedWarsX plugin;

    public PlayerDeathListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null) {
            // Clear drops and death message - game handles everything
            event.getDrops().clear();
            event.setDropExp(0);
            event.setDeathMessage(null);
            event.setKeepInventory(true);
        }
    }
}
