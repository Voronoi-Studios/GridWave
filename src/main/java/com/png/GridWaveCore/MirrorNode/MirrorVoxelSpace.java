package com.png.GridWaveCore.MirrorNode;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.NullSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;

public class MirrorVoxelSpace implements VoxelSpace<Material> {
    @Nonnull
    private final Axis axis;
    @Nonnull
    private final Bounds3i viewBounds;
    @Nonnull
    private final MaterialCache materialCache;
    @Nonnull
    private VoxelSpace<Material> source;
    @Nonnull
    private final Vector3i anchor;
    @Nonnull
    private final Vector3i rPosition;

    public MirrorVoxelSpace(@Nonnull Axis axis, @Nonnull MaterialCache materialCache) {
        this.axis = axis;
        this.materialCache = materialCache;
        this.viewBounds = new Bounds3i();
        this.source = NullSpace.instance();
        this.anchor = new Vector3i();
        this.rPosition = new Vector3i();
        this.setSource(NullSpace.instance(), Vector3i.ZERO);
    }

    public void setSource(@Nonnull VoxelSpace<Material> source, @Nonnull Vector3i anchor) {
        this.source = source;
        this.anchor.assign(anchor);
        this.viewBounds.assign(source.getBounds());
        Bounds3iExtension.mirrorBoundsAroundVoxel(this.viewBounds, this.axis, anchor);
    }

    @Override
    public void set(Material material, int x, int y, int z) {
        this.loadPosition(x, y, z);
        this.source.set(material, this.rPosition);
    }

    @Override
    public void set(Material material, @Nonnull Vector3i position) {
        this.set(material, position.x, position.y, position.z);
    }

    @Override
    public void setAll(Material material) {
        Bounds3i bounds = this.source.getBounds();

        for (int x = bounds.min.x; x < bounds.max.x; x++) {
            for (int y = bounds.min.y; y < bounds.max.y; y++) {
                for (int z = bounds.min.z; z < bounds.max.z; z++) {
                    this.set(material, x, y, z);
                }
            }
        }
    }

    @Override
    public Material get(int x, int y, int z) {
        this.loadPosition(x, y, z);
        return this.source.get(this.rPosition);
    }

    @Override
    public Material get(@Nonnull Vector3i position) {
        return this.get(position.x, position.y, position.z);
    }

    @Nonnull
    @Override
    public Bounds3i getBounds() {
        return this.viewBounds;
    }

    private void loadPosition(int x, int y, int z) {
        this.rPosition.assign(x, y, z);
        switch (this.axis) {
            case X:
                this.rPosition.x = this.anchor.x - (this.rPosition.x - this.anchor.x);
                break;
            case Y:
                this.rPosition.y = this.anchor.y - (this.rPosition.y - this.anchor.y);
                break;
            case Z:
                this.rPosition.z = this.anchor.z - (this.rPosition.z - this.anchor.z);
                break;
            default:
                throw new IllegalStateException("Unexpected axis: " + this.axis);
        }
    }
}
