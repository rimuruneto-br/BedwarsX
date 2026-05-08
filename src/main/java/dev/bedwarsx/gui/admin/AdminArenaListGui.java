package dev.bedwarsx.gui.admin;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.arena.ArenaState;
import dev.bedwarsx.game.Game;
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

public class AdminArenaListGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<Arena> arenas;
    private int pagina = 0;
    private static final int POR_PAGINA = 28; // 4 linhas x 7 colunas

    public AdminArenaListGui(BedWarsX plugin, Player player) {
        this(plugin, player, 0);
    }

    public AdminArenaListGui(BedWarsX plugin, Player player, int pagina) {
        this.plugin = plugin;
        this.player = player;
        this.pagina = pagina;
        this.arenas = new ArrayList<>(plugin.getArenaManager().getArenas());

        GuiBuilder builder = GuiBuilder.criar("&8✦ &6Arenas &8— &fGerenciar", 6);
        builder.bordaPadrao();

        // ── Segunda linha de borda
        for (int col = 1; col < 8; col++) builder.set(col, GuiBuilder.BORDER_DARK);
        for (int col = 1; col < 8; col++) builder.set(45 + col, GuiBuilder.BORDER_DARK);
        for (int l = 1; l < 5; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        // ── Ícone de cabeçalho
        builder.set(4, ItemUtil.create(Material.MAP, 1, "&6⚔ &lArenas &7(" + arenas.size() + " total)",
                "&7Clique em uma arena para gerenciá-la.",
                "&7Slots coloridos = status da arena.",
                "",
                "&a■ &7Esperando     &e■ &7Iniciando",
                "&c■ &7Em jogo       &8■ &7Desativada"));

        // ── Botão criar nova arena
        builder.set(2, ItemUtil.create(Material.EMERALD, 1,
                "&a✚ &lCriar Nova Arena",
                "&7Clique para criar uma nova arena.",
                "&7Você será solicitado o nome no chat."));

        // ── Preencher arenas
        int inicio = pagina * POR_PAGINA;
        int[] slots = gerarSlots();

        for (int i = 0; i < slots.length && (inicio + i) < arenas.size(); i++) {
            Arena arena = arenas.get(inicio + i);
            builder.set(slots[i], buildArenaItem(arena));
        }

        // ── Paginação e navegação
        boolean temAnterior = pagina > 0;
        boolean temProxima = (pagina + 1) * POR_PAGINA < arenas.size();
        builder.paginacao(temAnterior, temProxima);
        builder.set(inventory_size(builder) - 9, GuiBuilder.SETA_VOLTAR);

        this.inventory = builder.build();
    }

    private int inventory_size(GuiBuilder b) { return 54; }

    private int[] gerarSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int l = 1; l <= 4; l++) {
            for (int c = 2; c <= 6; c++) {
                slots.add(l * 9 + c);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private ItemStack buildArenaItem(Arena arena) {
        Game game = plugin.getGameManager().getGame(arena);
        int jogadores = game != null ? game.getPlayerCount() : 0;
        ArenaState estado = arena.getState();

        Material mat;
        short data;
        String corEstado;

        switch (estado) {
            case WAITING:
                mat = Material.WOOL; data = 13; corEstado = "&aEsperando"; break;
            case STARTING:
                mat = Material.WOOL; data = 4;  corEstado = "&eIniciando"; break;
            case IN_GAME:
                mat = Material.WOOL; data = 14; corEstado = "&cEm Jogo"; break;
            default:
                mat = Material.WOOL; data = 7;  corEstado = "&8Desativada"; break;
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("  &f● Status: " + corEstado));
        lore.add(ChatUtil.color("  &f● Modo: &e" + arena.getMode().getDisplayName()));
        lore.add(ChatUtil.color("  &f● Jogadores: &e" + jogadores + "&7/&e" + arena.getEffectiveMaxPlayers()));
        lore.add(ChatUtil.color("  &f● Times: &e" + arena.getEffectiveTeamLimit() + "x" + arena.getEffectiveTeamSize()));
        lore.add(ChatUtil.color("  &f● Configurada: " + (arena.isSetup() ? "&a✔ Sim" : "&c✗ Não")));

        if (game != null && game.getState() == dev.bedwarsx.game.GameState.IN_GAME) {
            lore.add(ChatUtil.color(""));
            lore.add(ChatUtil.color("  &fTimes ativos: &e" + game.getTeams().values().stream()
                    .filter(t -> !t.isEliminated()).count()));
        }

        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("&eClique &7para gerenciar"));
        lore.add(ChatUtil.color("&cShift + Clique &7para deletar"));

        return ItemUtil.create(mat, 1, data, "&f&l" + arena.getDisplayName() + " &8[&7" + arena.getId() + "&8]", lore);
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (item == null || !item.hasItemMeta()) return;

        // Voltar
        if (slot == 45) {
            plugin.getGuiManager().abrir(player, new AdminMainGui(plugin));
            return;
        }
        // Próxima página
        if (slot == 53) {
            plugin.getGuiManager().abrir(player, new AdminArenaListGui(plugin, player, pagina + 1));
            return;
        }
        // Criar nova arena
        if (slot == 2) {
            player.closeInventory();
            ChatUtil.send(player, Lang.prefix() + "&eDigite o nome da nova arena no chat: &7(ou &ccancel &7para cancelar)");
            plugin.getChatInputManager().esperar(player, input -> {
                if (input.equalsIgnoreCase("cancel")) {
                    ChatUtil.send(player, Lang.prefix() + "&cCriação cancelada.");
                    return;
                }
                if (plugin.getArenaManager().getArena(input) != null) {
                    ChatUtil.send(player, Lang.p("arena-ja-existe", "arena", input));
                    return;
                }
                plugin.getArenaManager().createArena(input);
                ChatUtil.send(player, Lang.p("arena-criada", "arena", input));
                plugin.getGuiManager().abrir(player, new AdminArenaListGui(plugin, player));
            });
            return;
        }

        // Clique em arena
        int[] slots = gerarSlots();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                int idx = pagina * POR_PAGINA + i;
                if (idx >= arenas.size()) return;
                Arena arena = arenas.get(idx);

                if (event.isShiftClick()) {
                    // Confirmar deleção
                    plugin.getGuiManager().abrir(player, new ConfirmGui(plugin, player,
                            "&cDeletar arena &e" + arena.getId() + "&c?",
                            () -> {
                                plugin.getArenaManager().deleteArena(arena.getId());
                                ChatUtil.send(player, Lang.p("arena-deletada", "arena", arena.getId()));
                                plugin.getGuiManager().abrir(player, new AdminArenaListGui(plugin, player));
                            },
                            () -> plugin.getGuiManager().abrir(player, new AdminArenaListGui(plugin, player))
                    ));
                } else {
                    plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
                }
                return;
            }
        }
    }
}
