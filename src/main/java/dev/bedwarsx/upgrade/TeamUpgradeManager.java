package dev.bedwarsx.upgrade;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.team.GameTeam;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TeamUpgradeManager {
    public static final String TITLE = "&2&lTeam Upgrades";

    private final BedWarsX plugin;

    public TeamUpgradeManager(BedWarsX plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Game game = plugin.getGameManager().getPlayerGame(player);
        GameTeam team = game == null ? null : game.getPlayerTeam(player);
        Inventory inv = plugin.getServer().createInventory(null, 54, ChatUtil.color(TITLE));
        inv.setItem(11, upgradeItem(Material.DIAMOND_SWORD, UpgradeType.SHARPNESS, team, 4, 1));
        inv.setItem(13, upgradeItem(Material.DIAMOND_CHESTPLATE, UpgradeType.PROTECTION, team, 4, 4));
        inv.setItem(15, upgradeItem(Material.GOLD_PICKAXE, UpgradeType.HASTE, team, 2, 2));
        inv.setItem(29, upgradeItem(Material.FURNACE, UpgradeType.FORGE, team, 4, 4));
        inv.setItem(31, upgradeItem(Material.BEACON, UpgradeType.HEAL_POOL, team, 1, 1));
        inv.setItem(33, ItemUtil.create(Material.TRIPWIRE_HOOK, 1, "&cTrap Queue",
                "&7Traps are ready in the model layer.",
                "&7Next phase will expose per-trap buying.",
                "",
                "&8Design adopted from RN."));
        fill(inv);
        player.openInventory(inv);
    }

    public boolean handleClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return false;
        Game game = plugin.getGameManager().getPlayerGame(player);
        GameTeam team = game == null ? null : game.getPlayerTeam(player);
        if (team == null) {
            ChatUtil.send(player, "&cYou need a team to buy upgrades.");
            return true;
        }
        String name = ChatUtil.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();
        if (name.contains("sharpened")) return buy(player, team, UpgradeType.SHARPNESS, 1, 4);
        if (name.contains("reinforced")) return buy(player, team, UpgradeType.PROTECTION, 4, 4);
        if (name.contains("miner")) return buy(player, team, UpgradeType.HASTE, 2, 2);
        if (name.contains("forge")) return buy(player, team, UpgradeType.FORGE, 4, 4);
        if (name.contains("heal")) return buy(player, team, UpgradeType.HEAL_POOL, 1, 1);
        return true;
    }

    private boolean buy(Player player, GameTeam team, UpgradeType type, int baseCost, int max) {
        int current = team.getUpgradeLevel(type);
        if (current >= max) {
            ChatUtil.send(player, "&cThis upgrade is already maxed.");
            return true;
        }
        int cost = baseCost * (current + 1);
        if (count(player, Material.DIAMOND) < cost) {
            ChatUtil.send(player, "&cYou need &b" + cost + " diamonds&c.");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1f, 0.6f);
            return true;
        }
        remove(player, Material.DIAMOND, cost);
        team.setUpgradeLevel(type, current + 1);
        ChatUtil.send(player, "&aTeam upgrade purchased: " + type.getDisplayName() + " &7Lv." + (current + 1));
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1.1f);
        open(player);
        return true;
    }

    private ItemStack upgradeItem(Material material, UpgradeType type, GameTeam team, int baseCost, int max) {
        int level = team == null ? 0 : team.getUpgradeLevel(type);
        int next = Math.min(max, level + 1);
        String status = level >= max ? "&aMAX" : "&eLv." + level + " -> " + next;
        String cost = level >= max ? "&aUnlocked" : "&6Cost: &b" + (baseCost * (level + 1)) + " Diamonds";
        return ItemUtil.create(material, 1, type.getDisplayName(),
                "&7Status: " + status,
                cost,
                "",
                level >= max ? "&7Fully upgraded." : "&aClick to buy.");
    }

    private void fill(Inventory inv) {
        ItemStack glass = ItemUtil.create(Material.STAINED_GLASS_PANE, 1, (short) 7, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
    }

    private int count(Player player, Material material) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) total += item.getAmount();
        }
        return total;
    }

    private void remove(Player player, Material material, int amount) {
        ItemStack[] contents = player.getInventory().getContents();
        int remaining = amount;
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) continue;
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                contents[i] = null;
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
        player.getInventory().setContents(contents);
    }
}
