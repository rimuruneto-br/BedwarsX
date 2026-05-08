package dev.bedwarsx.item.experimental;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import dev.bedwarsx.util.ParticleUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * 💣 FIREBALL JUMP - Explosive mobility item
 * Launches the player in the opposite direction of their view.
 */
public class FireballJumpItem {

    private static final String ITEM_NAME = "&c&l💣 FIREBALL JUMP";

    // Launch force - tuned to ~36 blocks
    private static final double HORIZONTAL_FORCE = 2.2;
    private static final double VERTICAL_FORCE = 0.35;

    public static ItemStack getItem() {
        return ItemUtil.create(Material.FIREBALL, 1, ITEM_NAME,
                "&7Impulso explosivo na direção oposta.",
                "",
                "&eEfeitos:",
                "  &f- Lança ~36 blocos de distância",
                "  &f- Força vertical leve",
                "  &f- Som de explosão",
                "  &f- Partículas visuais",
                "",
                "&eFoco: &fMobilidade ofensiva",
                "&7Categoria: &5Experimental");
    }

    public static boolean isFireballJumpItem(ItemStack item) {
        return ItemUtil.hasName(item, ITEM_NAME);
    }

    public static void use(Player player, Game game) {
        // Get player look direction (opposite = launch direction forward)
        // Player looks forward-down, launches forward-up
        Vector direction = player.getLocation().getDirection();

        // Reverse horizontal components (opposite to look direction = go that way)
        // Actually in bedwars fireball jump: player looks slightly down, launches forward
        // We take the look direction and boost in that direction (not opposite)
        // Normalize horizontal
        Vector launch = new Vector(direction.getX(), 0, direction.getZ()).normalize();
        launch.multiply(HORIZONTAL_FORCE);
        launch.setY(VERTICAL_FORCE);

        // Apply velocity
        player.setVelocity(launch);
        player.setFallDistance(0);

        // Sound effect
        player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 0.8f, 1.2f);

        // Particle explosion
        spawnExplosionParticles(player);

        // Visual feedback
        ChatUtil.sendActionBar(player, "&c💣 &fFireball Jump!");

        // Reduce item count (one-use item purchased from shop)
        ItemStack held = player.getItemInHand();
        if (held != null && held.getAmount() > 1) {
            held.setAmount(held.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
    }

    private static void spawnExplosionParticles(Player player) {
        Location loc = player.getLocation().add(0, 0.5, 0);
        World world = player.getWorld();

        ParticleUtil.spawnExplosion(loc);
        ParticleUtil.spawnFlame(loc, 20);
        ParticleUtil.spawnFirework(loc, 15);
    }
}
