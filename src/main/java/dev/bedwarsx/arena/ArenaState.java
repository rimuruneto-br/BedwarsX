package dev.bedwarsx.arena;

public enum ArenaState {
    WAITING("&aWaiting"),
    STARTING("&eStarting"),
    IN_GAME("&cIn Game"),
    ENDING("&6Ending"),
    RESTARTING("&7Restarting"),
    DISABLED("&8Disabled");

    private final String displayName;

    ArenaState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
