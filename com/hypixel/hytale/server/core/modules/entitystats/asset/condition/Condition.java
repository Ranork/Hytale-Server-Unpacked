package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public abstract class Condition {
   @Nonnull
   public static final CodecMapCodec<Condition> CODEC = new CodecMapCodec<>();
   @Nonnull
   protected static final BuilderCodec<Condition> BASE_CODEC = BuilderCodec.abstractBuilder(Condition.class)
      .append(new KeyedCodec<>("Inverse", Codec.BOOLEAN), (regenerating, value) -> regenerating.inverse = value, regenerating -> regenerating.inverse)
      .documentation("Determines whether the condition is inverted.")
      .add()
      .build();
   protected boolean inverse;

   protected Condition() {
      this.inverse = false;
   }

   public Condition(boolean inverse) {
      this.inverse = inverse;
   }

   public boolean eval(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      return this.inverse != this.eval0(componentAccessor, ref, currentTime);
   }

   public abstract boolean eval0(@Nonnull ComponentAccessor<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull Instant var3);

   public static boolean allConditionsMet(
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Instant currentTime,
      @Nonnull EntityStatType.Regenerating regenerating
   ) {
      return regenerating.getConditions() == null ? true : allConditionsMet(componentAccessor, ref, currentTime, regenerating.getConditions());
   }

   public static boolean allConditionsMet(
      @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime, @Nonnull Condition[] conditions
   ) {
      boolean allMet = true;

      for (Condition condition : conditions) {
         if (!condition.eval(componentAccessor, ref, currentTime)) {
            allMet = false;
            break;
         }
      }

      return allMet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Condition{inverse=" + this.inverse + "}";
   }
}
