package com.hypixel.hytale.builtin.adventure.objectives.systems;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectiveDataStore;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.builtin.adventure.objectives.interactions.StartObjectiveInteraction;
import com.hypixel.hytale.builtin.adventure.objectives.task.InventoryChangeAware;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTask;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectiveInventoryChangeSystem extends EntityEventSystem<EntityStore, InventoryChangeEvent> {
   public ObjectiveInventoryChangeSystem() {
      super(InventoryChangeEvent.class);
   }

   public void handle(
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InventoryChangeEvent event
   ) {
      ObjectiveDataStore objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();
      if (objectiveDataStore != null) {
         Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());

         assert playerComponent != null;

         Set<UUID> activeObjectiveUUIDs = playerComponent.getPlayerConfigData().getActiveObjectiveUUIDs();
         if (!activeObjectiveUUIDs.isEmpty()) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            handleRemoveOnItemDrop(ref, playerComponent, activeObjectiveUUIDs, objectiveDataStore, store);

            for (UUID objectiveUUID : activeObjectiveUUIDs) {
               Objective objective = objectiveDataStore.getObjective(objectiveUUID);
               if (objective != null) {
                  ObjectiveTask[] currentTasks = objective.getCurrentTasks();
                  if (currentTasks != null) {
                     for (ObjectiveTask task : currentTasks) {
                        if (task instanceof InventoryChangeAware aware) {
                           aware.onInventoryChange(objective, ref, store, event);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void handleRemoveOnItemDrop(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player playerComponent,
      @Nonnull Set<UUID> activeObjectiveUUIDs,
      @Nonnull ObjectiveDataStore objectiveDataStore,
      @Nonnull Store<EntityStore> store
   ) {
      Set<UUID> inventoryItemObjectiveUUIDs = null;
      CombinedItemContainer inventory = playerComponent.getInventory().getCombinedHotbarFirst();

      for (short i = 0; i < inventory.getCapacity(); i++) {
         ItemStack itemStack = inventory.getItemStack(i);
         if (!ItemStack.isEmpty(itemStack)) {
            UUID objectiveUUID = itemStack.getFromMetadataOrNull(StartObjectiveInteraction.OBJECTIVE_UUID);
            if (objectiveUUID != null) {
               if (inventoryItemObjectiveUUIDs == null) {
                  inventoryItemObjectiveUUIDs = new HashSet<>(activeObjectiveUUIDs);
               }

               inventoryItemObjectiveUUIDs.add(objectiveUUID);
            }
         }
      }

      UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      for (UUID activeObjectiveUUID : activeObjectiveUUIDs) {
         if (inventoryItemObjectiveUUIDs == null || !inventoryItemObjectiveUUIDs.contains(activeObjectiveUUID)) {
            Objective objective = objectiveDataStore.getObjective(activeObjectiveUUID);
            if (objective != null) {
               ObjectiveAsset objectiveAsset = objective.getObjectiveAsset();
               if (objectiveAsset != null && objectiveAsset.isRemoveOnItemDrop()) {
                  ObjectivePlugin.get().removePlayerFromExistingObjective(store, uuidComponent.getUuid(), activeObjectiveUUID);
               }
            }
         }
      }
   }

   @Nullable
   @Override
   public Query<EntityStore> getQuery() {
      return Player.getComponentType();
   }
}
