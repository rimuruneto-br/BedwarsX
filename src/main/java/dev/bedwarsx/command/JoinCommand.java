package dev.bedwarsx.command;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.arena.ArenaState;
import dev.bedwarsx.game.Game;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JoinCommand implements CommandExecutor, TabCompleter {

    private final BedWarsX plugin;

    public JoinCommand(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("bedwarsx.play")) {
            ChatUtil.send(player, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (plugin.getGameManager().isInGame(player)) {
            ChatUtil.send(player, "&cYou are already in a game! Use /leave first.");
            return true;
        }

        // Auto-join random available arena if no arg
        if (args.length == 0) {
            List<Arena> available = plugin.getArenaManager().getAvailableArenas();
            if (available.isEmpty()) {
                ChatUtil.send(player, "&cNo arenas available right now!");
                return true;
            }
            Collections.shuffle(available);
            Arena arena = available.get(0);
            boolean joined = plugin.getGameManager().joinGame(player, arena);
            if (!joined) ChatUtil.send(player, "&cFailed to join. Try again.");
            return true;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            ChatUtil.send(player, plugin.getConfigManager().getMessage("arena-not-found",
                    java.util.Collections.singletonMap("arena", args[0])));
            return true;
        }

        if (!arena.isSetup()) {
            ChatUtil.send(player, "&cThis arena is not yet available.");
            return true;
        }

        if (arena.getState() == ArenaState.IN_GAME) {
            ChatUtil.send(player, plugin.getConfigManager().getMessage("arena-in-game"));
            return true;
        }

        Game game = plugin.getGameManager().getGame(arena);
        if (game != null && game.getPlayerCount() >= arena.getEffectiveMaxPlayers()) {
            ChatUtil.send(player, plugin.getConfigManager().getMessage("arena-full"));
            return true;
        }

        boolean joined = plugin.getGameManager().joinGame(player, arena);
        if (!joined) {
            ChatUtil.send(player, "&cFailed to join. The arena may be full or in game.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Arena a : plugin.getArenaManager().getAvailableArenas()) {
                if (a.getId().toLowerCase().startsWith(args[0].toLowerCase())) names.add(a.getId());
            }
            return names;
        }
        return Collections.emptyList();
    }
}
