package dev.bedwarsx.gui.admin;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.arena.ArenaMode;
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

public class AdminArenaPlayersGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Arena arena;
    private final Inventory inventory;

    public AdminArenaPlayersGui(BedWarsX plugin, Player player, Arena arena) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;

        GuiBuilder builder = GuiBuilder.criar("&8✦ &7👥 &lJogadores &8— &f" + arena.getId(), 4);
        builder.bordaPadrao();
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(27 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 3; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        builder.set(4, ItemUtil.create(Material.SKULL_ITEM, 1,
                "&7👥 &lConfigurar Jogadores",
                "&7Ajuste o número de jogadores da arena.",
                "",
                "&eClique Esq: &7+1   &eClique Dir: &7-1",
                "&eShift+Esq: &7+2   &eShift+Dir: &7-2"));

        // ── Mínimo
        builder.set(11, ItemUtil.create(Material.IRON_INGOT, arena.getMinPlayers(),
                "&fMínimo de Jogadores",
                "&7Atual: &e" + arena.getMinPlayers(),
                "",
                "&eClique para ajustar"));

        // ── Máximo
        builder.set(13, ItemUtil.create(Material.GOLD_INGOT, arena.getMaxPlayers(),
                "&fMáximo de Jogadores",
                "&7Atual: &e" + arena.getMaxPlayers(),
                "",
                "&eClique para ajustar"));

        // ── Por time
        builder.set(15, ItemUtil.create(Material.DIAMOND, arena.getMaxPlayersPerTeam(),
                "&fJogadores por Time",
                "&7Atual: &e" + arena.getMaxPlayersPerTeam(),
                "",
                "&eClique para ajustar"));

        builder.set(22, ItemUtil.create(Material.NETHER_STAR, 1,
                "&6Modo da Arena",
                "&7Atual: &e" + arena.getMode().getDisplayName(),
                "&7Capacidade efetiva: &e" + arena.getEffectiveMaxPlayers(),
                "&7Formato: &e" + arena.getEffectiveTeamLimit() + "x" + arena.getEffectiveTeamSize(),
                "",
                "&eClique para alternar"));

        builder.set(27, GuiBuilder.SETA_VOLTAR);
        this.inventory = builder.build();
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (slot == 27) {
            plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
            return;
        }
        int delta = event.isShiftClick() ? 2 : 1;
        if (event.isRightClick()) delta = -delta;

        switch (slot) {
            case 11:
                arena.setMinPlayers(Math.max(2, Math.min(arena.getMaxPlayers(), arena.getMinPlayers() + delta)));
                plugin.getArenaManager().saveArenas();
                break;
            case 13:
                arena.setMaxPlayers(Math.max(arena.getMinPlayers(), Math.min(100, arena.getMaxPlayers() + delta)));
                plugin.getArenaManager().saveArenas();
                break;
            case 15:
                arena.setMaxPlayersPerTeam(Math.max(1, Math.min(10, arena.getMaxPlayersPerTeam() + delta)));
                plugin.getArenaManager().saveArenas();
                break;
            case 22:
                cycleMode();
                plugin.getArenaManager().saveArenas();
                break;
        }
        plugin.getGuiManager().abrir(player, new AdminArenaPlayersGui(plugin, player, arena));
    }

    private void cycleMode() {
        ArenaMode[] modes = ArenaMode.values();
        int index = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == arena.getMode()) {
                index = i;
                break;
            }
        }
        arena.setMode(modes[(index + 1) % modes.length]);
    }
}
