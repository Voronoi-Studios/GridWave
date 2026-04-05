package com.png.GridWaveCore.MirrorNode;

import com.hypixel.hytale.builtin.hytalegenerator.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.EntityFunnel;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;

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
        this.setSource(EntityFunnel.NULL, Vector3i.ZERO);
    }

    public void setSource(@Nonnull EntityFunnel source, @Nonnull Vector3i anchor) {
        this.source = source;
        this.anchor.assign(anchor);
        this.viewBounds.assign(source.getBounds());
        Bounds3iExtension.mirrorBoundsAroundVoxel(this.viewBounds, this.axis, anchor);
    }

    @Override
    public void addEntity(@Nonnull EntityPlacementData entityPlacementData) {
        entityPlacementData.getOffset().subtract(this.anchor);
        this.axis.flip(entityPlacementData.getOffset());
        entityPlacementData.getOffset().add(this.anchor);

        TransformComponent entityTransform = entityPlacementData.getEntityHolder().getComponent(TransformComponent.getComponentType());
        if (entityTransform != null) {
            Vector3d entityPosition = entityTransform.getPosition();
            entityPosition.subtract(this.anchor);
            this.axis.flip(entityPosition);
            entityPosition.add(this.anchor);
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