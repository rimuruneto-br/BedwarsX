package dev.bedwarsx.upgrade;

public enum UpgradeType {
    SHARPNESS("&bSharpened Swords"),
    PROTECTION("&bReinforced Armor"),
    HASTE("&eManiac Miner"),
    FORGE("&6Molten Forge"),
    HEAL_POOL("&aHeal Pool");

    private final String displayName;

    UpgradeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
