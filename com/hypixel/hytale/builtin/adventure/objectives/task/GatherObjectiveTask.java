package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.BlockTagOrItemIdField;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.GatherObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.RegistrationTransactionRecord;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GatherObjectiveTask extends CountObjectiveTask {
   @Nonnull
   public static final BuilderCodec<GatherObjectiveTask> CODEC = BuilderCodec.builder(
         GatherObjectiveTask.class, GatherObjectiveTask::new, CountObjectiveTask.CODEC
      )
      .build();

   public GatherObjectiveTask(@Nonnull GatherObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected GatherObjectiveTask() {
   }

   @Nonnull
   public GatherObjectiveTaskAsset getAsset() {
      return (GatherObjectiveTaskAsset)super.getAsset();
   }

   @Nullable
   @Override
   protected TransactionRecord[] setup0(@Nonnull Objective objective, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Set<UUID> participatingPlayers = objective.getPlayerUUIDs();
      int countItem = this.countObjectiveItemInInventories(participatingPlayers, store);
      if (this.areTaskConditionsFulfilled(null, null, participatingPlayers)) {
         this.count = MathUtil.clamp(countItem, 0, this.getAsset().getCount());
         if (this.checkCompletion()) {
            this.consumeTaskConditions(null, null, participatingPlayers);
            this.complete = true;
            return null;
         }
      }

      this.eventRegistry.register(LivingEntityInventoryChangeEvent.class, world.getName(), event -> {
         LivingEntity livingEntity = event.getEntity();
         if (livingEntity instanceof Player) {
            Ref<EntityStore> ref = livingEntity.getReference();
            if (ref != null && ref.isValid()) {
               World refWorld = store.getExternalData().getWorld();
               refWorld.execute(() -> {
                  if (ref.isValid()) {
                     UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
                     if (uuidComponent != null) {
                        Set<UUID> activePlayerUUIDs = objective.getActivePlayerUUIDs();
                        if (activePlayerUUIDs.contains(uuidComponent.getUuid())) {
                           int count = this.countObjectiveItemInInventories(activePlayerUUIDs, store);
                           this.setTaskCompletion(store, ref, count, objective);
                        }
                     }
                  }
               });
            }
         }
      });
      return RegistrationTransactionRecord.wrap(this.eventRegistry);
   }

   private int countObjectiveItemInInventories(@Nonnull Set<UUID> participatingPlayers, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      int count = 0;
      BlockTagOrItemIdField blockTypeOrSet = this.getAsset().getBlockTagOrItemIdField();

      for (UUID playerUUID : participatingPlayers) {
         PlayerRef playerRefComponent = Universe.get().getPlayer(playerUUID);
         if (playerRefComponent != null) {
            Ref<EntityStore> playerRef = playerRefComponent.getReference();
            if (playerRef != null && playerRef.isValid()) {
               Player playerComponent = componentAccessor.getComponent(playerRef, Player.getComponentType());

               assert playerComponent != null;

               CombinedItemContainer inventory = playerComponent.getInventory().getCombinedHotbarFirst();
               count += inventory.countItemStacks(itemStack -> blockTypeOrSet.isBlockTypeIncluded(itemStack.getItemId()));
            }
         }
      }

      return count;
   }

   @Nonnull
   @Override
   public String toString() {
      return "GatherObjectiveTask{} " + super.toString();
   }
}
