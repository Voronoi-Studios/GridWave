---
title: "Feature"
published: true
draft: true
---

# Feature Nodes

Allows to add special features to the Algo -> (global) or individual tiles -> (local)

## Variants

---

### Restrainer Feature (local)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/RestrainerFeature.png" alt="RestrainerFeature"/> 

Used to create POI Tiles, fixes the positon and rotation of a tile. The position needs to pe exact and needs to be present for the tile to be spawned.

---

### Random Restrainer Feature (local)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/RandomRestrainerFeature.png" alt="RandomRestrainerFeature"/> 

Same as RestrainerFeature, but takes a list of Positions as input where it chooses a random spot from using the seed.

Tip: By sharing the seed, you can for example make two separate algos spawn a top and bottom part of a stair in the same place

---

### Overlap Tile Feature (global or local)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/OverlapTileFeature.png" alt="OverlapTileFeature"/> 

Used if you want to make TileSets where the outer edge of the prefabs overlap each other (see dungeon example).
<details>
<summary>Under the hood, ...</summary>
all it actually does is to invert when we apply offsets when a tile is rotated. Even tiles don't have a center so we need to offset the center to keep it in the same spot if the grid is also even. If you want to overlap the tiles, this kinda flips: so an even grid would mean your tiles are odd so we don't need to offset. Vice versa if you have an odd gird.

</details>

---

### Path Cell Selector Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/PathCellSelectorFeature.png" alt="PathCellSelectorFeature"/> 

Replaces the default [`Greedy Lowest Entropy Cell Selector`](), with one that only allows propagation of edges with the specified key. StopAfterPercent allows you to revert back to the default cell selector after the entered percent of grid spots where filled.

---

### Path Key Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/PathKeyFeature.png" alt="PathKeyFeature"/> 

When the Algo is finished it checks if all POI's are connected (by performing a flood fill using the path keys (`,` separated) and raking what POI's are in the same connection pool) to each other. If not it marks it as a failed attempt. If multiple attempts are allowed the Algo will try again, and this feature will check again, till max Attempts are reached.
CleanIsloated removes any tile that are not connected to any POI, will likely become its own feature in the future.

> [!NOTE]
> In my own testing I had this feature lead to more success if I was NOT using "Path Cell Selector Feature" as well. Have no clue why...

---

### Border Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/BorderFeature.png" alt="BorderFeature"/> 

If not present the Algo does not care what rulesets touch a border. If added it allows you to now specify that, by defining an imaginary tile that would be placed around your grid.

---

### Conditional Weight Feature (local)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/ConditionalWeightFeature.png" alt="ConditionalWeightFeature"/> 

Allows you to overwrite the weight of a tile based on some condition

---

### Debug Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/DebugFeature.png" alt="DebugFeature"/> 

Gives various debug options:

* DebugGrid: places every tile with every possible rotation after each other. 
* LimitSteps & MaxSteps: Allows you to step through the solving process to see where ge might do something wrong.

---

### Multi Attempt Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/MultiAttemptFeature.png" alt="MultiAttemptFeature"/> 

Defines how many attempts (default: 1) he is allowed to make. A fresh attempt is started after the maximum Backtracks (default: 5k) are reached.

---

### Multithreading Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/MultithreadingFeature.png" alt="Multithreading"/> 

Splits the search of a correct solution for the WFC over multiple threads and shares the result with others. Mainly beneficial if any of the prefabs in the tile set are very large and cause many chunks to access a wfc section.

> [!WARNING]
> This node often breaks so use with caution

---

