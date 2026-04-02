package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;

import java.util.*;

public abstract class TileSet {

    protected static String[] rotate(String[] arr, int r) {
        int l = arr.length;
        String[] rotated = new String[l];
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

    /**
     * @param rot the rotation we need for spawning the prefab later on
     * @param ruleSets size 4: north, east, south, west, string represents connection type, so we can match it
     */
    public record TileEntry(Map<Vector3i, String[]> ruleSets, Vector3i identifierKey, double weight, int rot, WeightedMap<List<IPrefabBuffer>> weightedPathAssets) {
        public String[] getMainRuleSet() { return ruleSets.get(identifierKey); }
        public List<TileEntry> getSubTiles(){
            var result = new ArrayList<TileEntry>();
            for(Vector3i subIdentifier : ruleSets.keySet()) {
                result.add(new TileEntry(
                        new HashMap<>(ruleSets),
                        new Vector3i(subIdentifier),
                        weight,
                        rot,
                        subIdentifier.equals(identifierKey) ? new WeightedMap<>(weightedPathAssets) : null
                ));
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
    } //WeightedPaths empty if not corner

    protected static TileEntry offsetTileEntry(TileEntry entry, Vector3i offset) {
        Map<Vector3i, String[]> newRuleSets = new LinkedHashMap<>();
        for (Map.Entry<Vector3i, String[]> e : entry.ruleSets().entrySet()) {
            Vector3i newKey = new Vector3i(offset).add(e.getKey());
            newRuleSets.put(newKey, e.getValue());
        }
        Vector3i identifierKey = new Vector3i(offset).add(entry.identifierKey().clone());
        return new TileEntry(
                newRuleSets,
                identifierKey,
                entry.weight(),
                entry.rot(),
                entry.weightedPathAssets()
        );
    }
}
