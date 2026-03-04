package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemUtils {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static void interactivelyPickupItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull ItemStack itemStack, @Nullable Vector3d origin, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      LivingEntity entity = (LivingEntity)EntityUtils.getEntity(ref, componentAccessor);
      InteractivelyPickupItemEvent event = new InteractivelyPickupItemEvent(itemStack);
      componentAccessor.invoke(ref, event);
      if (event.isCancelled()) {
         dropItem(ref, itemStack, componentAccessor);
      } else {
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            Holder<EntityStore> pickupItemHolder = null;
            ItemStackTransaction transaction = playerComponent.giveItem(itemStack, ref, componentAccessor);
            ItemStack remainder = transaction.getRemainder();
            if (remainder != null && !remainder.isEmpty()) {
               int quantity = itemStack.getQuantity() - remainder.getQuantity();
               if (quantity > 0) {
                  ItemStack itemStackClone = itemStack.withQuantity(quantity);
                  playerComponent.notifyPickupItem(ref, itemStackClone, null, componentAccessor);
                  if (origin != null) {
                     pickupItemHolder = ItemComponent.generatePickedUpItem(itemStackClone, origin, componentAccessor, ref);
                  }
               }

               dropItem(ref, remainder, componentAccessor);
            } else {
               playerComponent.notifyPickupItem(ref, itemStack, null, componentAccessor);
               if (origin != null) {
                  pickupItemHolder = ItemComponent.generatePickedUpItem(itemStack, origin, componentAccessor, ref);
               }
            }

            if (pickupItemHolder != null) {
               componentAccessor.addEntity(pickupItemHolder, AddReason.SPAWN);
            }
         } else {
            SimpleItemContainer.addOrDropItemStack(componentAccessor, ref, entity.getInventory().getCombinedHotbarFirst(), itemStack);
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
            Vector3f rotation = headRotationComponent != null ? headRotationComponent.getRotation() : new Vector3f(0.0F, 0.0F, 0.0F);
            Vector3d direction = Transform.getDirection(rotation.getPitch(), rotation.getYaw());
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

            Vector3d throwPosition = transformComponent.getPosition().clone();
            throwPosition.add(0.0, eyeHeight, 0.0).add(throwDirection);
            Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(
               store,
               itemStack,
               throwPosition,
               Vector3f.ZERO,
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
}
