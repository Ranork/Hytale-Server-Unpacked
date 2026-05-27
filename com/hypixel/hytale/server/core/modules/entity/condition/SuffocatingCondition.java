package com.hypixel.hytale.server.core.modules.entity.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.BreathingComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class SuffocatingCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<SuffocatingCondition> CODEC = BuilderCodec.builder(
         SuffocatingCondition.class, SuffocatingCondition::new, Condition.BASE_CODEC
      )
      .build();

   protected SuffocatingCondition() {
   }

   public SuffocatingCondition(boolean inverse) {
      super(inverse);
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      BreathingComponent breathingComponent = componentAccessor.getComponent(ref, BreathingComponent.getComponentType());
      return breathingComponent == null ? false : breathingComponent.isSuffocating();
   }

   @Nonnull
   @Override
   public String toString() {
      return "SuffocatingCondition{} " + super.toString();
   }
}
