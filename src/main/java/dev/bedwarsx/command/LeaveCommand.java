package dev.bedwarsx.command;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

    private final BedWarsX plugin;

    public LeaveCommand(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }
        Player player = (Player) sender;

        if (!plugin.getGameManager().isInGame(player)) {
            ChatUtil.send(player, "&cYou are not in a game!");
            return true;
        }

        plugin.getGameManager().leaveGame(player);
        ChatUtil.send(player, plugin.getConfigManager().getMessage("game-left"));
        return true;
    }
}
