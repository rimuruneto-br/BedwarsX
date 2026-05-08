package dev.bedwarsx.gui.admin;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.team.BedWarsTeam;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI de edição completa de uma arena.
 * Permite configurar tudo sem digitar comandos.
 */
public class AdminArenaEditGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Arena arena;
    private final Inventory inventory;

    public AdminArenaEditGui(BedWarsX plugin, Player player, Arena arena) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;

        GuiBuilder builder = GuiBuilder.criar("&8✦ &6Arena: &f" + arena.getId() + " &8— &fEditar", 6);
        builder.bordaPadrao();

        // ── Decoração interna
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(45 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 5; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        // ── Ícone de info da arena (topo)
        builder.set(4, buildInfoIcon());

        // ── Configurações básicas (linha 1)
        builder.set(10, buildNomeBtn());       // Nome display
        builder.set(11, buildLobbyBtn());      // Lobby da arena
        builder.set(12, buildSpectatorBtn());  // Spawn spectator
        builder.set(13, buildMinMaxBtn());     // Min/Max jogadores
        builder.set(14, buildMundoBtn());      // Mundo

        // ── Times (linha 2)
        builder.set(19, buildTimesBtn());      // Gerenciar times

        // ── Geradores (linha 3)
        builder.set(20, buildGeradorBtn(Material.IRON_INGOT, "&fFerro", "iron", arena.getIronGenerators().size()));
        builder.set(21, buildGeradorBtn(Material.GOLD_INGOT, "&6Ouro", "gold", arena.getGoldGenerators().size()));
        builder.set(22, buildGeradorBtn(Material.DIAMOND, "&bDiamante", "diamond", arena.getDiamondGenerators().size()));
        builder.set(23, buildGeradorBtn(Material.EMERALD, "&2Esmeralda", "emerald", arena.getEmeraldGenerators().size()));

        // ── Status e ações (linha 4)
        builder.set(28, buildStatusBtn());
        builder.set(29, buildHabilitarBtn());

        // ── Ações de partida ativa
        Game game = plugin.getGameManager().getGame(arena);
        if (game != null && game.getState() == GameState.IN_GAME) {
            builder.set(31, ItemUtil.create(Material.EMERALD, 1,
                    "&a⚡ &lForçar Início",
                    "&7Inicia a partida imediatamente."));
            builder.set(33, ItemUtil.create(Material.BARRIER, 1,
                    "&c⛔ &lEncerrar Partida",
                    "&7Encerra a partida em andamento."));
        } else {
            builder.set(31, ItemUtil.create(Material.EMERALD, 1,
                    "&a⚡ &lForçar Início",
                    "&7Inicia a partida imediatamente.",
                    "",
                    arena.isSetup() ? "&eClique para iniciar" : "&cArena não configurada!"));
        }

        // ── Navegação
        builder.set(45, GuiBuilder.SETA_VOLTAR);

        this.inventory = builder.build();
    }

    private ItemStack buildInfoIcon() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("  &fID: &e" + arena.getId()));
        lore.add(ChatUtil.color("  &fNome: &e" + arena.getDisplayName()));
        lore.add(ChatUtil.color("  &fStatus: " + arena.getState().getDisplayName()));
        lore.add(ChatUtil.color("  &fConfigurada: " + (arena.isSetup() ? "&a✔" : "&c✗")));
        lore.add(ChatUtil.color("  &fModo: &e" + arena.getMode().getDisplayName()));
        lore.add(ChatUtil.color("  &fJogadores: &e" + arena.getMinPlayers() + "&7-&e" + arena.getEffectiveMaxPlayers()));
        lore.add(ChatUtil.color("  &fTimes: &e" + arena.getEffectiveTeamLimit() + "x" + arena.getEffectiveTeamSize()));
        lore.add(ChatUtil.color(""));
        return ItemUtil.create(Material.MAP, 1, (short)0, "&6&l" + arena.getDisplayName(), lore);
    }

    private ItemStack buildNomeBtn() {
        return ItemUtil.create(Material.NAME_TAG, 1,
                "&e✏ &lNome de Exibição",
                "&7Atual: &f" + arena.getDisplayName(),
                "",
                "&eClique para alterar");
    }

    private ItemStack buildLobbyBtn() {
        boolean temLobby = arena.getLobby() != null;
        return ItemUtil.create(Material.BED, 1,
                "&6🏠 &lLobby da Arena",
                temLobby ? "&a✔ Definido" : "&c✗ Não definido",
                "",
                "&eClique para definir na sua posição");
    }

    private ItemStack buildSpectatorBtn() {
        boolean tem = arena.getSpectatorSpawn() != null;
        return ItemUtil.create(Material.EYE_OF_ENDER, 1,
                "&b👁 &lSpawn Spectator",
                tem ? "&a✔ Definido" : "&c✗ Não definido",
                "",
                "&eClique para definir na sua posição");
    }

    private ItemStack buildMinMaxBtn() {
        return ItemUtil.create(Material.SIGN, 1,
                "&7👥 &lMín/Máx Jogadores",
                "&7Mínimo: &e" + arena.getMinPlayers(),
                "&7Máximo: &e" + arena.getMaxPlayers(),
                "&7Por time: &e" + arena.getMaxPlayersPerTeam(),
                "",
                "&eClique para alterar");
    }

    private ItemStack buildMundoBtn() {
        return ItemUtil.create(Material.GRASS, 1,
                "&2🌍 &lMundo",
                "&7Mundo atual: &e" + (arena.getWorldName() != null ? arena.getWorldName() : "Não definido"),
                "&7Template: &e" + (arena.getMapName() != null ? arena.getMapName() : "Não definido"),
                "",
                "&eClique &7para usar o mundo atual",
                "&eShift+Clique &7para importar como template");
    }

    private ItemStack buildTimesBtn() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Gerencie os times desta arena."));
        lore.add(ChatUtil.color(""));
        for (BedWarsTeam t : arena.getEnabledTeams()) {
            boolean temSpawn = arena.getTeamSpawns().containsKey(t);
            boolean temCama = arena.getTeamBeds().containsKey(t);
            lore.add(ChatUtil.color("  " + t.getColoredName() + " &8— "
                    + (temSpawn ? "&a⬛" : "&c⬛") + " Spawn "
                    + (temCama ? "&a⬛" : "&c⬛") + " Cama"));
        }
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("&eClique para gerenciar times"));
        return ItemUtil.create(Material.WOOL, 1, (short)14, "&c🏆 &lGerenciar Times", lore);
    }

    private ItemStack buildGeradorBtn(Material mat, String nome, String tipo, int count) {
        return ItemUtil.create(mat, Math.max(1, count),
                nome + " &7— &fGerador",
                "&7Geradores configurados: &e" + count,
                "",
                "&eClique &7para adicionar na sua posição",
                "&cShift + Clique &7para remover o último");
    }

    private ItemStack buildStatusBtn() {
        boolean ok = plugin.getArenaManager().isArenaComplete(arena);
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Checklist de configuração:"));
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color((arena.getLobby() != null ? "&a✔" : "&c✗") + " &fLobby definido"));
        for (BedWarsTeam t : arena.getEnabledTeams()) {
            lore.add(ChatUtil.color((arena.getTeamSpawns().containsKey(t) ? "&a✔" : "&c✗")
                    + " &fSpawn " + t.getColoredName()));
            lore.add(ChatUtil.color((arena.getTeamBeds().containsKey(t) ? "&a✔" : "&c✗")
                    + " &fCama " + t.getColoredName()));
        }
        lore.add(ChatUtil.color((arena.getEnabledTeams().size() >= 2 ? "&a✔" : "&c✗")
                + " &fMínimo 2 times"));
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color(ok ? "&aArena pronta!" : "&cArena incompleta!"));
        return ItemUtil.create(ok ? Material.EMERALD : Material.REDSTONE, 1,
                ok ? "&a✔ &lArena Completa" : "&c✗ &lArena Incompleta", lore);
    }

    private ItemStack buildHabilitarBtn() {
        boolean ativa = arena.isSetup();
        return GuiBuilder.criarBotaoToggle(!ativa,
                ativa ? "&a● &lArena Habilitada" : "&c● &lArena Desabilitada");
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (item == null || !item.hasItemMeta()) return;

        switch (slot) {
            case 45: // Voltar
                plugin.getGuiManager().abrir(player, new AdminArenaListGui(plugin, player));
                break;

            case 10: // Alterar nome
                player.closeInventory();
                ChatUtil.send(player, Lang.prefix() + "&eDigite o novo nome de exibição (ou &ccancel&e):");
                plugin.getChatInputManager().esperar(player, input -> {
                    if (!input.equalsIgnoreCase("cancel")) {
                        arena.setDisplayName(input);
                        plugin.getArenaManager().saveArenas();
                        ChatUtil.send(player, Lang.prefix() + "&aNome alterado para: &f" + input);
                    }
                    plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
                });
                break;

            case 11: // Lobby
                arena.setLobby(player.getLocation());
                plugin.getArenaManager().saveArenas();
                ChatUtil.send(player, Lang.p("adm-lobby-set", "arena", arena.getId()));
                plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
                break;

            case 12: // Spectator spawn
                arena.setSpectatorSpawn(player.getLocation());
                plugin.getArenaManager().saveArenas();
                ChatUtil.send(player, Lang.prefix() + "&aSpawn de spectator definido!");
                plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
                break;

            case 13: // Min/Max
                plugin.getGuiManager().abrir(player, new AdminArenaPlayersGui(plugin, player, arena));
                break;

            case 14: // Mundo
                handleWorldClick(player, event.isShiftClick());
                break;

            case 19: // Times
                plugin.getGuiManager().abrir(player, new AdminArenaTeamsGui(plugin, player, arena));
                break;

            case 20: case 21: case 22: case 23: // Geradores
                handleGeradorClick(player, slot, event.isShiftClick());
                break;

            case 28: // Status - checklist só visual
                break;

            case 29: // Habilitar/Desabilitar
                if (!plugin.getArenaManager().isArenaComplete(arena) && !arena.isSetup()) {
                    ChatUtil.send(player, Lang.p("arena-incompleta"));
                    return;
                }
                arena.setSetup(!arena.isSetup());
                plugin.getArenaManager().saveArenas();
                ChatUtil.send(player, arena.isSetup()
                        ? Lang.p("arena-habilitada", "arena", arena.getId())
                        : Lang.prefix() + "&cArena &e" + arena.getId() + " &cdesabilitada.");
                plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
                break;

            case 31: // Forçar início
                Game game = plugin.getGameManager().getGame(arena);
                if (game == null || !arena.isSetup()) {
                    ChatUtil.send(player, Lang.p("arena-nao-configurada"));
                    return;
                }
                game.startGame();
                ChatUtil.send(player, Lang.p("adm-force-start", "arena", arena.getId()));
                player.closeInventory();
                break;

            case 33: // Encerrar
                Game gameAtivo = plugin.getGameManager().getGame(arena);
                if (gameAtivo != null) {
                    gameAtivo.endGame();
                    ChatUtil.send(player, Lang.p("adm-force-stop", "arena", arena.getId()));
                }
                player.closeInventory();
                break;
        }
    }

    private void handleGeradorClick(Player player, int slot, boolean shift) {
        String[] tipos = {"iron", "gold", "diamond", "emerald"};
        int idx = slot - 20;
        String tipo = tipos[idx];

        if (shift) {
            // Remover último
            List<?> lista = getGeneratorList(tipo);
            if (!lista.isEmpty()) {
                lista.remove(lista.size() - 1);
                plugin.getArenaManager().saveArenas();
                ChatUtil.send(player, Lang.prefix() + "&cGerador de &e" + tipo + " &cremovido.");
            }
        } else {
            // Adicionar na posição atual
            plugin.getArenaManager().addGenerator(arena.getId(), tipo, player.getLocation());
            ChatUtil.send(player, Lang.p("adm-gerador-add", "tipo", tipo));
        }
        plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
    }

    private void handleWorldClick(Player player, boolean shift) {
        String worldName = player.getWorld().getName();
        arena.setWorldName(worldName);
        if (shift) {
            String template = plugin.getWorldInstanceManager().normalizeTemplateName(worldName);
            try {
                if (!plugin.getWorldInstanceManager().hasTemplate(template)) {
                    template = plugin.getWorldInstanceManager().importWorld(worldName);
                }
                arena.setMapName(template);
                ChatUtil.send(player, Lang.prefix() + "&aTemplate importado/selecionado: &e" + template);
            } catch (IOException e) {
                ChatUtil.send(player, Lang.prefix() + "&cNao foi possivel importar o mapa: &f" + e.getMessage());
            }
        } else {
            ChatUtil.send(player, Lang.prefix() + "&aMundo definido: &e" + worldName);
        }
        plugin.getArenaManager().saveArenas();
        plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
    }

    @SuppressWarnings("unchecked")
    private List getGeneratorList(String tipo) {
        switch (tipo) {
            case "iron":    return arena.getIronGenerators();
            case "gold":    return arena.getGoldGenerators();
            case "diamond": return arena.getDiamondGenerators();
            case "emerald": return arena.getEmeraldGenerators();
            default:        return new ArrayList<>();
        }
    }
}
