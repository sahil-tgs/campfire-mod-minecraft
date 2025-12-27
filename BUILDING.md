# Build Guide

This guide explains how to build the Campfire Overhaul mod from source.

## Prerequisites

- **Java 17** or higher (required for Minecraft 1.20.4)
- **Git** (optional, for cloning)
- Internet connection (to download dependencies)

## Quick Build

```bash
# Clone the repository
git clone https://github.com/sahil-tgs/campfire-mod-minecraft.git
cd campfire-mod-minecraft

# Build the mod
./gradlew build

# Find your JAR
# Windows: build\libs\campfire_overhaul-1.0.0.jar
# Linux/Mac: build/libs/campfire_overhaul-1.0.0.jar
```

## Windows (PowerShell)

```powershell
# Navigate to project
cd "path\to\campfire-mod-minecraft"

# Build
.\gradlew.bat build

# Output JAR location
build\libs\campfire_overhaul-1.0.0.jar
```

## Linux / macOS

```bash
# Navigate to project
cd path/to/campfire-mod-minecraft

# Make gradlew executable (first time only)
chmod +x gradlew

# Build
./gradlew build

# Output JAR location
build/libs/campfire_overhaul-1.0.0.jar
```

## Gradle Tasks

| Command | Description |
|---------|-------------|
| `./gradlew build` | Build the mod JAR |
| `./gradlew runClient` | Run Minecraft with mod loaded (dev mode) |
| `./gradlew genSources` | Generate decompiled Minecraft sources (for IDE) |
| `./gradlew clean` | Clean build files |

## IDE Setup

### IntelliJ IDEA
1. Open the project folder
2. Import as Gradle project
3. Run `./gradlew genSources`
4. Refresh Gradle project

### VS Code / Cursor
1. Open the project folder
2. Install Java Extension Pack
3. Run `./gradlew genSources` in terminal

## Troubleshooting

### "Java version too old"
Install Java 17 or newer from [Adoptium](https://adoptium.net/)

### "Could not resolve dependencies"
Check internet connection and try:
```bash
./gradlew build --refresh-dependencies
```

### Build takes too long
First build downloads ~500MB of dependencies. Subsequent builds are faster.

## Output Files

After successful build:
```
build/
└── libs/
    ├── campfire_overhaul-1.0.0.jar          # Main mod file
    └── campfire_overhaul-1.0.0-sources.jar  # Source code
```

Copy `campfire_overhaul-1.0.0.jar` to your `.minecraft/mods` folder.


