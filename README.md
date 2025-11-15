# ClearScoreboards

[![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.21+-green.svg)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.java.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)

A powerful and flexible Spigot/Bukkit library for managing scoreboards with multi-version support (1.8-1.21+).

> **Note:** This project is a fork of [JScoreboards](https://github.com/JordanOsterberg/JScoreboards) by JordanOsterberg. All credit for the original implementation goes to the original author.

## Features

- **Multi-Version Support** - Works seamlessly across Minecraft 1.8 through 1.21+
- **No NMS** - Uses only Bukkit/Spigot API for maximum stability
- **Multiple Implementation Types** - Choose between global or per-player scoreboards
- **Dynamic Content** - Support for functional suppliers and lambda expressions
- **Team Management** - Built-in team support with colors and prefixes
- **Health Display** - Configurable health display in tab list and below names
- **Abstraction Layer** - Clean architecture supporting multiple versions without breaking API compatibility

## Installation

### Maven

Add the repository and dependency to your `pom.xml`:

```xml
<repository>
    <id>skycun-repo</id>
    <url>https://repo.skycun.dev/releases</url>
</repository>

<dependency>
    <groupId>dev.skycun</groupId>
    <artifactId>ClearScoreboards</artifactId>
    <version>2.1.5-RELEASE</version>
</dependency>
```

### Build Configuration

Make sure to specify your Java version and shade the library into your plugin:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>8</source>
                <target>8</target>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.4</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Quick Start

### Global Scoreboard (Same for all players)

```java
// Simple method-based approach
ClearGlobalMethodBasedScoreboard scoreboard = new ClearGlobalMethodBasedScoreboard();
scoreboard.setTitle("&6&lMY SERVER");
scoreboard.setLines(Arrays.asList(
    "&7Players: &a" + Bukkit.getOnlinePlayers().size(),
    "&7Rank: &bVIP",
    "",
    "&ewww.example.com"
));
scoreboard.addPlayer(player);
scoreboard.updateScoreboard();

// Dynamic supplier-based approach
ClearGlobalScoreboard dynamicBoard = new ClearGlobalScoreboard(
    () -> "&6&lMY SERVER", // Title supplier
    () -> Arrays.asList(  // Lines supplier
        "&7Time: &f" + System.currentTimeMillis(),
        "&7Players: &a" + Bukkit.getOnlinePlayers().size(),
        "",
        "&ewww.example.com"
    )
);
dynamicBoard.addPlayer(player);

// Call updateScoreboard() periodically to refresh dynamic content
Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    dynamicBoard.updateScoreboard();
}, 0L, 20L); // Update every second
```

### Per-Player Scoreboard (Personalized for each player)

```java
// Method-based approach
ClearPerPlayerMethodBasedScoreboard scoreboard = new ClearPerPlayerMethodBasedScoreboard();
scoreboard.addPlayer(player);
scoreboard.setTitle(player, "&6Hello &e" + player.getName());
scoreboard.setLines(player, Arrays.asList(
    "&7Kills: &a" + getKills(player),
    "&7Deaths: &c" + getDeaths(player),
    "&7K/D: &b" + getKD(player),
    "",
    "&ewww.example.com"
));

// Function-based approach for dynamic content
ClearPerPlayerScoreboard dynamicBoard = new ClearPerPlayerScoreboard(
    player -> "&6Hello &e" + player.getName(), // Title function
    player -> Arrays.asList(                    // Lines function
        "&7Kills: &a" + getKills(player),
        "&7Deaths: &c" + getDeaths(player),
        "&7K/D: &b" + getKD(player),
        "",
        "&ewww.example.com"
    )
);
dynamicBoard.addPlayer(player);
```

### Team Management

```java
// Create a team with prefix and color
ClearScoreboardTeam adminTeam = scoreboard.createTeam(
    "admin",              // Team name (max 16 characters)
    "&c[ADMIN] ",        // Prefix
    ChatColor.RED        // Team color
);

// Add players to the team
adminTeam.addPlayer(player);

// Update team properties
adminTeam.setDisplayName("&4[OWNER] ");
adminTeam.refresh();

// Remove a team
scoreboard.removeTeam(adminTeam);
```

### Health Display Options

```java
ClearScoreboardOptions options = new ClearScoreboardOptions(
    ClearScoreboardTabHealthStyle.HEARTS, // Show hearts in tab list
    true                                   // Show health below name
);

scoreboard.setOptions(options);
scoreboard.updateScoreboard();
```

### Cleanup

Always destroy scoreboards when disabling your plugin:

```java
@Override
public void onDisable() {
    scoreboard.destroy(); // Resets all players to the main scoreboard
}
```

## API Documentation

### Scoreboard Types

| Type | Description | Use Case |
|------|-------------|----------|
| `ClearGlobalScoreboard` | Supplier-based global scoreboard | Dynamic content same for all players |
| `ClearGlobalMethodBasedScoreboard` | Method-based global scoreboard | Simple static content for all players |
| `ClearPerPlayerScoreboard` | Function-based per-player scoreboard | Dynamic personalized content |
| `ClearPerPlayerMethodBasedScoreboard` | Method-based per-player scoreboard | Simple personalized content |

### Key Methods

#### Common Methods (All Types)

- `addPlayer(Player player)` - Add a player to the scoreboard
- `removePlayer(Player player)` - Remove a player from the scoreboard
- `updateScoreboard()` - Refresh the scoreboard content
- `destroy()` - Clean up and reset all players
- `createTeam(String name, String displayName, ChatColor color)` - Create a new team
- `removeTeam(ClearScoreboardTeam team)` - Remove a team
- `setOptions(ClearScoreboardOptions options)` - Set display options

#### Method-Based Specific

- `setTitle(String title)` / `setTitle(Player player, String title)` - Set the scoreboard title
- `setLines(List<String> lines)` / `setLines(Player player, List<String> lines)` - Set the scoreboard lines

## Project Structure

ClearScoreboards uses a modular architecture to support multiple Minecraft versions:

```
ClearScoreboards/
├── api/                    # User-facing API (stable across versions)
├── abstraction/            # Version abstraction layer
├── 1_8-1_12/              # Minecraft 1.8-1.12 implementation
├── 1_13/                  # Minecraft 1.13 implementation
├── 1_14-1_21/             # Minecraft 1.14-1.21 implementation
└── team-support-1_12/     # Minecraft 1.12 team API support
```

### How It Works

1. **API Module** - Provides the stable public API that developers use
2. **Abstraction Module** - Defines interfaces for version-specific implementations
3. **Version Modules** - Implement the abstraction for each Minecraft version range
4. **Runtime Detection** - `SpigotAPIVersion` automatically detects server version and loads appropriate implementation

This architecture allows the same API to work across all supported Minecraft versions without breaking changes.

## Supported Versions

### Tested Versions
- ✅ Minecraft 1.8
- ✅ Minecraft 1.16
- ✅ Minecraft 1.17
- ✅ Minecraft 1.18
- ✅ Minecraft 1.19
- ✅ Minecraft 1.20
- ✅ Minecraft 1.21

### Version-Specific Features

| Feature | 1.8-1.11 | 1.12 | 1.13+ |
|---------|----------|------|-------|
| Max Line Length | 32 chars | 32 chars | 128 chars |
| Team Colors | ❌ | ✅ | ✅ |
| Modern Objectives API | ❌ | ❌ | ✅ |

## Examples

Check out the [examples directory](examples/) for more detailed usage examples including:
- Basic scoreboard setup
- Dynamic updating scoreboards
- Per-player stats tracking
- Team-based prefixes
- Health display configurations

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Issues and Support

If you find any bugs or have questions:
- Open an issue on [GitHub Issues](https://github.com/Skycun/ClearScoreboards/issues)
- Check the [Wiki](https://github.com/Skycun/ClearScoreboards/wiki) for detailed documentation

## Credits

- **Original Author**: [JordanOsterberg](https://github.com/JordanOsterberg) - Creator of [JScoreboards](https://github.com/JordanOsterberg/JScoreboards)
- **Fork Maintainer**: [Skycun](https://github.com/Skycun)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

---

Made with ❤️ by Skycun | Forked from [JScoreboards](https://github.com/JordanOsterberg/JScoreboards)
