package com.hypixel.hytale.server.core.modules.entitystats;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.Condition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.modifier.RegeneratingModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class RegeneratingValue {
   @Nonnull
   private final EntityStatType.Regenerating regenerating;
   private float remainingUntilRegen;

   public RegeneratingValue(@Nonnull EntityStatType.Regenerating regenerating) {
      this.regenerating = regenerating;
   }

   public boolean shouldRegenerate(
      @Nonnull ComponentAccessor<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Instant currentTime,
      float dt,
      @Nonnull EntityStatType.Regenerating regenerating
   ) {
      this.remainingUntilRegen -= dt;
      if (this.remainingUntilRegen < 0.0F) {
         this.remainingUntilRegen = this.remainingUntilRegen + regenerating.getInterval();
         return Condition.allConditionsMet(store, ref, currentTime, regenerating);
      } else {
         return false;
      }
   }

   public float regenerate(
      @Nonnull ComponentAccessor<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Instant currentTime,
      float dt,
      @Nonnull EntityStatValue value,
      float currentAmount
   ) {
      if (!this.shouldRegenerate(store, ref, currentTime, dt, this.regenerating)) {
         return 0.0F;
      } else {
         float toAdd = switch (this.regenerating.getRegenType()) {
            case ADDITIVE -> this.regenerating.getAmount();
            case PERCENTAGE -> this.regenerating.getAmount() * (value.getMax() - value.getMin());
         };
         if (this.regenerating.getModifiers() != null) {
            for (RegeneratingModifier modifier : this.regenerating.getModifiers()) {
               toAdd *= modifier.getModifier(store, ref, currentTime);
            }
         }

         return this.regenerating.clampAmount(toAdd, currentAmount, value);
      }
   }

   public EntityStatType.Regenerating getRegenerating() {
      return this.regenerating;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RegeneratingValue{regenerating=" + this.regenerating + ", remainingUntilRegen=" + this.remainingUntilRegen + "}";
   }
}
