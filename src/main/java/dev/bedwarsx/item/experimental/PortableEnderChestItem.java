package dev.bedwarsx.item.experimental;

import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PortableEnderChestItem {
    private static final String ITEM_NAME = "&5&lBAU DO FIM PORTATIL";

    public static ItemStack getItem() {
        return ItemUtil.create(Material.ENDER_CHEST, 1, ITEM_NAME,
                "&7Abre seu bau do fim",
                "&7durante a partida.");
    }

    public static boolean isPortableEnderChestItem(ItemStack item) {
        return ItemUtil.hasName(item, ITEM_NAME);
    }

    public static void use(Player player) {
        player.openInventory(player.getEnderChest());
    }
}
