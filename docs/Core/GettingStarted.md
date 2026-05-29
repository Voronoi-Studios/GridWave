---
title: "Getting Started"
published: true
draft: true
---

# Getting Started

## Using the AssetEditor

All nodes are available in the AssetEditor as well, thus the bellow steps can be replicated 1:1.

> [!TIP] 
> This is the easiest way to quickly edit something already deployed to a server.



## Using the NodeEditor

> [!IMPORTANT]
> This requires the [NodedEditor modification](core#adding-node-editor-support)

---

### 1. Create an Instance

Use `/worldgen 2 create` to set up a WorldGen V2 asset-pack and starter biome for editing, or fallow the guides on HytaleModding.com on how to create a new world/instance:
[How to edit and create Biomes](https://hytalemodding.dev/en/docs/official-documentation/worldgen/worldgen-tutorial/README)

---

### 2. Biome

This shows a minimal setup for a biome that imports a prop. In the same way a PropDistribution could also be imported.
Depending on the complexity more or less imports make sense. Everything *can* be done at this level without any imports.


<img height="200" alt="biome" src="https://github.com/user-attachments/assets/3c14f433-4381-4460-9696-8ec0982b6a44" />

---

## Algo Node

Shown is an example setup of a [Gridwave Algorithm Prop](algo#gridwave-algorithm-prop)
featuring directly setup Fixed Props, imported TileSet Group for the Base tiles and individually imported TileSets for the FancyTiles.

> [!TIP]
> Importing TileSetGroups allows for very easy setup of multi floored generations, see the [Multi Floor Maze]() as an example

<img height="200" alt="prop" src="https://github.com/user-attachments/assets/fcd2ef63-d145-4291-8e6d-60f2d0cbce16" />

## Tile

<img height="200" alt="Tile" src="https://github.com/user-attachments/assets/8d071295-ca07-4b32-a2d7-2743b2e76e8c" />