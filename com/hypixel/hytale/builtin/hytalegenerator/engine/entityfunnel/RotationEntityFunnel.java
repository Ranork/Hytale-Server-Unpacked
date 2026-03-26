package com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel;

import com.hypixel.hytale.builtin.hytalegenerator.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RotationEntityFunnel implements EntityFunnel {
   @Nonnull
   private final RotationTuple rotation_fromViewToSource;
   @Nonnull
   private Bounds3i viewBounds;
   @Nonnull
   private EntityFunnel source;
   @Nonnull
   private final Vector3i anchor;

   public RotationEntityFunnel(@Nonnull RotationTuple rotation) {
      this.rotation_fromViewToSource = rotation;
      this.anchor = new Vector3i();
      this.setSource(EntityFunnel.NULL, Vector3i.ZERO);
   }

   public void setSource(@Nonnull EntityFunnel source, @Nonnull Vector3i anchor) {
      this.source = source;
      this.anchor.assign(anchor);
      this.viewBounds = source.getBounds().clone();
      this.viewBounds.undoRotationAroundVoxel(this.rotation_fromViewToSource, anchor);
   }

   @Override
   public void addEntity(@NonNullDecl EntityPlacementData entityPlacementData) {
      Vector3i offset = entityPlacementData.getOffset();
      offset.subtract(this.anchor);
      this.rotation_fromViewToSource.applyRotationTo(offset);
      offset.add(this.anchor);
      TransformComponent entityTransform = entityPlacementData.getEntityHolder().getComponent(TransformComponent.getComponentType());
      if (entityTransform != null) {
         Vector3d entityPosition = entityTransform.getPosition();
         Vector3f entityRotation = entityTransform.getRotation();
         this.rotation_fromViewToSource.applyRotationTo(entityPosition);
         this.rotation_fromViewToSource.applyRotationTo(entityRotation);
      }

      this.source.addEntity(entityPlacementData);
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds() {
      return this.viewBounds;
   }
}
