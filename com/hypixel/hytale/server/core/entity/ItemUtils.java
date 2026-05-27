package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class ItemUtils {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static void interactivelyPickupItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull ItemStack itemStack, @Nullable Vector3d origin, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      InteractivelyPickupItemEvent event = new InteractivelyPickupItemEvent(itemStack);
      componentAccessor.invoke(ref, event);
      if (!event.isCancelled()) {
         itemStack = event.getItemStack();
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            Holder<EntityStore> pickupItemHolder = null;
            ItemStackTransaction transaction = Player.giveItem(itemStack, ref, componentAccessor);
            ItemStack remainder = transaction.getRemainder();
            if (remainder != null && !remainder.isEmpty()) {
               int quantity = itemStack.getQuantity() - remainder.getQuantity();
               if (quantity > 0) {
                  ItemStack itemStackClone = itemStack.withQuantity(quantity);
                  if (itemStackClone != null) {
                     Player.notifyPickupItem(ref, itemStackClone, null, componentAccessor);
                     if (origin != null) {
                        pickupItemHolder = ItemComponent.generatePickedUpItem(itemStackClone, origin, componentAccessor, ref);
                     }
                  }
               }

               dropItem(ref, remainder, componentAccessor);
            } else {
               Player.notifyPickupItem(ref, itemStack, null, componentAccessor);
               if (origin != null) {
                  pickupItemHolder = ItemComponent.generatePickedUpItem(itemStack, origin, componentAccessor, ref);
               }
            }

            if (pickupItemHolder != null) {
               componentAccessor.addEntity(pickupItemHolder, AddReason.SPAWN);
            }
         } else {
            CombinedItemContainer hotbarFirstCombinedItemContainer = InventoryComponent.getCombined(componentAccessor, ref, InventoryComponent.HOTBAR_FIRST);
            SimpleItemContainer.addOrDropItemStack(componentAccessor, ref, hotbarFirstCombinedItemContainer, itemStack);
         }
      }
   }

   @Nullable
   public static Ref<EntityStore> throwItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull ItemStack itemStack, float throwSpeed, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      DropItemEvent.Drop event = new DropItemEvent.Drop(itemStack, throwSpeed);
      componentAccessor.invoke(ref, event);
      if (event.isCancelled()) {
         return null;
      } else {
         throwSpeed = event.getThrowSpeed();
         itemStack = event.getItemStack();
         if (!itemStack.isEmpty() && itemStack.isValid()) {
            HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());
            Rotation3f rotation = headRotationComponent != null ? headRotationComponent.getRotation() : new Rotation3f(0.0F, 0.0F, 0.0F);
            Vector3d direction = Transform.getDirection(rotation.pitch(), rotation.yaw());
            return throwItem(ref, componentAccessor, itemStack, direction, throwSpeed);
         } else {
            LOGGER.at(Level.WARNING).log("Attempted to throw invalid item %s at %s by %s", itemStack, throwSpeed, ref.getIndex());
            return null;
         }
      }
   }

   @Nullable
   public static Ref<EntityStore> throwItem(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> store,
      @Nonnull ItemStack itemStack,
      @Nonnull Vector3d throwDirection,
      float throwSpeed
   ) {
      if (!ref.isValid()) {
         LOGGER.at(Level.WARNING).log("Attempted to throw item %s by invalid entity %s", itemStack, ref.getIndex());
         return null;
      } else {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent == null) {
            LOGGER.at(Level.WARNING).log("Attempted to throw item %s by entity %s without a TransformComponent", itemStack, ref.getIndex());
            return null;
         } else {
            float eyeHeight = 0.0F;
            ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
            if (modelComponent != null) {
               eyeHeight = modelComponent.getModel().getEyeHeight(ref, store);
            }

            Vector3d throwPosition = new Vector3d(transformComponent.getPosition());
            throwPosition.add(0.0, eyeHeight, 0.0).add(throwDirection);
            Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(
               store,
               itemStack,
               throwPosition,
               Rotation3f.IDENTITY,
               (float)throwDirection.x * throwSpeed,
               (float)throwDirection.y * throwSpeed,
               (float)throwDirection.z * throwSpeed
            );
            if (itemEntityHolder == null) {
               return null;
            } else {
               ItemComponent itemComponent = itemEntityHolder.getComponent(ItemComponent.getComponentType());
               if (itemComponent != null) {
                  itemComponent.setPickupDelay(1.5F);
               }

               return store.addEntity(itemEntityHolder, AddReason.SPAWN);
            }
         }
      }
   }

   @Nullable
   public static Ref<EntityStore> dropItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull ItemStack itemStack, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return throwItem(ref, itemStack, 1.0F, componentAccessor);
   }

   public static boolean canDecreaseItemStackDurability(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
      Player playerComponent = accessor.getComponent(ref, Player.getComponentType());
      return playerComponent != null ? playerComponent.getGameMode() != GameMode.Creative : false;
   }

   public static boolean canApplyItemStackPenalties(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
      Player playerComponent = accessor.getComponent(ref, Player.getComponentType());
      return playerComponent != null ? playerComponent.getGameMode() != GameMode.Creative : true;
   }

   @Nonnull
   public static ItemStackSlotTransaction updateItemStackDurability(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ItemStack itemStack,
      ItemContainer container,
      int slotId,
      double durabilityChange,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ItemStack updatedItemStack = itemStack.withIncreasedDurability(durabilityChange);
      ItemStackSlotTransaction transaction = container.replaceItemStackInSlot((short)slotId, itemStack, updatedItemStack);
      if (transaction.getSlotAfter().isBroken() && !itemStack.isBroken()) {
         PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            playerRefComponent.sendMessage(
               Message.translation("server.general.repair.itemBroken").param("itemName", itemStack.getDisplayName()).color("#ff5555")
            );
            int soundEventIndex = TempAssetIdUtil.getSoundEventIndex("SFX_Item_Break");
            SoundUtil.playSoundEvent2dToPlayer(playerRefComponent, soundEventIndex, SoundCategory.UI);
         }
      }

      return transaction;
   }

   @Nullable
   public static ItemStackSlotTransaction decreaseItemStackDurability(
      @Nonnull Ref<EntityStore> ref, @Nullable ItemStack itemStack, int inventoryId, int slotId, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!canDecreaseItemStackDurability(ref, componentAccessor)) {
         return null;
      } else if (itemStack == null || itemStack.isEmpty() || itemStack.getItem() == Item.UNKNOWN) {
         return null;
      } else if (itemStack.isBroken()) {
         return null;
      } else {
         Item item = itemStack.getItem();
         ItemContainer section = InventoryUtils.getSectionById(ref, inventoryId, componentAccessor);
         if (section == null) {
            return null;
         } else if (item.getArmor() != null) {
            ItemStackSlotTransaction transaction = updateItemStackDurability(ref, itemStack, section, slotId, -item.getDurabilityLossOnHit(), componentAccessor);
            if (transaction.getSlotAfter().isBroken()) {
               EntityStatMap entityStatMap = componentAccessor.getComponent(ref, EntityStatMap.getComponentType());
               if (entityStatMap != null) {
                  entityStatMap.getStatModifiersManager().scheduleRecalculate();
               }
            }

            return transaction;
         } else {
            return item.getWeapon() != null
               ? updateItemStackDurability(ref, itemStack, section, slotId, -item.getDurabilityLossOnHit(), componentAccessor)
               : null;
         }
      }
   }
}
