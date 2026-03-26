package com.hypixel.hytale.server.core.modules.entity.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class IsPlayerCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<IsPlayerCondition> CODEC = BuilderCodec.builder(IsPlayerCondition.class, IsPlayerCondition::new, Condition.BASE_CODEC)
      .build();

   protected IsPlayerCondition() {
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      return componentAccessor.getComponent(ref, Player.getComponentType()) != null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "IsPlayerCondition{} " + super.toString();
   }
}
