package dev.bedwarsx.listener;

import dev.bedwarsx.gui.lobby.LobbyItemManager;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.npc.BedWarsNPC;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final BedWarsX plugin;
    private final LobbyItemManager lobbyItems;

    public PlayerJoinListener(BedWarsX plugin) {
        this.plugin = plugin;
        this.lobbyItems = new LobbyItemManager(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.teleport(plugin.getConfigManager().getLobbyLocation());
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(20.0);
            player.setFoodLevel(20);
            PlayerUtil.clearPotionEffects(player);
            player.setExp(0);
            player.setLevel(0);

            // Dar itens do lobby
            lobbyItems.darItensLobby(player);

            // Spawnar NPCs para o jogador
            for (BedWarsNPC npc : plugin.getNpcManager().getNpcs()) {
                plugin.getNpcManager().spawnNPCForPlayer(npc, player);
            }

            // Scoreboard do lobby
            plugin.getScoreboardManager().updatePlayer(player, null);

            // Mensagem de boas-vindas
            player.sendMessage(ChatUtil.color("&8&m══════════════════════════════"));
            player.sendMessage(ChatUtil.color("   &6✦ &lBedWarsX &r&7— Bem-vindo!"));
            player.sendMessage(ChatUtil.color("   &7Use &e/bw &7para jogar."));
            player.sendMessage(ChatUtil.color("&8&m══════════════════════════════"));

        }, 5L);

        event.setJoinMessage(ChatUtil.color("&8[&a+&8] &e" + player.getName() + " &7entrou no servidor."));
    }
}
