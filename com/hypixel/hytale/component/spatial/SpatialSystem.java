package com.hypixel.hytale.component.spatial;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SpatialSystem<ECS_TYPE> extends TickingSystem<ECS_TYPE> implements QuerySystem<ECS_TYPE> {
   @Nonnull
   private final ResourceType<ECS_TYPE, SpatialResource<Ref<ECS_TYPE>, ECS_TYPE>> resourceType;

   public SpatialSystem(@Nonnull ResourceType<ECS_TYPE, SpatialResource<Ref<ECS_TYPE>, ECS_TYPE>> resourceType) {
      this.resourceType = resourceType;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<ECS_TYPE> store) {
      SpatialResource<Ref<ECS_TYPE>, ECS_TYPE> spatialResource = store.getResource(this.resourceType);
      SpatialData<Ref<ECS_TYPE>> spatialData = spatialResource.getSpatialData();
      spatialData.clear();
      store.forEachChunk(systemIndex, (archetypeChunk, commandBuffer) -> {
         int size = archetypeChunk.size();
         spatialData.addCapacity(size);

         for (int index = 0; index < size; index++) {
            Vector3d position = this.getPosition(archetypeChunk, index);
            if (position != null) {
               Ref<ECS_TYPE> ref = archetypeChunk.getReferenceTo(index);
               spatialData.append(position, ref);
            }
         }
      });
      spatialResource.getSpatialStructure().rebuild(spatialData);
   }

   @Nullable
   public abstract Vector3d getPosition(@Nonnull ArchetypeChunk<ECS_TYPE> var1, int var2);
}
