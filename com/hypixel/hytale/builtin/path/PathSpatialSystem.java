package com.hypixel.hytale.builtin.path;

import com.hypixel.hytale.builtin.path.entities.PatrolPathMarkerEntity;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PathSpatialSystem extends SpatialSystem<EntityStore> {
   private static final Archetype<EntityStore> QUERY = Archetype.of(PatrolPathMarkerEntity.getComponentType(), TransformComponent.getComponentType());

   public PathSpatialSystem(ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> resourceType) {
      super(resourceType);
   }

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
      return archetypeChunk.getComponent(index, TransformComponent.getComponentType()).getPosition();
   }
}
