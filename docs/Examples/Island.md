---
published: true
draft: true
---

![IslandImage](https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/Examples/IslandExample.png)

## _*Island (Kyst)*_ ![Static Badge](https://img.shields.io/badge/State-Preview-violet)


Inspired by the [Kyst](https://adamatomic.itch.io/kyst) tile sets from Adam Saltsmann.

[<img alt="Island Image" width="200" src="https://github.com/Voronoi-Studios/GridWave/raw/main/docs/Images/IslandImg.png" />](https://adamatomic.itch.io/kyst)

This example has lots of Advanced Rulesets to achieve the generation, specifically to prevent for example the edges to connect to other edges.

### Features:
- Different in and out rules to allow for edge, inner corner and outer corner tiles that transition from water to land
- Carefully chosen rules so certain land tiles only can connect to certain other ones.
- Transitional tiles to prevent certain tiles from spawning next to each other

### Related Files:
- Biome File: [Biome_Island.json](https://github.com/Voronoi-Studios/GridWave/blob/main/GridWaveExamples/src/main/resources/Server/HytaleGenerator/Biomes/Biome_Island.json)
- Feature Files: -
- Prop File: [Island.json](https://github.com/Voronoi-Studios/GridWave/blob/main/GridWaveExamples/src/main/resources/Server/HytaleGenerator/Props/Island.json)
- PropDistribution Files: -
- TileSet Files: [IslandBaseTiles1.json](https://github.com/Voronoi-Studios/GridWave/blob/main/GridWaveExamples/src/main/resources/Server/HytaleGenerator/TileSets/Island/IslandBaseTiles1.json), [IslandFancyTiles1.json](https://github.com/Voronoi-Studios/GridWave/blob/main/GridWaveExamples/src/main/resources/Server/HytaleGenerator/TileSets/Island/IslandFancyTiles1.json)
- Others: [WorldStructures_Island.json](https://github.com/Voronoi-Studios/GridWave/blob/main/GridWaveExamples/src/main/resources/Server/HytaleGenerator/WorldStructures/WorldStructures_Island.json), [instance.bson](https://github.com/Voronoi-Studios/GridWave/blob/main/GridWaveExamples/src/main/resources/Server/Instances/Island/instance.bson)
- **Prefabs:** [Prefabs/Island](https://github.com/Voronoi-Studios/GridWave/tree/main/GridWaveExamples/src/main/resources/Server/Prefabs/Island)

