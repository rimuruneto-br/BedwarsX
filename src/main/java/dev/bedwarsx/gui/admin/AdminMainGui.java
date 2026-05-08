package dev.bedwarsx.gui.admin;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════╗
 * ║   BedWarsX — Painel Administrativo ║
 * ╚══════════════════════════════════╝
 */
public class AdminMainGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Inventory inventory;

    public AdminMainGui(BedWarsX plugin) {
        this.plugin = plugin;

        GuiBuilder builder = GuiBuilder.criar("&8✦ &6&lBedWarsX &8— &fPainel Admin", 6);

        // ── Borda decorativa ──────────────────────────────────────────────────
        builder.borda(GuiBuilder.BORDER_BLACK);

        // ── Segunda camada de borda interna ───────────────────────────────────
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(45 + col, GuiBuilder.BORDER_DARK);
        }
        for (int linha = 1; linha < 5; linha++) {
            builder.set(linha * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(linha * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        // ── Título decorativo centro topo ─────────────────────────────────────
        builder.set(4, GuiBuilder.skull("MHF_Alex", "&6✦ &lBedWarsX Admin",
                "&7Bem-vindo ao painel administrativo.",
                "&7Gerencie arenas, NPCs e configurações."));

        // ── Botões principais ─────────────────────────────────────────────────

        // Gerenciar Arenas (slot 20)
        builder.set(20, buildArenaButton());

        // Status do Servidor (slot 22)
        builder.set(22, buildStatusButton());

        // Gerenciar NPCs (slot 24)
        builder.set(24, buildNPCButton());

        // Configurações (slot 29)
        builder.set(29, buildConfigButton());

        // Estatísticas (slot 31)
        builder.set(31, buildStatsButton());

        // Permissões (slot 33)
        builder.set(33, buildPermissaoButton());

        // ── Linha inferior de ações rápidas ───────────────────────────────────
        builder.set(46, ItemUtil.create(Material.EMERALD, 1,
                "&a⚡ &lForçar Início Rápido",
                "&7Inicia a partida mais populosa."));

        builder.set(48, ItemUtil.create(Material.BARRIER, 1,
                "&c⛔ &lEncerrar Todas",
                "&7Encerra todas as partidas ativas."));

        builder.set(50, ItemUtil.create(Material.NETHER_STAR, 1,
                "&e★ &lDefinir Lobby Principal",
                "&7Define o lobby na sua posição atual."));

        builder.set(52, ItemUtil.create(Material.BOOK_AND_QUILL, 1,
                "&b↻ &lRecarregar Plugin",
                "&7Recarrega configurações e mensagens."));

        this.inventory = builder.build();
    }

    private ItemStack buildArenaButton() {
        int total = plugin.getArenaManager().getArenas().size();
        long ativas = plugin.getGameManager().getGames().stream()
                .filter(g -> g.getState() == GameState.IN_GAME).count();
        long prontas = plugin.getArenaManager().getArenas().stream()
                .filter(Arena::isSetup).count();

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Gerencie todas as arenas do servidor."));
        lore.add("");
        lore.add(ChatUtil.color("  &fTotal de arenas: &e" + total));
        lore.add(ChatUtil.color("  &fProntas: &a" + prontas));
        lore.add(ChatUtil.color("  &fEm jogo: &c" + ativas));
        lore.add("");
        lore.add(ChatUtil.color("&eClique para gerenciar"));

        return ItemUtil.create(Material.MAP, 1, (short)0, "&a⚔ &lGerenciar Arenas", lore);
    }

    private ItemStack buildStatusButton() {
        int jogadores = plugin.getServer().getOnlinePlayers().size();
        long emPartida = plugin.getGameManager().getGames().stream()
                .mapToLong(g -> g.getPlayerCount()).sum();

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Visão geral do servidor."));
        lore.add("");
        lore.add(ChatUtil.color("  &fJogadores online: &e" + jogadores));
        lore.add(ChatUtil.color("  &fEm partidas: &a" + emPartida));
        lore.add(ChatUtil.color("  &fNo lobby: &7" + (jogadores - emPartida)));
        lore.add(ChatUtil.color("  &fTPS: &a" + getTPS()));
        lore.add("");
        lore.add(ChatUtil.color("&eClique para ver partidas ativas"));

        return ItemUtil.create(Material.REDSTONE_COMPARATOR, 1, (short)0, "&b📊 &lStatus do Servidor", lore);
    }

    private ItemStack buildNPCButton() {
        int total = plugin.getNpcManager().getNpcs().size();
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Crie e gerencie NPCs FakePlayer."));
        lore.add(ChatUtil.color("&7Suporte a skin customizada."));
        lore.add("");
        lore.add(ChatUtil.color("  &fNPCs criados: &e" + total));
        lore.add("");
        lore.add(ChatUtil.color("&eClique para gerenciar NPCs"));

        return ItemUtil.create(Material.SKULL_ITEM, 1, (short)3, "&d👤 &lGerenciar NPCs", lore);
    }

    private ItemStack buildConfigButton() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Altere configurações do plugin."));
        lore.add("");
        lore.add(ChatUtil.color("  &f● Contagem: &e" + plugin.getConfig().getInt("countdown.waiting", 30) + "s"));
        lore.add(ChatUtil.color("  &f● Respawn: &e" + plugin.getConfig().getInt("game.respawn-time", 5) + "s"));
        lore.add(ChatUtil.color("  &f● Scoreboard: " + (plugin.getConfig().getBoolean("scoreboard.enabled") ? "&aAtivado" : "&cDesativado")));
        lore.add("");
        lore.add(ChatUtil.color("&eClique para configurar"));

        return ItemUtil.create(Material.COMMAND, 1, (short)0, "&6⚙ &lConfigurações", lore);
    }

    private ItemStack buildStatsButton() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Veja e gerencie estatísticas."));
        lore.add("");
        lore.add(ChatUtil.color("  &f● Ver stats de jogador"));
        lore.add(ChatUtil.color("  &f● Resetar estatísticas"));
        lore.add(ChatUtil.color("  &f● Top jogadores"));
        lore.add("");
        lore.add(ChatUtil.color("&eClique para ver estatísticas"));

        return ItemUtil.create(Material.BOOK, 1, (short)0, "&e📈 &lEstatísticas", lore);
    }

    private ItemStack buildPermissaoButton() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Veja todas as permissões do plugin."));
        lore.add("");
        lore.add(ChatUtil.color("  &fbedwarsx.admin &8— &7Admin total"));
        lore.add(ChatUtil.color("  &fbedwarsx.jogar &8— &7Jogar"));
        lore.add(ChatUtil.color("  &fbedwarsx.vip &8— &7Benefícios VIP"));
        lore.add(ChatUtil.color("  &e+ muito mais..."));
        lore.add("");
        lore.add(ChatUtil.color("&eClique para ver permissões"));

        return ItemUtil.create(Material.GOLD_INGOT, 1, (short)0, "&6🔑 &lPermissões", lore);
    }

    private String getTPS() {
        try {
            Object server = plugin.getServer().getClass().getMethod("getServer").invoke(plugin.getServer());
            double[] tps = (double[]) server.getClass().getField("recentTps").get(server);
            double t = Math.min(tps[0], 20.0);
            String color = t >= 19 ? "&a" : t >= 15 ? "&e" : "&c";
            return color + String.format("%.1f", t);
        } catch (Exception e) {
            return "&a20.0";
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (item == null || !item.hasItemMeta()) return;
        String nome = item.getItemMeta().getDisplayName();

        switch (slot) {
            case 20: // Arenas
                plugin.getGuiManager().abrir(player, new AdminArenaListGui(plugin, player));
                break;
            case 22: // Status
                plugin.getGuiManager().abrir(player, new AdminStatusGui(plugin, player));
                break;
            case 24: // NPCs
                plugin.getGuiManager().abrir(player, new AdminNpcGui(plugin, player));
                break;
            case 29: // Config
                plugin.getGuiManager().abrir(player, new AdminConfigGui(plugin, player));
                break;
            case 31: // Stats
                plugin.getGuiManager().abrir(player, new AdminStatsGui(plugin, player));
                break;
            case 33: // Permissões
                plugin.getGuiManager().abrir(player, new AdminPermGui(plugin, player));
                break;
            case 46: // Forçar início rápido
                forcarInicioRapido(player);
                break;
            case 48: // Encerrar todas
                encerrarTodas(player);
                break;
            case 50: // Lobby
                plugin.getConfigManager().setLobbyLocation(player.getLocation());
                ChatUtil.send(player, Lang.p("adm-lobby-principal-set"));
                player.closeInventory();
                break;
            case 52: // Reload
                plugin.reloadConfig();
                Lang.reload();
                ChatUtil.send(player, Lang.p("recarga-sucesso"));
                player.closeInventory();
                break;
        }
    }

    private void forcarInicioRapido(Player player) {
        Game melhor = null;
        int maxPlayers = 0;
        for (Game g : plugin.getGameManager().getGames()) {
            if ((g.getState() == GameState.WAITING || g.getState() == GameState.STARTING)
                    && g.getPlayerCount() > maxPlayers) {
                melhor = g;
                maxPlayers = g.getPlayerCount();
            }
        }
        if (melhor == null) {
            ChatUtil.send(player, Lang.prefix() + "&cNenhuma partida disponível para iniciar.");
            return;
        }
        melhor.startGame();
        ChatUtil.send(player, Lang.p("adm-force-start", "arena", melhor.getArena().getId()));
        player.closeInventory();
    }

    private void encerrarTodas(Player player) {
        plugin.getGameManager().endAllGames();
        ChatUtil.send(player, Lang.prefix() + "&cTodas as partidas foram encerradas.");
        player.closeInventory();
    }
}
