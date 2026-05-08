package dev.bedwarsx.gui.lobby;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.arena.ArenaState;
import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.permission.Perm;
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
 * GUI bonita de seleção de arena para jogadores.
 */
public class ArenaSelectorGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Inventory inventory;

    private static final int[] ARENA_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };

    public ArenaSelectorGui(BedWarsX plugin) {
        this.plugin = plugin;

        List<Arena> arenas = new ArrayList<>(plugin.getArenaManager().getArenas());
        arenas.removeIf(a -> !a.isSetup());

        GuiBuilder builder = GuiBuilder.criar("&8✦ &6&lBedWarsX &8— &fSelecionar Arena", 5);

        // ── Borda dupla decorativa ─────────────────────────────────────────────
        builder.borda(GuiBuilder.BORDER_BLACK);
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.glass((short)4, " "));     // ouro topo
            builder.set(36 + col, GuiBuilder.glass((short)7, " ")); // cinza base
        }
        for (int l = 1; l < 4; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        // ── Cabeçalho ──────────────────────────────────────────────────────────
        builder.set(4, ItemUtil.create(Material.NETHER_STAR, 1,
                "&6✦ &lBedWarsX",
                "&7Selecione uma arena para jogar.",
                "",
                "&a■ &7Esperando  &e■ &7Iniciando  &c■ &7Em Jogo  &8■ &7Cheia"));

        // ── Info lateral ───────────────────────────────────────────────────────
        long emPartida = plugin.getGameManager().getGames().stream()
                .mapToLong(Game::getPlayerCount).sum();
        builder.set(3, ItemUtil.create(Material.SKULL_ITEM, 1,
                "&e👥 &lJogadores Online",
                "&7Online: &e" + plugin.getServer().getOnlinePlayers().size(),
                "&7Em partidas: &a" + emPartida));

        builder.set(5, ItemUtil.create(Material.MAP, 1,
                "&a🏟 &lArenas Disponíveis",
                "&7Total: &e" + arenas.size(),
                "&7Esperando: &a" + arenas.stream()
                        .filter(a -> a.getState() == ArenaState.WAITING).count()));

        // ── Preencher arenas ───────────────────────────────────────────────────
        for (int i = 0; i < Math.min(arenas.size(), ARENA_SLOTS.length); i++) {
            builder.set(ARENA_SLOTS[i], buildArenaItem(arenas.get(i)));
        }

        // ── Slot vazio decorativo ──────────────────────────────────────────────
        builder.preencher(GuiBuilder.BORDER_DARK);

        this.inventory = builder.build();
    }

    private ItemStack buildArenaItem(Arena arena) {
        Game game = plugin.getGameManager().getGame(arena);
        int jogadores = game != null ? game.getPlayerCount() : 0;
        int max = arena.getEffectiveMaxPlayers();

        ArenaState estado = arena.getState();
        Material mat;
        short data;
        String corStatus;
        String status;
        boolean podeEntrar;

        switch (estado) {
            case WAITING:
                mat = Material.WOOL; data = 13;
                corStatus = "&a"; status = "✔ Esperando";
                podeEntrar = true;
                break;
            case STARTING:
                mat = Material.WOOL; data = 4;
                corStatus = "&e"; status = "⏳ Iniciando";
                podeEntrar = true;
                break;
            case IN_GAME:
                mat = Material.WOOL; data = 14;
                corStatus = "&c"; status = "⚔ Em Jogo";
                podeEntrar = false;
                break;
            default:
                mat = Material.WOOL; data = 7;
                corStatus = "&8"; status = "✗ Indisponível";
                podeEntrar = false;
                break;
        }

        // Barra de jogadores visual
        String barra = buildPlayerBar(jogadores, max);

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("  &fStatus: " + corStatus + status));
        lore.add(ChatUtil.color("  &fModo: &e" + arena.getMode().getDisplayName()));
        lore.add(ChatUtil.color("  &fJogadores: &e" + jogadores + " &7/ &e" + max));
        lore.add(ChatUtil.color("  " + barra));
        lore.add(ChatUtil.color("  &fTimes: &e" + arena.getEffectiveTeamLimit()
                + " &7x &e" + arena.getEffectiveTeamSize()));

        if (game != null && estado == ArenaState.STARTING) {
            lore.add(ChatUtil.color("  &fIniciando em: &e" + game.getCountdown() + "s"));
        }

        lore.add(ChatUtil.color(""));

        if (podeEntrar) {
            lore.add(ChatUtil.color("  &a▶ &fClique para entrar!"));
        } else if (estado == ArenaState.IN_GAME) {
            lore.add(ChatUtil.color("  &b👁 &fClique para assistir!"));
        } else {
            lore.add(ChatUtil.color("  &c✗ &findisponível no momento"));
        }

        String nome = corStatus + "&l" + arena.getDisplayName();
        return ItemUtil.create(mat, 1, data, nome, lore);
    }

    /** Barra visual de progresso de jogadores. */
    private String buildPlayerBar(int atual, int max) {
        int filled = max == 0 ? 0 : (int) Math.round((double) atual / max * 10);
        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "&a■" : "&7■");
        }
        bar.append("&8]");
        return bar.toString();
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (item == null || item.getType() == Material.STAINED_GLASS_PANE) return;

        // Encontrar arena pelo slot
        List<Arena> arenas = new ArrayList<>(plugin.getArenaManager().getArenas());
        arenas.removeIf(a -> !a.isSetup());

        for (int i = 0; i < Math.min(arenas.size(), ARENA_SLOTS.length); i++) {
            if (ARENA_SLOTS[i] == slot) {
                Arena arena = arenas.get(i);
                player.closeInventory();
                handleArenaClick(player, arena);
                return;
            }
        }
    }

    private void handleArenaClick(Player player, Arena arena) {
        if (plugin.getGameManager().isInGame(player)) {
            ChatUtil.send(player, Lang.p("ja-em-jogo"));
            return;
        }

        if (arena.getState() == ArenaState.IN_GAME) {
            // Oferecer spectate
            if (Perm.SPECTATOR.has(player)) {
                Game game = plugin.getGameManager().getGame(arena);
                if (game != null) {
                    game.addSpectator(player);
                    ChatUtil.send(player, Lang.p("modo-spectator"));
                }
            } else {
                ChatUtil.send(player, Lang.p("arena-em-jogo"));
            }
            return;
        }

        Game game = plugin.getGameManager().getGame(arena);
        if (game != null && game.getPlayerCount() >= arena.getEffectiveMaxPlayers()) {
            // VIP pode entrar em arena cheia
            if (Perm.VIP_ENTRAR_CHEIO.has(player)) {
                plugin.getGameManager().joinGame(player, arena);
                return;
            }
            ChatUtil.send(player, Lang.p("arena-cheia"));
            return;
        }

        boolean entrou = plugin.getGameManager().joinGame(player, arena);
        if (!entrou) {
            ChatUtil.send(player, "&cNão foi possível entrar na arena. Tente novamente.");
        }
    }
}
