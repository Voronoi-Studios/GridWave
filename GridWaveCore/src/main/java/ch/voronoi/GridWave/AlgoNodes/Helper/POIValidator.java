package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.RandomRestrainerAsset;
import ch.voronoi.GridWave.FeatureNodes.RestrainerAsset;
import ch.voronoi.GridWave.TileCollectionNodes.TileSetCollectionAsset;
import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

public class POIValidator implements Validator<TileSetCollectionAsset> {
    @Override
    public void accept(TileSetCollectionAsset tileSetCollectionAsset, ValidationResults validationResults) {
        for (var tileSetAsset : tileSetCollectionAsset.getTileSetAssets()){
            if(tileSetAsset.getTileFeatureAssets().stream().noneMatch(x -> x instanceof RestrainerAsset || x instanceof RandomRestrainerAsset))
                validationResults.fail("All tiles assigned here need to have a Restrainer!");
        }
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {

    }
}
