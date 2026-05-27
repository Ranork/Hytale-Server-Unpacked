package com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel;

import com.hypixel.hytale.builtin.hytalegenerator.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class RotationEntityFunnel implements EntityFunnel {
   private static final Vector3dc BLOCK_CENTER = new Vector3d(0.5, 0.5, 0.5);
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
      this.setSource(EntityFunnel.NULL, Vector3iUtil.ZERO);
   }

   public void setSource(@Nonnull EntityFunnel source, @Nonnull Vector3ic anchor) {
      this.source = source;
      this.anchor.set(anchor);
      this.viewBounds = source.getBounds().clone();
      this.viewBounds.undoRotationAroundVoxel(this.rotation_fromViewToSource, anchor);
   }

   @Override
   public void addEntity(@NonNullDecl EntityPlacementData entityPlacementData) {
      Vector3i offset = entityPlacementData.getOffset();
      offset.sub(this.anchor);
      this.rotation_fromViewToSource.applyRotationTo(offset);
      offset.add(this.anchor);
      TransformComponent entityTransform = entityPlacementData.getEntityHolder().getComponent(TransformComponent.getComponentType());
      if (entityTransform != null) {
         Vector3d entityPosition = entityTransform.getPosition();
         Rotation3f entityRotation = entityTransform.getRotation();
         entityPosition.sub(BLOCK_CENTER);
         this.rotation_fromViewToSource.applyRotationTo(entityPosition);
         entityPosition.add(BLOCK_CENTER);
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
