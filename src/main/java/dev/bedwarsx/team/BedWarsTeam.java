package dev.bedwarsx.team;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

public enum BedWarsTeam {
    RED("Red", ChatColor.RED, DyeColor.RED, 14),
    BLUE("Blue", ChatColor.BLUE, DyeColor.BLUE, 11),
    GREEN("Green", ChatColor.GREEN, DyeColor.GREEN, 13),
    YELLOW("Yellow", ChatColor.YELLOW, DyeColor.YELLOW, 4),
    AQUA("Aqua", ChatColor.AQUA, DyeColor.CYAN, 9),
    WHITE("White", ChatColor.WHITE, DyeColor.WHITE, 0),
    PINK("Pink", ChatColor.LIGHT_PURPLE, DyeColor.PINK, 6),
    GRAY("Gray", ChatColor.GRAY, DyeColor.GRAY, 7);

    private final String displayName;
    private final ChatColor color;
    private final DyeColor dyeColor;
    private final int woolData;

    BedWarsTeam(String displayName, ChatColor color, DyeColor dyeColor, int woolData) {
        this.displayName = displayName;
        this.color = color;
        this.dyeColor = dyeColor;
        this.woolData = woolData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    public int getWoolData() {
        return woolData;
    }

    public String getColoredName() {
        return color + displayName;
    }

    public String getPrefix() {
        return color + "[" + displayName.charAt(0) + "] ";
    }

    public static BedWarsTeam fromString(String name) {
        for (BedWarsTeam team : values()) {
            if (team.name().equalsIgnoreCase(name) || team.displayName.equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }
}
