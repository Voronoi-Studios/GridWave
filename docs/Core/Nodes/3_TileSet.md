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

### Automatic TileSet
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/AutomaticTileSet.png" alt="AutomaticTileSet" />

Automatically creates all TileSets based on the folders naming, allows for export

<details>
<summary>show cheat sheet</summary>

<img style="border-radius:10px" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/NodeDefinitionExplainer.png" alt="NodeDefinitionExplainer" />

</details>

---

### Imported TileSet

<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/ImportedTileSet.png" alt="ImportedTileSet" />

Allows you to import nodes

---

### Single TileSet
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/SingleTileSet.png" alt="SingleTileSet"/> 

Standard node, allows for exports

<details>
<summary>show cheat sheet</summary>

<img style="border-radius:10px" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/NodeDefinitionExplainer.png" alt="NodeDefinitionExplainer" />

</details>

---

### Multi TileSet
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/MultiTileSet.png" alt="MultiTileSet" />

Used for prefabs that span multiple cells, allows for export.
ZSize defines how man tiles it spans

<details>
<summary>show cheat sheet</summary>

<img style="border-radius:10px" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/NodeDefinitionExplainer.png" alt="NodeDefinitionExplainer" />

</details>

---

### Prop TileSet
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/PropTileSet.png" alt="PropTileSet" />

RuleSet wise same as MultiTileSet, but takes a Prop as input, allows for exports.
This can be used for all sorts of shenanigans, like using a union prop to combine buildings with unique interiors.

---