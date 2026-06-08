---
title: "Feature"
published: true
draft: true
---

# Feature Nodes

Allows to add special features to the Algo -> (global) or individual tiles -> (local)

## Variants

---

### Group Feature (global or local)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/GroupFeature.png" alt="GroupFeature"/> 

Combines all Features that are returned by its child nodes in to a singular flattened list, which allows for visual clarity and exports of multiple Features at once.

---

### Debug Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/DebugFeature.png" alt="DebugFeature"/> 

Gives various debug options:

* WriteToConsole: Writes the same info as the Notification plus an ASCII representation of the generated section to the console 
* ShowNotification: Shows an in game notification after each run of the core algo with some handy information to any OP'ed person. (Currently there is no way to make it world specific)
* VisualizeGridPositions: Places a red wool at the cells center
* VisualizeSectionBounds: Outlines what belongs to a section or what bounds where set on the Algo node.
* DebugGrid: Places every tile with every possible rotation after each other. [BROCKEN?]
* LimitSteps & MaxSteps: Allows you to step through the solving process to see where ge might do something wrong.

---

### Restrainer Feature (local)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/RestrainerFeature.png" alt="RestrainerFeature"/> 

Used to create POI Tiles, fixes the positon and rotation of a tile. The position needs to be present for the tile to be spawned.
Will be seperated in the future in to rotation and position restrainer nodes respectively

---

### Random Restrainer Feature (local)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/RandomRestrainerFeature.png" alt="RandomRestrainerFeature"/> 

Same as RestrainerFeature, but takes a list of Positions as input where it chooses a random spot from using the seed.

> [!TIP]
> By sharing the seed, you can for example make two separate algos spawn a top and bottom part of a stair in the same place

---

### Multi Attempt Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/MultiAttemptFeature.png" alt="MultiAttemptFeature"/> 

Defines how many attempts (default: 1) he is allowed to make. A fresh attempt is started after the maximum Backtracks (default: 5k) are reached.
* New Attempts Behavor: [PLACEHOLDER] Behavior when starting a new attempt after reaching the maximum number of backtracking steps.
  * RETRY will simply start over with the same settings until MaxAttempts is reached.
  * The SIMPLIFY_X% options will remove a sub percentage of base tiles from the pool after each failed attempt.
  * The SIMPLIFY_INCRIMENTAL option will increase the percentage unitl it successfully generates a layout or reaches 90% simplification. This is the recommended option if you want to ensure a layout is generated, but don't care about the quality of the layout.

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

Replaces the default [Greedy Lowest Entropy Cell Selector](https://github.com/Voronoi-Studios/GridWave/blob/c4c6b195661f1bb5c7742b1e2fa29b1d0086fa5f/GridWaveCore/src/main/java/ch/voronoi/GridWave/AlgoNodes/GridWave.java#L120-L130), with one that only allows propagation of edges with the specified key. StopAfterPercent allows you to revert back to the default cell selector after the entered percent of grid spots where filled.

---

### Path Key Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/PathKeyFeature.png" alt="PathKeyFeature"/> 

When the Algo is finished it checks if all POI's are connected (by performing a flood fill using the path keys (`,` separated) and raking what POI's are in the same connection pool) to each other. If not it marks it as a failed attempt. If multiple attempts are allowed the Algo will try again, and this feature will check again, till max Attempts are reached.
CleanIsloated removes any tile that are not connected to any POI, will likely become its own feature in the future.

> [!NOTE]
> In my own testing I had this feature lead to more success if I was NOT using "Path Cell Selector Feature" as well.

---

### Border Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/BorderFeature.png" alt="BorderFeature"/> 

If not present the Algo does not care what rulesets touch a border. If added it allows you to now specify that, by defining an imaginary tile that would be placed around your grid.
* BorderType: Whether the border should be placed along the full bounds or on the individual [sections](feature#section-storage-feature-global).
* Border RuleSets : If multiple RuleSets are provided it chooses one randomly for outer borders and deterministically random (based on edge position) for inner borders.

---

### Conditional Weight Feature (local)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/ConditionalWeightFeature.png" alt="ConditionalWeightFeature"/> 

Allows you to overwrite the weight of a tile based on some condition

---


### Multithreading Feature (global) [DEPRECIATED]
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/MultithreadingFeature.png" alt="Multithreading"/> 

Splits the search of a correct solution for the WFC over multiple threads and shares the result with others. Mainly beneficial if any of the prefabs in the tile set are very large and cause many chunks to access the same wfc section.

> [!WARNING]
> This node often breaks so use with caution

---

### Section Storage Feature (global)
<img class="node" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Nodes/SectionStorageFeature.png" alt="SectionStorageFeature"/> 

If not present as a feature the above shown default values are used by the [GridWave Algorithm (PropDistribution)]().
Allows for sectioning of larger or infinite use cases.
* WriteToWorldFolder: [PLACEHOLDER] Will in the future save generation data so it can be read back later.
* CacheSize: Defines how many sections will be kept in memory
* HorizontalSectionSize: Size in cells (not voxles) a sections should be in x and z
* VerticalSectionSize: [PLACEHOLDER] Size in cells (not vocles) a section should be in y -> leave as is

---
