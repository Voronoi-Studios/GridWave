package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SectionData {
    @Nonnull
    public static final BuilderCodec<SectionData> CODEC = BuilderCodec.builder(SectionData.class, SectionData::new)
            .append(
                    new KeyedCodec<>("Cells", new MapCodec<>(Entry.CODEC, LinkedHashMap::new), true),
                    (t, v) -> t.cells = v,
                    t -> t.cells
            )
            .add()
            .build();

    @Nonnull
    private Map<String, SectionData.Entry> cells = new LinkedHashMap<>();

    public SectionData() { }

    public SectionData(List<GridTile> gridTiles) {
        this.cells = new LinkedHashMap<>();
        for (GridTile tile : gridTiles) {
            if(tile == null) continue;
            this.cells.putIfAbsent(cellKey(tile.actualPosition()), new Entry(tile));
        }
    }

    public static String cellKey(Vector3i address) {
        return address.x + "," + address.y + "," + address.z;
    }

    public SectionData.Entry getEntry(Vector3d pos) {
        return cells.get(cellKey(pos.toVector3i()));
    }

    public static class Entry {
        @Nonnull
        public static final BuilderCodec<Entry> CODEC = BuilderCodec.builder(Entry.class, Entry::new)
                .append(new KeyedCodec<>("Position", Vector3i.CODEC, true), (t, v) -> t.position = v, t -> t.position)
                .add()
                .append(new KeyedCodec<>("RuleSet", new ArrayCodec<>(Codec.STRING, String[]::new), true), (t, v) -> t.ruleSet = RuleCombo.fromHorizontalStringArray(v), t -> t.ruleSet.toHorizontalStringArray())
                .add()
                .append(new KeyedCodec<>("GridTileType", new EnumCodec<>(GridTileType.class), true), (t, v) -> t.gridTileType = v, t -> t.gridTileType)
                .add()
                .build();

        public Vector3i position;
        public RuleCombo ruleSet;
        public GridTileType gridTileType;
        public Function<TileSetAsset.Argument, Prop> propFunction = null;

        public Entry() {}
        public Entry(GridTile gridTile){
            this.position = gridTile.actualPosition();
            this.ruleSet = gridTile.tileEntry().getMainRuleSet();
            this.gridTileType = gridTile.type();
            this.propFunction = gridTile.tileEntry().propFunction();
        }
    }
}
