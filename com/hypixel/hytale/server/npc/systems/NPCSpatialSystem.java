package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class NPCSpatialSystem extends SpatialSystem<EntityStore> {
   public static final Query<EntityStore> QUERY = Archetype.of(NPCEntity.getComponentType(), TransformComponent.getComponentType());

   public NPCSpatialSystem(@Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> spatialResource) {
      super(spatialResource);
   }

   @Override
   public Query<EntityStore> getQuery() {
      return QUERY;
   }

   @Nonnull
   @Override
   public Vector3d getPosition(@Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
      return archetypeChunk.getComponent(index, TransformComponent.getComponentType()).getPosition();
   }
}
