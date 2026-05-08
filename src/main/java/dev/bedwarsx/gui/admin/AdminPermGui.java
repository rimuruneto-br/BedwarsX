package dev.bedwarsx.gui.admin;

import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
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

public class AdminPermGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Inventory inventory;

    public AdminPermGui(BedWarsX plugin, Player player) {
        this.plugin = plugin;

        GuiBuilder builder = GuiBuilder.criar("&8✦ &6🔑 &lPermissões do BedWarsX", 6);
        builder.bordaPadrao();
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(45 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 5; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        builder.set(4, ItemUtil.create(Material.GOLD_INGOT, 1,
                "&6🔑 &lLista de Permissões",
                "&7Todas as permissões do BedWarsX.",
                "&7Use um plugin de permissões (LuckPerms)",
                "&7para atribuir aos grupos.",
                "",
                "&a● &7= jogador tem  &c● &7= não tem"));

        // ── Grupos de permissão
        builder.set(10, buildGrupo("&c👑 Admin",
                Material.NETHER_STAR, Perm.ADMIN_NODES, player));

        builder.set(13, buildGrupo("&e⭐ VIP",
                Material.GOLD_INGOT, Perm.VIP_NODES, player));

        builder.set(16, buildGrupo("&f● Jogador",
                Material.IRON_INGOT, Perm.JOGADOR_NODES, player));

        // ── Lista detalhada de todas as permissões
        int slot = 19;
        for (Perm perm : Perm.values()) {
            if (slot > 43) break;
            boolean temPerm = player.hasPermission(perm.getNode());
            builder.set(slot, ItemUtil.create(
                    temPerm ? Material.EMERALD : Material.REDSTONE, 1,
                    (temPerm ? "&a● " : "&c● ") + "&f" + perm.getNode(),
                    "&7" + perm.getDescricao(),
                    "",
                    temPerm ? "&aVocê tem essa permissão" : "&cVocê não tem essa permissão"));
            slot++;
            if (slot % 9 == 8) slot += 2;
        }

        builder.set(45, GuiBuilder.SETA_VOLTAR);
        this.inventory = builder.build();
    }

    private ItemStack buildGrupo(String nome, Material mat, String[] nodes, Player viewer) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color("&7Permissões do grupo:"));
        lore.add(ChatUtil.color(""));
        for (String node : nodes) {
            boolean tem = viewer.hasPermission(node);
            lore.add(ChatUtil.color("  " + (tem ? "&a●" : "&c●") + " &f" + node));
        }
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("&7Dê &e" + nodes[0] + " &7para acesso completo."));
        return ItemUtil.create(mat, 1, nome, lore.toArray(new String[0]));
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (slot == 45) {
            plugin.getGuiManager().abrir(player, new AdminMainGui(plugin));
        }
    }
}
