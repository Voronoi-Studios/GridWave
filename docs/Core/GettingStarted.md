---
title: "Getting Started"
published: true
draft: true
---

# Getting Started

## Using the AssetEditor

All nodes are also available in the AssetEditor, thus the bellow steps can be fallowed there 1:1.

> [!TIP] 
> This is the easiest way to quickly edit something already deployed to a server if you dont have file access.



## Using the NodeEditor

> [!IMPORTANT]
> This requires the [NodedEditor modification](core#adding-node-editor-support)

---

### 0. Create an Instance

Use `/worldgen2 create` to set up a WorldGen V2 asset-pack and starter biome for editing, or fallow the guides on HytaleModding.com on how to create a new world/instance:
[How to edit and create Biomes](https://hytalemodding.dev/en/docs/official-documentation/worldgen/worldgen-tutorial/README)

---

### 1. Biome

Navigate to the biome file you want to add GridWave to. You can also use the [Examples](examples) jar (change ending to zip and unpack) as a starting point.
To Add GridWave you now add a [GridWaveAlgorithm](algo) Node. There are two variatns that can be usefull in different circumstances. The [Prop](algo#gridwave-algorithm-prop) can be handy as it allows for the use of a locator prop or other similar post processors. Its generally recogmended to use this one to start. In the same way a [PropDistribution](algo#gridwave-algorithm-propdistribution) could be used, which is more performant and allows for infinite grids. Reat its decumentation carefully to make sure you understand its strengs and weaknesses. In general the rule of thumb is: if you need sparce occurances use a [Prop](algo#gridwave-algorithm-prop), if you need a singular large generation use a [PropDistribution](algo#gridwave-algorithm-propdistribution).

Depending on the complexity of your graph, you can also choose to use imports like shown bellow.

> [!NOTE]
> Outdated picture from v0.7!

<img height="200" alt="biome" src="https://github.com/user-attachments/assets/3c14f433-4381-4460-9696-8ec0982b6a44" />

---

## Algo Node

Shown is an example setup of a [Gridwave Algorithm Prop](algo#gridwave-algorithm-prop)
featuring directly setup Fixed Props, an imported TileSet Group for the Base tiles and individually imported TileSets for the FancyTiles.

> [!TIP]
> Importing TileSetGroups allows for very easy setup of generations that reuse certain parts, see the [Jungle Dungeon](jungle-dungeon) as an example.

> [!NOTE]
> Outdated picture from v0.7!

<img height="200" alt="prop" src="https://github.com/user-attachments/assets/fcd2ef63-d145-4291-8e6d-60f2d0cbce16" />

## Tile

There are many different ways to define tiles, make sure to read trough the TileSet section carefully.
Tiles best allinged in the node editor left(north) to right(south) for the zSize, and then bellow eatch other for xSize (east to west), and if it spans multiple y levels the same but with a bigger gap to seperate the layers visually. Complex tiles should probably be saved in their own file so notes can be added without clutterign your main graph.
Shown is an example of a 2x2 cell MultiTile.

> [!CAUTION]
> Make sure the tiny Index [x] in the top left corner of the RuleSet reflects your desired order, they get assigned *only* based on the nodes vertical position. So if you want the index to neatly go left to right make sure the node to the right is positioned slightly lower.

> [!NOTE]
> Outdated picture from v0.7!

<img height="200" alt="Tile" src="https://github.com/user-attachments/assets/8d071295-ca07-4b32-a2d7-2743b2e76e8c" />
