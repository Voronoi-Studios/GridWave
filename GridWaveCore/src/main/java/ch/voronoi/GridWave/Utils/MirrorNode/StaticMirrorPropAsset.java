package ch.voronoi.GridWave.Utils.MirrorNode;

import ch.voronoi.GridWave.Utils.MirrorNode.Helper.MirrorDirection;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.EmptyPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class StaticMirrorPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<StaticMirrorPropAsset> CODEC = BuilderCodec.builder(
         StaticMirrorPropAsset.class, StaticMirrorPropAsset::new, PropAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, value) -> asset.propAsset = value, asset -> asset.propAsset)
      .add()
      .append(new KeyedCodec<>("Axis", MirrorDirection.CODEC, true), (asset, value) -> asset.axis = value, asset -> asset.axis)
      .add()
      .build();

   @Nonnull
   private PropAsset propAsset = new EmptyPropAsset();
   @Nonnull
   private MirrorDirection axis = MirrorDirection.None;

   @Nonnull
   @Override
   public Prop build(@Nonnull Argument argument) {
       Prop prop = this.propAsset.build(argument);
       if (super.skip() || axis.toAxis() == null) {
         return prop;
      } else {
         return new StaticMirrorProp(prop, axis.toAxis(), argument.materialCache);
      }
   }

   @Override
   public void cleanUp() {
      this.propAsset.cleanUp();
   }
}
