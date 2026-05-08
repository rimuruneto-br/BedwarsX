package dev.bedwarsx.npc;

import org.bukkit.Location;

import java.util.UUID;

public class BedWarsNPC {

    private final String id;
    private final String name;
    private final Location location;
    private final Object nmsPlayer;
    private final UUID uuid;
    private final String type; // lobby, shop, etc.
    private String skinValue;
    private String skinSignature;
    private String linkedArena; // for join NPCs

    public BedWarsNPC(String id, String name, Location location, Object nmsPlayer, UUID uuid, String type) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.nmsPlayer = nmsPlayer;
        this.uuid = uuid;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public Object getNmsPlayer() { return nmsPlayer; }
    public UUID getUuid() { return uuid; }
    public String getType() { return type; }
    public String getSkinValue() { return skinValue; }
    public void setSkinValue(String skinValue) { this.skinValue = skinValue; }
    public String getSkinSignature() { return skinSignature; }
    public void setSkinSignature(String skinSignature) { this.skinSignature = skinSignature; }
    public String getLinkedArena() { return linkedArena; }
    public void setLinkedArena(String linkedArena) { this.linkedArena = linkedArena; }
}
