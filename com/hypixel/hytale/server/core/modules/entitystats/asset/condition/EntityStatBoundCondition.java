package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public abstract class EntityStatBoundCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<EntityStatBoundCondition> CODEC = BuilderCodec.abstractBuilder(EntityStatBoundCondition.class, Condition.BASE_CODEC)
      .append(new KeyedCodec<>("Stat", Codec.STRING), (condition, value) -> condition.unknownStat = value, condition -> condition.unknownStat)
      .documentation("The stat to evaluate the condition against.")
      .addValidator(Validators.nonNull())
      .addValidatorLate(() -> EntityStatType.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   protected String unknownStat;
   protected int stat = Integer.MIN_VALUE;

   protected EntityStatBoundCondition() {
   }

   public EntityStatBoundCondition(boolean inverse, int stat) {
      super(inverse);
      this.stat = stat;
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      if (this.stat == Integer.MIN_VALUE) {
         this.stat = EntityStatType.getAssetMap().getIndex(this.unknownStat);
      }

      EntityStatMap entityStatMapComponent = componentAccessor.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
      if (entityStatMapComponent == null) {
         return false;
      } else {
         EntityStatValue statValue = entityStatMapComponent.get(this.stat);
         return statValue == null ? false : this.eval0(ref, currentTime, statValue);
      }
   }

   public abstract boolean eval0(@Nonnull Ref<EntityStore> var1, @Nonnull Instant var2, @Nonnull EntityStatValue var3);

   @Nonnull
   @Override
   public String toString() {
      return "EntityStatBoundCondition{unknownStat='" + this.unknownStat + "', stat=" + this.stat + "} " + super.toString();
   }
}
