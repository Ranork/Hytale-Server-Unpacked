package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IncreaseBackpackCapacityInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<IncreaseBackpackCapacityInteraction> CODEC = BuilderCodec.builder(
         IncreaseBackpackCapacityInteraction.class, IncreaseBackpackCapacityInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation(
         "Set the player's backpack capacity to a target value, gated on the player's current capacity matching the configured \"From\" value (so upgrades must be used in order)."
      )
      .<Short>appendInherited(new KeyedCodec<>("From", Codec.SHORT), (i, s) -> i.from = s, i -> i.from, (i, parent) -> i.from = parent.from)
      .documentation(
         "Required current backpack capacity for this upgrade to apply. Enforces strict upgrade ordering — the upgrade is rejected when the player's current capacity does not equal this value."
      )
      .addValidator(Validators.min((short)0))
      .add()
      .<Short>appendInherited(new KeyedCodec<>("Capacity", Codec.SHORT), (i, s) -> i.capacity = s, i -> i.capacity, (i, parent) -> i.capacity = parent.capacity)
      .documentation("Target backpack capacity to set when the upgrade applies. Must be greater than \"From\".")
      .addValidator(Validators.min((short)1))
      .add()
      .build();
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private short from = 0;
   private short capacity = 1;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      InventoryComponent.Backpack backpackInventoryComponent = commandBuffer.getComponent(ref, InventoryComponent.Backpack.getComponentType());
      if (backpackInventoryComponent == null) {
         LOGGER.at(Level.SEVERE).log("Entity %d has no Backpack inventory component when applying backpack upgrade", ref.getIndex());
      } else {
         PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
         ItemContainer inventory = backpackInventoryComponent.getInventory();
         short currentCapacity = inventory != null ? inventory.getCapacity() : 0;
         if (currentCapacity != this.from) {
            String errorMessageKey = "server.commands.inventory.backpack.usePreviousUpgrade";
            if (currentCapacity >= this.capacity) {
               errorMessageKey = "server.commands.inventory.backpack.upgradeNotApplied";
            }

            increaseNotApplied(context, playerRefComponent, errorMessageKey);
         } else {
            backpackInventoryComponent.resize(this.capacity, null);
            if (playerRefComponent != null) {
               playerRefComponent.sendMessage(
                  Message.translation("server.commands.inventory.backpack.size").param("capacity", backpackInventoryComponent.getInventory().getCapacity())
               );
            }

            context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);
         }
      }
   }

   private static void increaseNotApplied(@Nonnull InteractionContext context, @Nullable PlayerRef playerRefComponent, String messageKey) {
      context.getState().state = InteractionState.Failed;
      if (playerRefComponent != null) {
         playerRefComponent.sendMessage(Message.translation(messageKey));
      }
   }

   @Override
   public String toString() {
      return "IncreaseBackpackCapacityInteraction{from=" + this.from + ", capacity=" + this.capacity + "}" + super.toString();
   }
}
