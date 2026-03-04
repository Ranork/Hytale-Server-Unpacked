package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CollisionResultComponent implements Component<EntityStore> {
   private final CollisionResult collisionResult;
   private final Vector3d collisionStartPosition;
   private final Vector3d collisionPositionOffset;
   private final Vector3d collisionStartPositionCopy;
   private final Vector3d collisionPositionOffsetCopy;
   private boolean pendingCollisionCheck;

   public static ComponentType<EntityStore, CollisionResultComponent> getComponentType() {
      return EntityModule.get().getCollisionResultComponentType();
   }

   public CollisionResultComponent() {
      this.collisionResult = new CollisionResult(false, false);
      this.collisionStartPosition = new Vector3d();
      this.collisionPositionOffset = new Vector3d();
      this.collisionStartPositionCopy = new Vector3d();
      this.collisionPositionOffsetCopy = new Vector3d();
   }

   public CollisionResultComponent(@Nonnull CollisionResultComponent other) {
      this.collisionResult = other.collisionResult;
      this.collisionStartPosition = other.collisionStartPosition;
      this.collisionPositionOffset = other.collisionPositionOffset;
      this.collisionStartPositionCopy = other.collisionStartPositionCopy;
      this.collisionPositionOffsetCopy = other.collisionPositionOffsetCopy;
      this.pendingCollisionCheck = other.pendingCollisionCheck;
   }

   public CollisionResult getCollisionResult() {
      return this.collisionResult;
   }

   public Vector3d getCollisionStartPosition() {
      return this.collisionStartPosition;
   }

   public Vector3d getCollisionPositionOffset() {
      return this.collisionPositionOffset;
   }

   public Vector3d getCollisionStartPositionCopy() {
      return this.collisionStartPositionCopy;
   }

   public Vector3d getCollisionPositionOffsetCopy() {
      return this.collisionPositionOffsetCopy;
   }

   public boolean isPendingCollisionCheck() {
      return this.pendingCollisionCheck;
   }

   public void markPendingCollisionCheck() {
      this.pendingCollisionCheck = true;
   }

   public void consumePendingCollisionCheck() {
      this.pendingCollisionCheck = false;
   }

   public void resetLocationChange() {
      this.collisionPositionOffset.assign(Vector3d.ZERO);
      this.pendingCollisionCheck = false;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new CollisionResultComponent(this);
   }
}
