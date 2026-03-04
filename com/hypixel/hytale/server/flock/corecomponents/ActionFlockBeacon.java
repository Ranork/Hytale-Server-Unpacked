package com.hypixel.hytale.server.flock.corecomponents;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderActionFlockBeacon;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionFlockBeacon extends ActionBase {
   protected static final ComponentType<EntityStore, BeaconSupport> BEACON_SUPPORT_COMPONENT_TYPE = BeaconSupport.getComponentType();
   protected static final ComponentType<EntityStore, FlockMembership> FLOCK_MEMBERSHIP_COMPONENT_TYPE = FlockMembership.getComponentType();
   protected static final ComponentType<EntityStore, EntityGroup> ENTITY_GROUP_COMPONENT_TYPE = EntityGroup.getComponentType();
   protected final String message;
   protected final double expirationTime;
   protected final boolean sendToSelf;
   protected final boolean sendToLeaderOnly;
   protected final int sendTargetSlot;

   public ActionFlockBeacon(@Nonnull BuilderActionFlockBeacon builderActionFlockBeacon, @Nonnull BuilderSupport builderSupport) {
      super(builderActionFlockBeacon);
      this.message = builderActionFlockBeacon.getMessage(builderSupport);
      this.sendTargetSlot = builderActionFlockBeacon.getSendTargetSlot(builderSupport);
      this.expirationTime = builderActionFlockBeacon.getExpirationTime();
      this.sendToSelf = builderActionFlockBeacon.isSendToSelf();
      this.sendToLeaderOnly = builderActionFlockBeacon.isSendToLeaderOnly();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return !super.canExecute(ref, role, sensorInfo, dt, store)
         ? false
         : store.getArchetype(ref).contains(FLOCK_MEMBERSHIP_COMPONENT_TYPE)
            && (this.sendTargetSlot == Integer.MIN_VALUE || role.getMarkedEntitySupport().hasMarkedEntityInSlot(this.sendTargetSlot));
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      FlockMembership flockMembership = store.getComponent(ref, FLOCK_MEMBERSHIP_COMPONENT_TYPE);
      Ref<EntityStore> flockReference = flockMembership != null ? flockMembership.getFlockRef() : null;
      if (flockReference != null && flockReference.isValid()) {
         EntityGroup entityGroup = store.getComponent(flockReference, ENTITY_GROUP_COMPONENT_TYPE);
         if (entityGroup == null) {
            return true;
         } else {
            Ref<EntityStore> targetRef = this.sendTargetSlot >= 0 ? role.getMarkedEntitySupport().getMarkedEntityRef(this.sendTargetSlot) : ref;
            if (this.sendToLeaderOnly) {
               Ref<EntityStore> leaderReference = entityGroup.getLeaderRef();
               if ((this.sendToSelf || targetRef == null || !targetRef.equals(leaderReference)) && leaderReference.isValid()) {
                  this.sendNPCMessage(leaderReference, targetRef, store);
               }
            } else {
               entityGroup.forEachMember(
                  (flockMember, entity, _target) -> this.sendNPCMessage(flockMember, _target, store), ref, targetRef, this.sendToSelf ? null : ref
               );
            }

            return true;
         }
      } else {
         return true;
      }
   }

   protected void sendNPCMessage(@Nonnull Ref<EntityStore> ref, @Nullable Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      BeaconSupport beaconSupport = componentAccessor.getComponent(ref, BEACON_SUPPORT_COMPONENT_TYPE);
      if (beaconSupport != null) {
         beaconSupport.postMessage(this.message, targetRef, this.expirationTime);
      }
   }
}
