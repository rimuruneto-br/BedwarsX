package dev.bedwarsx.shop;

import dev.bedwarsx.util.ChatUtil;

import java.util.HashMap;
import java.util.Map;

public class ShopRegistry {

    private static final Map<String, ShopItem> items = new HashMap<>();
    private static final Map<String, ShopItem> byId = new HashMap<>();

    public static void register(String name, ShopItem item) {
        items.put(ChatUtil.color(name), item);
        if (item != null) byId.put(item.getId(), item);
    }

    public static ShopItem getItem(String displayName) {
        return items.get(displayName);
    }

    public static ShopItem getById(String id) {
        ShopItem item = byId.get(ShopItem.normalizeId(id));
        if (item != null) return item;
        return byId.get(id);
    }

    public static Map<String, ShopItem> getItems() {
        return items;
    }
}
