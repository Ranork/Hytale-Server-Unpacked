package com.hypixel.hytale.server.core.entity.effect;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.ChangeStatBehaviour;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCalculatorSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DamageCalculator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DamageEffects;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActiveEntityEffect implements Damage.Source {
   @Nonnull
   public static final BuilderCodec<ActiveEntityEffect> CODEC = BuilderCodec.builder(ActiveEntityEffect.class, ActiveEntityEffect::new)
      .append(
         new KeyedCodec<>("EntityEffectId", Codec.STRING), (entityEffect, x) -> entityEffect.entityEffectId = x, entityEffect -> entityEffect.entityEffectId
      )
      .add()
      .append(
         new KeyedCodec<>("InitialDuration", Codec.FLOAT), (entityEffect, x) -> entityEffect.initialDuration = x, entityEffect -> entityEffect.initialDuration
      )
      .add()
      .append(
         new KeyedCodec<>("RemainingDuration", Codec.FLOAT),
         (entityEffect, x) -> entityEffect.remainingDuration = x,
         entityEffect -> entityEffect.remainingDuration
      )
      .add()
      .append(
         new KeyedCodec<>("SinceLastDamage", Codec.FLOAT), (entityEffect, x) -> entityEffect.sinceLastDamage = x, entityEffect -> entityEffect.sinceLastDamage
      )
      .add()
      .append(
         new KeyedCodec<>("HasBeenDamaged", Codec.BOOLEAN), (entityEffect, x) -> entityEffect.hasBeenDamaged = x, entityEffect -> entityEffect.hasBeenDamaged
      )
      .add()
      .append(
         new KeyedCodec<>("SequentialHits", DamageCalculatorSystems.Sequence.CODEC),
         (entityEffect, x) -> entityEffect.sequentialHits = x,
         entityEffect -> entityEffect.sequentialHits
      )
      .add()
      .append(new KeyedCodec<>("Infinite", Codec.BOOLEAN), (entityEffect, aBoolean) -> entityEffect.infinite = aBoolean, entityEffect -> entityEffect.infinite)
      .add()
      .append(new KeyedCodec<>("Debuff", Codec.BOOLEAN), (entityEffect, aBoolean) -> entityEffect.debuff = aBoolean, entityEffect -> entityEffect.debuff)
      .add()
      .append(
         new KeyedCodec<>("StatusEffectIcon", Codec.STRING),
         (entityEffect, aString) -> entityEffect.statusEffectIcon = aString,
         entityEffect -> entityEffect.statusEffectIcon
      )
      .add()
      .append(
         new KeyedCodec<>("Invulnerable", Codec.BOOLEAN),
         (entityEffect, aBoolean) -> entityEffect.invulnerable = aBoolean,
         entityEffect -> entityEffect.invulnerable
      )
      .add()
      .build();
   private static final float DEFAULT_DURATION = 1.0F;
   @Nonnull
   private static final Message MESSAGE_GENERAL_DAMAGE_CAUSES_UNKNOWN = Message.translation("server.general.damageCauses.unknown");
   protected String entityEffectId;
   protected int entityEffectIndex;
   protected float initialDuration;
   protected float remainingDuration;
   protected boolean infinite;
   protected boolean debuff;
   @Nullable
   protected String statusEffectIcon;
   private float sinceLastDamage;
   private boolean hasBeenDamaged;
   protected boolean invulnerable;
   private DamageCalculatorSystems.Sequence sequentialHits;

   public ActiveEntityEffect() {
   }

   public ActiveEntityEffect(
      @Nonnull String entityEffectId,
      int entityEffectIndex,
      float initialDuration,
      float remainingDuration,
      boolean infinite,
      boolean debuff,
      @Nullable String statusEffectIcon,
      float sinceLastDamage,
      boolean hasBeenDamaged,
      @Nonnull DamageCalculatorSystems.Sequence sequentialHits,
      boolean invulnerable
   ) {
      this.entityEffectId = entityEffectId;
      this.entityEffectIndex = entityEffectIndex;
      this.initialDuration = initialDuration;
      this.remainingDuration = remainingDuration;
      this.infinite = infinite;
      this.debuff = debuff;
      this.statusEffectIcon = statusEffectIcon;
      this.sinceLastDamage = sinceLastDamage;
      this.hasBeenDamaged = hasBeenDamaged;
      this.sequentialHits = sequentialHits;
      this.invulnerable = invulnerable;
   }

   public ActiveEntityEffect(
      @Nonnull String entityEffectId, int entityEffectIndex, float duration, boolean debuff, @Nullable String statusEffectIcon, boolean invulnerable
   ) {
      this(
         entityEffectId,
         entityEffectIndex,
         duration,
         duration,
         false,
         debuff,
         statusEffectIcon,
         0.0F,
         false,
         new DamageCalculatorSystems.Sequence(),
         invulnerable
      );
   }

   public ActiveEntityEffect(@Nonnull String entityEffectId, int entityEffectIndex, boolean infinite, boolean invulnerable) {
      this(entityEffectId, entityEffectIndex, 1.0F, 1.0F, infinite, false, "", 0.0F, false, new DamageCalculatorSystems.Sequence(), invulnerable);
   }

   public void tick(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull EntityEffect entityEffect,
      @Nonnull EntityStatMap entityStatMapComponent,
      float dt
   ) {
      int cyclesToRun = this.calculateCyclesToRun(entityEffect, dt);
      this.tickDamage(commandBuffer, ref, entityEffect, cyclesToRun);
      tickStatChanges(commandBuffer, ref, entityEffect, entityStatMapComponent, cyclesToRun);
      if (!this.infinite) {
         this.remainingDuration -= dt;
      }
   }

   private int calculateCyclesToRun(@Nonnull EntityEffect entityEffect, float dt) {
      int cycles = 0;
      float damageCalculatorCooldown = entityEffect.getDamageCalculatorCooldown();
      if (damageCalculatorCooldown > 0.0F) {
         this.sinceLastDamage += dt;
         cycles = MathUtil.fastFloor(this.sinceLastDamage / damageCalculatorCooldown);
         this.sinceLastDamage %= damageCalculatorCooldown;
      } else if (!this.hasBeenDamaged) {
         cycles = 1;
         this.hasBeenDamaged = true;
      }

      return cycles;
   }

   private static void tickStatChanges(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull EntityEffect entityEffect,
      @Nonnull EntityStatMap entityStatMapComponent,
      int cyclesToRun
   ) {
      Int2FloatMap entityStats = entityEffect.getEntityStats();
      if (entityStats != null) {
         if (cyclesToRun > 0) {
            DamageEffects statModifierEffects = entityEffect.getStatModifierEffects();
            if (statModifierEffects != null) {
               statModifierEffects.spawnAtEntity(commandBuffer, ref);
            }

            entityStatMapComponent.processStatChanges(EntityStatMap.Predictable.ALL, entityStats, entityEffect.getValueType(), ChangeStatBehaviour.Add);
         }
      }
   }

   private void tickDamage(
      @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, @Nonnull EntityEffect entityEffect, int cyclesToRun
   ) {
      DamageCalculator damageCalculator = entityEffect.getDamageCalculator();
      if (damageCalculator != null) {
         if (cyclesToRun > 0) {
            Object2FloatMap<DamageCause> relativeDamage = damageCalculator.calculateDamage(this.initialDuration);
            if (relativeDamage != null && !relativeDamage.isEmpty()) {
               World world = commandBuffer.getExternalData().getWorld();
               DamageEffects damageEffects = entityEffect.getDamageEffects();
               Damage[] hits = DamageCalculatorSystems.queueDamageCalculator(world, relativeDamage, ref, commandBuffer, this, null);

               for (Damage damageEvent : hits) {
                  DamageCalculatorSystems.DamageSequence damageSequence = new DamageCalculatorSystems.DamageSequence(this.sequentialHits, damageCalculator);
                  damageEvent.putMetaObject(DamageCalculatorSystems.DAMAGE_SEQUENCE, damageSequence);
                  if (damageEffects != null) {
                     damageEffects.addToDamage(damageEvent);
                  }

                  commandBuffer.invoke(ref, damageEvent);
               }
            }
         }
      }
   }

   public int getEntityEffectIndex() {
      return this.entityEffectIndex;
   }

   public float getInitialDuration() {
      return this.initialDuration;
   }

   public float getRemainingDuration() {
      return this.remainingDuration;
   }

   public boolean isInfinite() {
      return this.infinite;
   }

   public boolean isDebuff() {
      return this.debuff;
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   @Nonnull
   @Override
   public Message getDeathMessage(@Nonnull Damage info, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      EntityEffect entityEffect = EntityEffect.getAssetMap().getAsset(this.entityEffectIndex);
      if (entityEffect == null) {
         return Message.translation("server.general.killedBy").param("damageSource", MESSAGE_GENERAL_DAMAGE_CAUSES_UNKNOWN);
      } else {
         String deathMessageKey = entityEffect.getDeathMessageKey();
         if (deathMessageKey != null) {
            return Message.translation(deathMessageKey);
         } else {
            String locale = entityEffect.getLocale();
            String reason = locale != null ? locale : entityEffect.getId().toLowerCase(Locale.ROOT);
            Message damageCauseMessage = Message.translation("server.general.damageCauses." + reason);
            return Message.translation("server.general.killedBy").param("damageSource", damageCauseMessage);
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ActiveEntityEffect{entityEffectIndex='"
         + this.entityEffectIndex
         + "', initialDuration="
         + this.initialDuration
         + ", remainingDuration="
         + this.remainingDuration
         + ", damageCooldown="
         + this.sinceLastDamage
         + ", hasBeenDamaged="
         + this.hasBeenDamaged
         + ", sequentialHits="
         + this.sequentialHits
         + ", infinite="
         + this.infinite
         + ", debuff="
         + this.debuff
         + ", statusEffectIcon="
         + this.statusEffectIcon
         + "}";
   }
}
