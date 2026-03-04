package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterMovementState;
import com.hypixel.hytale.server.npc.movement.MovementState;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public class EntityFilterMovementState extends EntityFilterBase {
   public static final int COST = 0;
   protected final MovementState movementState;

   public EntityFilterMovementState(@Nonnull BuilderEntityFilterMovementState builder) {
      this.movementState = builder.getMovementState();
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      return MotionController.isInMovementState(targetRef, this.movementState, store);
   }

   @Override
   public int cost() {
      return 0;
   }
}
