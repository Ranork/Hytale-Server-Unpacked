package com.hypixel.hytale.server.npc.corecomponents.items;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionWithDelay;
import com.hypixel.hytale.server.npc.corecomponents.items.builders.BuilderActionPickUpItem;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionPickUpItem extends ActionWithDelay {
   protected static final ComponentType<EntityStore, ItemComponent> ITEM_COMPONENT_TYPE = ItemComponent.getComponentType();
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final double range;
   protected final ActionPickUpItem.StorageTarget storageTarget;
   protected final boolean hoover;
   @Nullable
   protected final List<String> hooverItems;

   public ActionPickUpItem(@Nonnull BuilderActionPickUpItem builder, @Nonnull BuilderSupport support) {
      super(builder, support);
      this.range = builder.getRange(support);
      this.storageTarget = builder.getStorageTarget(support);
      this.hoover = builder.getHoover();
      String[] items = builder.getItems(support);
      this.hooverItems = items != null ? List.of(items) : null;
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getPositionCache().requireDroppedItemDistance(this.range);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.canExecute(ref, role, sensorInfo, dt, store)) {
         return false;
      } else {
         if (!this.hoover) {
            if (sensorInfo == null || !sensorInfo.hasPosition()) {
               return false;
            }

            Ref<EntityStore> targetRef = sensorInfo.getPositionProvider().getTarget();
            if (targetRef == null) {
               return false;
            }

            ItemComponent itemComponent = store.getComponent(targetRef, ITEM_COMPONENT_TYPE);
            if (itemComponent == null || !itemComponent.canPickUp()) {
               return false;
            }

            TransformComponent selfTransformComponent = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

            assert selfTransformComponent != null;

            Vector3d selfPosition = selfTransformComponent.getPosition();
            TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

            assert targetTransformComponent != null;

            Vector3d targetPosition = targetTransformComponent.getPosition();
            double distanceSquared = selfPosition.distanceSquaredTo(targetPosition);
            if (distanceSquared > this.range * this.range) {
               return false;
            }
         } else if (role.getPositionCache().getDroppedItemList().isEmpty()) {
            return false;
         }

         return !this.isDelaying();
      }
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      Ref<EntityStore> itemRef = null;
      if (!this.hoover) {
         if (sensorInfo != null) {
            itemRef = sensorInfo.getPositionProvider().getTarget();
         }
      } else {
         itemRef = role.getPositionCache().getClosestDroppedItemInRange(ref, 0.0, this.range, ActionPickUpItem::filterItem, role, this, store);
      }

      this.prepareDelay();
      this.startDelay(role.getEntitySupport());
      if (itemRef == null) {
         return false;
      } else {
         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         Inventory inventory = npcComponent.getInventory();
         switch (this.storageTarget) {
            case Hotbar:
               ItemComponent.addToItemContainer(store, itemRef, inventory.getCombinedHotbarFirst());
               break;
            case Inventory:
               ItemComponent.addToItemContainer(store, itemRef, inventory.getCombinedStorageFirst());
               break;
            case Destroy:
               store.removeEntity(itemRef, RemoveReason.REMOVE);
         }

         return true;
      }
   }

   protected boolean filterItem(@Nonnull Ref<EntityStore> ref, Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!ref.isValid()) {
         return false;
      } else if (this.hooverItems == null) {
         return true;
      } else {
         ItemComponent itemComponent = componentAccessor.getComponent(ref, ITEM_COMPONENT_TYPE);

         assert itemComponent != null;

         return InventoryHelper.matchesItem(this.hooverItems, itemComponent.getItemStack());
      }
   }

   public static enum StorageTarget implements Supplier<String> {
      Hotbar("Prioritise hotbar"),
      Inventory("Prioritise inventory"),
      Destroy("Destroy the item");

      private final String description;

      private StorageTarget(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
