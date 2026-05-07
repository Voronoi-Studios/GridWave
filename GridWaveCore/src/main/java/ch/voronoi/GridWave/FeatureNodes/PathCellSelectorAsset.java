package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.*;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static ch.voronoi.GridWave.AlgoNodes.GridWave.dirs;
import static ch.voronoi.GridWave.AlgoNodes.Helper.Match.oppositeDirection;

public class PathCellSelectorAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<PathCellSelectorAsset> CODEC = BuilderCodec.builder(PathCellSelectorAsset.class, PathCellSelectorAsset::new, FeatureAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("PathKey", Codec.STRING, true), (asset, value) -> asset.pathKey = value, asset -> asset.pathKey)
            .add()
            .append(new KeyedCodec<>("StopAfterPercent", Codec.INTEGER, true), (asset, value) -> asset.stopAfterPercent = value, asset -> asset.stopAfterPercent)
            .add()
            .build();

    private String pathKey = "";
    private int stopAfterPercent = 80;


    @Override
    public void ReplaceCellSelector(AtomicReference<CellSelector> cellSelector, TileSetAsset.Argument argument) {
        if(skip()) return;
        cellSelector.set(new CellSelector() {
            @Override
            public CellSelectiorResult select(Map<Vector3i, WaveCell> wave, Deque<WaveCellChange> stack,AttemptBehavior attemptBehavior, int backtracksCount, Random random) {
                Set<WaveCell> subWave = new LinkedHashSet<>();
                List<WaveCell> notCollapsed = wave.values().stream().filter(waveCell -> !waveCell.isCollapsed()).toList();
                if (100d / wave.size() * notCollapsed.size() < 100 - stopAfterPercent) { subWave = new LinkedHashSet<>(notCollapsed); }
                else for (WaveCell waveCell : notCollapsed) {
                    for (int r = 0; r < 4; r++) {
                        Vector3i neighborPos = new Vector3i(waveCell.getPosition()).add(dirs[r].clone().scale(argument.algoAsset.getGrid()));
                        WaveCell neighbor = wave.get(neighborPos);
                        if (neighbor != null && neighbor.isCollapsed()){
                            String[][] neighborRuleSetArrays = neighbor.getChosen().tileEntry().getMainRuleSet().providerRuleSet().getRuleSetArrays();
                            if(Arrays.stream(neighborRuleSetArrays[oppositeDirection[r]]).toList().contains(pathKey)){
                                subWave.add(waveCell); break;
                            }
                        }
                    }
                }
                if(subWave.isEmpty()) return Backtrack(stack, wave);

                Optional<WaveCell> lowestEntropyCell = subWave.stream().min(Comparator.comparingInt(WaveCell::getEntropy));
                if (lowestEntropyCell.isPresent() && lowestEntropyCell.get().getEntropy() == 0) {
                    if (backtracksCount > attemptBehavior.maxBacktracks) return new CellSelectiorResult(null, EarlyExitReason.MAX_BACKTRACKS_HIT);
                    else return Backtrack(stack, wave);
                }
                return new CellSelectiorResult(lowestEntropyCell.orElse(null), null);

            }
        });
    }
}
