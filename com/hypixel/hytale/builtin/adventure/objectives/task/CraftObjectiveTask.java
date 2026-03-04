package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.CraftObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.RegistrationTransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.event.events.player.PlayerCraftEvent;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import javax.annotation.Nonnull;

public class CraftObjectiveTask extends CountObjectiveTask {
   @Nonnull
   public static final BuilderCodec<CraftObjectiveTask> CODEC = BuilderCodec.builder(
         CraftObjectiveTask.class, CraftObjectiveTask::new, CountObjectiveTask.CODEC
      )
      .build();

   public CraftObjectiveTask(@Nonnull CraftObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected CraftObjectiveTask() {
   }

   @Nonnull
   public CraftObjectiveTaskAsset getAsset() {
      return (CraftObjectiveTaskAsset)super.getAsset();
   }

   @Nonnull
   @Override
   protected TransactionRecord[] setup0(@Nonnull Objective objective, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      this.eventRegistry.register(PlayerCraftEvent.class, world.getName(), event -> {
         String desiredItemId = this.getAsset().getItemId();
         CraftingRecipe recipe = event.getCraftedRecipe();
         boolean isOutput = false;

         for (MaterialQuantity materialQuantity : recipe.getOutputs()) {
            if (Objects.equals(materialQuantity.getItemId(), desiredItemId)) {
               isOutput = true;
               break;
            }
         }

         if (isOutput) {
            Ref<EntityStore> ref = event.getPlayerRef();
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            if (objective.getActivePlayerUUIDs().contains(uuidComponent.getUuid())) {
               this.increaseTaskCompletion(store, ref, event.getQuantity(), objective);
            }
         }
      });
      return RegistrationTransactionRecord.wrap(this.eventRegistry);
   }

   @Nonnull
   @Override
   public String toString() {
      return "CraftObjectiveTask{} " + super.toString();
   }
}
