# GridWave

GridWave is a Hytale plugin that integrates Wave Function Collapse (WFC) into the World Generation V2 Node Editor. It enables structured, constraint-based procedural generation for dungeons, mazes, cities, towns, castles, structures, and many more.

Check out the [`GridWaveExamples`](https://github.com/Voronoi-Studios/GridWaveExamples) to get an idea an idea how different things can be accieved.

## Features

* WFC-based generation for deterministic but varied worlds
* Seamless integration with Worldgen V2
* Support for custom tilesets and constraints
* Scalable generation across large regions
* Modular design for easy extension

## Installation
### Default:
1. Download the latest release
2. Place the plugin in your global or world `Mods` folder


### Adding Node Editor support:
1. Copy contents of [`HytaleGenerator Java`](https://github.com/Voronoi-Studios/GridWave/tree/main/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java) to your corresponding folder in `%Appdata%/Hytale/install\release\package\game\latest\Client\NodeEditor\Workspaces\HytaleGenerator Java`
> [!NOTE]
> Node modding is currently not offically suported by Hytale, thus we need to do some manual work.
> [!CAUTION]
> For ease of use, the included _Workspace file is intended as is a replacement for the original, if you have other node mods, you need to manually copy the sections that are different.

## Usage

Check out the [`GridWaveExamples`](https://github.com/Voronoi-Studios/GridWaveExamples) to get an idea an idea how different things can be accieved using the Node Editor.

## How It Works

GridWave applies Wave Function Collapse by:

1. Reads all positions delivered by a ListPosition Node
2. Places any POI (FixedTiles) on the grid if position exists
3. Uses BaseTiles to fill the grid using WFC
   1. Backtracks if it encounters impossible situations
   2. Starts a new attempt if to many backtracks where reached, _Configurable, to give possibility to increase success chance_
4. Uses pattern matching to try and replace base tiles with FancyTiles to add variety
5. Assembles a single UnionProp

## Roadmap

* Need to add later

## Contributing

Pull requests and issue reports will be welcome, as soon as I'm trough the early phase (May)

## License

MIT License
