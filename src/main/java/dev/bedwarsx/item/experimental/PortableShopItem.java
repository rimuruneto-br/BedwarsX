package dev.bedwarsx.item.experimental;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.shop.ShopCategory;
import dev.bedwarsx.shop.ShopManager;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PortableShopItem {
    private static final String ITEM_NAME = "&6&lLOJA PORTATIL";

    public static ItemStack getItem() {
        return ItemUtil.create(Material.EMERALD, 1, ITEM_NAME,
                "&7Abre a loja de itens",
                "&7em qualquer lugar da partida.");
    }

    public static boolean isPortableShopItem(ItemStack item) {
        return ItemUtil.hasName(item, ITEM_NAME);
    }

    public static void use(Player player) {
        new ShopManager(BedWarsX.getInstance()).openShop(player, ShopCategory.MAIN);
    }
}
