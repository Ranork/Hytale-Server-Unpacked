package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.TimeUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nonnull;

public class ChargingCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<ChargingCondition> CODEC = BuilderCodec.builder(ChargingCondition.class, ChargingCondition::new, Condition.BASE_CODEC)
      .append(new KeyedCodec<>("Delay", Codec.DURATION_SECONDS), (condition, value) -> condition.delay = value, condition -> condition.delay)
      .documentation("The delay duration within which a recent charge is considered valid.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected Duration delay = Duration.ZERO;

   protected ChargingCondition() {
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      InteractionManager interactionManager = componentAccessor.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
      Boolean result = interactionManager.forEachInteraction(
         (chain, interaction, val) -> val ? Boolean.TRUE : interaction instanceof ChargingInteraction, Boolean.FALSE
      );
      if (result) {
         return true;
      } else {
         DamageDataComponent damageDataComponent = componentAccessor.getComponent(ref, DamageDataComponent.getComponentType());
         Instant timeInstant = damageDataComponent.getLastChargeTime();
         return timeInstant != null && TimeUtil.compareDifference(timeInstant, currentTime, this.delay) <= 0;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChargingCondition{delay=" + this.delay + "} " + super.toString();
   }
}
