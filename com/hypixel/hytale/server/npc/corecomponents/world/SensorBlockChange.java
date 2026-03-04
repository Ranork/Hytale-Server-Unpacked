package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.view.event.block.BlockEventType;
import com.hypixel.hytale.server.npc.components.messaging.NPCBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerBlockEventSupport;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorBlockChange;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorBlockChange extends SensorEvent {
   public SensorBlockChange(@Nonnull BuilderSensorBlockChange builder, @Nonnull BuilderSupport support) {
      super(builder, support);
      BlockEventType type = builder.getEventType(support);
      int blockSet = builder.getBlockSet(support);
      switch (this.searchType) {
         case PlayerFirst:
         case NpcFirst:
            this.playerEventMessageSlot = support.getBlockEventSlot(type, blockSet, this.range, true);
            this.npcEventMessageSlot = support.getBlockEventSlot(type, blockSet, this.range, false);
            break;
         case PlayerOnly:
            this.playerEventMessageSlot = support.getBlockEventSlot(type, blockSet, this.range, true);
            this.npcEventMessageSlot = -1;
            break;
         case NpcOnly:
            this.playerEventMessageSlot = -1;
            this.npcEventMessageSlot = support.getBlockEventSlot(type, blockSet, this.range, false);
            break;
         default:
            this.playerEventMessageSlot = -1;
            this.npcEventMessageSlot = -1;
      }
   }

   @Nullable
   @Override
   protected Ref<EntityStore> getPlayerTarget(@Nonnull Ref<EntityStore> parent, @Nonnull Store<EntityStore> store) {
      PlayerBlockEventSupport blockEventSupportComponent = store.getComponent(parent, PlayerBlockEventSupport.getComponentType());

      assert blockEventSupportComponent != null;

      TransformComponent transformComponent = store.getComponent(parent, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      return !blockEventSupportComponent.hasMatchingMessage(this.playerEventMessageSlot, position, this.range)
         ? null
         : blockEventSupportComponent.pollMessage(this.playerEventMessageSlot);
   }

   @Nullable
   @Override
   protected Ref<EntityStore> getNpcTarget(@Nonnull Ref<EntityStore> parent, @Nonnull Store<EntityStore> store) {
      NPCBlockEventSupport blockEventSupportComponent = store.getComponent(parent, NPCBlockEventSupport.getComponentType());

      assert blockEventSupportComponent != null;

      TransformComponent transformComponent = store.getComponent(parent, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      return !blockEventSupportComponent.hasMatchingMessage(this.npcEventMessageSlot, position, this.range)
         ? null
         : blockEventSupportComponent.pollMessage(this.npcEventMessageSlot);
   }
}
