package com.hypixel.hytale.builtin.npccombatactionevaluator.memory;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageMemorySystems {
   public static class CollectDamage extends DamageEventSystem {
      @Nonnull
      private final ComponentType<EntityStore, DamageMemory> damageMemoryComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public CollectDamage(@Nonnull ComponentType<EntityStore, DamageMemory> damageMemoryComponentType) {
         this.damageMemoryComponentType = damageMemoryComponentType;
         this.query = damageMemoryComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Damage damage
      ) {
         DamageMemory damageMemoryComponent = archetypeChunk.getComponent(index, this.damageMemoryComponentType);

         assert damageMemoryComponent != null;

         damageMemoryComponent.addDamage(damage.getAmount());
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return DamageModule.get().getInspectDamageGroup();
      }
   }
}
