package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.shop.ShopManager;
import dev.bedwarsx.upgrade.TeamUpgradeManager;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Delega cliques de shop (NPC loja em jogo) ao ShopManager.
 * GUIs admin/lobby são tratadas pelo GuiManager.
 */
public class InventoryClickListener implements Listener {

    private final BedWarsX plugin;
    private final ShopManager shopManager;
    private final TeamUpgradeManager upgradeManager;

    public InventoryClickListener(BedWarsX plugin) {
        this.plugin = plugin;
        this.shopManager = new ShopManager(plugin);
        this.upgradeManager = new TeamUpgradeManager(plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        // Lojas de jogo (abertas por NPC)
        if (title.contains("Loja") || title.contains("Shop") || title.contains("Experimental") || title.contains("Quick Buy")) {
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta()) return;
            Game game = plugin.getGameManager().getPlayerGame(player);
            shopManager.handleShopClick(player, clicked, game, event);
            return;
        }

        if (title.contains("Team Upgrades")) {
            event.setCancelled(true);
            upgradeManager.handleClick(player, clicked);
        }
    }
}
