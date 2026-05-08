package dev.bedwarsx.game;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final BedWarsX plugin;
    private final Map<String, Game> games = new HashMap<>();

    public GameManager(BedWarsX plugin) {
        this.plugin = plugin;
    }

    public Game getOrCreateGame(Arena arena) {
        return games.computeIfAbsent(arena.getId(), id -> new Game(plugin, arena));
    }

    public Game getGame(Arena arena) {
        return games.get(arena.getId());
    }

    public Game getGame(String arenaId) {
        return games.get(arenaId);
    }

    public Game getPlayerGame(Player player) {
        for (Game game : games.values()) {
            if (game.isInGame(player)) return game;
        }
        return null;
    }

    public boolean isInGame(Player player) {
        return getPlayerGame(player) != null;
    }

    public boolean isInGame(Player player, Arena arena) {
        Game game = games.get(arena.getId());
        return game != null && game.isInGame(player);
    }

    public boolean joinGame(Player player, Arena arena) {
        if (isInGame(player)) return false;

        Game game = getOrCreateGame(arena);
        return game.addPlayer(player);
    }

    public void leaveGame(Player player) {
        Game game = getPlayerGame(player);
        if (game != null) {
            game.removePlayer(player);
        }
    }

    public Collection<Game> getGames() {
        return games.values();
    }

    public void endAllGames() {
        for (Game game : games.values()) {
            if (game.getState() == GameState.IN_GAME || game.getState() == GameState.STARTING) {
                game.endGame();
            }
        }
    }

    public void shutdownAllGames() {
        for (Game game : new ArrayList<>(games.values())) {
            game.forceShutdown();
        }
        games.clear();
    }
}
