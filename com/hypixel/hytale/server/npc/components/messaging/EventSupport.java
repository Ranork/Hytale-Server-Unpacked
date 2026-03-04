package com.hypixel.hytale.server.npc.components.messaging;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.view.event.EventNotification;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EventSupport<EventType extends Enum<EventType>, NotificationType extends EventNotification> extends MessageSupport {
   protected static final double EVENT_AGE = 2.0;
   protected EventMessage[] messageSlots;
   protected Map<EventType, Int2IntMap> messageIndices;

   public void postMessage(EventType type, @Nonnull NotificationType notification, @Nonnull Ref<EntityStore> parent, @Nonnull Store<EntityStore> store) {
      EventMessage slot = this.getMessageSlot(type, notification);
      if (slot != null && slot.isEnabled()) {
         Vector3d parentEntityPosition = store.getComponent(parent, TransformComponent.getComponentType()).getPosition();
         Vector3d pos = notification.getPosition();
         double x = pos.getX();
         double y = pos.getY();
         double z = pos.getZ();
         double distanceSquared = parentEntityPosition.distanceSquaredTo(x, y, z);
         if (distanceSquared <= slot.getMaxRangeSquared()
            && (!slot.isActivated() || distanceSquared < slot.getPosition().distanceSquaredTo(parentEntityPosition))) {
            slot.activate(x, y, z, notification.getInitiator(), 2.0);
         }
      }
   }

   @Nullable
   public EventMessage getMessageSlot(EventType type, @Nonnull NotificationType notification) {
      if (this.messageSlots == null) {
         return null;
      } else {
         Int2IntMap typeSlots = this.messageIndices.get(type);
         if (typeSlots == null) {
            return null;
         } else {
            int slotIdx = typeSlots.get(notification.getSet());
            return slotIdx == Integer.MIN_VALUE ? null : this.messageSlots[slotIdx];
         }
      }
   }

   public boolean hasMatchingMessage(int messageIndex, @Nonnull Vector3d parentPosition, double range) {
      if (!this.isMessageQueued(messageIndex)) {
         return false;
      } else {
         EventMessage event = this.messageSlots[messageIndex];
         return event.getPosition().distanceSquaredTo(parentPosition) < range * range;
      }
   }

   @Nullable
   public Ref<EntityStore> pollMessage(int messageIndex) {
      EventMessage event = this.messageSlots[messageIndex];
      event.deactivate();
      return event.getTarget();
   }

   public void initialise(Map<EventType, Int2IntMap> setIndices, @Nonnull Int2DoubleMap messageRanges, int count) {
      this.messageIndices = setIndices;
      EventMessage[] messages = new EventMessage[count];

      for (int i = 0; i < messages.length; i++) {
         messages[i] = new EventMessage(messageRanges.get(i));
      }

      this.messageSlots = messages;
   }

   public void cloneTo(@Nonnull EventSupport<EventType, NotificationType> other) {
      other.messageSlots = new EventMessage[this.messageSlots.length];

      for (int i = 0; i < other.messageSlots.length; i++) {
         other.messageSlots[i] = this.messageSlots[i].clone();
      }

      other.messageIndices = this.messageIndices;
   }

   @Override
   public NPCMessage[] getMessageSlots() {
      return this.messageSlots;
   }
}
