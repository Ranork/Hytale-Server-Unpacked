package com.hypixel.hytale.server.npc.corecomponents.items;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionWithDelay;
import com.hypixel.hytale.server.npc.corecomponents.items.builders.BuilderActionDropItem;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.AimingHelper;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import javax.annotation.Nonnull;

public class ActionDropItem extends ActionWithDelay {
   protected final String item;
   protected final String dropList;
   protected final float dropSectorStart;
   protected final float dropSectorEnd;
   protected final double minDistance;
   protected final double maxDistance;
   protected final boolean highPitch;
   protected final float[] pitch = new float[2];
   protected final Vector3d dropDirection = new Vector3d();
   protected float throwSpeed;

   public ActionDropItem(@Nonnull BuilderActionDropItem builder, @Nonnull BuilderSupport support) {
      super(builder, support);
      this.item = builder.getItem(support);
      this.dropList = builder.getDropList(support);
      double[] distance = builder.getDistance();
      this.minDistance = distance[0];
      this.maxDistance = distance[1];
      this.throwSpeed = builder.getThrowSpeed();
      double[] dropSector = builder.getDropSectorRadians();
      this.dropSectorStart = (float) (Math.PI / 180.0) * (float)dropSector[0];
      float end = (float) (Math.PI / 180.0) * (float)dropSector[1];
      if (this.dropSectorStart > end) {
         end += (float) (Math.PI * 2);
      }

      this.dropSectorEnd = end;
      this.highPitch = builder.isHighPitch();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && !this.isDelaying();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      this.prepareDelay();
      this.startDelay(role.getEntitySupport());
      ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
      float eyeHeight = modelComponent != null ? modelComponent.getModel().getEyeHeight(ref, store) : 0.0F;
      float height = -eyeHeight;
      if (this.item != null) {
         this.newDirection(ref, this.pickDistance(), height, store);
         ItemStack drop = InventoryHelper.createItem(this.item);
         if (drop != null) {
            ItemUtils.throwItem(ref, store, drop, this.dropDirection, this.throwSpeed);
         }

         return true;
      } else {
         ItemModule itemModule = ItemModule.get();
         if (itemModule.isEnabled()) {
            for (ItemStack randomItem : itemModule.getRandomItemDrops(this.dropList)) {
               this.newDirection(ref, this.pickDistance(), height, store);
               ItemUtils.throwItem(ref, store, randomItem, this.dropDirection, this.throwSpeed);
            }
         }

         return true;
      }
   }

   protected double pickDistance() {
      return RandomExtra.randomRange(this.minDistance, this.maxDistance);
   }

   protected void newDirection(@Nonnull Ref<EntityStore> ref, double distance, double height, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());
      Vector3d direction;
      if (headRotationComponent != null) {
         direction = headRotationComponent.getDirection();
      } else {
         direction = transformComponent.getRotation().toVector3d();
      }

      this.dropDirection.assign(direction);
      this.dropDirection.rotateY(RandomExtra.randomRange(this.dropSectorStart, this.dropSectorEnd));
      if (!AimingHelper.computePitch(distance, height, this.throwSpeed, 32.0, this.pitch)) {
         throw new IllegalStateException(
            String.format(
               "Error in computing pitch with distance %s, height %s, and speed %s that was not caught in validation", distance, height, this.throwSpeed
            )
         );
      } else {
         float heading = PhysicsMath.headingFromDirection(this.dropDirection.x, this.dropDirection.z);
         PhysicsMath.vectorFromAngles(heading, this.highPitch ? this.pitch[1] : this.pitch[0], this.dropDirection).normalize();
      }
   }
}
