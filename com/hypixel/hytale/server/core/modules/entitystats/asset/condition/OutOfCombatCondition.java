package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.TimeUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.gameplay.CombatConfig;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nonnull;

public class OutOfCombatCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<OutOfCombatCondition> CODEC = BuilderCodec.builder(
         OutOfCombatCondition.class, OutOfCombatCondition::new, Condition.BASE_CODEC
      )
      .append(new KeyedCodec<>("DelaySeconds", Codec.DURATION_SECONDS), (condition, value) -> condition.delay = value, condition -> condition.delay)
      .documentation("Delay before an entity is considered out of combat. Expressed in seconds.")
      .add()
      .build();
   protected Duration delay;

   protected OutOfCombatCondition() {
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      World world = componentAccessor.getExternalData().getWorld();
      CombatConfig combatConfig = world.getGameplayConfig().getCombatConfig();
      Duration delayToUse = this.delay != null ? this.delay : combatConfig.getOutOfCombatDelay();
      DamageDataComponent damageDataComponent = componentAccessor.getComponent(ref, DamageDataComponent.getComponentType());

      assert damageDataComponent != null;

      Instant lastCombatAction = damageDataComponent.getLastCombatAction();
      return TimeUtil.compareDifference(lastCombatAction, currentTime, delayToUse) >= 0;
   }

   @Nonnull
   @Override
   public String toString() {
      return "OutOfCombatCondition{delay=" + this.delay + "} " + super.toString();
   }
}
