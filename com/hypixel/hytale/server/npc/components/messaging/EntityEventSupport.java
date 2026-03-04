package com.hypixel.hytale.server.npc.components.messaging;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.blackboard.view.event.EntityEventNotification;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventType;
import javax.annotation.Nonnull;

public abstract class EntityEventSupport extends EventSupport<EntityEventType, EntityEventNotification> {
   public void postMessage(
      EntityEventType type, @Nonnull EntityEventNotification notification, @Nonnull Ref<EntityStore> parent, @Nonnull Store<EntityStore> store
   ) {
      EventMessage slot = this.getMessageSlot(type, notification);
      if (slot != null && slot.isEnabled()) {
         Vector3d parentEntityPosition = store.getComponent(parent, TransformComponent.getComponentType()).getPosition();
         Vector3d pos = notification.getPosition();
         double x = pos.getX();
         double y = pos.getY();
         double z = pos.getZ();
         double distanceSquared = parentEntityPosition.distanceSquaredTo(x, y, z);
         if (!(distanceSquared > slot.getMaxRangeSquared())) {
            FlockMembership flockMembership = store.getComponent(parent, FlockMembership.getComponentType());
            Ref<EntityStore> flockReference = flockMembership != null ? flockMembership.getFlockRef() : null;
            boolean isSameFlock = flockReference != null && flockReference.equals(notification.getFlockReference());
            if (!slot.isActivated() || distanceSquared < slot.getPosition().distanceSquaredTo(parentEntityPosition) || !slot.isSameFlock() && isSameFlock) {
               slot.activate(x, y, z, notification.getInitiator(), 2.0);
               slot.setSameFlock(isSameFlock);
            }
         }
      }
   }

   public boolean hasFlockMatchingMessage(int messageIndex, @Nonnull Vector3d parentPosition, double range, boolean flockOnly) {
      if (!this.isMessageQueued(messageIndex)) {
         return false;
      } else {
         EventMessage event = this.messageSlots[messageIndex];
         return flockOnly && !event.isSameFlock() ? false : event.getPosition().distanceSquaredTo(parentPosition) < range * range;
      }
   }
}
