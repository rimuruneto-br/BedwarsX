package dev.bedwarsx.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Interface base para todas as GUIs do BedWarsX.
 */
public interface BedWarsGui {
    Inventory getInventory();
    void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event);
}
