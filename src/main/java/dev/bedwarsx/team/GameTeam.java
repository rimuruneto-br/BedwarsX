package dev.bedwarsx.team;

import dev.bedwarsx.generator.GeneratorType;
import dev.bedwarsx.upgrade.TrapType;
import dev.bedwarsx.upgrade.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class GameTeam {

    private final BedWarsTeam type;
    private final List<UUID> members = new ArrayList<>();
    private final Map<UpgradeType, Integer> upgrades = new EnumMap<>(UpgradeType.class);
    private final Queue<TrapType> traps = new ArrayDeque<>();
    private Location spawn;
    private Location bedLocation;
    private Location generatorLocation;
    private Location shopLocation;
    private Location upgradeShopLocation;
    private Location indicatorLocation;
    private Location islandCenter;
    private int islandRadius = 18;
    private boolean bedAlive = true;
    private boolean eliminated = false;
    private boolean active = true;
    private int ironCounter;
    private int goldCounter;
    private int pendingIron;
    private int pendingGold;
    private double ironProgress;
    private double goldProgress;
    private transient Inventory teamChest;

    public GameTeam(BedWarsTeam type) {
        this.type = type;
        for (UpgradeType upgrade : UpgradeType.values()) {
            upgrades.put(upgrade, 0);
        }
    }

    public BedWarsTeam getType() {
        return type;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID uuid) {
        if (!members.contains(uuid)) {
            members.add(uuid);
        }
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean hasMember(UUID uuid) {
        return members.contains(uuid);
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public Location getBedLocation() {
        return bedLocation;
    }

    public void setBedLocation(Location bedLocation) {
        this.bedLocation = bedLocation;
    }

    public Location getGeneratorLocation() {
        return generatorLocation;
    }

    public void setGeneratorLocation(Location generatorLocation) {
        this.generatorLocation = generatorLocation;
    }

    public Location getShopLocation() {
        return shopLocation;
    }

    public void setShopLocation(Location shopLocation) {
        this.shopLocation = shopLocation;
    }

    public Location getUpgradeShopLocation() {
        return upgradeShopLocation;
    }

    public void setUpgradeShopLocation(Location upgradeShopLocation) {
        this.upgradeShopLocation = upgradeShopLocation;
    }

    public Location getIndicatorLocation() {
        return indicatorLocation;
    }

    public void setIndicatorLocation(Location indicatorLocation) {
        this.indicatorLocation = indicatorLocation;
    }

    public Location getIslandCenter() {
        return islandCenter;
    }

    public void setIslandCenter(Location islandCenter) {
        this.islandCenter = islandCenter;
    }

    public int getIslandRadius() {
        return islandRadius <= 0 ? 18 : islandRadius;
    }

    public void setIslandRadius(int islandRadius) {
        this.islandRadius = islandRadius <= 0 ? 18 : islandRadius;
    }

    public boolean isBedAlive() {
        return bedAlive;
    }

    public void setBedAlive(boolean bedAlive) {
        this.bedAlive = bedAlive;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<UpgradeType, Integer> getUpgrades() {
        return upgrades;
    }

    public int getUpgradeLevel(UpgradeType upgrade) {
        Integer level = upgrades.get(upgrade);
        return level == null ? 0 : level;
    }

    public void setUpgradeLevel(UpgradeType upgrade, int level) {
        upgrades.put(upgrade, Math.max(0, level));
    }

    public Queue<TrapType> getTraps() {
        return traps;
    }

    public int getCounter(GeneratorType type) {
        return type == GeneratorType.GOLD ? goldCounter : ironCounter;
    }

    public void setCounter(GeneratorType type, int counter) {
        if (type == GeneratorType.GOLD) {
            goldCounter = counter;
        } else {
            ironCounter = counter;
        }
    }

    public int getPending(GeneratorType type) {
        return type == GeneratorType.GOLD ? pendingGold : pendingIron;
    }

    public void addPending(GeneratorType type, int amount) {
        if (amount <= 0) return;
        if (type == GeneratorType.GOLD) {
            pendingGold += amount;
        } else {
            pendingIron += amount;
        }
    }

    public void consumePending(GeneratorType type, int amount) {
        if (amount <= 0) return;
        if (type == GeneratorType.GOLD) {
            pendingGold = Math.max(0, pendingGold - amount);
        } else {
            pendingIron = Math.max(0, pendingIron - amount);
        }
    }

    public double getProgress(GeneratorType type) {
        return type == GeneratorType.GOLD ? goldProgress : ironProgress;
    }

    public void setProgress(GeneratorType type, double progress) {
        if (type == GeneratorType.GOLD) {
            goldProgress = Math.max(0.0, progress);
        } else {
            ironProgress = Math.max(0.0, progress);
        }
    }

    public Inventory getTeamChest() {
        if (teamChest == null) {
            teamChest = Bukkit.createInventory(null, 27, "Team Chest - " + type.getColor() + type.getDisplayName());
        }
        return teamChest;
    }

    public void resetMatchState() {
        bedAlive = true;
        eliminated = false;
        active = true;
        ironCounter = 0;
        goldCounter = 0;
        pendingIron = 0;
        pendingGold = 0;
        ironProgress = 0.0;
        goldProgress = 0.0;
        traps.clear();
        teamChest = null;
        for (UpgradeType upgrade : UpgradeType.values()) {
            upgrades.put(upgrade, 0);
        }
    }

    public int size() {
        return members.size();
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public String getColoredName() {
        return type.getColoredName();
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }
}
