package dev.bedwarsx.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpgradeDefinition {
    private final UpgradeType type;
    private final String name;
    private final int slot;
    private final int maxLevel;
    private final List<Integer> costs;

    public UpgradeDefinition(UpgradeType type, String name, int slot, int maxLevel, List<Integer> costs) {
        this.type = type;
        this.name = name;
        this.slot = slot;
        this.maxLevel = maxLevel;
        this.costs = costs == null ? new ArrayList<>() : new ArrayList<>(costs);
    }

    public UpgradeType getType() { return type; }
    public String getName() { return name; }
    public int getSlot() { return slot; }
    public int getMaxLevel() { return maxLevel; }
    public List<Integer> getCosts() { return Collections.unmodifiableList(costs); }

    public int getCostForLevel(int nextLevel) {
        if (nextLevel <= 0 || costs.isEmpty()) return 0;
        return costs.get(Math.min(costs.size() - 1, nextLevel - 1));
    }
}
