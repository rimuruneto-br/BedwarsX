package dev.bedwarsx.gui.admin;

import dev.bedwarsx.arena.Arena;
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

import java.util.ArrayList;
import java.util.List;

public class AdminArenaTeamsGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Arena arena;
    private final Inventory inventory;

    // Slots dos times (4 por linha, 2 linhas)
    private static final int[] TEAM_SLOTS = {11, 12, 13, 14, 15, 20, 21, 22};
    private static final BedWarsTeam[] TEAMS = BedWarsTeam.values();

    public AdminArenaTeamsGui(BedWarsX plugin, Player player, Arena arena) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;

        GuiBuilder builder = GuiBuilder.criar("&8✦ &6Times &8— &f" + arena.getId(), 5);
        builder.bordaPadrao();
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(36 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 4; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        builder.set(4, ItemUtil.create(Material.WOOL, 1, (short)14,
                "&c🏆 &lGerenciar Times",
                "&7Clique no time para ativar/desativar.",
                "&7Verde = ativo | Vermelho = inativo.",
                "",
                "&eShift + Clique &7em time ativo para",
                "&7definir spawn e cama."));

        // Preencher times
        for (int i = 0; i < Math.min(TEAMS.length, TEAM_SLOTS.length); i++) {
            builder.set(TEAM_SLOTS[i], buildTimeItem(TEAMS[i]));
        }

        builder.set(36, GuiBuilder.SETA_VOLTAR);

        this.inventory = builder.build();
    }

    private ItemStack buildTimeItem(BedWarsTeam team) {
        boolean ativo = arena.getEnabledTeams().contains(team);
        boolean temSpawn = arena.getTeamSpawns().containsKey(team);
        boolean temCama = arena.getTeamBeds().containsKey(team);

        short woolData = (short) team.getWoolData();
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("  &fStatus: " + (ativo ? "&aAtivado" : "&cDesativado")));

        if (ativo) {
            lore.add(ChatUtil.color("  &fSpawn: " + (temSpawn ? "&a✔ Definido" : "&c✗ Faltando")));
            lore.add(ChatUtil.color("  &fCama:  " + (temCama  ? "&a✔ Definido" : "&c✗ Faltando")));
        }

        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("&eClique &7para " + (ativo ? "desativar" : "ativar")));
        if (ativo) {
            lore.add(ChatUtil.color("&eShift+Clique &7para configurar spawn/cama"));
        }

        String nome = (ativo ? "&a✔ " : "&c✗ ") + team.getColoredName();
        return ItemUtil.create(Material.WOOL, 1, woolData, nome, lore);
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        // Voltar
        if (slot == 36) {
            plugin.getGuiManager().abrir(player, new AdminArenaEditGui(plugin, player, arena));
            return;
        }

        // Clique em time
        for (int i = 0; i < TEAM_SLOTS.length; i++) {
            if (TEAM_SLOTS[i] == slot && i < TEAMS.length) {
                BedWarsTeam team = TEAMS[i];
                boolean ativo = arena.getEnabledTeams().contains(team);

                if (event.isShiftClick() && ativo) {
                    // Abrir GUI de configuração desse time
                    plugin.getGuiManager().abrir(player, new AdminTeamConfigGui(plugin, player, arena, team));
                } else {
                    // Toggle ativar/desativar
                    if (ativo) {
                        arena.getEnabledTeams().remove(team);
                        arena.getTeamSpawns().remove(team);
                        arena.getTeamBeds().remove(team);
                        ChatUtil.send(player, Lang.prefix() + "&7Time " + team.getColoredName() + " &7desativado.");
                    } else {
                        arena.getEnabledTeams().add(team);
                        ChatUtil.send(player, Lang.prefix() + "&7Time " + team.getColoredName() + " &aativado!");
                    }
                    plugin.getArenaManager().saveArenas();
                    plugin.getGuiManager().abrir(player, new AdminArenaTeamsGui(plugin, player, arena));
                }
                return;
            }
        }
    }
}
