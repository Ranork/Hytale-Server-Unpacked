package com.hypixel.hytale.builtin.adventure.objectives.blockstates;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.events.TreasureChestOpeningEvent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.BreakValidatedBlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public class TreasureChestState extends ItemContainerState implements BreakValidatedBlockState {
   @Nonnull
   public static final BuilderCodec<TreasureChestState> CODEC = BuilderCodec.builder(TreasureChestState.class, TreasureChestState::new, BlockState.BASE_CODEC)
      .append(
         new KeyedCodec<>("ObjectiveUUID", Codec.UUID_BINARY),
         (treasureChestState, uuid) -> treasureChestState.objectiveUUID = uuid,
         treasureChestState -> treasureChestState.objectiveUUID
      )
      .add()
      .append(
         new KeyedCodec<>("ChestUUID", Codec.UUID_BINARY),
         (treasureChestState, uuid) -> treasureChestState.chestUUID = uuid,
         treasureChestState -> treasureChestState.chestUUID
      )
      .add()
      .append(
         new KeyedCodec<>("Opened", Codec.BOOLEAN),
         (treasureChestState, aBoolean) -> treasureChestState.opened = aBoolean,
         treasureChestState -> treasureChestState.opened
      )
      .add()
      .build();
   protected UUID objectiveUUID;
   protected UUID chestUUID;
   protected boolean opened;

   @Override
   public boolean canOpen(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.opened) {
         UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         Objective objective = ObjectivePlugin.get().getObjectiveDataStore().getObjective(this.objectiveUUID);
         return objective != null && objective.getActivePlayerUUIDs().contains(uuidComponent.getUuid());
      } else {
         return true;
      }
   }

   @Override
   public boolean canDestroy(@Nonnull Ref<EntityStore> playerRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return this.opened;
   }

   @Override
   public void onOpen(@Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      IEventDispatcher<TreasureChestOpeningEvent, TreasureChestOpeningEvent> dispatcher = HytaleServer.get()
         .getEventBus()
         .dispatchFor(TreasureChestOpeningEvent.class, world.getName());
      if (dispatcher.hasListener()) {
         dispatcher.dispatch(new TreasureChestOpeningEvent(this.objectiveUUID, this.chestUUID, ref, store));
      }

      this.setOpened(true);
   }

   public void setOpened(boolean opened) {
      this.opened = opened;
   }

   public void setObjectiveData(UUID objectiveUUID, UUID chestUUID, List<ItemStack> itemStacks) {
      this.objectiveUUID = objectiveUUID;
      this.chestUUID = chestUUID;
      this.itemContainer.addItemStacks(itemStacks);
   }
}
