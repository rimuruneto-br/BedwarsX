package dev.bedwarsx.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.List;

public class ItemUtil {

    public static ItemStack create(Material material, int amount, String name, String... lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (name != null) meta.setDisplayName(ChatUtil.color(name));
        if (lore.length > 0) meta.setLore(Arrays.asList(ChatUtil.color(Arrays.asList(lore)).toArray(new String[0])));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack create(Material material, int amount, short data, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, amount, data);
        ItemMeta meta = item.getItemMeta();
        if (name != null) meta.setDisplayName(ChatUtil.color(name));
        if (lore != null && !lore.isEmpty()) meta.setLore(ChatUtil.color(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createEnchanted(Material material, int amount, String name, Enchantment enchant, int level, String... lore) {
        ItemStack item = create(material, amount, name, lore);
        item.addUnsafeEnchantment(enchant, level);
        return item;
    }

    public static ItemStack createLeather(Material material, int r, int g, int b, String name) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(org.bukkit.Color.fromRGB(r, g, b));
        meta.setDisplayName(ChatUtil.color(name));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack setGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta meta = item.getItemMeta();
        // HideFlag for enchantments
        try {
            java.lang.reflect.Method addFlags = meta.getClass().getMethod("addItemFlags",
                    Class.forName("org.bukkit.inventory.ItemFlag[]"));
            Object hideEnchants = Class.forName("org.bukkit.inventory.ItemFlag")
                    .getField("HIDE_ENCHANTS").get(null);
            java.lang.reflect.Array.newInstance(Class.forName("org.bukkit.inventory.ItemFlag"), 1);
        } catch (Exception ignored) {}
        item.setItemMeta(meta);
        return item;
    }

    public static boolean hasName(ItemStack item, String name) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasDisplayName()) return false;
        return item.getItemMeta().getDisplayName().equals(ChatUtil.color(name));
    }

    public static boolean isNull(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}
