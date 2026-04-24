package com.png.GridWaveCore.Utils.MirrorNode.Helper;

import com.hypixel.hytale.builtin.hytalegenerator.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.EntityFunnel;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;

public class MirrorEntityFunnel implements EntityFunnel {
    @Nonnull
    private final Axis axis;
    @Nonnull
    private final Bounds3i viewBounds;
    @Nonnull
    private EntityFunnel source;
    @Nonnull
    private final Vector3i anchor;

    public MirrorEntityFunnel(@Nonnull Axis axis) {
        this.axis = axis;
        this.viewBounds = new Bounds3i();
        this.source = EntityFunnel.NULL;
        this.anchor = new Vector3i();
        this.setSource(EntityFunnel.NULL, new Vector3i(Vector3iUtil.ZERO));
    }

    public void setSource(@Nonnull EntityFunnel source, @Nonnull Vector3ic anchor) {
        this.source = source;
        this.anchor.set(anchor);
        this.viewBounds.assign(source.getBounds());
        Bounds3iExtension.mirrorBoundsAroundVoxel(this.viewBounds, this.axis, new Vector3i(anchor));
    }

    @Override
    public void addEntity(@Nonnull EntityPlacementData entityPlacementData) {
        entityPlacementData.getOffset().sub(this.anchor);
        this.axis.flip(entityPlacementData.getOffset());
        entityPlacementData.getOffset().add(this.anchor);

        TransformComponent entityTransform = entityPlacementData.getEntityHolder().getComponent(TransformComponent.getComponentType());
        if (entityTransform != null) {
            Vector3d entityPosition = entityTransform.getPosition();
            entityPosition.sub(Vector3iUtil.toVector3d(this.anchor));
            this.axis.flip(entityPosition);
            entityPosition.add(Vector3iUtil.toVector3d(this.anchor));
            this.axis.flipRotation(entityTransform.getRotation());
        }

        this.source.addEntity(entityPlacementData);
    }

    @Nonnull
    @Override
    public Bounds3i getBounds() {
        return this.viewBounds;
    }
}