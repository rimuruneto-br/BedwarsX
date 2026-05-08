package dev.bedwarsx.util;

import dev.bedwarsx.main.BedWarsX;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple hologram using invisible ArmorStands with name tags.
 * Compatible with 1.8.9.
 */
public class HologramManager {

    private static final double LINE_HEIGHT = 0.25;

    /**
     * Spawns a hologram at the given location with multiple lines.
     * Returns a list of ArmorStand entities (one per line).
     */
    public static List<ArmorStand> createHologram(Location location, List<String> lines) {
        List<ArmorStand> stands = new ArrayList<>();
        double y = location.getY() + (lines.size() * LINE_HEIGHT);

        for (String line : lines) {
            Location lineLoc = location.clone();
            lineLoc.setY(y);

            ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(lineLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCanPickupItems(false);
            stand.setCustomName(ChatUtil.color(line));
            stand.setCustomNameVisible(true);
            stand.setSmall(true);
            stand.setArms(false);
            stand.setBasePlate(false);

            // Make it invulnerable via NMS
            try {
                Object nmsEntity = stand.getClass().getMethod("getHandle").invoke(stand);
                nmsEntity.getClass().getField("invulnerable").set(nmsEntity, true);
            } catch (Exception ignored) {}

            stands.add(stand);
            y -= LINE_HEIGHT;
        }
        return stands;
    }

    /**
     * Removes all ArmorStands from a hologram list.
     */
    public static void removeHologram(List<ArmorStand> stands) {
        for (ArmorStand stand : stands) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        stands.clear();
    }

    /**
     * Updates a specific line in a hologram.
     */
    public static void updateLine(List<ArmorStand> stands, int index, String text) {
        if (index >= 0 && index < stands.size()) {
            ArmorStand stand = stands.get(index);
            if (stand != null && !stand.isDead()) {
                stand.setCustomName(ChatUtil.color(text));
            }
        }
    }
}
