package dev.bedwarsx.command;

import dev.bedwarsx.gui.admin.AdminMainGui;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.permission.Perm;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /bwadm — Abre o painel administrativo completo via GUI.
 * Admins que preferem comandos de texto podem usar /bw arena, /bw npc, etc.
 */
public class BwAdmCommand implements CommandExecutor {

    private final BedWarsX plugin;

    public BwAdmCommand(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.p("apenas-jogadores"));
            return true;
        }
        Player player = (Player) sender;

        if (!Perm.ADMIN_GUI.has(player)) {
            ChatUtil.send(player, Lang.p("sem-permissao"));
            return true;
        }

        plugin.getGuiManager().abrir(player, new AdminMainGui(plugin));
        return true;
    }
}
