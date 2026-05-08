package dev.bedwarsx.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private final String id;
    private final String category;
    private final int slot;
    private final String name;
    private final ItemStack[] items;
    private final Material currency;
    private final int cost;
    private final String special;

    public ShopItem(String name, ItemStack[] items, Material currency, int cost) {
        this(normalizeId(name), "legacy", -1, name, items, currency, cost, "NORMAL");
    }

    public ShopItem(String id, String category, int slot, String name, ItemStack[] items, Material currency, int cost, String special) {
        this.id = normalizeId(id);
        this.category = category == null ? "misc" : category.toLowerCase();
        this.slot = slot;
        this.name = name;
        this.items = items;
        this.currency = currency;
        this.cost = cost;
        this.special = special == null ? "NORMAL" : special.toUpperCase();
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public int getSlot() { return slot; }
    public String getName() { return name; }
    public ItemStack[] getItems() { return items; }
    public Material getCurrency() { return currency; }
    public int getCost() { return cost; }
    public String getSpecial() { return special; }

    public static String normalizeId(String value) {
        if (value == null) return "";
        return value.replaceAll("(?i)(\u00A7|&)[0-9A-FK-OR]", "")
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .toLowerCase();
    }
}
