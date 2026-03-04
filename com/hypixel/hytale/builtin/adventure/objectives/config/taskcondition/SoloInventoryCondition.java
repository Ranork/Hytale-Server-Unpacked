package com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition;

import com.hypixel.hytale.builtin.adventure.objectives.config.task.BlockTagOrItemIdField;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoloInventoryCondition extends TaskConditionAsset {
   @Nonnull
   public static final BuilderCodec<SoloInventoryCondition> CODEC = BuilderCodec.builder(SoloInventoryCondition.class, SoloInventoryCondition::new)
      .append(
         new KeyedCodec<>("BlockTagOrItemId", BlockTagOrItemIdField.CODEC),
         (soloInventoryCondition, blockTagOrItemIdField) -> soloInventoryCondition.blockTypeOrTagTask = blockTagOrItemIdField,
         soloInventoryCondition -> soloInventoryCondition.blockTypeOrTagTask
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Integer>append(
         new KeyedCodec<>("Quantity", Codec.INTEGER),
         (soloInventoryCondition, integer) -> soloInventoryCondition.quantity = integer,
         soloInventoryCondition -> soloInventoryCondition.quantity
      )
      .addValidator(Validators.greaterThan(0))
      .add()
      .append(
         new KeyedCodec<>("ConsumeOnCompletion", Codec.BOOLEAN),
         (soloInventoryCondition, aBoolean) -> soloInventoryCondition.consumeOnCompletion = aBoolean,
         soloInventoryCondition -> soloInventoryCondition.consumeOnCompletion
      )
      .add()
      .append(
         new KeyedCodec<>("HoldInHand", Codec.BOOLEAN),
         (soloInventoryCondition, aBoolean) -> soloInventoryCondition.holdInHand = aBoolean,
         soloInventoryCondition -> soloInventoryCondition.holdInHand
      )
      .add()
      .build();
   protected BlockTagOrItemIdField blockTypeOrTagTask;
   protected int quantity = 1;
   protected boolean consumeOnCompletion;
   protected boolean holdInHand;

   public BlockTagOrItemIdField getBlockTypeOrTagTask() {
      return this.blockTypeOrTagTask;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public boolean isConsumeOnCompletion() {
      return this.consumeOnCompletion;
   }

   public boolean isHoldInHand() {
      return this.holdInHand;
   }

   @Override
   public boolean isConditionFulfilled(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, Set<UUID> objectivePlayers) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         return false;
      } else {
         Inventory inventory = playerComponent.getInventory();
         if (this.holdInHand) {
            ItemStack itemInHand = inventory.getItemInHand();
            if (itemInHand == null) {
               return false;
            } else {
               return !this.blockTypeOrTagTask.isBlockTypeIncluded(itemInHand.getItemId()) ? false : inventory.getItemInHand().getQuantity() >= this.quantity;
            }
         } else {
            return inventory.getCombinedHotbarFirst().countItemStacks(itemStack -> this.blockTypeOrTagTask.isBlockTypeIncluded(itemStack.getItemId()))
               >= this.quantity;
         }
      }
   }

   @Override
   public void consumeCondition(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, Set<UUID> objectivePlayers) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         if (this.consumeOnCompletion) {
            this.blockTypeOrTagTask.consumeItemStacks(playerComponent.getInventory().getCombinedHotbarFirst(), this.quantity);
         }
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SoloInventoryCondition that = (SoloInventoryCondition)o;
         if (this.quantity != that.quantity) {
            return false;
         } else if (this.consumeOnCompletion != that.consumeOnCompletion) {
            return false;
         } else if (this.holdInHand != that.holdInHand) {
            return false;
         } else {
            return this.blockTypeOrTagTask != null ? this.blockTypeOrTagTask.equals(that.blockTypeOrTagTask) : that.blockTypeOrTagTask == null;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.blockTypeOrTagTask != null ? this.blockTypeOrTagTask.hashCode() : 0;
      result = 31 * result + this.quantity;
      result = 31 * result + (this.consumeOnCompletion ? 1 : 0);
      return 31 * result + (this.holdInHand ? 1 : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "SoloInventoryCondition{blockTypeOrTagTask="
         + this.blockTypeOrTagTask
         + ", quantity="
         + this.quantity
         + ", consumeOnCompletion="
         + this.consumeOnCompletion
         + ", holdInHand="
         + this.holdInHand
         + "} "
         + super.toString();
   }
}
