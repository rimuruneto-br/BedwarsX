package dev.bedwarsx.game;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.HologramManager;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages floating holograms above each resource generator.
 */
public class GeneratorHologram {

    private final BedWarsX plugin;
    private final Map<Location, List<ArmorStand>> holograms = new ConcurrentHashMap<>();
    private BukkitTask updateTask;

    public GeneratorHologram(BedWarsX plugin) {
        this.plugin = plugin;
    }

    public void spawnForArena(Game game) {
        // Iron generators
        for (Location loc : game.getArena().getIronGenerators()) {
            spawnGeneratorHologram(loc, "&fIron", "&7Spawns every 1s");
        }
        // Gold generators
        for (Location loc : game.getArena().getGoldGenerators()) {
            spawnGeneratorHologram(loc, "&6Gold", "&7Spawns every 8s");
        }
        // Diamond generators
        for (Location loc : game.getArena().getDiamondGenerators()) {
            spawnGeneratorHologram(loc, "&bDiamond", "&7Spawns every 30s");
        }
        // Emerald generators
        for (Location loc : game.getArena().getEmeraldGenerators()) {
            spawnGeneratorHologram(loc, "&2Emerald", "&7Spawns every 60s");
        }
    }

    private void spawnGeneratorHologram(Location loc, String name, String info) {
        Location hologramLoc = loc.clone().add(0.5, 2.5, 0.5);
        List<String> lines = Arrays.asList(
                "&r",
                info,
                name + " &fGenerator"
        );
        List<ArmorStand> stands = HologramManager.createHologram(hologramLoc, lines);
        holograms.put(loc, stands);
    }

    public void removeAll() {
        for (List<ArmorStand> stands : holograms.values()) {
            HologramManager.removeHologram(stands);
        }
        holograms.clear();
        if (updateTask != null) updateTask.cancel();
    }
}
