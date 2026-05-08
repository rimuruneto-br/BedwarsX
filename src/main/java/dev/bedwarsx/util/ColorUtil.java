package dev.bedwarsx.util;

import dev.bedwarsx.team.BedWarsTeam;
import org.bukkit.ChatColor;

/**
 * Maps ChatColor team colors to RGB for leather armor dyeing in 1.8.9.
 */
public class ColorUtil {

    public static int[] toRGB(BedWarsTeam team) {
        ChatColor color = team.getColor();
        switch (color) {
            case RED:           return new int[]{170, 0, 0};
            case BLUE:          return new int[]{0, 0, 170};
            case GREEN:         return new int[]{0, 170, 0};
            case YELLOW:        return new int[]{255, 170, 0};
            case AQUA:          return new int[]{85, 255, 255};
            case WHITE:         return new int[]{255, 255, 255};
            case LIGHT_PURPLE:  return new int[]{255, 85, 255};
            case GRAY:          return new int[]{170, 170, 170};
            case DARK_RED:      return new int[]{128, 0, 0};
            case DARK_BLUE:     return new int[]{0, 0, 128};
            case DARK_GREEN:    return new int[]{0, 128, 0};
            case GOLD:          return new int[]{255, 165, 0};
            case DARK_AQUA:     return new int[]{0, 139, 139};
            default:            return new int[]{200, 200, 200};
        }
    }
}
