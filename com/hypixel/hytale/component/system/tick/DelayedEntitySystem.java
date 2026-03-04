package com.hypixel.hytale.component.system.tick;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import javax.annotation.Nonnull;

public abstract class DelayedEntitySystem<ECS_TYPE> extends EntityTickingSystem<ECS_TYPE> {
   private final ResourceType<ECS_TYPE, DelayedEntitySystem.Data<ECS_TYPE>> resourceType = this.registerResource(
      DelayedEntitySystem.Data.class, DelayedEntitySystem.Data::new
   );
   private final float intervalSec;

   public DelayedEntitySystem(float intervalSec) {
      this.intervalSec = intervalSec;
   }

   @Nonnull
   public ResourceType<ECS_TYPE, DelayedEntitySystem.Data<ECS_TYPE>> getResourceType() {
      return this.resourceType;
   }

   public float getIntervalSec() {
      return this.intervalSec;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<ECS_TYPE> store) {
      DelayedEntitySystem.Data<ECS_TYPE> data = store.getResource(this.resourceType);
      data.dt += dt;
      if (data.dt >= this.intervalSec) {
         float fullDt = data.dt;
         data.dt = 0.0F;
         super.tick(fullDt, systemIndex, store);
      }
   }

   private static class Data<ECS_TYPE> implements Resource<ECS_TYPE> {
      private float dt;

      @Nonnull
      @Override
      public Resource<ECS_TYPE> clone() {
         DelayedEntitySystem.Data<ECS_TYPE> data = new DelayedEntitySystem.Data<>();
         data.dt = this.dt;
         return data;
      }
   }
}
