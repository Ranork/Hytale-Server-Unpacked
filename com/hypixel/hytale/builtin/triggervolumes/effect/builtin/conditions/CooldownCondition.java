package com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class CooldownCondition extends TriggerCondition {
   @Nonnull
   public static final BuilderCodec<CooldownCondition> CODEC = BuilderCodec.builder(CooldownCondition.class, CooldownCondition::new, BASE_CODEC)
      .append(new KeyedCodec<>("Cooldown", Codec.FLOAT, false), (c, v) -> c.cooldown = v, c -> c.cooldown)
      .add()
      .append(new KeyedCodec<>("Scope", new EnumCodec<>(CooldownCondition.Scope.class, EnumCodec.EnumStyle.LEGACY), false), (c, v) -> c.scope = v, c -> c.scope)
      .add()
      .build();
   private float cooldown;
   @Nonnull
   private CooldownCondition.Scope scope = CooldownCondition.Scope.PER_PLAYER;
   @Nonnull
   private final transient Map<String, Long> lastVolumeActivationNanos = new ConcurrentHashMap<>();
   @Nonnull
   private final transient Map<CooldownCondition.VolumeEntityKey, Long> lastEntityActivationNanos = new ConcurrentHashMap<>();

   @Nonnull
   public static CooldownCondition create(@Nonnull TriggerEventType eventType, float cooldown) {
      return create(eventType, cooldown, CooldownCondition.Scope.PER_PLAYER);
   }

   @Nonnull
   public static CooldownCondition create(@Nonnull TriggerEventType eventType, float cooldown, @Nonnull CooldownCondition.Scope scope) {
      CooldownCondition condition = new CooldownCondition();
      condition.setEventType(eventType);
      condition.cooldown = cooldown;
      condition.scope = scope;
      return condition;
   }

   @Override
   public boolean test(@Nonnull TriggerContext context) {
      if (this.cooldown <= 0.0F) {
         return true;
      } else {
         long nowNanos = System.nanoTime();
         long cooldownNanos = TimeUnit.MILLISECONDS.toNanos((long)(this.cooldown * 1000.0F));
         String volumeId = context.getVolume().getId();
         if (this.scope == CooldownCondition.Scope.WHOLE_VOLUME) {
            Long lastActivation = this.lastVolumeActivationNanos.get(volumeId);
            if (lastActivation != null && nowNanos - lastActivation < cooldownNanos) {
               return false;
            } else {
               this.lastVolumeActivationNanos.put(volumeId, nowNanos);
               return true;
            }
         } else {
            UUIDComponent uuidComponent = context.getStore().getComponent(context.getEntityRef(), UUIDComponent.getComponentType());
            if (uuidComponent == null) {
               return false;
            } else {
               UUID entityUuid = uuidComponent.getUuid();
               return this.testForEntity(volumeId, entityUuid, nowNanos);
            }
         }
      }
   }

   boolean testForEntity(@Nonnull String volumeId, @Nonnull UUID entityUuid, long nowNanos) {
      if (this.cooldown <= 0.0F) {
         return true;
      } else {
         long cooldownNanos = TimeUnit.MILLISECONDS.toNanos((long)(this.cooldown * 1000.0F));
         CooldownCondition.VolumeEntityKey key = new CooldownCondition.VolumeEntityKey(volumeId, entityUuid);
         if (this.scope == CooldownCondition.Scope.WHOLE_VOLUME) {
            Long lastActivation = this.lastVolumeActivationNanos.get(volumeId);
            if (lastActivation != null && nowNanos - lastActivation < cooldownNanos) {
               return false;
            } else {
               this.lastVolumeActivationNanos.put(volumeId, nowNanos);
               return true;
            }
         } else {
            this.lastEntityActivationNanos.entrySet().removeIf(entry -> nowNanos - entry.getValue() >= cooldownNanos);
            Long lastActivation = this.lastEntityActivationNanos.get(key);
            if (lastActivation != null && nowNanos - lastActivation < cooldownNanos) {
               return false;
            } else {
               this.lastEntityActivationNanos.put(key, nowNanos);
               return true;
            }
         }
      }
   }

   public static enum Scope {
      PER_PLAYER,
      WHOLE_VOLUME;
   }

   private record VolumeEntityKey(@Nonnull String volumeId, @Nonnull UUID entityUuid) {
   }
}
