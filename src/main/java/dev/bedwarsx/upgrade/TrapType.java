package dev.bedwarsx.upgrade;

public enum TrapType {
    ALARM("&cAlarm Trap"),
    SLOWNESS("&bMiner Fatigue Trap"),
    WEAKNESS("&7Counter-Offensive Trap"),
    DAMAGE("&4It's a Trap!");

    private final String displayName;

    TrapType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
