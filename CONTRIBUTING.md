# Contributing to Immersive Campfire

First off, thank you for considering contributing to Immersive Campfire! 🎉

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
- [Style Guidelines](#style-guidelines)
- [Commit Messages](#commit-messages)

## Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior by opening an issue.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates.

When creating a bug report, include:

- **A clear title** describing the issue
- **Minecraft version** and **mod version**
- **Fabric Loader version** and **Fabric API version**
- **Other mods installed** (to identify conflicts)
- **Steps to reproduce** the behavior
- **Expected behavior** vs **actual behavior**
- **Screenshots or videos** if applicable
- **Crash logs** if the game crashes (found in `.minecraft/crash-reports/` or `.minecraft/logs/latest.log`)

### Suggesting Features

Feature suggestions are welcome! When suggesting a feature:

- **Check existing issues** to see if it's already been suggested
- **Describe the feature** clearly and concisely
- **Explain the use case** - why would this feature be useful?
- **Consider the scope** - does it fit the mod's vision of immersive campfire creation?

### Pull Requests

1. **Fork** the repository
2. **Create a branch** from `main` for your feature/fix
3. **Make your changes** following the style guidelines
4. **Test thoroughly** in-game
5. **Submit a pull request** with a clear description

## Development Setup

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Git
- An IDE (IntelliJ IDEA recommended for Minecraft modding)

### Setting Up the Project

```bash
# Clone your fork
git clone https://github.com/YOUR-USERNAME/Immersive-Campfire.git
cd Immersive-Campfire

# Generate IDE files (IntelliJ IDEA)
./gradlew genIdeaRuns

# Or for Eclipse
./gradlew genEclipseRuns

# Build the project
./gradlew build

# Run the Minecraft client with your mod
./gradlew runClient
```

### Project Structure

```
src/
├── main/
│   ├── java/com/example/simpleblocks/
│   │   ├── block/          # Block classes
│   │   ├── item/           # Item classes
│   │   ├── ModBlocks.java  # Block registry
│   │   ├── ModItems.java   # Item registry
│   │   └── SimpleBlocksMod.java  # Main mod class
│   └── resources/
│       ├── assets/         # Textures, models, lang files
│       ├── data/           # Recipes, loot tables
│       └── fabric.mod.json # Mod metadata
└── client/
    └── java/               # Client-side code
```

## Style Guidelines

### Java Code Style

- Use **4 spaces** for indentation (no tabs)
- Follow standard **Java naming conventions**:
  - `PascalCase` for classes
  - `camelCase` for methods and variables
  - `SCREAMING_SNAKE_CASE` for constants
- Keep lines under **120 characters** when possible
- Add **Javadoc comments** for public methods
- Use **meaningful variable names**

### JSON Files

- Use **2 or 4 spaces** for indentation
- Keep consistent formatting with existing files

### Resources

- **Textures:** 16x16 pixels, PNG format
- **Models:** Follow Minecraft's JSON model format
- **Lang files:** Use lowercase keys with underscores

## Commit Messages

Follow these conventions for commit messages:

- Use the **present tense** ("Add feature" not "Added feature")
- Use the **imperative mood** ("Move cursor to..." not "Moves cursor to...")
- Keep the first line under **50 characters**
- Reference issues when applicable (`Fixes #123`)

### Examples

```
Add soul campfire support

Implement chopping animation for stump block

Fix firewood stack not saving properly on world reload

Update fabric.mod.json dependencies
```

### Commit Types

Consider prefixing commits with a type:

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

## Questions?

If you have questions, feel free to:

- Open a [Discussion](https://github.com/sahil-tgs/Immersive-Campfire/discussions)
- Ask in an issue

---

Thank you for contributing! 🔥
