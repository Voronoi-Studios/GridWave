---
title: "Algo"
published: true
draft: true
---

# GridWave Algorithm Nodes

> The main workhorse of this plugin

## Variants

---

### GridWave Algorithm (PropDistribution)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/AlgoProp.png" alt="AlgoProp"/>

---

### GridWave Algorithm (Prop)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/AlgoPropDistribution.png" alt="AlgoPropDistribution" />

---

## Details

<details>
<summary>GridPositions</summary>

Takes in a list of positions (currently need to be in a perfect grid). At each position a tile will later be spawned.
You can absolutely supply partial grids or grids with wired shapes, just keep in mind that this could lead to impossible or very hard to solve cells.

</details>


<details>
<summary>Grid</summary>

Defines what the spacing is in to every direction, so the Algo knows how to fetch a position of a neighboring cell

</details>


<details>
<summary>(Bounds)</summary>

Only present on the Prop version, restrains the positions. can be left empty if a finite amount of positions was provided.

</details>


<details>
<summary>FixedTiles</summary>

Input for a [`TileSetCollection`](TileSetCollection).
These tiles will be places first, before anything runs. Make sure your stuff is not overlapping. 

> [!IMPORTANT]
> All tiles do need a Restrainer [`Feature`](Feature) for this to work or else he might place a tile everywhere.

</details>


<details>
<summary>BaseTiles</summary>

Input for a [`TileSetCollection`](TileSetCollection).
He will now try to figure out which of the provided tiles should be placed at every cell position.
Uses the [`Greedy Lowest Entropy Cell Selector`]() by default to choose the next cell to collapse.

</details>


<details>
<summary>FancyTiles</summary>

Before the tiles get actually spawned in, we now have the opportunity to use Pattern matching to replace singular or multiple tiles with others. 

This is extremely powerful and can be used for many things;
* Cars on a road
* Hanging bridges connecting platforms
* replacing odd generation with cool stuff (like a 4 long boring hallways with a 4 long integrate lava trap obstacle)
* Add mini encounters
* much more

</details>


<details>
<summary>Seed</summary>

Input for a [`Seed`](Seed).

</details>


<details>
<summary>Features</summary>

Input for any global scoped [`Feature`](Feature), to modify the Algo.

</details>