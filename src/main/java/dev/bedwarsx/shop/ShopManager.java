package dev.bedwarsx.shop;

import dev.bedwarsx.item.experimental.FireballJumpItem;
import dev.bedwarsx.game.Game;
import dev.bedwarsx.item.experimental.PortableEnderChestItem;
import dev.bedwarsx.item.experimental.PortableShopItem;
import dev.bedwarsx.item.experimental.RewindItem;
import dev.bedwarsx.item.experimental.SlingshotItem;
import dev.bedwarsx.item.experimental.SwitcherItem;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopManager {

    private final BedWarsX plugin;
    private static boolean catalogSeeded;

    public ShopManager(BedWarsX plugin) {
        this.plugin = plugin;
        ensureCatalogSeeded();
    }

    public void openShop(Player player, ShopCategory category) {
        switch (category) {
            case MAIN:
                openMainShop(player);
                break;
            case QUICK_BUY:
                openQuickBuy(player);
                break;
            case BLOCKS:
                openBlocksShop(player);
                break;
            case WEAPONS:
                openWeaponsShop(player);
                break;
            case ARMOR:
                openArmorShop(player);
                break;
            case TOOLS:
                openToolsShop(player);
                break;
            case POTIONS:
                openPotionsShop(player);
                break;
            case EXPERIMENTAL:
                openExperimentalShop(player);
                break;
        }
    }

    private void openMainShop(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color("&6&lItem Shop"));

        // Category buttons
        inv.setItem(9, ItemUtil.create(Material.NETHER_STAR, 1, (short)0, "&e&lQuick Buy",
                Arrays.asList("&7Your favorite items.", "&7Shift-right items in shops", "&7to pin them here.")));
        inv.setItem(10, ItemUtil.create(Material.WOOL, 1, (short)14, "&c&lBlocks",
                Arrays.asList("&7Purchase building blocks", "&7for bridges and defense.")));
        inv.setItem(11, ItemUtil.create(Material.DIAMOND_SWORD, 1, (short)0, "&b&lWeapons",
                Arrays.asList("&7Upgrade your arsenal.")));
        inv.setItem(12, ItemUtil.create(Material.DIAMOND_CHESTPLATE, 1, (short)0, "&a&lArmor",
                Arrays.asList("&7Protect yourself.")));
        inv.setItem(13, ItemUtil.create(Material.IRON_PICKAXE, 1, (short)0, "&e&lTools",
                Arrays.asList("&7Mining tools and utilities.")));
        inv.setItem(14, ItemUtil.create(Material.BREWING_STAND_ITEM, 1, (short)0, "&d&lPotions",
                Arrays.asList("&7Combat enhancements.")));
        inv.setItem(15, ItemUtil.create(Material.ENDER_PEARL, 1, (short)0, "&5&l✦ Experimental",
                Arrays.asList("&7Experimental items.", "&eNew and powerful tools!")));

        // Fill with glass
        fillGlass(inv);

        player.openInventory(inv);
    }

    private void openQuickBuy(Player player) {
        ensureCatalogSeeded();
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color("&e&lQuick Buy"));
        List<String> favorites = plugin.getQuickBuyManager().getFavorites(player);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int index = 0;
        for (String id : favorites) {
            ShopItem shopItem = ShopRegistry.getById(id);
            if (shopItem == null || index >= slots.length) continue;
            ItemStack preview = shopItem.getItems()[0].clone();
            org.bukkit.inventory.meta.ItemMeta meta = preview.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatUtil.color("&7Quick Buy favorite"));
            lore.add(ChatUtil.color("&6Cost: &e" + shopItem.getCost() + " " + formatMaterial(shopItem.getCurrency())));
            lore.add(ChatUtil.color("&8Shift-right to remove"));
            meta.setDisplayName(ChatUtil.color(shopItem.getName()));
            meta.setLore(lore);
            preview.setItemMeta(meta);
            inv.setItem(slots[index++], preview);
        }
        if (index == 0) {
            inv.setItem(22, ItemUtil.create(Material.PAPER, 1, "&7No favorites yet",
                    "&7Shift-right an item in any shop",
                    "&7to add it to Quick Buy."));
        }
        addBackButton(inv);
        fillGlass(inv);
        player.openInventory(inv);
    }

    private void openBlocksShop(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color("&c&lBlocks Shop"));

        // Wool - Iron
        inv.setItem(10, buildShopItem(Material.WOOL, 16, (short)14, "&fWool x16",
                Material.IRON_INGOT, 4, "&7Building block."));
        // Hardened Clay - Iron
        inv.setItem(11, buildShopItem(Material.STAINED_CLAY, 16, (short)14, "&6Hardened Clay x16",
                Material.IRON_INGOT, 12, "&7Stronger building block."));
        // Blast-Proof Glass - Iron
        inv.setItem(12, buildShopItem(Material.GLASS, 4, (short)0, "&fBlast-Proof Glass x4",
                Material.IRON_INGOT, 12, "&7Blocks projectiles."));
        // End Stone - Iron
        inv.setItem(13, buildShopItem(Material.ENDER_STONE, 4, (short)0, "&aEnd Stone x4",
                Material.IRON_INGOT, 24, "&7Very strong building block."));
        // Wood - Iron
        inv.setItem(14, buildShopItem(Material.WOOD, 16, (short)0, "&6Wood x16",
                Material.IRON_INGOT, 8, "&7Classic building material."));
        // Obsidian - Emerald
        inv.setItem(15, buildShopItem(Material.OBSIDIAN, 4, (short)0, "&8Obsidian x4",
                Material.EMERALD, 4, "&7Ultimate bed protection."));
        // Ladder - Iron
        inv.setItem(16, buildShopItem(Material.LADDER, 16, (short)0, "&7Ladder x16",
                Material.IRON_INGOT, 4, "&7Climb surfaces."));

        addBackButton(inv);
        fillGlass(inv);
        player.openInventory(inv);
    }

    private void openWeaponsShop(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color("&b&lWeapons Shop"));

        inv.setItem(10, buildShopItem(Material.STONE_SWORD, 1, (short)0, "&7Stone Sword",
                Material.IRON_INGOT, 10, "&7Sharp stone blade."));
        inv.setItem(11, buildShopItem(Material.IRON_SWORD, 1, (short)0, "&fIron Sword",
                Material.GOLD_INGOT, 7, "&7Reliable iron sword."));
        inv.setItem(12, buildShopItem(Material.DIAMOND_SWORD, 1, (short)0, "&bDiamond Sword",
                Material.DIAMOND, 4, "&7The ultimate blade."));
        inv.setItem(13, buildShopItem(Material.BOW, 1, (short)0, "&6Bow",
                Material.GOLD_INGOT, 12, "&7Ranged weapon."));
        inv.setItem(14, buildShopItem(Material.ARROW, 8, (short)0, "&7Arrow x8",
                Material.GOLD_INGOT, 2, "&7Ammunition for your bow."));

        addBackButton(inv);
        fillGlass(inv);
        player.openInventory(inv);
    }

    private void openArmorShop(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color("&a&lArmor Shop"));

        inv.setItem(10, buildShopItem(Material.CHAINMAIL_BOOTS, 1, (short)0, "&7Chain Boots",
                Material.IRON_INGOT, 40, "&7Chainmail boots protection."));
        inv.setItem(11, buildShopItem(Material.IRON_BOOTS, 1, (short)0, "&fIron Boots",
                Material.GOLD_INGOT, 6, "&7Iron boots protection."));
        inv.setItem(12, buildShopItem(Material.DIAMOND_BOOTS, 1, (short)0, "&bDiamond Boots",
                Material.DIAMOND, 6, "&7Diamond boots protection."));

        inv.setItem(19, buildShopItem(Material.CHAINMAIL_LEGGINGS, 1, (short)0, "&7Chain Leggings",
                Material.IRON_INGOT, 40, "&7Chainmail leggings protection."));
        inv.setItem(20, buildShopItem(Material.IRON_LEGGINGS, 1, (short)0, "&fIron Leggings",
                Material.GOLD_INGOT, 10, "&7Iron leggings protection."));
        inv.setItem(21, buildShopItem(Material.DIAMOND_LEGGINGS, 1, (short)0, "&bDiamond Leggings",
                Material.DIAMOND, 12, "&7Diamond leggings protection."));

        addBackButton(inv);
        fillGlass(inv);
        player.openInventory(inv);
    }

    private void openToolsShop(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color("&e&lTools Shop"));

        inv.setItem(10, buildShopItem(Material.WOOD_PICKAXE, 1, (short)0, "&6Wooden Pickaxe",
                Material.IRON_INGOT, 10, "&7Basic mining tool."));
        inv.setItem(11, buildShopItem(Material.STONE_PICKAXE, 1, (short)0, "&7Stone Pickaxe",
                Material.IRON_INGOT, 20, "&7Faster mining."));
        inv.setItem(12, buildShopItem(Material.IRON_PICKAXE, 1, (short)0, "&fIron Pickaxe",
                Material.GOLD_INGOT, 4, "&7Efficient mining tool."));
        inv.setItem(13, buildShopItem(Material.DIAMOND_PICKAXE, 1, (short)0, "&bDiamond Pickaxe",
                Material.GOLD_INGOT, 8, "&7Ultimate mining tool."));
        inv.setItem(14, buildShopItem(Material.SHEARS, 1, (short)0, "&7Shears",
                Material.IRON_INGOT, 20, "&7Cuts through wool instantly."));
        inv.setItem(15, buildShopItem(Material.TNT, 1, (short)0, "&cTNT",
                Material.GOLD_INGOT, 4, "&7Explosive destruction."));
        inv.setItem(16, buildShopItem(Material.WATER_BUCKET, 1, (short)0, "&9Water Bucket",
                Material.GOLD_INGOT, 4, "&7Place water for defense."));
        inv.setItem(19, buildShopItem(Material.GOLDEN_APPLE, 1, (short)0, "&6Golden Apple",
                Material.GOLD_INGOT, 3, "&7Restores health."));
        inv.setItem(20, buildShopItem(Material.ENDER_PEARL, 1, (short)0, "&5Ender Pearl",
                Material.EMERALD, 4, "&7Teleportation tool."));
        inv.setItem(21, buildShopItem(Material.FLINT_AND_STEEL, 1, (short)0, "&6Flint and Steel",
                Material.GOLD_INGOT, 6, "&7Light fires."));

        addBackButton(inv);
        fillGlass(inv);
        player.openInventory(inv);
    }

    private void openPotionsShop(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color("&d&lPotions Shop"));

        inv.setItem(10, buildShopItem(Material.POTION, 1, (short)8193, "&aSpeed Potion I",
                Material.GOLD_INGOT, 4, "&7Speed I for 45 seconds."));
        inv.setItem(11, buildShopItem(Material.POTION, 1, (short)8194, "&aSpeed Potion II",
                Material.GOLD_INGOT, 10, "&7Speed II for 30 seconds."));
        inv.setItem(12, buildShopItem(Material.POTION, 1, (short)8225, "&aJump Potion V",
                Material.GOLD_INGOT, 6, "&7Jump Boost V for 45 seconds."));
        inv.setItem(13, buildShopItem(Material.POTION, 1, (short)8232, "&cInvisibility Potion",
                Material.EMERALD, 4, "&7Invisible for 30 seconds."));

        addBackButton(inv);
        fillGlass(inv);
        player.openInventory(inv);
    }

    private void openExperimentalShop(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color("&5&l✦ Experimental Shop"));

        // Header
        inv.setItem(4, ItemUtil.create(Material.NETHER_STAR, 1, "&5&l✦ EXPERIMENTAL",
                "&7These items are experimental",
                "&7and may be rebalanced.",
                "",
                "&eUse them to gain an edge!"));

        // Rewind
        inv.setItem(20, ItemUtil.create(Material.WATCH, 1, "&d&l🔮 REBOBINAR",
                "&7Voltar 3 segundos no tempo.",
                "",
                "&eEfeitos:",
                "  &7- Teleporta para posição anterior",
                "  &7- Restaura vida anterior",
                "  &7- Invulnerável durante uso",
                "  &7- Regeneração I por 2s",
                "",
                "&eCooldown: &f15-25 segundos",
                "",
                "&6Custo: &b12 Esmeraldas"));

        // Fireball Jump
        inv.setItem(22, ItemUtil.create(Material.FIREBALL, 1, "&c&l💣 FIREBALL JUMP",
                "&7Impulso explosivo!",
                "",
                "&eEfeitos:",
                "  &7- Lança ~36 blocos de distância",
                "  &7- Impulso vertical leve",
                "  &7- Som de explosão",
                "  &7- Partículas",
                "",
                "&eFoco: &fMobilidade ofensiva",
                "",
                "&6Custo: &68 Pepitas de Ouro"));

        // Slingshot
        inv.setItem(24, ItemUtil.create(Material.LEASH, 1, "&a&l🪃 ESTILINGUE",
                "&7Impulso controlado.",
                "",
                "&eEfeitos:",
                "  &7- Lança para frente e para cima",
                "  &7- Impulso consistente",
                "  &7- Cooldown: 5 segundos",
                "",
                "&eFoco: &fMobilidade rápida",
                "",
                "&6Custo: &76 Ferros"));

        inv.setItem(20, buildSpecialShopItem(inv.getItem(20), Material.EMERALD, 12));
        inv.setItem(22, buildSpecialShopItem(inv.getItem(22), Material.GOLD_INGOT, 8));
        inv.setItem(24, buildSpecialShopItem(inv.getItem(24), Material.IRON_INGOT, 6));
        inv.setItem(29, buildSpecialShopItem(SwitcherItem.getItem(), Material.EMERALD, 6));
        inv.setItem(31, buildSpecialShopItem(PortableShopItem.getItem(), Material.GOLD_INGOT, 6));
        inv.setItem(33, buildSpecialShopItem(PortableEnderChestItem.getItem(), Material.EMERALD, 3));

        addBackButton(inv);
        fillGlass(inv);
        player.openInventory(inv);
    }

    public boolean handleShopClick(Player player, ItemStack clicked, Game game) {
        return handleShopClick(player, clicked, game, null);
    }

    public boolean handleShopClick(Player player, ItemStack clicked, Game game, InventoryClickEvent event) {
        if (clicked == null || !clicked.hasItemMeta()) return false;
        if (!clicked.getItemMeta().hasDisplayName()) return false;

        String name = clicked.getItemMeta().getDisplayName();

        if (name.equals(ChatUtil.color("&7← Back"))) {
            openMainShop(player);
            return true;
        }

        // Category buttons
        if (name.equals(ChatUtil.color("&e&lQuick Buy"))) { openShop(player, ShopCategory.QUICK_BUY); return true; }
        if (name.equals(ChatUtil.color("&c&lBlocks"))) { openShop(player, ShopCategory.BLOCKS); return true; }
        if (name.equals(ChatUtil.color("&b&lWeapons"))) { openShop(player, ShopCategory.WEAPONS); return true; }
        if (name.equals(ChatUtil.color("&a&lArmor"))) { openShop(player, ShopCategory.ARMOR); return true; }
        if (name.equals(ChatUtil.color("&e&lTools"))) { openShop(player, ShopCategory.TOOLS); return true; }
        if (name.equals(ChatUtil.color("&d&lPotions"))) { openShop(player, ShopCategory.POTIONS); return true; }
        if (name.equals(ChatUtil.color("&5&l✦ Experimental"))) { openShop(player, ShopCategory.EXPERIMENTAL); return true; }

        if (event != null && event.isShiftClick() && event.isRightClick()) {
            ShopItem item = ShopRegistry.getItem(name);
            if (item != null) {
                boolean added = plugin.getQuickBuyManager().toggleFavorite(player, item.getId());
                ChatUtil.send(player, added ? "&aAdded to Quick Buy." : "&eRemoved from Quick Buy.");
                return true;
            }
        }

        return handlePurchase(player, name, game);
    }

    private boolean handlePurchase(Player player, String itemName, Game game) {
        ShopItem shopItem = ShopRegistry.getItem(itemName);
        if (shopItem == null) return false;

        // Check currency
        int cost = shopItem.getCost();
        Material currency = shopItem.getCurrency();

        int playerHas = countMaterial(player, currency);
        if (playerHas < cost) {
            ChatUtil.send(player, "&cYou need &e" + cost + " &c" + formatMaterial(currency) + "! You have &e" + playerHas + "&c.");
            player.playSound(player.getLocation(), org.bukkit.Sound.NOTE_BASS, 1f, 0.5f);
            return true;
        }

        // Remove currency
        removeMaterial(player, currency, cost);

        // Give item
        for (ItemStack reward : shopItem.getItems()) {
            giveItem(player, reward.clone());
        }

        ChatUtil.send(player, "&aPurchased: &f" + itemName);
        player.playSound(player.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1f, 1f);
        return true;
    }

    private int countMaterial(Player player, Material mat) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeMaterial(Player player, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            if (contents[i] != null && contents[i].getType() == mat) {
                if (contents[i].getAmount() <= remaining) {
                    remaining -= contents[i].getAmount();
                    contents[i] = null;
                } else {
                    contents[i].setAmount(contents[i].getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
        player.getInventory().setContents(contents);
    }

    private void giveItem(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }
    }

    private String formatMaterial(Material mat) {
        return mat.name().replace("_", " ").toLowerCase();
    }

    private ItemStack buildShopItem(Material mat, int amount, short data, String name, Material currency, int cost, String... lore) {
        List<String> loreList = new ArrayList<>();
        for (String l : lore) loreList.add(ChatUtil.color(l));
        loreList.add("");
        loreList.add(ChatUtil.color("&6Cost: &e" + cost + " " + formatMaterial(currency)));

        ShopRegistry.register(name, new ShopItem(ShopItem.normalizeId(name), "default", -1, name,
                new ItemStack[]{new ItemStack(mat, amount, data)}, currency, cost, "NORMAL"));

        return ItemUtil.create(mat, amount, data, name, loreList);
    }

    private ItemStack buildSpecialShopItem(ItemStack item, Material currency, int cost) {
        ItemStack preview = item.clone();
        org.bukkit.inventory.meta.ItemMeta meta = preview.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<String>();
        lore.add("");
        lore.add(ChatUtil.color("&6Cost: &e" + cost + " " + formatMaterial(currency)));
        lore.add(ChatUtil.color("&8Shift-right for Quick Buy"));
        meta.setLore(lore);
        preview.setItemMeta(meta);
        String name = meta.getDisplayName();
        ShopRegistry.register(name, new ShopItem(ShopItem.normalizeId(name), "experimental", -1, name,
                new ItemStack[]{item.clone()}, currency, cost, "SPECIAL"));
        return preview;
    }

    private void ensureCatalogSeeded() {
        if (catalogSeeded) return;
        catalogSeeded = true;

        registerCatalogItem("blocks", Material.WOOL, 16, (short) 14, "&fWool x16", Material.IRON_INGOT, 4);
        registerCatalogItem("blocks", Material.STAINED_CLAY, 16, (short) 14, "&6Hardened Clay x16", Material.IRON_INGOT, 12);
        registerCatalogItem("blocks", Material.GLASS, 4, (short) 0, "&fBlast-Proof Glass x4", Material.IRON_INGOT, 12);
        registerCatalogItem("blocks", Material.ENDER_STONE, 4, (short) 0, "&aEnd Stone x4", Material.IRON_INGOT, 24);
        registerCatalogItem("blocks", Material.WOOD, 16, (short) 0, "&6Wood x16", Material.IRON_INGOT, 8);
        registerCatalogItem("blocks", Material.OBSIDIAN, 4, (short) 0, "&8Obsidian x4", Material.EMERALD, 4);
        registerCatalogItem("blocks", Material.LADDER, 16, (short) 0, "&7Ladder x16", Material.IRON_INGOT, 4);

        registerCatalogItem("weapons", Material.STONE_SWORD, 1, (short) 0, "&7Stone Sword", Material.IRON_INGOT, 10);
        registerCatalogItem("weapons", Material.IRON_SWORD, 1, (short) 0, "&fIron Sword", Material.GOLD_INGOT, 7);
        registerCatalogItem("weapons", Material.DIAMOND_SWORD, 1, (short) 0, "&bDiamond Sword", Material.DIAMOND, 4);
        registerCatalogItem("weapons", Material.BOW, 1, (short) 0, "&6Bow", Material.GOLD_INGOT, 12);
        registerCatalogItem("weapons", Material.ARROW, 8, (short) 0, "&7Arrow x8", Material.GOLD_INGOT, 2);

        registerCatalogItem("armor", Material.CHAINMAIL_BOOTS, 1, (short) 0, "&7Chain Boots", Material.IRON_INGOT, 40);
        registerCatalogItem("armor", Material.IRON_BOOTS, 1, (short) 0, "&fIron Boots", Material.GOLD_INGOT, 6);
        registerCatalogItem("armor", Material.DIAMOND_BOOTS, 1, (short) 0, "&bDiamond Boots", Material.DIAMOND, 6);
        registerCatalogItem("armor", Material.CHAINMAIL_LEGGINGS, 1, (short) 0, "&7Chain Leggings", Material.IRON_INGOT, 40);
        registerCatalogItem("armor", Material.IRON_LEGGINGS, 1, (short) 0, "&fIron Leggings", Material.GOLD_INGOT, 10);
        registerCatalogItem("armor", Material.DIAMOND_LEGGINGS, 1, (short) 0, "&bDiamond Leggings", Material.DIAMOND, 12);

        registerCatalogItem("tools", Material.WOOD_PICKAXE, 1, (short) 0, "&6Wooden Pickaxe", Material.IRON_INGOT, 10);
        registerCatalogItem("tools", Material.STONE_PICKAXE, 1, (short) 0, "&7Stone Pickaxe", Material.IRON_INGOT, 20);
        registerCatalogItem("tools", Material.IRON_PICKAXE, 1, (short) 0, "&fIron Pickaxe", Material.GOLD_INGOT, 4);
        registerCatalogItem("tools", Material.DIAMOND_PICKAXE, 1, (short) 0, "&bDiamond Pickaxe", Material.GOLD_INGOT, 8);
        registerCatalogItem("tools", Material.SHEARS, 1, (short) 0, "&7Shears", Material.IRON_INGOT, 20);
        registerCatalogItem("tools", Material.TNT, 1, (short) 0, "&cTNT", Material.GOLD_INGOT, 4);
        registerCatalogItem("tools", Material.WATER_BUCKET, 1, (short) 0, "&9Water Bucket", Material.GOLD_INGOT, 4);
        registerCatalogItem("tools", Material.GOLDEN_APPLE, 1, (short) 0, "&6Golden Apple", Material.GOLD_INGOT, 3);
        registerCatalogItem("tools", Material.ENDER_PEARL, 1, (short) 0, "&5Ender Pearl", Material.EMERALD, 4);
        registerCatalogItem("tools", Material.FLINT_AND_STEEL, 1, (short) 0, "&6Flint and Steel", Material.GOLD_INGOT, 6);

        registerCatalogItem("potions", Material.POTION, 1, (short) 8193, "&aSpeed Potion I", Material.GOLD_INGOT, 4);
        registerCatalogItem("potions", Material.POTION, 1, (short) 8194, "&aSpeed Potion II", Material.GOLD_INGOT, 10);
        registerCatalogItem("potions", Material.POTION, 1, (short) 8225, "&aJump Potion V", Material.GOLD_INGOT, 6);
        registerCatalogItem("potions", Material.POTION, 1, (short) 8232, "&cInvisibility Potion", Material.EMERALD, 4);

        registerCatalogItem("experimental", RewindItem.getItem(), Material.EMERALD, 12, "SPECIAL");
        registerCatalogItem("experimental", FireballJumpItem.getItem(), Material.GOLD_INGOT, 8, "SPECIAL");
        registerCatalogItem("experimental", SlingshotItem.getItem(), Material.IRON_INGOT, 6, "SPECIAL");
        registerCatalogItem("experimental", SwitcherItem.getItem(), Material.EMERALD, 6, "SPECIAL");
        registerCatalogItem("experimental", PortableShopItem.getItem(), Material.GOLD_INGOT, 6, "SPECIAL");
        registerCatalogItem("experimental", PortableEnderChestItem.getItem(), Material.EMERALD, 3, "SPECIAL");
    }

    private void registerCatalogItem(String category, Material mat, int amount, short data, String name, Material currency, int cost) {
        registerCatalogItem(category, new ItemStack(mat, amount, data), name, currency, cost, "NORMAL");
    }

    private void registerCatalogItem(String category, ItemStack item, Material currency, int cost, String special) {
        String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name();
        registerCatalogItem(category, item, name, currency, cost, special);
    }

    private void registerCatalogItem(String category, ItemStack item, String name, Material currency, int cost, String special) {
        ShopRegistry.register(name, new ShopItem(ShopItem.normalizeId(name), category, -1, name,
                new ItemStack[]{item.clone()}, currency, cost, special));
    }

    private void addBackButton(Inventory inv) {
        inv.setItem(49, ItemUtil.create(Material.ARROW, 1, "&7← Back", "&7Return to main shop."));
    }

    private void fillGlass(Inventory inv) {
        ItemStack glass = ItemUtil.create(Material.STAINED_GLASS_PANE, 1, (short)15, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }
}
