package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.UseBlockObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.RegistrationTransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityUseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class UseBlockObjectiveTask extends CountObjectiveTask {
   @Nonnull
   public static final BuilderCodec<UseBlockObjectiveTask> CODEC = BuilderCodec.builder(
         UseBlockObjectiveTask.class, UseBlockObjectiveTask::new, CountObjectiveTask.CODEC
      )
      .build();

   public UseBlockObjectiveTask(@Nonnull UseBlockObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected UseBlockObjectiveTask() {
   }

   @Nonnull
   public UseBlockObjectiveTaskAsset getAsset() {
      return (UseBlockObjectiveTaskAsset)super.getAsset();
   }

   @Nonnull
   @Override
   protected TransactionRecord[] setup0(@Nonnull Objective objective, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      this.eventRegistry.register(LivingEntityUseBlockEvent.class, world.getName(), event -> {
         BlockType blockType = BlockType.getAssetMap().getAsset(event.getBlockType());
         if (blockType != null) {
            String baseItem = blockType.getItem().getId();
            if (this.getAsset().getBlockTagOrItemIdField().isBlockTypeIncluded(baseItem)) {
               Ref<EntityStore> entityRef = event.getRef();
               Store<EntityStore> entityStore = entityRef.getStore();
               Player playerComponent = entityStore.getComponent(entityRef, Player.getComponentType());
               if (playerComponent != null) {
                  UUIDComponent uuidComponent = store.getComponent(entityRef, UUIDComponent.getComponentType());

                  assert uuidComponent != null;

                  if (objective.getActivePlayerUUIDs().contains(uuidComponent.getUuid())) {
                     this.increaseTaskCompletion(store, entityRef, 1, objective);
                  }
               }
            }
         }
      });
      return RegistrationTransactionRecord.wrap(this.eventRegistry);
   }

   @Nonnull
   @Override
   public String toString() {
      return "UseBlockObjectiveTask{} " + super.toString();
   }
}
