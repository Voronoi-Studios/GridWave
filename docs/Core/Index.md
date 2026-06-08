---
title: "Core"
icon: BrainCircuit
order: 1
published: true
draft: true
---

<img width="1207" height="130" alt="core" src="https://github.com/user-attachments/assets/d54d7c5f-02e9-43f2-8b3c-d23c92174414" />


GridWave is a Hytale plugin that integrates Wave Function Collapse (WFC) into the World Generation V2 Node Editor. It enables structured, constraint-based procedural generation for dungeons, mazes, cities, towns, castles, structures, and many more.

Check out the [GridWaveExamples](examples) to get an idea an idea how different things can be accieved.

# Features

* WFC-based generation for deterministic but varied worlds
* Seamless integration with Worldgen V2
* Support for custom tilesets and constraints
* Scalable generation across large regions
* Modular design for easy extension


# How It Works

GridWave applies Wave Function Collapse by:

1. Reads all positions delivered by a ListPosition Node
2. Places any POI (FixedTiles) on the grid if position exists
3. Uses BaseTiles to fill the grid using WFC
   1. Backtracks if it encounters impossible situations
   2. Starts a new attempt if to many backtracks where reached, _Configurable, to give possibility to increase success chance_
4. Uses pattern matching to try and replace base tiles with FancyTiles to add variety
5. Assembles a PropDistribution or Prop


# Installation

## Default:
1. Download the latest release
2. Place the plugin in your global or world's `Mods` folder

---

## Adding Node Editor support:

### Automatic

Run `/GridWave.core patch` in any world where the mod is loaded. Only works in local worlds not on servers.

### Manual:

Copy contents of [HytaleGenerator Java](https://github.com/Voronoi-Studios/GridWave/tree/main/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java) to your corresponding folder in `%Appdata%/Hytale/install\release\package\game\latest\Client\NodeEditor\Workspaces\HytaleGenerator Java`

> [!CAUTION]
> For ease of use, the included _Workspace.json file is intended as is a replacement for the original, if you have modified this file, you need to manually copy the sections that are different.

> [!WARNING]
> Because we overwrite the _Workspace.json, Hytale will think your installation is corrupt. Just restore the _Workspace_orig.json copy to get rid of that error or uninstall the previous version before updating. You will need to patch the Node Editor again afterwards.

---

## Features

### New Root Spaces

For convenience some new root spaces where added:

> [!NOTE]
> Outdated picture from v0.7!

<img width="440" height="492" alt="new root spaces" src="https://github.com/user-attachments/assets/61200491-8557-4235-91d3-26207dee1eea" />

---

## Usage

Check out [Getting Started](getting-started) and the [GridWave.examples](examples) to get an idea how different things can be achieved.

---

## Roadmap

### Beeing worked on:

* Neighborhood system
* bool or distance for self connection
* New Attempt Behavior
* Elevation


### Far future Wishlist

* MultiTile support for baseTileSets
* 3D functionallity


## CheatSheet

<img style="border-radius:10px" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/NodeDefinitionExplainer.png" alt="NodeDefinitionExplainer" />
