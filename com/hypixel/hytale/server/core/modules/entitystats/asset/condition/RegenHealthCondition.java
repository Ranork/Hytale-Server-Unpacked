package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class RegenHealthCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<RegenHealthCondition> CODEC = BuilderCodec.builder(
         RegenHealthCondition.class, RegenHealthCondition::new, Condition.BASE_CODEC
      )
      .build();

   protected RegenHealthCondition() {
   }

   public RegenHealthCondition(boolean inverse) {
      super(inverse);
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      return true;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RegenHealthCondition{} " + super.toString();
   }
}
