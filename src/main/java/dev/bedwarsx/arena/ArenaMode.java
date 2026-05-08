package dev.bedwarsx.arena;

public enum ArenaMode {
    SOLO(1, 8, "Solo"),
    DUPLA(2, 8, "Dupla"),
    TRIO(3, 4, "Trio"),
    SQUAD(4, 4, "Quarteto");

    private final int teamSize;
    private final int maxTeams;
    private final String displayName;

    ArenaMode(int teamSize, int maxTeams, String displayName) {
        this.teamSize = teamSize;
        this.maxTeams = maxTeams;
        this.displayName = displayName;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public int getMaxTeams() {
        return maxTeams;
    }

    public int getDefaultCapacity() {
        return teamSize * maxTeams;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ArenaMode fromString(String value) {
        if (value == null || value.trim().isEmpty()) return SQUAD;
        String normalized = value.trim().toUpperCase();
        if (normalized.equals("DUO") || normalized.equals("DUPLAS")) return DUPLA;
        if (normalized.equals("TRIOS")) return TRIO;
        if (normalized.equals("QUARTETO") || normalized.equals("QUAD") || normalized.equals("QUARTETOS")) return SQUAD;
        for (ArenaMode mode : values()) {
            if (mode.name().equalsIgnoreCase(normalized) || mode.displayName.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return SQUAD;
    }
}
