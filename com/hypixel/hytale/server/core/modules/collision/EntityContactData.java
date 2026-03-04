package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityContactData {
   protected final Vector3d collisionPoint = new Vector3d();
   protected double collisionStart;
   protected double collisionEnd;
   @Nullable
   protected Ref<EntityStore> entityReference;
   protected String collisionDetailName;

   @Nonnull
   public Vector3d getCollisionPoint() {
      return this.collisionPoint;
   }

   public double getCollisionStart() {
      return this.collisionStart;
   }

   public double getCollisionEnd() {
      return this.collisionEnd;
   }

   @Nullable
   public Ref<EntityStore> getEntityReference() {
      return this.entityReference;
   }

   public String getCollisionDetailName() {
      return this.collisionDetailName;
   }

   public void assign(@Nonnull Vector3d position, double start, double end, Ref<EntityStore> entity, String collisionDetailName) {
      this.collisionPoint.assign(position);
      this.collisionStart = start;
      this.collisionEnd = end;
      this.entityReference = entity;
      this.collisionDetailName = collisionDetailName;
   }

   public void clear() {
      this.entityReference = null;
   }
}
