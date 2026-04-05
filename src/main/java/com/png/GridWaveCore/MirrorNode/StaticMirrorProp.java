package com.png.GridWaveCore.MirrorNode;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class StaticMirrorProp extends Prop {
    @Nonnull
    private final Prop prop;
    @Nonnull
    private final MirrorVoxelSpace readMirrorVoxelSpace;
    @Nonnull
    private final MirrorVoxelSpace writeMirrorVoxelSpace;
    @Nonnull
    private final MirrorEntityFunnel mirrorEntityFunnel;
    @Nonnull
    private final Bounds3i readBounds;
    @Nonnull
    private final Bounds3i writeBounds;
    @Nonnull
    private final Context rChildContext;

    public StaticMirrorProp(@Nonnull Prop prop, @Nonnull Axis axis, @Nonnull MaterialCache materialCache) {
        this.prop = prop;
        this.readMirrorVoxelSpace = new MirrorVoxelSpace(axis, materialCache);
        this.writeMirrorVoxelSpace = new MirrorVoxelSpace(axis, materialCache);
        this.mirrorEntityFunnel = new MirrorEntityFunnel(axis);
        this.readBounds = prop.getReadBounds_voxelGrid().clone();
        this.writeBounds = prop.getWriteBounds_voxelGrid().clone();
        Bounds3iExtension.mirrorBoundsAroundVoxel(this.readBounds, axis, Vector3i.ZERO);
        Bounds3iExtension.mirrorBoundsAroundVoxel(this.writeBounds, axis, Vector3i.ZERO);
        this.rChildContext = new Context();
    }

   @Override
   public boolean generate(@NonNullDecl Context context) {
      this.readMirrorVoxelSpace.setSource(context.materialReadSpace, context.position);
      this.writeMirrorVoxelSpace.setSource(context.materialWriteSpace, context.position);
      this.mirrorEntityFunnel.setSource(context.entityWriteBuffer, context.position);
      this.rChildContext.assign(context);
      this.rChildContext.materialReadSpace = this.readMirrorVoxelSpace;
      this.rChildContext.materialWriteSpace = this.writeMirrorVoxelSpace;
      this.rChildContext.entityWriteBuffer = this.mirrorEntityFunnel;
      return this.prop.generate(this.rChildContext);
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.readBounds;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds;
   }
}
