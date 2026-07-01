---
title: "TileSet"
published: true
draft: true
---

# TileSet Nodes

Defines a Set of Tiles.
Can be read from in the folder `Server/HytaleGenerator/TileSets` 

## Variants

---

### TileSet Group
<img class="node" src="https://voronoi.ch/node.php?src=https://github.com/Voronoi-Studios/GridWave/raw/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/TileSet/Group.TileSet.GridWave.json" alt="GroupTileSet"/> 
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/GroupTileSet.png" alt="GroupTileSet"/> 

Combines all TileSets that are returned by its child nodes in to a singular flattened list, which allows for visual clarity and exports of multiple TileSets at once.
Features added to this node will be applied to all child nodes.

---

### Automatic TileSet Group
<img class="node" src="https://voronoi.ch/node.php?src=https://github.com/Voronoi-Studios/GridWave/raw/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/TileSet/AutoGroup.TileSet.GridWave.json" alt="AutomaticTileSetGroup" />
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/AutomaticTileSetGroup.png" alt="AutomaticTileSetGroup" />

Automatically creates the TileSets based on the sub folders naming according to [Automatic TileSet](tileset#automatic-tileset)'s rules , allows for export.
Sub folders under the specified folder are allowed. Features added to this node will be applied to all child nodes.

---


### Single TileSet
<img class="node" src="https://voronoi.ch/node.php?src=https://github.com/Voronoi-Studios/GridWave/raw/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/TileSet/Single.TileSet.GridWave.json" alt="SingleTileSet"/> 
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/SingleTileSet.png" alt="SingleTileSet"/> 

Standard node, allows for exports

<details>
<summary>show cheat sheet</summary>

<img style="border-radius:10px" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/NodeDefinitionExplainer.png" alt="NodeDefinitionExplainer" />

</details>

---

### Multi TileSet
<img class="node" src="https://voronoi.ch/node.php?src=https://github.com/Voronoi-Studios/GridWave/raw/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/TileSet/Multi.TileSet.GridWave.json" alt="MultiTileSet" />
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/MultiTileSet.png" alt="MultiTileSet" />

Used for prefabs that span multiple cells, allows for export.
- XSize: Size east to west
- ZSize: Size north to south
- (YSize): hidden, calculated automatically based on the number of rulesets provided.

<details>
<summary>show cheat sheet</summary>

<img style="border-radius:10px" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/NodeDefinitionExplainer.png" alt="NodeDefinitionExplainer" />

</details>

---

### Prop TileSet
<img class="node" src="https://voronoi.ch/node.php?src=https://github.com/Voronoi-Studios/GridWave/raw/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/TileSet/Prop.TileSet.GridWave.json" alt="PropTileSet" />
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/PropTileSet.png" alt="PropTileSet" />

Same as MultiTileSet, but takes a Prop as input, allows for exports.
This can be used for all sorts of shenanigans, like using a union prop to combine buildings with unique interiors.

---

### Automatic TileSet
<img class="node" src="https://voronoi.ch/node.php?src=https://github.com/Voronoi-Studios/GridWave/raw/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/TileSet/Auto.TileSet.GridWave.json" alt="AutomaticTileSet" />
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/AutomaticTileSet.png" alt="AutomaticTileSet" />

Automatically creates all TileSets based on the folders naming, allows for export.

The strings have the fallowing components:
```
1x3/10X0-X0X0-X010
| |  |  |
1 2  3  4
```
1. xSize
2. zSize
3. 4 or 6 numbers or letters as keys defining `north, east, south, west, up, down` X and N have special functions respectively: 
- X: blank/any connection is allwed
- N: Null, same as X but tile wont replace the base tile under it, allows for shapes with "holes". The top right tile cant be one of those as it holds the actual prefabs information. 
4. divider to seperate rulesets

<details>
<summary>show cheat sheet</summary>

<img style="border-radius:10px" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/NodeDefinitionExplainer.png" alt="NodeDefinitionExplainer" />

</details>

---


### Imported TileSet
<img class="node" src="https://voronoi.ch/node.php?src=https://github.com/Voronoi-Studios/GridWave/raw/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/TileSet/Imported.TileSet.GridWave.json" alt="ImportedTileSet" />
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/ImportedTileSet.png" alt="ImportedTileSet" />

Allows you to import nodes.
Features can be added in this step as well, allowing you to for example save a very big and complex multi tile seperatly and use it multiple times with different restrainer features.

---
