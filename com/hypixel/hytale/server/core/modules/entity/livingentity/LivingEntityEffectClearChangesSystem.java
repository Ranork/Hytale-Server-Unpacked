package com.hypixel.hytale.server.core.modules.entity.livingentity;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public class LivingEntityEffectClearChangesSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
      new SystemDependency<>(Order.AFTER, EntityTrackerSystems.EffectControllerSystem.class)
   );

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return EffectControllerComponent.getComponentType();
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return DEPENDENCIES;
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      EffectControllerComponent effectControllerComponent = archetypeChunk.getComponent(index, EffectControllerComponent.getComponentType());

      assert effectControllerComponent != null;

      effectControllerComponent.clearChanges();
   }
}
