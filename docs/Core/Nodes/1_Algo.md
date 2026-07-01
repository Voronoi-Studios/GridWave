---
title: "Algo"
published: true
draft: true
---

# GridWave Algorithm Nodes

> The main workhorse of this plugin

## Variants

---

### GridWave Algorithm (Prop)
<img class="node" src="https://voronoi.ch/node.php?src=https://raw.githubusercontent.com/Voronoi-Studios/GridWave/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/PropAlgo.GridWave.json" alt="AlgoProp"/>
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/AlgoProp.png" alt="AlgoProp"/>

---

### GridWave Algorithm (PropDistribution)
<img class="node" src="https://voronoi.ch/node.php?src=https://raw.githubusercontent.com/Voronoi-Studios/GridWave/refs/heads/main/GridWaveCore/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java/GridWave/PropDistributionAlgo.GridWave.json" alt="AlgoPropDistribution"/>
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/AlgoPropDistribution.png" alt="AlgoPropDistribution" />

---

## Details

<details open>
<summary>GridPositions</summary>

Takes in a list of positions (currently need to be in a perfect grid). At each position a tile will later be spawned.
You can absolutely supply partial grids or grids with wired shapes, just keep in mind that this could lead to impossible or very hard to solve cells.

</details>


<details open>
<summary>Grid</summary>

Defines what the spacing in to every direction, so the Algo knows how to fetch a position of a neighboring cell. Can be left empty if [Grid Point Generator](grid-point-generator) is used for GridPositions.

</details>


<details open>
<summary>Bounds</summary>

On the Prop version this is required.

Adds some limiting bounds to the grid positions (not the prefabs). Can be left empty if [Grid Point Generator](grid-point-generator) is used for GridPositions.


</details>


<details open>
<summary>POIs (FixedTiles)</summary>

Input for multiple [TileSet Nodes](TileSet).
These tiles will be placed first, before anything runs. Make sure your stuff is not overlapping.

> [!IMPORTANT]  
> All tiles do need a [Restrainer Feature](Feature#restrainer-feature-local) for this to work.

</details>


<details open>
<summary>BaseTiles</summary>

Input for multiple [TileSet Nodes](TileSet).
The Algo will now try to figure out which of the provided tiles should be placed at every cell position.
Uses the [Greedy Lowest Entropy Cell Selector](https://github.com/Voronoi-Studios/GridWave/blob/c4c6b195661f1bb5c7742b1e2fa29b1d0086fa5f/GridWaveCore/src/main/java/ch/voronoi/GridWave/AlgoNodes/GridWave.java#L120-L130) by default to choose the next cell to collapse.

</details>


<details open>
<summary>FancyTiles</summary>

Input for multiple [TileSet Nodes](TileSet).
Before the tiles get actually spawned in, we now have the opportunity to use Pattern matching to replace certain tiles with others. 

This is extremely powerful and can be used for many things;
* Cars on a road
* Hanging bridges connecting platforms
* replacing odd generation with cool stuff (like a 4 long boring hallways with a challenging 4 long lava trap obstacle)
* Add mini encounters
* much more

<details>
<summary>show cheat sheet</summary>

<img style="border-radius:10px" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/NodeDefinitionExplainer.png" alt="NodeDefinitionExplainer" />

</details>
</details>


<details open>
<summary>Seed</summary>

Input for a [Seed](Seed).

</details>


<details open>
<summary>Features</summary>

Input for any global scoped [Feature](Feature), to modify the Algo.

</details>
