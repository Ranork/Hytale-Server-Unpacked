package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class IncreaseBackpackCapacityInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<IncreaseBackpackCapacityInteraction> CODEC = BuilderCodec.builder(
         IncreaseBackpackCapacityInteraction.class, IncreaseBackpackCapacityInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Increase the player's backpack capacity.")
      .<Short>appendInherited(new KeyedCodec<>("Capacity", Codec.SHORT), (i, s) -> i.capacity = s, i -> i.capacity, (i, parent) -> i.capacity = parent.capacity)
      .documentation("Defines the amount by which the backpack capacity needs to be increased.")
      .addValidator(Validators.min((short)1))
      .add()
      .build();
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
      if (backpackInventoryComponent != null) {
         short newBackpackCapacity = (short)(backpackInventoryComponent.getInventory().getCapacity() + this.capacity);
         backpackInventoryComponent.resize(newBackpackCapacity, null);
         Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            playerComponent.sendMessage(
               Message.translation("server.commands.inventory.backpack.size").param("capacity", backpackInventoryComponent.getInventory().getCapacity())
            );
         }

         context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);
      }
   }

   @Override
   public String toString() {
      return "IncreaseBackpackCapacityInteraction{capacity=" + this.capacity + "}" + super.toString();
   }
}
