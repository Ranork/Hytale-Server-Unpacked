package com.hypixel.hytale.builtin.crafting.system;

import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerCraftingSystems {
   public static class CraftingHolderSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, CraftingManager> craftingManagerComponentType;

      public CraftingHolderSystem(
         @Nonnull ComponentType<EntityStore, Player> playerComponentType, @Nonnull ComponentType<EntityStore, CraftingManager> craftingManagerComponentType
      ) {
         this.playerComponentType = playerComponentType;
         this.craftingManagerComponentType = craftingManagerComponentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.craftingManagerComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         World world = store.getExternalData().getWorld();
         if (world.getWorldConfig().isSavingPlayers()) {
            Player playerComponent = holder.getComponent(this.playerComponentType);

            assert playerComponent != null;

            playerComponent.saveConfig(world, holder);
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.playerComponentType;
      }
   }

   public static class CraftingRefSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, CraftingManager> craftingManagerComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public CraftingRefSystem(
         @Nonnull ComponentType<EntityStore, Player> playerComponentType, @Nonnull ComponentType<EntityStore, CraftingManager> craftingManagerComponentType
      ) {
         this.craftingManagerComponentType = craftingManagerComponentType;
         this.query = Query.and(playerComponentType, craftingManagerComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         CraftingManager craftingManagerComponent = commandBuffer.getComponent(ref, this.craftingManagerComponentType);

         assert craftingManagerComponent != null;

         craftingManagerComponent.cancelAllCrafting(ref, store);
      }
   }

   public static class CraftingTickingSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, CraftingManager> craftingManagerComponentType;

      public CraftingTickingSystem(@Nonnull ComponentType<EntityStore, CraftingManager> craftingManagerComponentType) {
         this.craftingManagerComponentType = craftingManagerComponentType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.craftingManagerComponentType;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         CraftingManager craftingManagerComponent = archetypeChunk.getComponent(index, this.craftingManagerComponentType);

         assert craftingManagerComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         craftingManagerComponent.tick(ref, commandBuffer, dt);
      }
   }
}
