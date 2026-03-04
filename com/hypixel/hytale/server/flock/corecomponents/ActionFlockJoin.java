package com.hypixel.hytale.server.flock.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.flock.FlockMembershipSystems;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderActionFlockJoin;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionFlockJoin extends ActionBase {
   protected final boolean forceJoin;

   public ActionFlockJoin(@Nonnull BuilderActionFlockJoin builderActionFlockJoin) {
      super(builderActionFlockJoin);
      this.forceJoin = builderActionFlockJoin.isForceJoin();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && sensorInfo != null && sensorInfo.hasPosition();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      Ref<EntityStore> target = sensorInfo != null && sensorInfo.hasPosition() ? sensorInfo.getPositionProvider().getTarget() : null;
      if (target == null) {
         return false;
      } else {
         FlockMembership targetMembership = target.getStore().getComponent(target, FlockMembership.getComponentType());
         Ref<EntityStore> targetFlockReference = targetMembership != null ? targetMembership.getFlockRef() : null;
         Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(ref, store);
         if (flockReference != null && targetFlockReference != null) {
            return true;
         } else {
            if (flockReference != null) {
               FlockMembershipSystems.join(target, flockReference, store);
            } else if (targetFlockReference != null) {
               FlockMembershipSystems.join(ref, targetFlockReference, store);
            } else {
               flockReference = FlockPlugin.createFlock(store, role);
               if (role.isCanLeadFlock()) {
                  FlockMembershipSystems.join(ref, flockReference, store);
                  FlockMembershipSystems.join(target, flockReference, store);
               } else {
                  FlockMembershipSystems.join(target, flockReference, store);
                  FlockMembershipSystems.join(ref, flockReference, store);
               }
            }

            return true;
         }
      }
   }
}
