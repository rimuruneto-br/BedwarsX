package dev.bedwarsx.gui.admin;

import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.npc.BedWarsNPC;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminNpcGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Inventory inventory;

    private static final int[] NPC_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34
    };

    public AdminNpcGui(BedWarsX plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        List<BedWarsNPC> npcs = plugin.getNpcManager().getNpcs();
        GuiBuilder builder = GuiBuilder.criar("&8✦ &d👤 &lGerenciar NPCs", 5);
        builder.bordaPadrao();
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(36 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 4; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        builder.set(4, ItemUtil.create(Material.SKULL_ITEM, 1, (short)3,
                "&d👤 &lNPCs Criados &7(" + npcs.size() + ")",
                "&7NPCs são FakePlayers com skin real.",
                "&7Skin pode ser definida via mineskin.org.",
                "",
                "&eClique para criar um novo NPC."));

        // ── Botão criar
        builder.set(2, ItemUtil.create(Material.EMERALD, 1,
                "&a✚ &lCriar NPC",
                "&7Cria um NPC na sua posição.",
                "&eClique para iniciar."));

        // ── Lista de NPCs
        for (int i = 0; i < Math.min(npcs.size(), NPC_SLOTS.length); i++) {
            builder.set(NPC_SLOTS[i], buildNpcItem(npcs.get(i)));
        }

        builder.set(36, GuiBuilder.SETA_VOLTAR);
        this.inventory = builder.build();
    }

    private ItemStack buildNpcItem(BedWarsNPC npc) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("  &fID: &e" + npc.getId()));
        lore.add(ChatUtil.color("  &fNome: &e" + npc.getName()));
        lore.add(ChatUtil.color("  &fTipo: &e" + npc.getType()));
        lore.add(ChatUtil.color("  &fSkin: " + (npc.getSkinValue() != null ? "&a✔ Definida" : "&c✗ Padrão")));
        if (npc.getLinkedArena() != null) {
            lore.add(ChatUtil.color("  &fArena: &e" + npc.getLinkedArena()));
        }
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("&eClique &7para editar skin"));
        lore.add(ChatUtil.color("&cShift+Clique &7para deletar"));

        return GuiBuilder.skull(npc.getName(),
                "&f&l" + npc.getName() + " &8[&7" + npc.getId() + "&8]",
                lore.stream().map(s -> s.replace(ChatUtil.color(""), "")).toArray(String[]::new));
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (slot == 36) {
            plugin.getGuiManager().abrir(player, new AdminMainGui(plugin));
            return;
        }

        if (slot == 2) {
            // Criar NPC
            player.closeInventory();
            ChatUtil.send(player, Lang.prefix() + "&eDigite o ID do NPC (ou &ccancel&e):");
            plugin.getChatInputManager().esperar(player, id -> {
                if (id.equalsIgnoreCase("cancel")) {
                    plugin.getGuiManager().abrir(player, new AdminNpcGui(plugin, player));
                    return;
                }
                ChatUtil.send(player, Lang.prefix() + "&eDigite o nome de exibição do NPC:");
                plugin.getChatInputManager().esperar(player, nome -> {
                    ChatUtil.send(player, Lang.prefix() + "&eDigite o tipo (shop / upgrade / join / lobby):");
                    plugin.getChatInputManager().esperar(player, tipo -> {
                        BedWarsNPC npc = plugin.getNpcManager().createNPC(
                                id, nome.replace("_", " "), player.getLocation(), "", "", tipo);
                        if (npc != null) {
                            plugin.getNpcManager().getNpcs().add(npc);
                            plugin.getNpcManager().spawnNPCForAll(npc);
                            plugin.getNpcManager().saveNPC(npc);
                            ChatUtil.send(player, Lang.p("npc-criado", "id", id));
                        } else {
                            ChatUtil.send(player, "&cFalha ao criar NPC.");
                        }
                        plugin.getGuiManager().abrir(player, new AdminNpcGui(plugin, player));
                    });
                });
            });
            return;
        }

        // Clique em NPC existente
        List<BedWarsNPC> npcs = plugin.getNpcManager().getNpcs();
        for (int i = 0; i < Math.min(npcs.size(), NPC_SLOTS.length); i++) {
            if (NPC_SLOTS[i] == slot) {
                BedWarsNPC npc = npcs.get(i);
                if (event.isShiftClick()) {
                    plugin.getGuiManager().abrir(player, new ConfirmGui(plugin, player,
                            "&cDeletar NPC &e" + npc.getId() + "&c?",
                            () -> {
                                plugin.getNpcManager().deleteNPC(npc.getId());
                                ChatUtil.send(player, Lang.p("npc-deletado", "id", npc.getId()));
                                plugin.getGuiManager().abrir(player, new AdminNpcGui(plugin, player));
                            },
                            () -> plugin.getGuiManager().abrir(player, new AdminNpcGui(plugin, player))
                    ));
                } else {
                    // Editar skin
                    player.closeInventory();
                    ChatUtil.send(player, Lang.prefix() + "&eDigite o valor da skin (Base64 - mineskin.org) ou &ccancel&e:");
                    plugin.getChatInputManager().esperar(player, value -> {
                        if (value.equalsIgnoreCase("cancel")) {
                            plugin.getGuiManager().abrir(player, new AdminNpcGui(plugin, player));
                            return;
                        }
                        ChatUtil.send(player, Lang.prefix() + "&eDigite a assinatura da skin:");
                        plugin.getChatInputManager().esperar(player, sig -> {
                            npc.setSkinValue(value);
                            npc.setSkinSignature(sig);
                            plugin.getNpcManager().saveNPC(npc);
                            plugin.getNpcManager().despawnNPCForAll(npc);
                            plugin.getNpcManager().spawnNPCForAll(npc);
                            ChatUtil.send(player, Lang.p("npc-skin-atualizada", "id", npc.getId()));
                            plugin.getGuiManager().abrir(player, new AdminNpcGui(plugin, player));
                        });
                    });
                }
                return;
            }
        }
    }
}
