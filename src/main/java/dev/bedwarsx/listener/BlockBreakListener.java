package dev.bedwarsx.listener;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final BedWarsX plugin;

    public BlockBreakListener(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game == null) {
            // Outside game - cancel all breaking (lobby protection)
            event.setCancelled(true);
            return;
        }

        if (game.getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        GamePlayer gp = game.getGamePlayer(player);
        if (gp == null || !gp.isAlive()) {
            event.setCancelled(true);
            return;
        }

        Material type = event.getBlock().getType();
        Location loc = event.getBlock().getLocation();

        // Bed breaking
        if (type == Material.BED_BLOCK || type == Material.BED) {
            event.setCancelled(true);
            game.handleBedBreak(loc, player);

            // Remove bed blocks visually
            event.getBlock().setType(Material.AIR);
            // Check for adjacent bed block
            for (org.bukkit.block.BlockFace face : new org.bukkit.block.BlockFace[]{
                    org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH,
                    org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST}) {
                org.bukkit.block.Block adjacent = event.getBlock().getRelative(face);
                if (adjacent.getType() == Material.BED_BLOCK || adjacent.getType() == Material.BED) {
                    adjacent.setType(Material.AIR);
                }
            }
            return;
        }

        // Only allow breaking of player-placed blocks
        if (!game.getPlacedBlocks().contains(loc) && !game.getArena().isPlacedBlock(loc)) {
            event.setCancelled(true);
            ChatUtil.send(player, "&cYou can only break player-placed blocks!");
            return;
        }

        // Remove from placed blocks tracking
        game.getPlacedBlocks().remove(loc);
        game.getArena().removePlacedBlock(loc);
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
    }
}
