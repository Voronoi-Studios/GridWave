# GridWave

GridWave is a Hytale plugin that integrates Wave Function Collapse (WFC) into the World Generation V2 Node Editor. It enables structured, constraint-based procedural generation for dungeons, mazes, cities, towns, castles, structures, and many more.

## Features

* WFC-based generation for deterministic but varied worlds
* Seamless integration with Worldgen V2
* Support for custom tilesets and constraints
* Scalable generation across large regions
* Modular design for easy extension

## Installation
> [!NOTE]
> Node modding is currently not offically suported by Hytale, thus we need to do some manual work.
1. Download the latest release
2. Place the plugin in your `Mods` folder
3. Copy contents of [`HytaleGenerator Java`](https://github.com/Voronoi-Studios/GridWave/tree/main/src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator%20Java) to your corresponding folder in `%Appdata%/Hytale/install\release\package\game\latest\Client\NodeEditor\Workspaces\HytaleGenerator Java`
> [!CAUTION]
> For ease of use, the included _Workspace file is intended as is a replacement for the original, if you have other node mods, you need to manually copy the sections that are different.

## Usage

* To be explained

## Configuration

Example:
![NodeTree](https://media.discordapp.net/attachments/1480193655988817960/1484508842782883921/image.png?ex=69d04858&is=69cef6d8&hm=d3fcccaa049f9ea1d3a5ffa65adbac21642e7db92e296ce8f0d3fe2268d7c48e&=&format=webp&quality=lossless&width=1149&height=518)

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

Pull requests and issue reports are welcome.

## License

MIT License
