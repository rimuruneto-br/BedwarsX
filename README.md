# 🛏️ BedWarsX - Plugin Profissional de BedWars

> Um plugin de BedWars profissional e completo para servidores Paper 1.8.9+

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)]()
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8.9+-brightgreen.svg)]()

---

## 📋 Sobre o Projeto

**BedWarsX** é um plugin de BedWars altamente profissional e funcional desenvolvido em Java para servidores Minecraft Paper. O projeto oferece uma experiência completa de BedWars com gerenciamento de arenas, sistema de progresso, missions, lojas de compra rápida, NPCs interativos e muito mais.

### ✨ Características Principais

- 🎮 **Sistema de Jogo Completo** - Mecânicas de BedWars totalmente implementadas
- 🏛️ **Gerenciamento de Arenas** - Criar, editar e deletar arenas facilmente
- 👥 **Sistema de Equipes** - 8 equipes coloridas (Red, Blue, Green, Yellow, Aqua, White, Pink, Gray)
- 📊 **Placar (Scoreboard)** - Sistema de scoreboard atualizado em tempo real
- 💰 **Loja de Compra Rápida** - Sistema de loja experimental com itens especiais
- 🎯 **Missions (Missões)** - Sistema de missões para progressão de jogadores
- 🧬 **NPC Manager** - NPCs que spawn no lobby e na arena
- 💾 **Banco de Dados** - Suporte a SQLite e MySQL
- 🎨 **Cosméticos** - Efeitos visuais VIP e MVP
- 📈 **Sistema de Progressão** - Rastreamento de estatísticas de jogadores
- 🌍 **Múltiplos Mundos** - Gerenciamento de instâncias de mundo por partida
- 🔧 **Painel Administrativo** - GUI para gerenciar o plugin
- 💬 **Chat Input Manager** - Sistema de entrada de chat interativa

---

## 📁 Estrutura do Projeto

```
BedwarsX/
├── src/main/
│   ├── java/dev/bedwarsx/
│   │   ├── main/              # Classe principal do plugin
│   │   ├── arena/             # Gerenciamento de arenas
│   │   ├── command/           # Comandos do plugin
│   │   ├── config/            # Gerenciador de configuração
│   │   ├── database/          # Camada de banco de dados
│   │   ├── game/              # Lógica principal do jogo
│   │   ├── generator/         # Geração de conteúdo
│   │   ├── gui/               # Sistema de interfaces gráficas
│   │   ├── item/              # Gerenciamento de itens (incluindo experimental)
│   │   ├── lang/              # Internacionalização e linguagem
│   │   ├── listener/          # Listeners de eventos do Bukkit
│   │   ├── mission/           # Sistema de missões
│   │   ├── npc/               # Gerenciamento de NPCs
│   │   ├── permission/        # Sistema de permissões
│   │   ├── player/            # Sessões e dados de jogadores
│   │   ├── scoreboard/        # Sistema de placar
│   │   ├── shop/              # Sistema de lojas
│   │   ├── stats/             # Estatísticas e progressão
│   │   ├── team/              # Gerenciamento de equipes
│   │   ├── upgrade/           # Sistema de upgrades
│   │   ├── util/              # Utilidades gerais
│   │   └── world/             # Gerenciamento de mundos instânciados
│   └── resources/
│       ├── plugin.yml         # Configuração do plugin
│       ├── config.yml         # Configuração geral
│       └── messages.yml       # Mensagens personalizáveis
├── .gitignore
├── LICENSE                    # Apache License 2.0
└── README.md
```

---

## 🎯 Componentes Principais

### 1. **Arena Manager** (`arena/`)
- Carregamento e salvamento de arenas
- Gerenciamento do estado das arenas
- Suporte a múltiplas arenas simultâneas

### 2. **Game Manager** (`game/`)
- Controle da lógica principal do jogo
- Estados de jogo (lobby, starting, in-game, ending)
- Shutdown coordenado de todas as partidas

### 3. **Player Session Manager** (`player/`)
- Gerenciamento de sessões de jogadores
- Rastreamento de dados durante o jogo

### 4. **Database Manager** (`database/`)
- Suporte a SQLite e MySQL
- Persistência de estatísticas de jogadores
- Operações assincronadas

### 5. **NPC Manager** (`npc/`)
- Spawn de NPCs no lobby
- Gerenciamento e limpeza de NPCs

### 6. **GUI Manager** (`gui/`)
- Interfaces visuais do plugin
- Sistema de chat input para entrada de dados
- Painel administrativo

### 7. **Scoreboard Manager** (`scoreboard/`)
- Atualização em tempo real (a cada 10 ticks por padrão)
- Informações dinâmicas do jogo

### 8. **Shop Manager** (`shop/`)
- Quick Buy system
- Loja experimental com itens especiais
- Sistema de descontos VIP

### 9. **Mission Manager** (`mission/`)
- Sistema de missões para jogadores
- Rastreamento de progresso

### 10. **World Instance Manager** (`world/`)
- Gerenciamento de mundos instânciados por partida
- Limpeza de mundos órfãos ao desligar

---

## 🔧 Configuração

### Requisitos
- **Java 8+**
- **Paper 1.8.9+**
- **Maven** (para build)

### Arquivo de Configuração (`config.yml`)

```yaml
# Localização do lobby
lobby:
  world: "lobby"
  x: 0.5
  y: 64.0
  z: 0.5

# Banco de dados
database:
  type: SQLITE  # ou MYSQL
  host: localhost
  port: 3306

# Placar
scoreboard:
  enabled: true
  update-interval: 10  # ticks

# Configurações de jogo
game:
  min-players: 2
  max-players-per-team: 4
  respawn-time: 5
  spectator-mode: true

# Equipes (8 opções)
teams:
  RED, BLUE, GREEN, YELLOW, AQUA, WHITE, PINK, GRAY
```

---

## 📜 Permissões

### Permissões de Administrador
```
bedwarsx.admin                    # Acesso total
bedwarsx.admin.gui                # Painel administrativo
bedwarsx.admin.reload             # Recarregar plugin
bedwarsx.arena.*                  # Gerenciar arenas
bedwarsx.npc.*                    # Gerenciar NPCs
bedwarsx.stats.resetar            # Resetar estatísticas
```

### Permissões de Jogador
```
bedwarsx.jogar                    # Jogar BedWars
bedwarsx.spectator                # Assistir partidas
bedwarsx.stats.ver                # Ver próprias estatísticas
bedwarsx.vip.*                    # Benefícios VIP
bedwarsx.mvp.*                    # Benefícios MVP
```

---

## 💻 Comandos

### Comando Principal
```
/bw [subcomando]
Aliases: /bedwars, /bwx
```

### Comando Administrativo
```
/bwadm
Aliases: /bedwarsadm
Permission: bedwarsx.admin.gui
```

---

## 🔗 Event Listeners Registrados

O plugin monitora diversos eventos Bukkit:

- ✅ Player Join/Quit
- ✅ Player Death/Respawn
- ✅ Block Break/Place
- ✅ Entity Damage
- ✅ Food Level Change
- ✅ Player Interact
- ✅ Inventory Click
- ✅ Player Move
- ✅ Chat
- ✅ NPC Interaction
- ✅ Item Drop
- ✅ Projectile Launch/Hit
- ✅ Weather Change

---

## 🎨 Recursos de Jogo

### Equipes Dinâmicas
O plugin suporta 8 equipes com cores e woolores customizáveis:
- **RED** - Lã vermelha
- **BLUE** - Lã azul
- **GREEN** - Lã verde
- **YELLOW** - Lã amarela
- **AQUA** - Lã água
- **WHITE** - Lã branca
- **PINK** - Lã rosa
- **GRAY** - Lã cinza

### Sistema de Cosmética
- Trails de partículas para MVP
- Efeitos cosméticos para VIP
- Customização visual

### Loja Experimental
Itens especiais disponíveis:
- 🔥 **Rewind** (12 Emeralds) - Volta no tempo de ações
- 🔥 **Fireball Jump** (8 Gold Ingots) - Salto com bola de fogo
- 🎯 **Slingshot** (6 Iron Ingots) - Arma de longo alcance

---

## 🗂️ Estrutura de Pacotes

| Pacote | Responsabilidade |
|--------|-----------------|
| `main` | Classe principal e inicialização |
| `arena` | Gerenciamento de arenas |
| `command` | Processamento de comandos |
| `config` | Configuração e YAML |
| `database` | Persistência de dados |
| `game` | Lógica de jogo |
| `generator` | Geração procedural |
| `gui` | Interfaces de usuário |
| `item` | Itens de jogo |
| `lang` | Internacionalização |
| `listener` | Event handlers Bukkit |
| `mission` | Sistema de missões |
| `npc` | NPCs de lobby |
| `permission` | Controle de acesso |
| `player` | Dados de jogadores |
| `scoreboard` | Placar em tempo real |
| `shop` | Sistema de lojas |
| `stats` | Estatísticas e progressão |
| `team` | Gerenciamento de equipes |
| `upgrade` | Upgrades e progressão |
| `util` | Funções utilitárias |
| `world` | Instâncias de mundo |

---

## 🚀 Instalação e Build

### 1. Clone o repositório
```bash
git clone https://github.com/rimuruneto-br/BedwarsX.git
cd BedwarsX
```

### 2. Build com Maven
```bash
mvn clean package
```

### 3. Coloque o JAR gerado em plugins/
```bash
cp target/BedwarsX-*.jar /caminho/do/servidor/plugins/
```

### 4. Reinicie o servidor
```bash
# O plugin criará as configurações padrão
```

---

## ⚙️ Arquitetura

### Design Patterns Utilizados

1. **Singleton Pattern** - Classe principal BedWarsX
2. **Manager Pattern** - Gerenciadores especializados
3. **Event-Driven Architecture** - Listeners Bukkit
4. **Session Management** - Rastreamento de jogadores
5. **Repository Pattern** - Abstração de dados

### Fluxo de Inicialização

```
onEnable()
  ├─ Carrega configurações (Lang, Config)
  ├─ Inicializa Database
  ├─ Cria Managers (Arena, Game, NPC, etc)
  ├─ Registra listeners
  ├─ Inicia task de scoreboard
  ├─ Carrega arenas
  └─ Spawn NPCs no lobby
```

---

## 📊 Estatísticas do Código

- **Linguagem**: 100% Java
- **Estrutura**: Maven Multi-module Ready
- **Versão Mínima**: Java 8
- **Compatibilidade**: Minecraft 1.8.9+
- **Padrão**: Spigot/Paper Plugin

---

## 📝 Configuração de Mensagens

Arquivo `messages.yml` contém todas as mensagens customizáveis:

```yaml
no-permission: "&cYou don't have permission to do that."
arena-not-found: "&cArena &e{arena} &cnot found."
bed-broken: "&c{team}'s &cbed was destroyed by &e{player}&c!"
final-kill: "&c&lFINAL KILL! &e{killer} &7eliminated &e{player}&7!"
```

---

## 🛡️ Licença

Este projeto está licenciado sob a **Apache License 2.0** - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 👨‍💻 Desenvolvimento

### Estrutura de Branches
- `main` - Código estável e pronto para produção
- `dev` - Desenvolvimento ativo

### Como Contribuir
1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 🐛 Tratamento de Erros

O plugin implementa tratamento robusto de erros:
- Logging detalhado de operações
- Verificação de conexão com banco de dados
- Limpeza apropriada de recursos no shutdown
- Recuperação de mundos órfãos

---

## ⚡ Performance

### Otimizações Implementadas

- **Atualização de Scoreboard**: Configurável (padrão: 10 ticks)
- **Async Database**: Operações não-bloqueantes
- **Event Filtering**: Listeners especializados
- **Resource Cleanup**: Limpeza apropriada de mundos e NPCs

---

## 📞 Suporte

Para problemas ou sugestões, abra uma [Issue](https://github.com/rimuruneto-br/BedwarsX/issues) no repositório.

---

## 🎓 Aprendizado

Este projeto é excelente para aprender sobre:
- ✅ Desenvolvimento de plugins Spigot/Paper
- ✅ Arquitetura de aplicações Java grandes
- ✅ Event-driven programming
- ✅ Gerenciamento de banco de dados em Minecraft
- ✅ Design patterns profissionais
- ✅ YAML parsing e configuração

---

## 📦 Dependências Principais

- **Spigot API 1.8.9** - Framework Minecraft
- **Paper Server** - Servidor base otimizado
- **Maven** - Build automation

---

**Desenvolvido com ❤️ para a comunidade Minecraft**

*Última atualização: 2026-05-08*
