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
![NodeTreeBiome](https://media.discordapp.net/attachments/1480191590935822457/1489793295633743912/image.png?ex=69d1b5a0&is=69d06420&hm=0c2c2b3d851471dbed42fb811c2bfe11926f81d20e48288a141255b794cbcdf1&=&format=webp&quality=lossless&width=2733&height=1128)
![NodeTreeProp](https://media.discordapp.net/attachments/1480191590935822457/1489793296183332905/image.png?ex=69d1b5a0&is=69d06420&hm=ad4b3b2b3b1427a2e9e5735c6071610c512112795e8023c9651bdd32755695a1&=&format=webp&quality=lossless&width=2633&height=1220)
![NodeTreeTile](https://media.discordapp.net/attachments/1480191590935822457/1489793296761880757/image.png?ex=69d1b5a0&is=69d06420&hm=f00546876e78d333325b750edc60db39b3ba0176fbb7f9ef1c7f0f80eeafe47e&=&format=webp&quality=lossless&width=2153&height=1018)

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
