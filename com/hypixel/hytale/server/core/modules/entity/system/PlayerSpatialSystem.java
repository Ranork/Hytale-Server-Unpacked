package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerSpatialSystem extends SpatialSystem<EntityStore> {
   @Nonnull
   public static final Query<EntityStore> QUERY = Archetype.of(Player.getComponentType(), TransformComponent.getComponentType());

   public PlayerSpatialSystem(@Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> spatialResource) {
      super(spatialResource);
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
      TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

      assert transformComponent != null;

      return transformComponent.getPosition();
   }
}
