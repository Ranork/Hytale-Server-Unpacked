package com.hypixel.hytale.server.npc.corecomponents.items;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.items.builders.BuilderSensorDroppedItem;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorDroppedItem extends SensorBase {
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   @Nullable
   protected final List<String> items;
   @Nonnull
   protected final EnumSet<Attitude> attitudes;
   protected final double range;
   protected final float viewCone;
   protected final boolean hasLineOfSight;
   protected float heading;
   protected final EntityPositionProvider positionProvider = new EntityPositionProvider();

   public SensorDroppedItem(@Nonnull BuilderSensorDroppedItem builder, @Nonnull BuilderSupport support) {
      super(builder);
      String[] itemArray = builder.getItems(support);
      this.items = itemArray != null ? List.of(itemArray) : null;
      this.attitudes = builder.getAttitudes(support);
      this.range = builder.getRange(support);
      this.viewCone = builder.getViewSectorRadians(support);
      this.hasLineOfSight = builder.getHasLineOfSight(support);
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getPositionCache().requireDroppedItemDistance(this.range);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         return false;
      } else {
         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         this.heading = headRotationComponent.getRotation().getYaw();
         Ref<EntityStore> droppedItem = role.getPositionCache()
            .getClosestDroppedItemInRange(
               ref,
               0.0,
               this.range,
               (sensorDroppedItem, itemRef, role1, componentAccessor) -> sensorDroppedItem.filterItem(ref, itemRef, role1, componentAccessor),
               role,
               this,
               store
            );
         if (droppedItem == null) {
            this.positionProvider.clear();
            return false;
         } else {
            this.positionProvider.setTarget(droppedItem, store);
            return true;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }

   protected boolean filterItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> itemRef, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!itemRef.isValid()) {
         return false;
      } else {
         if (this.viewCone > 0.0F) {
            TransformComponent selfTransformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

            assert selfTransformComponent != null;

            Vector3d selfPosition = selfTransformComponent.getPosition();
            TransformComponent itemTransformComponent = componentAccessor.getComponent(itemRef, TRANSFORM_COMPONENT_TYPE);

            assert itemTransformComponent != null;

            Vector3d itemPosition = itemTransformComponent.getPosition();
            if (!NPCPhysicsMath.inViewSector(selfPosition.getX(), selfPosition.getZ(), this.heading, this.viewCone, itemPosition.getX(), itemPosition.getZ())) {
               return false;
            }
         }

         if (this.hasLineOfSight && !role.getPositionCache().hasLineOfSight(ref, itemRef, componentAccessor)) {
            return false;
         } else if (this.items == null && this.attitudes.isEmpty()) {
            return true;
         } else {
            ItemComponent itemComponent = componentAccessor.getComponent(itemRef, ItemComponent.getComponentType());

            assert itemComponent != null;

            ItemStack itemStack = itemComponent.getItemStack();
            if (InventoryHelper.matchesItem(this.items, itemStack)) {
               return true;
            } else {
               Attitude attitude = role.getWorldSupport().getItemAttitude(itemStack);
               return attitude != null && this.attitudes.contains(attitude);
            }
         }
      }
   }
}
