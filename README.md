# Aether Client — Minecraft Java 1.21.11

A source project for a Fabric client GUI prototype with a dark glass look, soft blue-violet accents, compact module cards, search, category sidebar, and Right Shift toggle.

## Opening animation behavior
The intro animation runs the first time the GUI is opened after entering a world/server session. Reopening the GUI in that same session skips the intro. Leaving the world and entering another resets it.

## Build
Requires JDK 21 and internet access for Gradle dependencies.

```bash
gradle build --no-daemon
```

The output JAR is created in `build/libs/`.

## GitHub Actions cloud build
1. Create a GitHub repository.
2. Upload the contents of this folder to the repository's `main` branch.
3. Open **Actions** and run **Build Aether Client**, or push a commit.
4. Open the completed workflow run and download the `aether-client-1.21.11` artifact.

## Controls
- Right Shift: open/close GUI
- Escape: close GUI
- Click module cards: toggle the prototype setting
- Type while the GUI is open: filter module names
- Right-click the top bar and drag: move the window

This is a GUI foundation, not a complete implementation of every gameplay module listed in the concept. The visible modules are interface prototypes and do not automate gameplay.
