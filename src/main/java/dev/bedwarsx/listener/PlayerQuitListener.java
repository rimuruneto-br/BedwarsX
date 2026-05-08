package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final BedWarsX plugin;

    public PlayerQuitListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game != null) {
            if (game.getState() == GameState.IN_GAME) {
                game.handlePlayerDeath(player, null);
            }
            game.removePlayer(player);
        }

        // Limpar GUI / chat input pendente
        plugin.getGuiManager().fechar(player);
        plugin.getChatInputManager().cancelar(player);

        // Limpar itens experimentais
        plugin.getExperimentalItemManager().cleanupPlayer(player.getUniqueId());
        plugin.getPlayerSessionManager().clear(player);

        plugin.getScoreboardManager().removePlayer(player);

        event.setQuitMessage(ChatUtil.color("&8[&c-&8] &e" + player.getName() + " &7saiu do servidor."));
    }
}
