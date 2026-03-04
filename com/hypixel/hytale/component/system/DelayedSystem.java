package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import javax.annotation.Nonnull;

public abstract class DelayedSystem<ECS_TYPE> extends TickingSystem<ECS_TYPE> {
   @Nonnull
   private final ResourceType<ECS_TYPE, DelayedSystem.Data<ECS_TYPE>> resourceType = this.registerResource(DelayedSystem.Data.class, DelayedSystem.Data::new);
   private final float intervalSec;

   public DelayedSystem(float intervalSec) {
      this.intervalSec = intervalSec;
   }

   @Nonnull
   public ResourceType<ECS_TYPE, DelayedSystem.Data<ECS_TYPE>> getResourceType() {
      return this.resourceType;
   }

   public float getIntervalSec() {
      return this.intervalSec;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<ECS_TYPE> store) {
      DelayedSystem.Data<ECS_TYPE> data = store.getResource(this.resourceType);
      data.dt += dt;
      if (data.dt >= this.intervalSec) {
         float fullDeltaTime = data.dt;
         data.dt = 0.0F;
         this.delayedTick(fullDeltaTime, systemIndex, store);
      }
   }

   public abstract void delayedTick(float var1, int var2, @Nonnull Store<ECS_TYPE> var3);

   private static class Data<ECS_TYPE> implements Resource<ECS_TYPE> {
      private float dt;

      @Nonnull
      @Override
      public Resource<ECS_TYPE> clone() {
         DelayedSystem.Data<ECS_TYPE> data = new DelayedSystem.Data<>();
         data.dt = this.dt;
         return data;
      }
   }
}
