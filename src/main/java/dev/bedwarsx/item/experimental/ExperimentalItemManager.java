package dev.bedwarsx.item.experimental;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Manages experimental items: snapshot recording, purchases, and shop delivery.
 */
public class ExperimentalItemManager {

    private final BedWarsX plugin;

    public ExperimentalItemManager(BedWarsX plugin) {
        this.plugin = plugin;
        startSnapshotTask();
    }

    /**
     * Records player snapshots every 2 ticks for Rewind item.
     */
    private void startSnapshotTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    Game game = plugin.getGameManager().getPlayerGame(player);
                    if (game != null) {
                        RewindItem.recordSnapshot(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /**
     * Handle right-click use of experimental items.
     */
    public boolean handleUse(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        Game game = plugin.getGameManager().getPlayerGame(player);

        if (RewindItem.isRewindItem(item)) {
            RewindItem.use(player, game);
            return true;
        }

        if (FireballJumpItem.isFireballJumpItem(item)) {
            FireballJumpItem.use(player, game);
            return true;
        }

        if (SlingshotItem.isSlingshotItem(item)) {
            SlingshotItem.use(player, game);
            return true;
        }

        if (SwitcherItem.isSwitcherItem(item)) {
            SwitcherItem.use(player, game);
            return true;
        }

        if (PortableShopItem.isPortableShopItem(item)) {
            PortableShopItem.use(player);
            return true;
        }

        if (PortableEnderChestItem.isPortableEnderChestItem(item)) {
            PortableEnderChestItem.use(player);
            return true;
        }

        return false;
    }

    /**
     * Give experimental item to player from shop purchase.
     */
    public void giveItem(Player player, String itemType) {
        ItemStack item = null;
        switch (itemType.toLowerCase()) {
            case "rewind":
                item = RewindItem.getItem();
                break;
            case "fireballjump":
            case "fireball_jump":
                item = FireballJumpItem.getItem();
                break;
            case "slingshot":
            case "estilingue":
                item = SlingshotItem.getItem();
                break;
            case "switcher":
                item = SwitcherItem.getItem();
                break;
            case "portable_shop":
            case "lojaportatil":
                item = PortableShopItem.getItem();
                break;
            case "portable_ender_chest":
            case "enderchest":
                item = PortableEnderChestItem.getItem();
                break;
        }
        if (item != null) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }
        }
    }

    /**
     * Clean up all data for a player leaving a game.
     */
    public void cleanupPlayer(UUID uuid) {
        RewindItem.clearPlayer(uuid);
        SlingshotItem.clearPlayer(uuid);
    }
}
