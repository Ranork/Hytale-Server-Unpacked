package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.AndQuery;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ItemSpatialSystem extends SpatialSystem<EntityStore> {
   @Nonnull
   private static final AndQuery<EntityStore> QUERY = Query.and(
      ItemComponent.getComponentType(), TransformComponent.getComponentType(), Query.not(PreventItemMerging.getComponentType())
   );

   public ItemSpatialSystem(ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> spatialResource) {
      super(spatialResource);
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return QUERY;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      super.tick(dt, systemIndex, store);
   }

   @Nonnull
   @Override
   public Vector3d getPosition(@Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
      TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

      assert transformComponent != null;

      return transformComponent.getPosition();
   }
}
