package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.math.vector.Vector3d;

public interface IAlgoAsset {
    int getPOICount();
    Vector3d getAnchorPosition(PositionProviderAsset.Argument argument);
}
