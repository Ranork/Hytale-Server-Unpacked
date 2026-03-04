package com.hypixel.hytale.builtin.adventure.objectives.systems;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.interactions.StartObjectiveInteraction;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ObjectiveItemEntityRemovalSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private static final ComponentType<EntityStore, ItemComponent> COMPONENT_TYPE = ItemComponent.getComponentType();

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return COMPONENT_TYPE;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      if (reason == RemoveReason.REMOVE) {
         ItemComponent itemComponent = holder.getComponent(COMPONENT_TYPE);

         assert itemComponent != null;

         ItemStack itemStack = itemComponent.getItemStack();
         if (itemStack != null) {
            UUID objectiveUUID = itemStack.getFromMetadataOrNull(StartObjectiveInteraction.OBJECTIVE_UUID);
            if (objectiveUUID != null) {
               if (!itemComponent.isRemovedByPlayerPickup()) {
                  ObjectivePlugin.get().cancelObjective(objectiveUUID, store);
               }
            }
         }
      }
   }
}
