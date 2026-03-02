# SoulBoundSMP

A Paper plugin for Minecraft 1.21 where players collect souls instead of hearts when they eliminate other players. Souls can be stored, traded, or consumed to unlock special abilities. When a player dies, they drop a physical soul item that others can pick up. If a player runs out of souls, they enter a weakened state with reduced max health until revived using a ritual system.

## Features
- **Soul Collection**: Gain 1 soul per player kill (with configurable cooldown)
- **Physical Soul Items**: Souls drop as Ender Pearl items on death
- **Special Abilities**: Use souls to activate temporary effects (Strength, Speed)
- **Weakened State**: When you have 0 souls, your max health is reduced
- **Revival Ritual**: Other players can revive you from weakened state using a soul
- **GUI Management**: `/souls` command opens a GUI to manage abilities
- **YAML Storage**: All player data stored in `plugins/SoulBoundSMP/players/`
- **Permissions**: Granular permissions for commands and features
- **Configurable**: Full configuration via config.yml

## Requirements
- Paper 1.21 or higher
- Java 21

## Installation
1. Download the latest `SoulBoundSMP.jar` from releases
2. Place the jar file in your server's `plugins` folder
3. Restart or reload your server
4. Configure `plugins/SoulBoundSMP/config.yml` to your liking

## Commands
- `/souls` - Opens the souls management GUI
- `/soulbound revive [player]` - Revive yourself or another player from weakened state (requires 1 soul from reviver)

## Permissions
- `soulbound.use` - Allows using `/souls` command (default: true)
- `soulbound.revive` - Allows reviving yourself (default: true)
- `soulbound.revive.other` - Allows reviving other players (default: op)

## Configuration
Edit `plugins/SoulBoundSMP/config.yml` to customize:
- Cooldowns (kill, ability cooldowns)
- Health values (normal and weakened)
- Ability definitions (cost, duration, effects)
- All messages
- Soul item appearance

## How It Works
1. **Killing Players**: When you kill another player, you gain 1 soul (with cooldown)
2. **Dying**: When you die, you drop a soul item that others can pick up
3. **Weakened State**: If your soul count reaches 0, your max health drops to 4 HP
4. **Revival**: Another player can revive you using `/soulbound revive <yourname>` (they lose 1 soul)
5. **Abilities**: Use souls in the GUI to activate temporary effects

## Data Storage
Player data is stored in `plugins/SoulBoundSMP/players/<UUID>.yml`:
- `souls`: Current soul count
- `lastKillTime`: Timestamp of last soul gain (for cooldown)
- `weakened`: Whether player is in weakened state
- `originalMaxHealth`: Player's max health before weakening effects

## Compilation
Use Maven to compile: