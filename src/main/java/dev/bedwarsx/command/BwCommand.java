package dev.bedwarsx.command;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.arena.ArenaState;
import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.gui.lobby.ArenaSelectorGui;
import dev.bedwarsx.gui.lobby.PlayerStatsGui;
import dev.bedwarsx.gui.lobby.PlayerSettingsGui;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.permission.Perm;
import dev.bedwarsx.stats.ProgressionManager;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Comando principal do jogador: /bw
 * Subcomandos: jogar, sair, assistir, stats, top, ajuda
 * 
 * Este é o comando do JOGADOR (não admin)
 * O comando admin é: /bwadm (BedWarsCommand)
 */
public class BwCommand implements CommandExecutor, TabCompleter {

    private final BedWarsX plugin;

    public BwCommand(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&c[BedWarsX] Este comando é apenas para jogadores!"));
            return true;
        }
        Player player = (Player) sender;

        // /bw sem argumento — abre seletor de arenas
        if (args.length == 0) {
            abrirSeletor(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "jogar":
            case "play":
                cmdJogar(player, args);
                break;
            case "sair":
            case "leave":
                cmdSair(player);
                break;
            case "assistir":
            case "spectate":
            case "spec":
                cmdAssistir(player, args);
                break;
            case "stats":
            case "estatisticas":
                cmdStats(player, args);
                break;
            case "top":
            case "ranking":
            case "leaderboard":
                cmdTop(player, args);
                break;
            case "configuracoes":
            case "config":
            case "cfg":
                cmdConfig(player);
                break;
            case "ajuda":
            case "help":
            case "?":
                cmdAjuda(player);
                break;
            default:
                ChatUtil.send(player, Lang.prefix() + "&cSubcomando inválido. Use &e/bw ajuda&c.");
        }
        return true;
    }

    private void abrirSeletor(Player player) {
        if (!Perm.JOGAR.has(player)) {
            ChatUtil.send(player, Lang.p("sem-permissao"));
            return;
        }
        plugin.getGuiManager().abrir(player, new ArenaSelectorGui(plugin));
    }

    private void cmdJogar(Player player, String[] args) {
        if (!Perm.JOGAR.has(player)) {
            ChatUtil.send(player, Lang.p("sem-permissao"));
            return;
        }
        if (plugin.getGameManager().isInGame(player)) {
            ChatUtil.send(player, Lang.p("ja-em-jogo"));
            return;
        }
        if (args.length < 2) {
            // Auto-join arena disponível
            List<Arena> disponiveis = plugin.getArenaManager().getAvailableArenas();
            // FIXO: Validação de null/empty
            if (disponiveis == null || disponiveis.isEmpty()) {
                ChatUtil.send(player, Lang.p("sem-arenas-disponiveis"));
                return;
            }
            Collections.shuffle(disponiveis);
            plugin.getGameManager().joinGame(player, disponiveis.get(0));
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            ChatUtil.send(player, Lang.p("arena-nao-encontrada", "arena", args[1]));
            return;
        }
        if (!arena.isSetup()) {
            ChatUtil.send(player, Lang.p("arena-nao-configurada"));
            return;
        }
        if (arena.getState() == ArenaState.IN_GAME) {
            ChatUtil.send(player, Lang.p("arena-em-jogo"));
            return;
        }
        Game game = plugin.getGameManager().getGame(arena);
        if (game != null && game.getPlayerCount() >= arena.getEffectiveMaxPlayers()
                && !Perm.VIP_ENTRAR_CHEIO.has(player)) {
            ChatUtil.send(player, Lang.p("arena-cheia"));
            return;
        }
        plugin.getGameManager().joinGame(player, arena);
    }

    private void cmdSair(Player player) {
        if (!plugin.getGameManager().isInGame(player)) {
            ChatUtil.send(player, Lang.p("nao-em-jogo"));
            return;
        }
        plugin.getGameManager().leaveGame(player);
        ChatUtil.send(player, Lang.p("saiu-arena"));
    }

    private void cmdAssistir(Player player, String[] args) {
        if (!Perm.SPECTATOR.has(player)) {
            ChatUtil.send(player, Lang.p("sem-permissao"));
            return;
        }
        if (plugin.getGameManager().isInGame(player)) {
            ChatUtil.send(player, Lang.p("ja-em-jogo"));
            return;
        }
        if (args.length < 2) {
            ChatUtil.send(player, Lang.p("uso-invalido", "uso", "/bw assistir <arena>"));
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            ChatUtil.send(player, Lang.p("arena-nao-encontrada", "arena", args[1]));
            return;
        }
        Game game = plugin.getGameManager().getGame(arena);
        if (game == null || game.getState() != GameState.IN_GAME) {
            ChatUtil.send(player, Lang.p("spec-nao-disponivel"));
            return;
        }
        game.addSpectator(player);
    }

    private void cmdStats(Player player, String[] args) {
        if (!Perm.STATS_VER.has(player)) {
            ChatUtil.send(player, Lang.p("sem-permissao"));
            return;
        }
        Player alvo = player;
        if (args.length > 1) {
            if (!Perm.STATS_VER_OUTROS.has(player)) {
                ChatUtil.send(player, Lang.p("sem-permissao"));
                return;
            }
            alvo = plugin.getServer().getPlayer(args[1]);
            if (alvo == null) {
                ChatUtil.send(player, Lang.p("jogador-nao-encontrado", "jogador", args[1]));
                return;
            }
        }
        plugin.getGuiManager().abrir(player, new PlayerStatsGui(plugin, alvo));
    }

    private void cmdTop(Player player, String[] args) {
        String period = args.length > 1 ? args[1] : "weekly";
        String stat = normalizeTopStat(args.length > 2 ? args[2] : "wins");
        if (!period.equalsIgnoreCase("weekly") && !period.equalsIgnoreCase("monthly")) {
            ChatUtil.send(player, "&cUse &eweekly &cou &emonthly&c.");
            return;
        }
        if (stat == null) {
            ChatUtil.send(player, "&cStats validas: &ewins, kills, beds, finalKills, games&c.");
            return;
        }

        List<ProgressionManager.LeaderboardEntry> top = plugin.getProgressionManager().top(period, stat, 10);
        ChatUtil.send(player, "&6&lTop " + period.toLowerCase() + " &7- &e" + stat);
        
        // FIXO: Validação de null e empty list
        if (top == null || top.isEmpty()) {
            ChatUtil.send(player, "&7Ainda nao ha dados suficientes.");
            return;
        }
        
        int pos = 1;
        for (ProgressionManager.LeaderboardEntry entry : top) {
            // FIXO: Validação de entry null e entry.name null
            if (entry == null || entry.name == null) {
                continue;
            }
            String value = entry.value != null ? entry.value.toString() : "0";
            ChatUtil.send(player, "&e#" + pos++ + " &f" + entry.name + " &7- &a" + value);
        }
    }

    private String normalizeTopStat(String stat) {
        if (stat == null) return null;
        String clean = stat.toLowerCase();
        if (clean.equals("wins") || clean.equals("kills") || clean.equals("beds") || clean.equals("games")) return clean;
        if (clean.equals("finalkills") || clean.equals("final_kills") || clean.equals("final-kills")) return "finalKills";
        return null;
    }

    private void cmdConfig(Player player) {
        plugin.getGuiManager().abrir(player, new PlayerSettingsGui(plugin, player));
    }

    private void cmdAjuda(Player player) {
        player.sendMessage(ChatUtil.color("&8&m                              "));
        player.sendMessage(ChatUtil.color("  &6✦ &lBedWarsX &8— &fAjuda"));
        player.sendMessage(ChatUtil.color("&8&m                              "));
        player.sendMessage(ChatUtil.color("  &e/bw &7— Abre o seletor de arenas"));
        player.sendMessage(ChatUtil.color("  &e/bw jogar [arena] &7— Entra em uma partida"));
        player.sendMessage(ChatUtil.color("  &e/bw sair &7— Sai da partida atual"));
        player.sendMessage(ChatUtil.color("  &e/bw assistir <arena> &7— Assiste uma partida"));
        player.sendMessage(ChatUtil.color("  &e/bw stats [jogador] &7— Veja estatísticas"));
        player.sendMessage(ChatUtil.color("  &e/bw top [weekly|monthly] [stat] &7— Rankings"));
        player.sendMessage(ChatUtil.color("  &e/bw configuracoes &7— Suas configurações"));
        player.sendMessage(ChatUtil.color("&8&m                              "));
        if (Perm.ADMIN_GUI.has(player)) {
            player.sendMessage(ChatUtil.color("  &c/bwadm &7— Painel administrativo"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        Player player = (Player) sender;
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = Arrays.asList("jogar", "sair", "assistir", "stats", "top", "configuracoes", "ajuda");
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) result.add(s);
            }
            return result;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("jogar") || args[0].equalsIgnoreCase("assistir")) {
                for (Arena a : plugin.getArenaManager().getArenas()) {
                    if (a.getId().startsWith(args[1])) result.add(a.getId());
                }
            }
            if (args[0].equalsIgnoreCase("stats") && Perm.STATS_VER_OUTROS.has(player)) {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) result.add(p.getName());
                }
            }
            if (args[0].equalsIgnoreCase("top") || args[0].equalsIgnoreCase("ranking")) {
                for (String period : Arrays.asList("weekly", "monthly")) {
                    if (period.startsWith(args[1].toLowerCase())) result.add(period);
                }
            }
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("top") || args[0].equalsIgnoreCase("ranking"))) {
            for (String stat : Arrays.asList("wins", "kills", "beds", "finalKills", "games")) {
                if (stat.toLowerCase().startsWith(args[2].toLowerCase())) result.add(stat);
            }
        }
        return result;
    }
}
