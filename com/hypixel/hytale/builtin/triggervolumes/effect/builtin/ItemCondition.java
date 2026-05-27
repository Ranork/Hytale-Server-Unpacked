package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ActiveSlotInventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemCondition extends TriggerCondition {
   @Nonnull
   public static final BuilderCodec<ItemCondition> CODEC = BuilderCodec.builder(ItemCondition.class, ItemCondition::new, BASE_CODEC)
      .append(new KeyedCodec<>("Item", Codec.STRING), (condition, itemId) -> condition.itemId = itemId, condition -> condition.itemId)
      .add()
      .append(
         new KeyedCodec<>("Location", new EnumCodec<>(ItemCondition.Location.class), false),
         (condition, location) -> condition.location = location,
         condition -> condition.location
      )
      .add()
      .append(new KeyedCodec<>("Quantity", Codec.INTEGER, false), (condition, quantity) -> condition.quantity = quantity, condition -> condition.quantity)
      .add()
      .append(new KeyedCodec<>("Consume", Codec.BOOLEAN, false), (condition, consume) -> condition.consume = consume, condition -> condition.consume)
      .add()
      .build();
   @Nullable
   private String itemId;
   @Nonnull
   private ItemCondition.Location location = ItemCondition.Location.IN_HAND;
   private int quantity = 1;
   private boolean consume;

   @Nonnull
   public static ItemCondition create(@Nonnull TriggerEventType eventType, @Nonnull String itemId, @Nonnull ItemCondition.Location location, int quantity) {
      ItemCondition condition = new ItemCondition();
      condition.setEventType(eventType);
      condition.itemId = itemId;
      condition.location = location;
      condition.quantity = quantity;
      return condition;
   }

   @Override
   public boolean test(@Nonnull TriggerContext context) {
      if (this.itemId != null && !this.itemId.isBlank()) {
         if (context.getStore().getComponent(context.getEntityRef(), Player.getComponentType()) == null) {
            return false;
         } else {
            int requiredQuantity = Math.max(0, this.quantity);
            if (requiredQuantity == 0) {
               return true;
            } else {
               return switch (this.location != null ? this.location : ItemCondition.Location.IN_HAND) {
                  case IN_HAND -> this.countStack(InventoryComponent.getItemInHand(context.getStore(), context.getEntityRef())) >= requiredQuantity;
                  case HOTBAR -> {
                     InventoryComponent.Hotbar hotbar = context.getStore().getComponent(context.getEntityRef(), InventoryComponent.Hotbar.getComponentType());
                     yield hotbar != null && this.countStacks(hotbar.getInventory()) >= requiredQuantity;
                  }
                  case INVENTORY -> this.countStacks(InventoryComponent.getCombined(context.getStore(), context.getEntityRef(), InventoryComponent.EVERYTHING))
                     >= requiredQuantity;
               };
            }
         }
      } else {
         return true;
      }
   }

   @Override
   public void applyOnAccept(@Nonnull TriggerContext context) {
      if (this.consume && this.itemId != null && !this.itemId.isBlank()) {
         int requiredQuantity = Math.max(0, this.quantity);
         if (requiredQuantity != 0) {
            this.consumeMatchedItems(context, requiredQuantity);
         }
      }
   }

   private void consumeMatchedItems(@Nonnull TriggerContext context, int requiredQuantity) {
      switch (this.location != null ? this.location : ItemCondition.Location.IN_HAND) {
         case IN_HAND:
            this.consumeFromActiveInventory(context, requiredQuantity);
            break;
         case HOTBAR:
            InventoryComponent.Hotbar hotbar = context.getStore().getComponent(context.getEntityRef(), InventoryComponent.Hotbar.getComponentType());
            if (hotbar != null) {
               hotbar.getInventory().removeItemStack(new ItemStack(this.itemId, requiredQuantity));
            }
            break;
         case INVENTORY:
            InventoryComponent.getCombined(context.getStore(), context.getEntityRef(), InventoryComponent.EVERYTHING)
               .removeItemStack(new ItemStack(this.itemId, requiredQuantity));
      }
   }

   private void consumeFromActiveInventory(@Nonnull TriggerContext context, int requiredQuantity) {
      InventoryComponent.Tool toolComponent = context.getStore().getComponent(context.getEntityRef(), InventoryComponent.Tool.getComponentType());
      if (toolComponent == null || !toolComponent.isUsingToolsItem() || !this.consumeFromActiveSlot(toolComponent, requiredQuantity)) {
         InventoryComponent.Hotbar hotbarComponent = context.getStore().getComponent(context.getEntityRef(), InventoryComponent.Hotbar.getComponentType());
         if (hotbarComponent != null) {
            this.consumeFromActiveSlot(hotbarComponent, requiredQuantity);
         }
      }
   }

   private boolean consumeFromActiveSlot(@Nonnull ActiveSlotInventoryComponent inventoryComponent, int requiredQuantity) {
      byte activeSlot = inventoryComponent.getActiveSlot();
      if (activeSlot == -1) {
         return false;
      } else {
         ItemStack activeItem = inventoryComponent.getInventory().getItemStack(activeSlot);
         if (this.countStack(activeItem) < requiredQuantity) {
            return false;
         } else {
            inventoryComponent.getInventory().removeItemStackFromSlot(activeSlot, activeItem, requiredQuantity);
            return true;
         }
      }
   }

   private int countStacks(@Nonnull ItemContainer container) {
      return container.countItemStacks(stack -> this.itemId.equals(stack.getItemId()));
   }

   private int countStack(@Nullable ItemStack stack) {
      return !ItemStack.isEmpty(stack) && this.itemId.equals(stack.getItemId()) ? stack.getQuantity() : 0;
   }

   public static enum Location {
      IN_HAND,
      HOTBAR,
      INVENTORY;
   }
}
