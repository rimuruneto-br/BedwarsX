package dev.bedwarsx.gui;

import dev.bedwarsx.main.BedWarsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Coleta input de texto via chat para menus administrativos.
 */
public class ChatInputManager implements Listener {

    private final BedWarsX plugin;
    private final Map<UUID, Consumer<String>> pendingInputs = new HashMap<>();

    public ChatInputManager(BedWarsX plugin) {
        this.plugin = plugin;
    }

    /** Aguarda o próximo chat do jogador e chama o callback com o texto. */
    public void esperar(Player player, Consumer<String> callback) {
        pendingInputs.put(player.getUniqueId(), callback);
    }

    public boolean estaEsperando(Player player) {
        return pendingInputs.containsKey(player.getUniqueId());
    }

    public void cancelar(Player player) {
        pendingInputs.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Consumer<String> callback = pendingInputs.remove(player.getUniqueId());
        if (callback == null) return;

        event.setCancelled(true);
        String input = event.getMessage().trim();

        // Run on main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(input));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingInputs.remove(event.getPlayer().getUniqueId());
    }
}
