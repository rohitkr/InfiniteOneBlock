# 🌍 Infinite OneBlock (Minecraft 26.2 / Fabric)

An advanced, high-performance **OneBlock Skyblock** mod built for modern Minecraft architecture using the Fabric API. This mod generates a completely isolated void dimension for every player where a single dynamic block evolves, progresses through stages, and drops unique tier-based rewards as it is mined.

---

## ✨ Features

- **True Void Generation:** Custom dimension built from the ground up to prevent vanilla world-leakage.
- **Flawless Physics Protection:** Custom anti-clip mechanics preventing players from slipping into the void during server lag spikes or gravity block (Sand/Gravel) updates.
- **Smart Item Capture:** Instantly captures broken block loot straight into the player's inventory to completely eliminate losing drops into the abyss.
- **Multiplayer Ready:** Dynamic `IslandManager` tracks individual player dimensions, protection boundaries, and respawn matrices cleanly.
- **Progression Phases:** Configurable block breaking tiers—mine through early logs up into underground mineral stages.

---

## 💻 Installation & Usage Guide

Follow these simple steps to install and play the mod on either **Windows** or **macOS**.

### Prerequisites
Make sure you have the official Minecraft Launcher installed on your machine.

### Step 1: Install the Fabric Loader
1. Go to the [Official Fabric Download Page](https://fabricmc.net).
2. Click **Download Universal Installer** (recommended for both Windows and Mac).
3. Open the downloaded `.jar` file to launch the installer interface.
4. Select the **Client** tab, choose **Minecraft Version 26.2**, and click **Install**.

### Step 2: Install the Mod and Dependencies
You must place both this mod and its required dependency file into your system's game directory:

1. **Download the Fabric API** matching version `26.2` from [Modrinth](https://modrinth.com) or [CurseForge](https://curseforge.com).
2. Place both the **Fabric API jar** and your compiled **InfiniteOneBlock jar** into your system's `mods` folder:
    - 🪟 **Windows:** Press `Win + R`, type `%appdata%\.minecraft\mods` and hit Enter.
    - 🍎 **macOS:** Open Finder, press `Cmd + Shift + G`, type `~/Library/Application Support/minecraft/mods` and hit Enter.
      *(Note: If the `mods` folder doesn't exist yet, simply create a new folder named `mods` in lowercase).*

### Step 3: Launch and Play!
1. Open the official **Minecraft Launcher**.
2. Click the installations dropdown menu (usually near the bottom-left corner).
3. Select the newly created **Fabric Loader 26.2** profile.
4. Hit **Play** and enjoy your custom void block adventure!

---

## 🛠️ Developer Setup (Workspace Compiling)

If you want to clone this repository and modify the source code:
1. Clone the repository: `git clone https://github.com`
2. Open the directory in **IntelliJ IDEA** or **VS Code**.
3. Re-index and build the developer environments:
   ```bash
   ./gradlew genSources
   ```
4. Run a local test client:
   ```bash
   ./gradlew runClient
   ```

---

## 🪙 Support & Monetization

If you love this mod, you can support its active development or download the official compiled releases via these channels:

- **Download on CurseForge:** [Insert CurseForge URL]
- **Download on Modrinth:** [Insert Modrinth URL]
- **Support me on Patreon:** [Insert Patreon/Ko-Fi URL]

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details. You are free to view, modify, and learn from this codebase!