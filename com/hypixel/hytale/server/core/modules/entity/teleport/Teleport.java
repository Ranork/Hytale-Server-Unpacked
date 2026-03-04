package com.hypixel.hytale.server.core.modules.entity.teleport;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Teleport implements Component<EntityStore> {
   @Nullable
   private final World world;
   @Nonnull
   private final Vector3d position = new Vector3d();
   @Nonnull
   private final Vector3f rotation = new Vector3f();
   @Nullable
   private Vector3f headRotation;
   private boolean resetVelocity = true;
   private CompletableFuture<Void> onComplete;

   @Nonnull
   public static ComponentType<EntityStore, Teleport> getComponentType() {
      return EntityModule.get().getTeleportComponentType();
   }

   public Teleport(@Nullable World world, @Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      this.world = world;
      this.position.assign(position);
      this.rotation.assign(rotation);
   }

   public Teleport(@Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      this.world = null;
      this.position.assign(position);
      this.rotation.assign(rotation);
   }

   @Nonnull
   public static Teleport createForPlayer(@Nullable World world, @Nonnull Transform transform) {
      Vector3f headRotation = transform.getRotation();
      Vector3f bodyRotation = new Vector3f(0.0F, headRotation.getYaw(), 0.0F);
      return new Teleport(world, transform.getPosition(), bodyRotation).setHeadRotation(headRotation);
   }

   @Nonnull
   public static Teleport createForPlayer(@Nullable World world, @Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      Vector3f headRotation = rotation.clone();
      Vector3f bodyRotation = new Vector3f(0.0F, headRotation.getYaw(), 0.0F);
      return new Teleport(world, position, bodyRotation).setHeadRotation(headRotation);
   }

   @Nonnull
   public static Teleport createForPlayer(@Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      return createForPlayer(null, position, rotation);
   }

   @Nonnull
   public static Teleport createForPlayer(@Nonnull Transform transform) {
      return createForPlayer(null, transform);
   }

   @Nonnull
   public static Teleport createExact(@Nonnull Vector3d position, @Nonnull Vector3f bodyRotation, @Nonnull Vector3f headRotation) {
      return new Teleport(position, bodyRotation).setHeadRotation(headRotation);
   }

   @Nonnull
   public static Teleport createExact(@Nonnull Vector3d position, @Nonnull Vector3f bodyRotation) {
      return new Teleport(position, bodyRotation);
   }

   public void setPosition(@Nonnull Vector3d position) {
      this.position.assign(position);
   }

   public void setRotation(@Nonnull Vector3f rotation) {
      this.rotation.assign(rotation);
   }

   @Nonnull
   public Teleport setHeadRotation(@Nonnull Vector3f headRotation) {
      this.headRotation = headRotation.clone();
      return this;
   }

   public Teleport withoutVelocityReset() {
      this.resetVelocity = false;
      return this;
   }

   public void setOnComplete(@Nonnull CompletableFuture<Void> onComplete) {
      this.onComplete = onComplete;
   }

   public CompletableFuture<Void> getOnComplete() {
      return this.onComplete;
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   @Nonnull
   public Vector3f getRotation() {
      return this.rotation;
   }

   @Nullable
   public Vector3f getHeadRotation() {
      return this.headRotation;
   }

   public boolean isResetVelocity() {
      return this.resetVelocity;
   }

   @Nonnull
   public Teleport clone() {
      return new Teleport(this.world, this.position, this.rotation);
   }
}
