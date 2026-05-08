package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final BedWarsX plugin;

    public ChatListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) return;

        GamePlayer gp = game.getGamePlayer(player);
        if (gp == null) return;

        event.setCancelled(true);

        String prefix;
        if (gp.isSpectator()) {
            prefix = ChatUtil.color("&7[SPEC] &f");
        } else if (gp.getTeam() != null) {
            prefix = ChatUtil.color(gp.getTeam().getColor() + "[" + gp.getTeam().getDisplayName() + "] &f");
        } else {
            prefix = "";
        }

        String formatted = prefix + player.getName() + " &7» &f" + event.getMessage();
        String colored = ChatUtil.color(formatted);

        // Send to all players in the game (same team only for dead? No - all can see)
        for (java.util.UUID uuid : game.getPlayers().keySet()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) p.sendMessage(colored);
        }
        for (java.util.UUID uuid : game.getSpectators()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) p.sendMessage(colored);
        }
    }
}
