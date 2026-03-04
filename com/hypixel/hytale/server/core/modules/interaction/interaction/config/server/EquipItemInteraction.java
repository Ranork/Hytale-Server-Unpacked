package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EquipItemInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<EquipItemInteraction> CODEC = BuilderCodec.builder(
         EquipItemInteraction.class, EquipItemInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Equips the item being held.")
      .build();

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      if (EntityUtils.getEntity(ref, commandBuffer) instanceof LivingEntity livingEntity) {
         Inventory var15 = livingEntity.getInventory();
         byte activeSlot = context.getHeldItemSlot();
         ItemStack itemInHand = context.getHeldItem();
         if (itemInHand != null) {
            Item item = itemInHand.getItem();
            if (item != null) {
               ItemArmor armor = item.getArmor();
               if (armor != null) {
                  short slotId = (short)armor.getArmorSlot().ordinal();
                  ItemContainer armorContainer = var15.getArmor();
                  if (slotId <= armorContainer.getCapacity()) {
                     MoveTransaction<ItemStackTransaction> stackTransaction = context.getHeldItemContainer()
                        .moveItemStackFromSlot(activeSlot, itemInHand.getQuantity(), armorContainer);
                     if (!stackTransaction.succeeded()) {
                        context.getState().state = InteractionState.Failed;
                     }
                  }
               }
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "EquipItemInteraction{} " + super.toString();
   }
}
