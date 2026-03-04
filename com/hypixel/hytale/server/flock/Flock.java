package com.hypixel.hytale.server.flock;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.util.DamageData;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Flock implements Component<EntityStore> {
   private boolean trace;
   private PersistentFlockData flockData;
   private DamageData nextDamageData = new DamageData();
   private DamageData currentDamageData = new DamageData();
   private DamageData nextLeaderDamageData = new DamageData();
   private DamageData currentLeaderDamageData = new DamageData();
   private Flock.FlockRemovedStatus removedStatus = Flock.FlockRemovedStatus.NOT_REMOVED;
   private int visFlockMemberCount;

   public static ComponentType<EntityStore, Flock> getComponentType() {
      return FlockPlugin.get().getFlockComponentType();
   }

   public Flock() {
   }

   public Flock(@Nullable FlockAsset flockDefinition, @Nonnull String[] allowedRoles) {
      this.flockData = new PersistentFlockData(flockDefinition, allowedRoles);
   }

   public DamageData getDamageData() {
      return this.currentDamageData;
   }

   public DamageData getNextDamageData() {
      return this.nextDamageData;
   }

   public DamageData getLeaderDamageData() {
      return this.currentLeaderDamageData;
   }

   public DamageData getNextLeaderDamageData() {
      return this.nextLeaderDamageData;
   }

   public boolean isTrace() {
      return this.trace;
   }

   public void setTrace(boolean trace) {
      this.trace = trace;
   }

   public PersistentFlockData getFlockData() {
      return this.flockData;
   }

   public void setFlockData(PersistentFlockData flockData) {
      this.flockData = flockData;
   }

   public Flock.FlockRemovedStatus getRemovedStatus() {
      return this.removedStatus;
   }

   public void setRemovedStatus(Flock.FlockRemovedStatus status) {
      this.removedStatus = status;
   }

   public boolean hasVisFlockMember() {
      return this.visFlockMemberCount > 0;
   }

   public void incrementVisFlockMemberCount() {
      this.visFlockMemberCount++;
   }

   public void decrementVisFlockMemberCount() {
      if (this.visFlockMemberCount > 0) {
         this.visFlockMemberCount--;
      }
   }

   public void onTargetKilled(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> targetEntityReference) {
      TransformComponent targetTransformComponent = componentAccessor.getComponent(targetEntityReference, TransformComponent.getComponentType());
      if (targetTransformComponent != null) {
         this.nextDamageData.onKill(targetEntityReference, targetTransformComponent.getPosition().clone());
      }
   }

   public void swapDamageDataBuffers() {
      DamageData nextData = this.nextDamageData;
      this.nextDamageData = this.currentDamageData;
      this.currentDamageData = nextData;
      DamageData nextLeaderData = this.nextLeaderDamageData;
      this.nextLeaderDamageData = this.currentLeaderDamageData;
      this.currentLeaderDamageData = nextLeaderData;
      this.nextDamageData.reset();
      this.nextLeaderDamageData.reset();
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      Flock flock = new Flock();
      flock.trace = this.trace;
      flock.flockData = (PersistentFlockData)this.flockData.clone();
      flock.nextDamageData = this.nextDamageData.clone();
      flock.currentDamageData = this.currentDamageData.clone();
      flock.nextLeaderDamageData = this.nextLeaderDamageData.clone();
      flock.currentLeaderDamageData = this.currentLeaderDamageData.clone();
      flock.removedStatus = this.removedStatus;
      return flock;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Flock{trace="
         + this.trace
         + ", flockData="
         + this.flockData
         + ", nextDamageData="
         + this.nextDamageData
         + ", currentDamageData="
         + this.currentDamageData
         + ", nextLeaderDamageData="
         + this.nextLeaderDamageData
         + ", currentLeaderDamageData="
         + this.currentLeaderDamageData
         + ", removedStatus="
         + this.removedStatus
         + "}";
   }

   public static enum FlockRemovedStatus {
      NOT_REMOVED,
      DISSOLVED,
      UNLOADED;
   }
}
