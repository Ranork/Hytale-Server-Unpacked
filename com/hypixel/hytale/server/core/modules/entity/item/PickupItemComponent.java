package com.hypixel.hytale.server.core.modules.entity.item;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PickupItemComponent implements Component<EntityStore> {
   public static final float PICKUP_TRAVEL_TIME_DEFAULT = 0.15F;
   private Ref<EntityStore> targetRef;
   private Vector3d startPosition;
   private float originalLifeTime;
   private float lifeTime = 0.15F;
   private boolean finished;

   @Nonnull
   public static ComponentType<EntityStore, PickupItemComponent> getComponentType() {
      return EntityModule.get().getPickupItemComponentType();
   }

   public PickupItemComponent() {
   }

   public PickupItemComponent(@Nonnull Ref<EntityStore> targetRef, @Nonnull Vector3d startPosition) {
      this(targetRef, startPosition, 0.15F);
   }

   public PickupItemComponent(@Nonnull Ref<EntityStore> targetRef, @Nonnull Vector3d startPosition, float lifeTime) {
      this.targetRef = targetRef;
      this.startPosition = startPosition;
      this.lifeTime = lifeTime;
      this.originalLifeTime = lifeTime;
   }

   public PickupItemComponent(@Nonnull PickupItemComponent pickupItemComponent) {
      this.targetRef = pickupItemComponent.targetRef;
      this.lifeTime = pickupItemComponent.lifeTime;
      this.startPosition = pickupItemComponent.startPosition;
      this.originalLifeTime = pickupItemComponent.originalLifeTime;
      this.finished = pickupItemComponent.finished;
   }

   public boolean hasFinished() {
      return this.finished;
   }

   public void setFinished(boolean finished) {
      this.finished = finished;
   }

   public void decreaseLifetime(float amount) {
      this.lifeTime -= amount;
   }

   public float getLifeTime() {
      return this.lifeTime;
   }

   public float getOriginalLifeTime() {
      return this.originalLifeTime;
   }

   public void setInitialLifeTime(float lifeTimeS) {
      this.originalLifeTime = lifeTimeS;
      this.lifeTime = lifeTimeS;
   }

   @Nonnull
   public Vector3d getStartPosition() {
      return this.startPosition;
   }

   @Nullable
   public Ref<EntityStore> getTargetRef() {
      return this.targetRef;
   }

   @Nonnull
   public PickupItemComponent clone() {
      return new PickupItemComponent(this);
   }
}
