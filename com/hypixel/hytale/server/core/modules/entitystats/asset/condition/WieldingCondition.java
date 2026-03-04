package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class WieldingCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<WieldingCondition> CODEC = BuilderCodec.builder(WieldingCondition.class, WieldingCondition::new, Condition.BASE_CODEC)
      .build();

   protected WieldingCondition() {
   }

   public WieldingCondition(boolean inverse) {
      super(inverse);
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      DamageDataComponent damageComponent = componentAccessor.getComponent(ref, DamageDataComponent.getComponentType());

      assert damageComponent != null;

      return damageComponent.getCurrentWielding() != null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WieldingCondition{} " + super.toString();
   }
}
