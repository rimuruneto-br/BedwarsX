package dev.bedwarsx.upgrade;

public class TrapDefinition {
    private final TrapType type;
    private final int slot;
    private final int cost;

    public TrapDefinition(TrapType type, int slot, int cost) {
        this.type = type;
        this.slot = slot;
        this.cost = cost;
    }

    public TrapType getType() { return type; }
    public int getSlot() { return slot; }
    public int getCost() { return cost; }
}
