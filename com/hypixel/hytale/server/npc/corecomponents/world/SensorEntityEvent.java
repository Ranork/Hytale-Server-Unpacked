package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventType;
import com.hypixel.hytale.server.npc.components.messaging.PlayerEntityEventSupport;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorEntityEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorEntityEvent extends SensorEvent {
   private final boolean flockOnly;

   public SensorEntityEvent(@Nonnull BuilderSensorEntityEvent builder, @Nonnull BuilderSupport support) {
      super(builder, support);
      this.flockOnly = builder.isFlockOnly(support);
      EntityEventType type = builder.getEventType(support);
      int npcGroup = builder.getNPCGroup(support);
      switch (this.searchType) {
         case PlayerFirst:
         case NpcFirst:
            this.playerEventMessageSlot = support.getEntityEventSlot(type, npcGroup, this.range, true);
            this.npcEventMessageSlot = support.getEntityEventSlot(type, npcGroup, this.range, false);
            break;
         case PlayerOnly:
            this.playerEventMessageSlot = support.getEntityEventSlot(type, npcGroup, this.range, true);
            this.npcEventMessageSlot = -1;
            break;
         case NpcOnly:
            this.playerEventMessageSlot = -1;
            this.npcEventMessageSlot = support.getEntityEventSlot(type, npcGroup, this.range, false);
            break;
         default:
            this.playerEventMessageSlot = -1;
            this.npcEventMessageSlot = -1;
      }
   }

   @Nullable
   @Override
   protected Ref<EntityStore> getPlayerTarget(@Nonnull Ref<EntityStore> parent, @Nonnull Store<EntityStore> store) {
      PlayerEntityEventSupport entityEventSupportComponent = store.getComponent(parent, PlayerEntityEventSupport.getComponentType());

      assert entityEventSupportComponent != null;

      TransformComponent transformComponent = store.getComponent(parent, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      return !entityEventSupportComponent.hasFlockMatchingMessage(this.playerEventMessageSlot, position, this.range, this.flockOnly)
         ? null
         : entityEventSupportComponent.pollMessage(this.playerEventMessageSlot);
   }

   @Nullable
   @Override
   protected Ref<EntityStore> getNpcTarget(@Nonnull Ref<EntityStore> parent, @Nonnull Store<EntityStore> store) {
      PlayerEntityEventSupport entityEventSupportComponent = store.getComponent(parent, PlayerEntityEventSupport.getComponentType());

      assert entityEventSupportComponent != null;

      TransformComponent transformComponent = store.getComponent(parent, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      return !entityEventSupportComponent.hasFlockMatchingMessage(this.npcEventMessageSlot, position, this.range, this.flockOnly)
         ? null
         : entityEventSupportComponent.pollMessage(this.npcEventMessageSlot);
   }
}
