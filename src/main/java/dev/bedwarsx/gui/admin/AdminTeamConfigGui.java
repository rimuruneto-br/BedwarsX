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

public class AdminTeamConfigGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Arena arena;
    private final BedWarsTeam team;
    private final Inventory inventory;

    public AdminTeamConfigGui(BedWarsX plugin, Player player, Arena arena, BedWarsTeam team) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;
        this.team = team;

        GuiBuilder builder = GuiBuilder.criar(
                "&8✦ " + team.getColoredName() + " &8— &fConfigurar Time", 4);
        builder.bordaPadrao();
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(27 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 3; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        // ── Ícone do time
        builder.set(4, ItemUtil.create(Material.WOOL, 1, (short) team.getWoolData(),
                team.getColoredName() + " &8— &fConfiguração",
                "&7Configure spawn, cama, gerador e lojas.",
                "&7Fique na posição desejada e clique."));

        // ── Definir spawn
        boolean temSpawn = arena.getTeamSpawns().containsKey(team);
        builder.set(11, ItemUtil.create(Material.BED, 1,
                team.getColor() + "⬛ &lSpawn do Time",
                temSpawn ? "&a✔ Spawn definido" : "&c✗ Spawn não definido",
                "",
                "&eFique no local do spawn e clique",
                "&7para definir."));

        boolean temGerador = arena.getTeamGenerators().containsKey(team);
        builder.set(12, ItemUtil.create(Material.FURNACE, 1,
                team.getColor() + "⬛ &lGerador do Time",
                temGerador ? "&a✔ Gerador definido" : "&c✗ Gerador não definido",
                "",
                "&eClique para usar sua posição"));

        boolean temLoja = arena.getTeamShopLocations().containsKey(team);
        builder.set(13, ItemUtil.create(Material.EMERALD, 1,
                team.getColor() + "⬛ &lLoja de Itens",
                temLoja ? "&a✔ Loja definida" : "&c✗ Loja não definida",
                "",
                "&eClique para usar sua posição"));

        boolean temUpgrade = arena.getTeamUpgradeShopLocations().containsKey(team);
        builder.set(14, ItemUtil.create(Material.DIAMOND, 1,
                team.getColor() + "⬛ &lLoja de Upgrades",
                temUpgrade ? "&a✔ Upgrade shop definida" : "&c✗ Upgrade shop não definida",
                "",
                "&eClique para usar sua posição"));

        // ── Definir cama
        boolean temCama = arena.getTeamBeds().containsKey(team);
        builder.set(15, ItemUtil.create(Material.WOOL, 1, (short) team.getWoolData(),
                team.getColor() + "⬛ &lCama do Time",
                temCama ? "&a✔ Cama definida" : "&c✗ Cama não definida",
                "",
                "&eFique ao lado da cama e clique",
                "&7para definir a localização."));

        boolean temCentro = arena.getTeamCenters().containsKey(team);
        builder.set(20, ItemUtil.create(Material.BEACON, 1,
                team.getColor() + "⬛ &lCentro da Ilha",
                temCentro ? "&a✔ Centro definido" : "&c✗ Centro não definido",
                "&7Raio: &e" + arena.getTeamIslandRadius(team),
                "",
                "&eClique para usar sua posição"));

        // ── Status geral
        builder.set(22, ItemUtil.create(
                (temSpawn && temCama) ? Material.EMERALD : Material.REDSTONE, 1,
                (temSpawn && temCama) ? "&a✔ &lTime Completo" : "&c✗ &lTime Incompleto",
                "  &fSpawn: " + (temSpawn ? "&a✔" : "&c✗"),
                "  &fCama:  " + (temCama  ? "&a✔" : "&c✗"),
                "  &fGerador: " + (temGerador ? "&a✔" : "&c✗"),
                "  &fLoja: " + (temLoja ? "&a✔" : "&c✗"),
                "  &fUpgrade: " + (temUpgrade ? "&a✔" : "&c✗")));

        builder.set(27, GuiBuilder.SETA_VOLTAR);

        this.inventory = builder.build();
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        switch (slot) {
            case 27:
                plugin.getGuiManager().abrir(player, new AdminArenaTeamsGui(plugin, player, arena));
                break;
            case 11: // Spawn
                plugin.getArenaManager().setTeamSpawn(arena.getId(), team, player.getLocation());
                ChatUtil.send(player, Lang.p("adm-spawn-set", "time", team.getDisplayName()));
                plugin.getGuiManager().abrir(player, new AdminTeamConfigGui(plugin, player, arena, team));
                break;
            case 12:
                plugin.getArenaManager().setTeamGenerator(arena.getId(), team, player.getLocation());
                ChatUtil.send(player, Lang.prefix() + "&aGerador definido para " + team.getColoredName());
                plugin.getGuiManager().abrir(player, new AdminTeamConfigGui(plugin, player, arena, team));
                break;
            case 13:
                plugin.getArenaManager().setTeamShop(arena.getId(), team, player.getLocation());
                ChatUtil.send(player, Lang.prefix() + "&aLoja de itens definida para " + team.getColoredName());
                plugin.getGuiManager().abrir(player, new AdminTeamConfigGui(plugin, player, arena, team));
                break;
            case 14:
                plugin.getArenaManager().setTeamUpgradeShop(arena.getId(), team, player.getLocation());
                ChatUtil.send(player, Lang.prefix() + "&aLoja de upgrades definida para " + team.getColoredName());
                plugin.getGuiManager().abrir(player, new AdminTeamConfigGui(plugin, player, arena, team));
                break;
            case 15: // Cama
                plugin.getArenaManager().setTeamBed(arena.getId(), team, player.getLocation());
                ChatUtil.send(player, Lang.p("adm-cama-set", "time", team.getDisplayName()));
                plugin.getGuiManager().abrir(player, new AdminTeamConfigGui(plugin, player, arena, team));
                break;
            case 20:
                arena.getTeamCenters().put(team, player.getLocation());
                plugin.getArenaManager().saveArenas();
                ChatUtil.send(player, Lang.prefix() + "&aCentro da ilha definido para " + team.getColoredName());
                plugin.getGuiManager().abrir(player, new AdminTeamConfigGui(plugin, player, arena, team));
                break;
        }
    }
}
