package dev.bedwarsx.generator;

import org.bukkit.Material;

public enum GeneratorType {
    IRON(Material.IRON_INGOT, "&fIron"),
    GOLD(Material.GOLD_INGOT, "&6Gold"),
    DIAMOND(Material.DIAMOND, "&bDiamond"),
    EMERALD(Material.EMERALD, "&aEmerald");

    private final Material material;
    private final String displayName;

    GeneratorType(Material material, String displayName) {
        this.material = material;
        this.displayName = displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GeneratorType fromString(String value) {
        if (value == null) return null;
        for (GeneratorType type : values()) {
            if (type.name().equalsIgnoreCase(value)) return type;
        }
        return null;
    }
}
