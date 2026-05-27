package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.math.vector.Rotation3f;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class EntitySnapshot {
   @Nonnull
   private final Vector3d position = new Vector3d();
   @Nonnull
   private final Rotation3f bodyRotation = new Rotation3f();

   public EntitySnapshot() {
   }

   public EntitySnapshot(@Nonnull Vector3d position, @Nonnull Rotation3f bodyRotation) {
      this.position.set(position);
      this.bodyRotation.set(bodyRotation);
   }

   public void init(@Nonnull Vector3d position, @Nonnull Rotation3f bodyRotation) {
      this.position.set(position);
      this.bodyRotation.set(bodyRotation);
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   @Nonnull
   public Rotation3f getBodyRotation() {
      return this.bodyRotation;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntitySnapshot{position=" + this.position + ", bodyRotation=" + this.bodyRotation + "}";
   }
}
