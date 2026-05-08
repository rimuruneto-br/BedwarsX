package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.gui.lobby.ArenaSelectorGui;
import dev.bedwarsx.gui.lobby.PlayerSettingsGui;
import dev.bedwarsx.gui.lobby.PlayerStatsGui;
import dev.bedwarsx.gui.lobby.LobbyItemManager;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.shop.ShopCategory;
import dev.bedwarsx.shop.ShopManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private final BedWarsX plugin;
    private final ShopManager shopManager;

    public PlayerInteractListener(BedWarsX plugin) {
        this.plugin = plugin;
        this.shopManager = new ShopManager(plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;

        Action action = event.getAction();
        boolean isRightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (!isRightClick) return;

        // ── Itens do lobby ────────────────────────────────────────────────────
        if (!plugin.getGameManager().isInGame(player)) {
            if (item.getType() == Material.COMPASS) {
                event.setCancelled(true);
                plugin.getGuiManager().abrir(player, new ArenaSelectorGui(plugin));
                return;
            }
            if (item.getType() == Material.BOOK) {
                event.setCancelled(true);
                plugin.getGuiManager().abrir(player, new PlayerStatsGui(plugin, player));
                return;
            }
            if (item.getType() == Material.REDSTONE_COMPARATOR) {
                event.setCancelled(true);
                plugin.getGuiManager().abrir(player, new PlayerSettingsGui(plugin, player));
                return;
            }
            return;
        }

        // ── Itens experimentais ────────────────────────────────────────────────
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game != null) {
            boolean handled = plugin.getExperimentalItemManager().handleUse(player, item);
            if (handled) event.setCancelled(true);
        }
    }
}
