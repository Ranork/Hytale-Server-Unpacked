package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class SprintingCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<SprintingCondition> CODEC = BuilderCodec.builder(SprintingCondition.class, SprintingCondition::new, Condition.BASE_CODEC)
      .build();

   protected SprintingCondition() {
   }

   public SprintingCondition(boolean inverse) {
      super(inverse);
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      MovementStatesComponent movementStatesComponent = componentAccessor.getComponent(ref, MovementStatesComponent.getComponentType());

      assert movementStatesComponent != null;

      return movementStatesComponent.getMovementStates().sprinting;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SprintingCondition{} " + super.toString();
   }
}
