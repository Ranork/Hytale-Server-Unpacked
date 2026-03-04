package com.hypixel.hytale.builtin.adventure.objectives.completion;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ClearObjectiveItemsCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.interactions.StartObjectiveInteraction;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ClearObjectiveItemsCompletion extends ObjectiveCompletion {
   public ClearObjectiveItemsCompletion(@Nonnull ObjectiveCompletionAsset asset) {
      super(asset);
   }

   @Nonnull
   public ClearObjectiveItemsCompletionAsset getAsset() {
      return (ClearObjectiveItemsCompletionAsset)super.getAsset();
   }

   @Override
   public void handle(@Nonnull Objective objective, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      objective.forEachParticipant((participantReference, objectiveUuid) -> {
         if (EntityUtils.getEntity(participantReference, componentAccessor) instanceof LivingEntity livingEntity) {
            CombinedItemContainer inventory = livingEntity.getInventory().getCombinedHotbarFirst();

            for (short i = 0; i < inventory.getCapacity(); i++) {
               ItemStack itemStack = inventory.getItemStack(i);
               if (itemStack != null) {
                  UUID savedObjectiveUuid = itemStack.getFromMetadataOrNull(StartObjectiveInteraction.OBJECTIVE_UUID);
                  if (objectiveUuid.equals(savedObjectiveUuid)) {
                     inventory.removeItemStackFromSlot(i);
                  }
               }
            }
         }
      }, objective.getObjectiveUUID());
   }

   @Nonnull
   @Override
   public String toString() {
      return "ClearObjectiveItemsCompletion{} " + super.toString();
   }
}
