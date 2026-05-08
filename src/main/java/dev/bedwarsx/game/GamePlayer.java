package dev.bedwarsx.game;

import dev.bedwarsx.team.BedWarsTeam;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GamePlayer {

    private final UUID uuid;
    private final String name;
    private BedWarsTeam team;
    private GamePlayerState state;
    private boolean spectator = false;
    private int kills = 0;
    private int deaths = 0;
    private int finalKills = 0;
    private int bedsBroken = 0;
    private boolean bedDestroyed = false;  // own bed destroyed

    public GamePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.state = GamePlayerState.ALIVE;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public BedWarsTeam getTeam() { return team; }
    public void setTeam(BedWarsTeam team) { this.team = team; }
    public GamePlayerState getState() { return state; }
    public void setState(GamePlayerState state) { this.state = state; }
    public boolean isSpectator() { return spectator; }
    public void setSpectator(boolean spectator) { this.spectator = spectator; }
    public int getKills() { return kills; }
    public void addKill() { kills++; }
    public int getDeaths() { return deaths; }
    public void addDeath() { deaths++; }
    public int getFinalKills() { return finalKills; }
    public void addFinalKill() { finalKills++; }
    public int getBedsBroken() { return bedsBroken; }
    public void addBedBroken() { bedsBroken++; }
    public boolean isBedDestroyed() { return bedDestroyed; }
    public void setBedDestroyed(boolean bedDestroyed) { this.bedDestroyed = bedDestroyed; }

    public boolean isAlive() {
        return state == GamePlayerState.ALIVE;
    }

    public boolean isDead() {
        return state == GamePlayerState.DEAD || state == GamePlayerState.ELIMINATED;
    }
}
