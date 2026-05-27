package com.hypixel.hytale.builtin.hytalegenerator.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class RotationVoxelSpace implements VoxelSpace<Material> {
   @Nonnull
   private final RotationTuple rotation_fromViewToSource;
   @Nonnull
   private final RotationTuple rotation_materialFromSourceToView;
   @Nonnull
   private final Bounds3i viewBounds;
   @Nonnull
   private final MaterialCache materialCache;
   @Nonnull
   private VoxelSpace<Material> source;
   @Nonnull
   private final Vector3i anchor;
   @Nonnull
   private final Vector3i rSourcePosition;

   public RotationVoxelSpace(@Nonnull RotationTuple rotation_fromViewToSource, @Nonnull MaterialCache materialCache) {
      this.rotation_fromViewToSource = rotation_fromViewToSource;
      this.rotation_materialFromSourceToView = RotationTuple.of(
         rotation_fromViewToSource.yaw().toInverse(), rotation_fromViewToSource.pitch().toInverse(), rotation_fromViewToSource.roll().toInverse()
      );
      this.materialCache = materialCache;
      this.viewBounds = new Bounds3i();
      this.anchor = new Vector3i();
      this.setSource(NullSpace.instance(), Vector3iUtil.ZERO);
      this.rSourcePosition = new Vector3i();
   }

   public void setSource(@Nonnull VoxelSpace<Material> source, @Nonnull Vector3ic anchor) {
      this.source = source;
      this.anchor.set(anchor);
      this.viewBounds.assign(source.getBounds());
      this.viewBounds.undoRotationAroundVoxel(this.rotation_fromViewToSource, anchor);
   }

   public void set(@NullableDecl Material material, int x, int y, int z) {
      this.loadPosition(x, y, z);
      Material rotatedMaterial = this.materialCache.getMaterialRotated(material, this.rotation_fromViewToSource);
      this.source.set(rotatedMaterial, this.rSourcePosition);
   }

   public void set(@NullableDecl Material material, @NonNullDecl Vector3i position) {
      this.set(material, position.x, position.y, position.z);
   }

   public void setAll(@NullableDecl Material material) {
      Bounds3i bounds = this.source.getBounds();

      for (int x = bounds.min.x; x < bounds.max.x; x++) {
         for (int z = bounds.min.z; z < bounds.max.z; z++) {
            for (int y = bounds.min.y; y < bounds.max.y; y++) {
               this.set(material, x, y, z);
            }
         }
      }
   }

   @NullableDecl
   public Material get(int x, int y, int z) {
      this.loadPosition(x, y, z);
      Material material = this.source.get(this.rSourcePosition);
      return this.materialCache.getMaterialRotated(material, this.rotation_materialFromSourceToView);
   }

   @NullableDecl
   public Material get(@NonNullDecl Vector3i position) {
      return this.get(position.x, position.y, position.z);
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds() {
      return this.viewBounds;
   }

   private void loadPosition(int x, int y, int z) {
      assert this.viewBounds.contains(x, y, z);

      this.rSourcePosition.set(x, y, z);
      this.rSourcePosition.sub(this.anchor);
      this.rotation_fromViewToSource.applyRotationTo(this.rSourcePosition);
      this.rSourcePosition.add(this.anchor);

      assert this.source.getBounds().contains(this.rSourcePosition);
   }
}
