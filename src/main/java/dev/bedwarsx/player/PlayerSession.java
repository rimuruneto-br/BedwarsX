package dev.bedwarsx.player;

import dev.bedwarsx.game.GamePlayerState;

import java.util.UUID;

public class PlayerSession {
    private final UUID uuid;
    private String playerName;
    private GamePlayerState state = GamePlayerState.ALIVE;
    private String arenaId;
    private long updatedAt;

    public PlayerSession(UUID uuid) {
        this.uuid = uuid;
        this.updatedAt = System.currentTimeMillis();
    }

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; touch(); }
    public GamePlayerState getState() { return state; }
    public void setState(GamePlayerState state) { this.state = state == null ? GamePlayerState.ALIVE : state; touch(); }
    public String getArenaId() { return arenaId; }
    public void setArenaId(String arenaId) { this.arenaId = arenaId; touch(); }
    public long getUpdatedAt() { return updatedAt; }

    private void touch() {
        updatedAt = System.currentTimeMillis();
    }
}
