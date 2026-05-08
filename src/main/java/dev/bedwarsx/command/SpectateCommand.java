package dev.bedwarsx.command;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.arena.ArenaState;
import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
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

public class SpectateCommand implements CommandExecutor, TabCompleter {

    private final BedWarsX plugin;

    public SpectateCommand(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }
        Player player = (Player) sender;

        if (plugin.getGameManager().isInGame(player)) {
            ChatUtil.send(player, "&cLeave your current game first with /leave.");
            return true;
        }

        if (args.length == 0) {
            ChatUtil.send(player, "&cUsage: /spectate <arena>");
            return true;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            ChatUtil.send(player, plugin.getConfigManager().getMessage("arena-not-found",
                    Collections.singletonMap("arena", args[0])));
            return true;
        }

        Game game = plugin.getGameManager().getGame(arena);
        if (game == null || game.getState() != GameState.IN_GAME) {
            ChatUtil.send(player, "&cNo active game in that arena.");
            return true;
        }

        game.addSpectator(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Arena a : plugin.getArenaManager().getArenas()) {
                if (a.getState() == ArenaState.IN_GAME
                        && a.getId().toLowerCase().startsWith(args[0].toLowerCase())) {
                    names.add(a.getId());
                }
            }
            return names;
        }
        return Collections.emptyList();
    }
}
