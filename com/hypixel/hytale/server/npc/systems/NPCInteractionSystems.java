package com.hypixel.hytale.server.npc.systems;

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
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.interactions.NPCInteractionSimulationHandler;
import javax.annotation.Nonnull;

public class NPCInteractionSystems {
   public static class AddSimulationManagerSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public AddSimulationManagerSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponentType) {
         this.npcEntityComponentType = npcEntityComponentType;
         this.query = Query.and(npcEntityComponentType, Query.not(InteractionModule.get().getInteractionManagerComponent()));
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         NPCEntity npcComponent = holder.getComponent(this.npcEntityComponentType);

         assert npcComponent != null;

         holder.addComponent(
            InteractionModule.get().getInteractionManagerComponent(), new InteractionManager(npcComponent, null, new NPCInteractionSimulationHandler())
         );
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class TickHeldInteractionsSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, InteractionManager> interactionManagerComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public TickHeldInteractionsSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponentType) {
         this.npcComponentType = npcEntityComponentType;
         this.interactionManagerComponentType = InteractionModule.get().getInteractionManagerComponent();
         this.query = Query.and(npcEntityComponentType, this.interactionManagerComponentType);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcComponentType);

         assert npcComponent != null;

         InteractionManager interactionManager = archetypeChunk.getComponent(index, this.interactionManagerComponentType);

         assert interactionManager != null;

         Inventory inventory = npcComponent.getInventory();
         ItemContainer armorInventory = inventory.getArmor();
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         interactionManager.tryRunHeldInteraction(ref, commandBuffer, InteractionType.Held);
         interactionManager.tryRunHeldInteraction(ref, commandBuffer, InteractionType.HeldOffhand);

         for (short i = 0; i < armorInventory.getCapacity(); i++) {
            interactionManager.tryRunHeldInteraction(ref, commandBuffer, InteractionType.Equipped, i);
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }
}
