package com.hypixel.hytale.builtin.buildertools.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PickupItemInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<PickupItemInteraction> CODEC = BuilderCodec.builder(
         PickupItemInteraction.class, PickupItemInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Picks up an item entity and adds it to the player's inventory.")
      .build();
   public static final String DEFAULT_ID = "*PickupItem";
   public static final RootInteraction DEFAULT_ROOT = new RootInteraction("*PickupItem", "*PickupItem");

   public PickupItemInteraction(String id) {
      super(id);
   }

   protected PickupItemInteraction() {
   }

   @Override
   protected final void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         Ref<EntityStore> targetRef = context.getTargetEntity();
         if (targetRef != null && targetRef.isValid()) {
            ItemComponent itemComponent = commandBuffer.getComponent(targetRef, ItemComponent.getComponentType());
            if (itemComponent == null) {
               context.getState().state = InteractionState.Failed;
            } else {
               TransformComponent transformComponent = commandBuffer.getComponent(targetRef, TransformComponent.getComponentType());
               if (transformComponent == null) {
                  context.getState().state = InteractionState.Failed;
               } else {
                  ItemStack itemStack = itemComponent.getItemStack();
                  if (!ItemStack.isEmpty(itemStack)) {
                     Vector3d itemEntityPosition = transformComponent.getPosition();
                     ItemStackTransaction transaction = playerComponent.giveItem(itemStack, ref, commandBuffer);
                     ItemStack remainder = transaction.getRemainder();
                     if (ItemStack.isEmpty(remainder)) {
                        itemComponent.setRemovedByPlayerPickup(true);
                        commandBuffer.removeEntity(targetRef, RemoveReason.REMOVE);
                        playerComponent.notifyPickupItem(ref, itemStack, itemEntityPosition, commandBuffer);
                        Holder<EntityStore> pickupItemHolder = ItemComponent.generatePickedUpItem(targetRef, commandBuffer, ref, itemEntityPosition);
                        if (pickupItemHolder != null) {
                           commandBuffer.addEntity(pickupItemHolder, AddReason.SPAWN);
                        }
                     } else if (!remainder.equals(itemStack)) {
                        int quantity = itemStack.getQuantity() - remainder.getQuantity();
                        itemComponent.setItemStack(remainder);
                        Holder<EntityStore> pickupItemHolder = ItemComponent.generatePickedUpItem(targetRef, commandBuffer, ref, itemEntityPosition);
                        if (pickupItemHolder != null) {
                           commandBuffer.addEntity(pickupItemHolder, AddReason.SPAWN);
                        }

                        if (quantity > 0) {
                           playerComponent.notifyPickupItem(ref, itemStack.withQuantity(quantity), itemEntityPosition, commandBuffer);
                        }
                     }
                  }
               }
            }
         } else {
            context.getState().state = InteractionState.Failed;
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "PickupItemInteraction{} " + super.toString();
   }
}
