# 🐛 Relatório de Problemas e Conflitos - BedwarsX

**Data**: 08/05/2026  
**Status**: ⚠️ CRÍTICO - Requer Correção

---

## 🔴 Problemas Críticos

### 1. CONFLITO: Dois Comandos `/bw` Diferentes
**Localização**: `src/main/java/dev/bedwarsx/command/`

**Classes Afetadas**:
- `BwCommand.java` (Comando do jogador - linhas 46-89)
- `BedWarsCommand.java` (Comando do admin - linhas 31-43)

**Problema**:
```java
// BwCommand.java - Jogador
case "jogar": case "play": cmdJogar(player, args); break;

// BedWarsCommand.java - Admin  
case "arena": handleArena(sender, args); break;
case "npc": handleNPC(sender, args); break;
```

Ambas implementam `/bw` mas com propósitos diferentes!

**Solução Recomendada**:
- Verificar em `plugin.yml` qual comando está registrado
- `BwCommand` → `/bw` (Player Commands)
- `BedWarsCommand` → `/bwadm` ou separar completamente

**Impacto**: ALTO - Conflito direto de comandos

---

### 2. ClassCastException Potencial
**Localização**: `BedWarsCommand.java:41`

**Código Problemático**:
```java
ChatUtil.send((Player) sender, "&cUnknown subcommand. Use &e/bw help&c.");
```

**Problema**: 
`sender` pode ser Console, causa `ClassCastException`

**Solução**:
```java
if (!(sender instanceof Player)) {
    sender.sendMessage(ChatUtil.color("&cEste comando é apenas para jogadores!"));
    return true;
}
Player player = (Player) sender;
ChatUtil.send(player, "&cSubcomando desconhecido. Use &e/bw ajuda&c.");
```

**Impacto**: MÉDIO - Crash se executado via console

---

### 3. NullPointerException em getStats()
**Localização**: `BedWarsCommand.java:311-322`

**Código Problemático**:
```java
dev.bedwarsx.database.DatabaseManager.PlayerStats stats =
        plugin.getDatabaseManager().getStats(target.getUniqueId());

sender.sendMessage(ChatUtil.color("&6&l━━━ Stats: " + target.getName() + " ━━━"));
sender.sendMessage(ChatUtil.color("  &fWins: &a" + stats.getWins()));
// ... mais acessos a stats
```

**Problema**: 
`stats` pode ser null se o jogador não tem dados

**Solução**:
```java
dev.bedwarsx.database.DatabaseManager.PlayerStats stats =
        plugin.getDatabaseManager().getStats(target.getUniqueId());

if (stats == null) {
    sender.sendMessage(ChatUtil.color("&cNenhuma estatística encontrada para " + target.getName()));
    return;
}

sender.sendMessage(ChatUtil.color("&6&l━━━ Stats: " + target.getName() + " ━━━"));
sender.sendMessage(ChatUtil.color("  &fWins: &a" + stats.getWins()));
```

**Impacto**: ALTO - Crash ao visualizar stats

---

## 🟡 Problemas Secundários

### 4. Possível Null em LeaderboardEntry
**Localização**: `BwCommand.java:215-217`

**Código**:
```java
for (ProgressionManager.LeaderboardEntry entry : top) {
    ChatUtil.send(player, "&e#" + pos++ + " &f" + entry.name + " &7- &a" + entry.value);
}
```

**Problema**: 
`entry.name` ou `entry.value` podem ser null

**Solução**:
```java
for (ProgressionManager.LeaderboardEntry entry : top) {
    if (entry == null || entry.name == null) continue;
    ChatUtil.send(player, "&e#" + pos++ + " &f" + entry.name + " &7- &a" + entry.value);
}
```

**Impacto**: MÉDIO - NPE ocasional

---

### 5. Falta Verificação em getNPCById()
**Localização**: `BedWarsCommand.java:243, 284, 263`

**Problema**: 
Método retorna null sem verificação em alguns casos

**Solução**: Sempre verificar antes de usar
```java
BedWarsNPC npc = plugin.getNpcManager().getNPCById(args[2]);
if (npc == null) {
    sender.sendMessage(ChatUtil.color("&cNPC não encontrado: " + args[2]));
    return;
}
```

**Impacto**: BAIXO - Erros de lógica

---

## 📋 Checklist de Correção

- [ ] Resolver conflito de comandos `/bw`
- [ ] Corrigir ClassCastException em BedWarsCommand
- [ ] Adicionar validação de null em getStats()
- [ ] Validar LeaderboardEntry
- [ ] Adicionar verificações em getNPCById()
- [ ] Testar todos os comandos após correção
- [ ] Verificar permissões após consolidação

---

## 🔧 Testes Recomendados

1. **Executar `/bw` como console** → Deve retornar mensagem, não crash
2. **Executar `/bw stats [novo-player]** → Deve retornar mensagem apropriada
3. **Executar `/bw top` → Sem entrada no leaderboard → Verificar null
4. **Executar `/bwadm npc list** com NPC não registrado

---

**Prioridade**: 🔴 CRÍTICA - Requer correção imediata para produção

