package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;
import javax.annotation.Nonnull;

public class ModifyInventoryInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<ModifyInventoryInteraction> CODEC = BuilderCodec.builder(
         ModifyInventoryInteraction.class, ModifyInventoryInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Modifies an item in the inventory.")
      .appendInherited(
         new KeyedCodec<>("RequiredGameMode", ProtocolCodecs.GAMEMODE),
         (interaction, s) -> interaction.requiredGameMode = s,
         interaction -> interaction.requiredGameMode,
         (interaction, parent) -> interaction.requiredGameMode = parent.requiredGameMode
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ItemToRemove", ItemStack.CODEC),
         (interaction, s) -> interaction.itemToRemove = s,
         interaction -> interaction.itemToRemove,
         (interaction, parent) -> interaction.itemToRemove = parent.itemToRemove
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AdjustHeldItemQuantity", Codec.INTEGER),
         (interaction, s) -> interaction.adjustHeldItemQuantity = s,
         interaction -> interaction.adjustHeldItemQuantity,
         (interaction, parent) -> interaction.adjustHeldItemQuantity = parent.adjustHeldItemQuantity
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ItemToAdd", ItemStack.CODEC),
         (interaction, s) -> interaction.itemToAdd = s,
         interaction -> interaction.itemToAdd,
         (interaction, parent) -> interaction.itemToAdd = parent.itemToAdd
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AdjustHeldItemDurability", Codec.DOUBLE),
         (interaction, s) -> interaction.adjustHeldItemDurability = s,
         interaction -> interaction.adjustHeldItemDurability,
         (interaction, parent) -> interaction.adjustHeldItemDurability = parent.adjustHeldItemDurability
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("BrokenItem", Codec.STRING),
         (interaction, s) -> interaction.brokenItem = s,
         interaction -> interaction.brokenItem,
         (interaction, parent) -> interaction.brokenItem = parent.brokenItem
      )
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("NotifyOnBreak", Codec.BOOLEAN),
         (interaction, s) -> interaction.notifyOnBreak = s,
         interaction -> interaction.notifyOnBreak,
         (interaction, parent) -> interaction.notifyOnBreak = parent.notifyOnBreak
      )
      .documentation(
         "If true, shows the 'item broken' message and plays the break sound when durability reaches 0. Defaults to true for tools (no BrokenItem or same item), false for transformations (different BrokenItem)."
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("NotifyOnBreakMessage", Codec.STRING),
         (interaction, s) -> interaction.notifyOnBreakMessage = s,
         interaction -> interaction.notifyOnBreakMessage,
         (interaction, parent) -> interaction.notifyOnBreakMessage = parent.notifyOnBreakMessage
      )
      .documentation(
         "Custom translation key for the break notification message. Supports {itemName} parameter. Defaults to 'server.general.repair.itemBroken' if not specified."
      )
      .add()
      .build();
   private GameMode requiredGameMode;
   private ItemStack itemToRemove;
   private int adjustHeldItemQuantity;
   private ItemStack itemToAdd;
   private double adjustHeldItemDurability;
   private String brokenItem;
   private Boolean notifyOnBreak;
   private String notifyOnBreakMessage;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         boolean hasRequiredGameMode = this.requiredGameMode == null || playerComponent.getGameMode() == this.requiredGameMode;
         if (hasRequiredGameMode) {
            CombinedItemContainer combinedHotbarFirst = playerComponent.getInventory().getCombinedBackpackStorageHotbarFirst();
            if (this.itemToRemove != null) {
               ItemStackTransaction removeItemStack = combinedHotbarFirst.removeItemStack(this.itemToRemove, true, true);
               if (!removeItemStack.succeeded()) {
                  context.getState().state = InteractionState.Failed;
                  return;
               }
            }

            ItemStack heldItem = context.getHeldItem();
            if (heldItem != null && this.adjustHeldItemQuantity != 0) {
               if (this.adjustHeldItemQuantity < 0) {
                  ItemStackSlotTransaction slotTransaction = context.getHeldItemContainer()
                     .removeItemStackFromSlot(context.getHeldItemSlot(), heldItem, -this.adjustHeldItemQuantity);
                  if (!slotTransaction.succeeded()) {
                     context.getState().state = InteractionState.Failed;
                     return;
                  }

                  context.setHeldItem(slotTransaction.getSlotAfter());
               } else {
                  SimpleItemContainer.addOrDropItemStack(commandBuffer, ref, combinedHotbarFirst, heldItem.withQuantity(this.adjustHeldItemQuantity));
               }
            }

            if (this.itemToAdd != null) {
               SimpleItemContainer.addOrDropItemStack(commandBuffer, ref, combinedHotbarFirst, this.itemToAdd);
            }

            if (this.adjustHeldItemDurability != 0.0) {
               ItemStack item = context.getHeldItem();
               if (item != null) {
                  ItemStack newItem = item.withIncreasedDurability(this.adjustHeldItemDurability);
                  boolean justBroke = newItem.isBroken() && !item.isBroken();
                  if (newItem.isBroken() && this.brokenItem != null) {
                     if (this.brokenItem.equals("Empty")) {
                        newItem = null;
                     } else if (!this.brokenItem.equals(item.getItemId())) {
                        newItem = new ItemStack(this.brokenItem, 1);
                     }
                  }

                  boolean isTransformation = this.brokenItem != null && !this.brokenItem.equals(item.getItemId());
                  boolean shouldNotify = this.notifyOnBreak != null ? this.notifyOnBreak : !isTransformation;
                  if (justBroke && shouldNotify) {
                     Message itemNameMessage = Message.translation(item.getItem().getTranslationKey());
                     String messageKey = this.notifyOnBreakMessage != null ? this.notifyOnBreakMessage : "server.general.repair.itemBroken";
                     playerComponent.sendMessage(Message.translation(messageKey).param("itemName", itemNameMessage).color("#ff5555"));
                     PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
                     if (playerRefComponent != null) {
                        int soundEventIndex = TempAssetIdUtil.getSoundEventIndex("SFX_Item_Break");
                        SoundUtil.playSoundEvent2dToPlayer(playerRefComponent, soundEventIndex, SoundCategory.SFX);
                     }
                  }

                  ItemStackSlotTransaction slotTransaction = context.getHeldItemContainer().setItemStackForSlot(context.getHeldItemSlot(), newItem);
                  if (!slotTransaction.succeeded()) {
                     context.getState().state = InteractionState.Failed;
                  } else {
                     context.setHeldItem(newItem);
                  }
               }
            }
         }
      }
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ModifyInventoryInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ModifyInventoryInteraction p = (com.hypixel.hytale.protocol.ModifyInventoryInteraction)packet;
      if (this.itemToRemove != null) {
         p.itemToRemove = this.itemToRemove.toPacket();
      }

      p.adjustHeldItemQuantity = this.adjustHeldItemQuantity;
      if (this.itemToAdd != null) {
         p.itemToAdd = this.itemToAdd.toPacket();
      }

      if (this.brokenItem != null) {
         p.brokenItem = this.brokenItem.toString();
      }

      p.adjustHeldItemDurability = this.adjustHeldItemDurability;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ModifyInventoryInteraction{requiredGameMode="
         + this.requiredGameMode
         + ", itemToRemove="
         + this.itemToRemove
         + ", adjustHeldItemQuantity="
         + this.adjustHeldItemQuantity
         + ", itemToAdd="
         + this.itemToAdd
         + ", adjustHeldItemDurability="
         + this.adjustHeldItemDurability
         + ", brokenItem="
         + this.brokenItem
         + ", notifyOnBreak="
         + this.notifyOnBreak
         + ", notifyOnBreakMessage='"
         + this.notifyOnBreakMessage
         + "'} "
         + super.toString();
   }
}
