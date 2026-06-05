package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;

import java.util.List;
import java.util.Optional;

public interface IFeatureCheck {
    List<FeatureAsset> getFeatureAssets();

    default <T> boolean hasFeature(Class<T> type) {
        return getFeatureAssets().stream().anyMatch(type::isInstance);
    }

    default  <T> Optional<T> getFirstFeatureOf(Class<T> type) {
        return getFeatureAssets().stream().filter(type::isInstance).map(type::cast).findFirst();
    }
}
