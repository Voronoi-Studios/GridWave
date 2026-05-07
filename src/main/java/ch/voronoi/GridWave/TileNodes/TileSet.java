package ch.voronoi.GridWave.TileNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import ch.voronoi.GridWave.RuleSetNodes.RuleSet;
import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public abstract class TileSet {

    protected static RuleSet.Combo rotate(RuleSet.Combo combo, int r) {
        String[][] providerArr = combo.providerRuleSet().getRuleSetArrays();
        String[][] receiverArr = combo.recieverRuleSet().getRuleSetArrays();
        return new RuleSet.Combo(new RuleSet(rotate(providerArr, r)), new RuleSet(rotate(receiverArr, r)));
    }

    protected static String[][] rotate(String[][] arr, int r) {
        int l = 4; //Only affect first 4
        String[][] rotated = new String[l][];
        for (int i = 0; i < l; i++) {
            rotated[i] = arr[(i + r) % l];
        }
        return rotated;
    }

    protected static Vector3i rotate(Vector3i v, int r) {
        int x = v.x;
        int z = v.z;

        return switch (r & 3) {
            case 1 -> new Vector3i(z, v.y, -x);
            case 2 -> new Vector3i(-x, v.y, -z);
            case 3 -> new Vector3i(-z, v.y, x);
            default -> v;
        };
    }

    public abstract List<TileEntry> getTileEntries();
    public abstract List<TileEntry> getAllTileEntries();
    public abstract List<FeatureAsset> getTileFeatureAssets();
    public abstract Prop getProp(TileSetAsset.Argument argument);

    /**
     * @param rot the rotation we need for spawning the prefab later on
     * @param ruleSets size 4: north, east, south, west, string represents connection type, so we can match it
     */
    public record TileEntry(
            Map<Vector3i, RuleSet.Combo> ruleSets,
            Vector3i identifierKey,
            double weight, int rot,
            Function<TileSetAsset.Argument, Prop> propFunction,
            List<FeatureAsset> tileFeatures) {

        public TileEntry(TileEntry tileEntry) {
            this(
                    new LinkedHashMap<>(tileEntry.ruleSets),
                    tileEntry.identifierKey.clone(),
                    tileEntry.weight,
                    tileEntry.rot,
                    tileEntry.propFunction,
                    new ArrayList<>(tileEntry.tileFeatures)
            );
        }

        public RuleSet.Combo getMainRuleSet() { return ruleSets.get(identifierKey); }
        public List<TileEntry> getSubTiles(){
            var result = new ArrayList<TileEntry>();
            for(Vector3i subIdentifier : ruleSets.keySet()) {
                result.add(new TileEntry(
                        new HashMap<>(ruleSets),
                        new Vector3i(subIdentifier),
                        weight,
                        rot,
                        subIdentifier.equals(identifierKey) ? propFunction : null,
                        new ArrayList<>(tileFeatures))
                );
            }

            return result;
        }

        public Rotation rotation() {
            return switch (rot) {
                case 1 -> Rotation.Ninety;
                case 2 -> Rotation.OneEighty;
                case 3 -> Rotation.TwoSeventy;
                default -> Rotation.None;
            };
        }

        public Vector3i getOffset() {
            if (ruleSets.isEmpty()) return new Vector3i(0, 0, 0);

            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

            for (Vector3i v : ruleSets.keySet()) {
                int relX = v.x - identifierKey().x;
                int relY = v.y - identifierKey().y;
                int relZ = v.z - identifierKey().z;

                minX = Math.min(minX, relX);
                maxX = Math.max(maxX, relX);
                minY = Math.min(minY, relY);
                maxY = Math.max(maxY, relY);
                minZ = Math.min(minZ, relZ);
                maxZ = Math.max(maxZ, relZ);
            }

            return new Vector3i(
                    (minX + maxX) / 2,
                    (minY + maxY) / 2,
                    (minZ + maxZ) / 2
            );
        }

        public double getWeight(Map<Vector3i, WaveCell> wave, TileSetAsset.Argument argument) {
            AtomicReference<Double> newWeight = new AtomicReference<>(weight);
            tileFeatures.forEach(feature -> feature.ReplaceWeight(newWeight, this, wave, argument));
            return newWeight.get();
        }
    } //WeightedPaths empty if not corner

    public static TileEntry offsetTileEntry(TileEntry entry, Vector3i offset) {
        Map<Vector3i, RuleSet.Combo> newRuleSets = new LinkedHashMap<>();
        for (Map.Entry<Vector3i, RuleSet.Combo> e : entry.ruleSets().entrySet()) {
            Vector3i newKey = new Vector3i(offset).add(e.getKey());
            newRuleSets.put(newKey, e.getValue());
        }
        Vector3i identifierKey = new Vector3i(offset).add(entry.identifierKey().clone());
        return new TileEntry(
                newRuleSets,
                identifierKey,
                entry.weight(),
                entry.rot(),
                entry.propFunction,
                new ArrayList<>(entry.tileFeatures)
        );
    }
}
