package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorBeacon;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorBeacon extends SensorBase {
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final int messageIndex;
   protected final double range;
   protected final int targetSlot;
   protected final boolean consume;
   private final EntityPositionProvider positionProvider = new EntityPositionProvider();

   public SensorBeacon(@Nonnull BuilderSensorBeacon builderSensorBeacon, @Nonnull BuilderSupport builderSupport) {
      super(builderSensorBeacon);
      this.messageIndex = builderSensorBeacon.getMessageSlot(builderSupport);
      this.range = builderSensorBeacon.getRange(builderSupport);
      this.targetSlot = builderSensorBeacon.getTargetSlot(builderSupport);
      this.consume = builderSensorBeacon.isConsume();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         BeaconSupport beaconSupportComponent = store.getComponent(ref, BeaconSupport.getComponentType());
         if (beaconSupportComponent == null) {
            this.positionProvider.clear();
            return false;
         } else if (!beaconSupportComponent.isMessageQueued(this.messageIndex)) {
            this.positionProvider.clear();
            return false;
         } else {
            Ref<EntityStore> target = this.consume
               ? beaconSupportComponent.pollMessage(this.messageIndex)
               : beaconSupportComponent.peekMessage(this.messageIndex);
            if (target == null) {
               this.positionProvider.clear();
               return false;
            } else {
               Ref<EntityStore> targetRef = this.positionProvider.setTarget(target, store);
               if (targetRef != null && targetRef.isValid()) {
                  TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

                  assert targetTransformComponent != null;

                  Vector3d targetPosition = targetTransformComponent.getPosition();
                  TransformComponent transformComponent = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

                  assert transformComponent != null;

                  Vector3d position = transformComponent.getPosition();
                  if (targetPosition.distanceSquaredTo(position) > this.range * this.range) {
                     this.positionProvider.clear();
                     return false;
                  } else {
                     if (this.targetSlot >= 0) {
                        role.getMarkedEntitySupport().setMarkedEntity(this.targetSlot, targetRef);
                     }

                     if (role.getDebugSupport().isDebugFlagSet(RoleDebugFlags.BeaconMessages)) {
                        ((HytaleLogger.Api)NPCPlugin.get().getLogger().atInfo())
                           .log(
                              "ID %d received message '%s' with target ID %d",
                              ref.getIndex(),
                              beaconSupportComponent.getMessageTextForIndex(this.messageIndex),
                              target.getIndex()
                           );
                     }

                     return true;
                  }
               } else {
                  this.positionProvider.clear();
                  return false;
               }
            }
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
