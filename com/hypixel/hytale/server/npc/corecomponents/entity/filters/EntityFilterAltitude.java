package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterAltitude;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFilterAltitude extends EntityFilterBase {
   public static final int COST = 0;
   @Nullable
   protected static final ComponentType<EntityStore, NPCEntity> NPC_COMPONENT_TYPE = NPCEntity.getComponentType();
   protected final double[] altitudeRange;

   public EntityFilterAltitude(@Nonnull BuilderEntityFilterAltitude builder, @Nonnull BuilderSupport support) {
      this.altitudeRange = builder.getAltitudeRange(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      NPCEntity targetNpcComponent = store.getComponent(targetRef, NPC_COMPONENT_TYPE);
      double heightOverGround;
      if (targetNpcComponent != null) {
         MotionController targetActiveMotionController = targetNpcComponent.getRole().getActiveMotionController();
         heightOverGround = targetActiveMotionController.getHeightOverGround();
      } else {
         heightOverGround = 0.0;
      }

      return heightOverGround >= this.altitudeRange[0] && heightOverGround <= this.altitudeRange[1];
   }

   @Override
   public int cost() {
      return 0;
   }
}
