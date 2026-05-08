package dev.bedwarsx.generator;

import dev.bedwarsx.team.BedWarsTeam;
import org.bukkit.Location;

public class ArenaGenerator {
    private final GeneratorType type;
    private final Location location;
    private final BedWarsTeam owner;
    private int counter;

    public ArenaGenerator(GeneratorType type, Location location, BedWarsTeam owner) {
        this.type = type;
        this.location = location;
        this.owner = owner;
    }

    public GeneratorType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public BedWarsTeam getOwner() {
        return owner;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void reset() {
        counter = 0;
    }
}
