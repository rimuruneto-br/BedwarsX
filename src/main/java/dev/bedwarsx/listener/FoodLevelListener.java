package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelListener implements Listener {

    private final BedWarsX plugin;

    public FoodLevelListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        Game game = plugin.getGameManager().getPlayerGame(player);

        // In lobby or waiting: don't lose hunger
        if (game == null || game.getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }
}
