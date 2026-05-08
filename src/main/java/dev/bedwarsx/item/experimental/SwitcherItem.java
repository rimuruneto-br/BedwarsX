package dev.bedwarsx.item.experimental;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwitcherItem {
    private static final String ITEM_NAME = "&b&lSWITCHER";
    private static final long COOLDOWN_MS = 15000L;
    private static final double RANGE = 22.0;
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public static ItemStack getItem() {
        return ItemUtil.create(Material.SNOW_BALL, 1, ITEM_NAME,
                "&7Troca de lugar com um inimigo",
                "&7na sua linha de visao.",
                "",
                "&eAlcance: &f22 blocos",
                "&eCooldown: &f15s");
    }

    public static boolean isSwitcherItem(ItemStack item) {
        return ItemUtil.hasName(item, ITEM_NAME);
    }

    public static void use(Player player, Game game) {
        if (game == null) return;
        long now = System.currentTimeMillis();
        Long next = cooldowns.get(player.getUniqueId());
        if (next != null && next > now) {
            ChatUtil.send(player, "&cSwitcher recarregando: &e" + ((next - now) / 1000 + 1) + "s");
            return;
        }

        Player target = findTarget(player, game);
        if (target == null) {
            ChatUtil.send(player, "&cNenhum inimigo valido na mira.");
            return;
        }

        Location playerLoc = player.getLocation().clone();
        Location targetLoc = target.getLocation().clone();
        player.teleport(targetLoc);
        target.teleport(playerLoc);
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1.2f);
        target.playSound(target.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 0.8f);
        consumeOne(player);
        cooldowns.put(player.getUniqueId(), now + COOLDOWN_MS);
    }

    private static Player findTarget(Player player, Game game) {
        Player best = null;
        double bestDot = 0.985;
        for (UUID uuid : game.getPlayers().keySet()) {
            Player candidate = BedWarsX.getInstance().getServer().getPlayer(uuid);
            if (candidate == null || candidate.equals(player) || !candidate.isOnline()) continue;
            if (game.areSameTeam(player, candidate)) continue;
            if (!candidate.getWorld().equals(player.getWorld())) continue;
            if (candidate.getLocation().distance(player.getLocation()) > RANGE) continue;
            if (!player.hasLineOfSight(candidate)) continue;
            org.bukkit.util.Vector direction = player.getLocation().getDirection().normalize();
            org.bukkit.util.Vector toTarget = candidate.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            double dot = direction.dot(toTarget);
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }
        return best;
    }

    private static void consumeOne(Player player) {
        ItemStack hand = player.getItemInHand();
        if (hand == null) return;
        if (hand.getAmount() <= 1) {
            player.setItemInHand(new ItemStack(Material.AIR));
        } else {
            hand.setAmount(hand.getAmount() - 1);
            player.setItemInHand(hand);
        }
    }
}
