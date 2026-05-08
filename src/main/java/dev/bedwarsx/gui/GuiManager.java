package dev.bedwarsx.gui;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gerencia todas as GUIs abertas no servidor.
 * Cada GUI registra um handler funcional para cliques.
 */
public class GuiManager implements Listener {

    private final BedWarsX plugin;
    private final Map<UUID, BedWarsGui> openGuis = new HashMap<>();

    public GuiManager(BedWarsX plugin) {
        this.plugin = plugin;
    }

    public void abrir(Player player, BedWarsGui gui) {
        openGuis.put(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory());
    }

    public void fechar(Player player) {
        openGuis.remove(player.getUniqueId());
    }

    public BedWarsGui getGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        BedWarsGui gui = openGuis.get(player.getUniqueId());
        if (gui == null) return;
        if (!event.getView().getTitle().equals(gui.getInventory().getTitle())) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(gui.getInventory())) return;

        gui.onClick(player, event.getSlot(), event.getCurrentItem(), event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (openGuis.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        openGuis.remove(player.getUniqueId());
    }
}
