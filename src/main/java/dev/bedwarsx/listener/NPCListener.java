package dev.bedwarsx.listener;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.npc.BedWarsNPC;
import dev.bedwarsx.shop.ShopCategory;
import dev.bedwarsx.shop.ShopManager;
import dev.bedwarsx.upgrade.TeamUpgradeManager;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCListener implements Listener {

    private final BedWarsX plugin;
    private final ShopManager shopManager;
    private final TeamUpgradeManager upgradeManager;
    // Track last NPC interaction per player (prevent double-clicks)
    private final Map<UUID, Long> lastInteract = new HashMap<>();

    public NPCListener(BedWarsX plugin) {
        this.plugin = plugin;
        this.shopManager = new ShopManager(plugin);
        this.upgradeManager = new TeamUpgradeManager(plugin);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        // Check interact cooldown (500ms)
        long now = System.currentTimeMillis();
        if (lastInteract.getOrDefault(player.getUniqueId(), 0L) + 500 > now) {
            return;
        }

        // Check if near any NPC
        BedWarsNPC npc = getNearbyNPC(player.getLocation(), 2.5);
        if (npc == null) return;

        lastInteract.put(player.getUniqueId(), now);
        event.setCancelled(true);
        handleNPCClick(player, npc);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!hasMoved(event.getFrom(), event.getTo())) return;

        // Check if player walked near any NPC to show name tag hologram
        // (NPCs in 1.8 don't auto-show name, so we refresh on proximity)
        BedWarsNPC npc = getNearbyNPC(player.getLocation(), 10);
        if (npc != null && player.getLocation().distance(npc.getLocation()) < 10) {
            // Re-spawn NPC if far away player approaches
            plugin.getNpcManager().spawnNPCForPlayer(npc, player);
        }
    }

    private void handleNPCClick(Player player, BedWarsNPC npc) {
        switch (npc.getType().toLowerCase()) {
            case "shop":
            case "item_shop":
                shopManager.openShop(player, ShopCategory.MAIN);
                break;

            case "upgrade":
            case "team_upgrade":
                upgradeManager.open(player);
                break;

            case "join":
                if (npc.getLinkedArena() != null) {
                    Arena arena = plugin.getArenaManager().getArena(npc.getLinkedArena());
                    if (arena != null) {
                        plugin.getGameManager().joinGame(player, arena);
                    }
                }
                break;

            case "lobby":
            default:
                ChatUtil.send(player, "&7NPC: &fHello, " + player.getName() + "!");
                break;
        }
    }

    private void openTeamUpgrades(Player player) {
        // Team upgrades shop
        org.bukkit.inventory.Inventory inv = plugin.getServer().createInventory(null, 54,
                ChatUtil.color("&2&lTeam Upgrades"));

        // Sharpness upgrade
        inv.setItem(11, dev.bedwarsx.util.ItemUtil.create(
                org.bukkit.Material.DIAMOND_SWORD, 1,
                "&bSharpened Swords I",
                "&7Grants your team Sharpness I",
                "&7on all swords.",
                "",
                "&6Cost: &34 Diamonds"
        ));

        // Protection upgrade
        inv.setItem(13, dev.bedwarsx.util.ItemUtil.create(
                org.bukkit.Material.DIAMOND_CHESTPLATE, 1,
                "&bArmor I",
                "&7Grants your team Protection I",
                "&7on all armor.",
                "",
                "&6Cost: &34 Diamonds"
        ));

        // Haste upgrade
        inv.setItem(15, dev.bedwarsx.util.ItemUtil.create(
                org.bukkit.Material.GOLD_PICKAXE, 1,
                "&bHaste I",
                "&7Grants your team Haste I",
                "&7permanently.",
                "",
                "&6Cost: &32 Diamonds"
        ));

        // Fill glass
        org.bukkit.inventory.ItemStack glass = dev.bedwarsx.util.ItemUtil.create(
                org.bukkit.Material.STAINED_GLASS_PANE, 1, (short) 7, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }

        player.openInventory(inv);
    }

    private BedWarsNPC getNearbyNPC(Location loc, double radius) {
        for (BedWarsNPC npc : plugin.getNpcManager().getNpcs()) {
            if (!npc.getLocation().getWorld().equals(loc.getWorld())) continue;
            if (npc.getLocation().distance(loc) <= radius) {
                return npc;
            }
        }
        return null;
    }

    private boolean hasMoved(Location from, Location to) {
        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }
}
