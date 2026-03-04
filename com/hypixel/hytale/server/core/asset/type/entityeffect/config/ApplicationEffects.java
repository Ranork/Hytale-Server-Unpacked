package com.hypixel.hytale.server.core.asset.type.entityeffect.config;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.modifiers.MovementEffects;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.modelvfx.config.ModelVFX;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ApplicationEffects implements NetworkSerializable<com.hypixel.hytale.protocol.ApplicationEffects> {
   @Nonnull
   public static final BuilderCodec<ApplicationEffects> CODEC = BuilderCodec.builder(ApplicationEffects.class, ApplicationEffects::new)
      .appendInherited(
         new KeyedCodec<>("EntityBottomTint", ProtocolCodecs.COLOR),
         (entityEffect, s) -> entityEffect.entityBottomTint = s,
         entityEffect -> entityEffect.entityBottomTint,
         (entityEffect, parent) -> entityEffect.entityBottomTint = parent.entityBottomTint
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("EntityTopTint", ProtocolCodecs.COLOR),
         (entityEffect, s) -> entityEffect.entityTopTint = s,
         entityEffect -> entityEffect.entityTopTint,
         (entityEffect, parent) -> entityEffect.entityTopTint = parent.entityTopTint
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("EntityAnimationId", Codec.STRING),
         (entityEffect, s) -> entityEffect.entityAnimationId = s,
         entityEffect -> entityEffect.entityAnimationId,
         (entityEffect, parent) -> entityEffect.entityAnimationId = parent.entityAnimationId
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
         (entityEffect, s) -> entityEffect.particles = s,
         entityEffect -> entityEffect.particles,
         (entityEffect, parent) -> entityEffect.particles = parent.particles
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("FirstPersonParticles", ModelParticle.ARRAY_CODEC),
         (entityEffect, s) -> entityEffect.firstPersonParticles = s,
         entityEffect -> entityEffect.firstPersonParticles,
         (entityEffect, parent) -> entityEffect.firstPersonParticles = parent.firstPersonParticles
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ScreenEffect", Codec.STRING),
         (entityEffect, s) -> entityEffect.screenEffect = s,
         entityEffect -> entityEffect.screenEffect,
         (entityEffect, parent) -> entityEffect.screenEffect = parent.screenEffect
      )
      .addValidator(CommonAssetValidator.UI_SCREEN_EFFECT)
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("HorizontalSpeedMultiplier", Codec.DOUBLE),
         (entityEffect, s) -> entityEffect.horizontalSpeedMultiplier = s.floatValue(),
         entityEffect -> (double)entityEffect.horizontalSpeedMultiplier,
         (entityEffect, parent) -> entityEffect.horizontalSpeedMultiplier = parent.horizontalSpeedMultiplier
      )
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("KnockbackMultiplier", Codec.DOUBLE),
         (entityEffect, s) -> entityEffect.knockbackMultiplier = s.floatValue(),
         entityEffect -> (double)entityEffect.knockbackMultiplier,
         (entityEffect, parent) -> entityEffect.knockbackMultiplier = parent.knockbackMultiplier
      )
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("LocalSoundEventId", Codec.STRING),
         (entityEffect, s) -> entityEffect.soundEventIdLocal = s,
         entityEffect -> entityEffect.soundEventIdLocal,
         (entityEffect, parent) -> entityEffect.soundEventIdLocal = parent.soundEventIdLocal
      )
      .documentation("Local sound event played to the affected player")
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("WorldSoundEventId", Codec.STRING),
         (entityEffect, s) -> entityEffect.soundEventIdWorld = s,
         entityEffect -> entityEffect.soundEventIdWorld,
         (entityEffect, parent) -> entityEffect.soundEventIdWorld = parent.soundEventIdWorld
      )
      .documentation("World sound event played to surrounding players")
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .add()
      .afterDecode(ApplicationEffects::processConfig)
      .<String>appendInherited(
         new KeyedCodec<>("ModelVFXId", Codec.STRING),
         (entityEffect, s) -> entityEffect.modelVFXId = s,
         entityEffect -> entityEffect.modelVFXId,
         (entityEffect, parent) -> entityEffect.modelVFXId = parent.modelVFXId
      )
      .addValidator(ModelVFX.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("MovementEffects", MovementEffects.CODEC),
         (entityEffect, s) -> entityEffect.movementEffects = s,
         entityEffect -> entityEffect.movementEffects,
         (entityEffect, parent) -> entityEffect.movementEffects = parent.movementEffects
      )
      .add()
      .<AbilityEffects>appendInherited(
         new KeyedCodec<>("AbilityEffects", AbilityEffects.CODEC),
         (entityEffect, s) -> entityEffect.abilityEffects = s,
         entityEffect -> entityEffect.abilityEffects,
         (entityEffect, parent) -> entityEffect.abilityEffects = parent.abilityEffects
      )
      .documentation("Handles any effects applied that are affiliated with abilities")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("MouseSensitivityAdjustmentTarget", Codec.FLOAT),
         (interaction, doubles) -> interaction.mouseSensitivityAdjustmentTarget = doubles,
         interaction -> interaction.mouseSensitivityAdjustmentTarget,
         (interaction, parent) -> interaction.mouseSensitivityAdjustmentTarget = parent.mouseSensitivityAdjustmentTarget
      )
      .documentation("What is the target modifier to apply to mouse sensitivity while this interaction is active.")
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("MouseSensitivityAdjustmentDuration", Codec.FLOAT),
         (interaction, doubles) -> interaction.mouseSensitivityAdjustmentDuration = doubles,
         interaction -> interaction.mouseSensitivityAdjustmentDuration,
         (interaction, parent) -> interaction.mouseSensitivityAdjustmentDuration = parent.mouseSensitivityAdjustmentDuration
      )
      .documentation("Override the global linear modifier adjustment with this as the time to go from 1.0 to 0.0.")
      .add()
      .build();
   protected Color entityBottomTint;
   protected Color entityTopTint;
   protected String entityAnimationId;
   protected ModelParticle[] particles;
   protected ModelParticle[] firstPersonParticles;
   protected String screenEffect;
   protected float horizontalSpeedMultiplier = 1.0F;
   protected float knockbackMultiplier = 1.0F;
   protected String soundEventIdLocal;
   protected transient int soundEventIndexLocal = 0;
   protected String soundEventIdWorld;
   protected transient int soundEventIndexWorld = 0;
   protected String modelVFXId;
   protected MovementEffects movementEffects;
   protected AbilityEffects abilityEffects;
   private float mouseSensitivityAdjustmentTarget;
   private float mouseSensitivityAdjustmentDuration;

   protected ApplicationEffects() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ApplicationEffects toPacket() {
      com.hypixel.hytale.protocol.ApplicationEffects packet = new com.hypixel.hytale.protocol.ApplicationEffects();
      packet.entityBottomTint = this.entityBottomTint;
      packet.entityTopTint = this.entityTopTint;
      packet.entityAnimationId = this.entityAnimationId;
      if (this.particles != null && this.particles.length > 0) {
         packet.particles = new com.hypixel.hytale.protocol.ModelParticle[this.particles.length];

         for (int i = 0; i < this.particles.length; i++) {
            packet.particles[i] = this.particles[i].toPacket();
         }
      }

      if (this.firstPersonParticles != null && this.firstPersonParticles.length > 0) {
         packet.firstPersonParticles = new com.hypixel.hytale.protocol.ModelParticle[this.firstPersonParticles.length];

         for (int i = 0; i < this.firstPersonParticles.length; i++) {
            packet.firstPersonParticles[i] = this.firstPersonParticles[i].toPacket();
         }
      }

      packet.screenEffect = this.screenEffect;
      packet.horizontalSpeedMultiplier = this.horizontalSpeedMultiplier;
      packet.soundEventIndexLocal = this.soundEventIndexLocal != 0 ? this.soundEventIndexLocal : this.soundEventIndexWorld;
      packet.soundEventIndexWorld = this.soundEventIndexWorld;
      packet.modelVFXId = this.modelVFXId;
      if (this.movementEffects != null) {
         packet.movementEffects = this.movementEffects.toPacket();
      }

      if (this.abilityEffects != null) {
         packet.abilityEffects = this.abilityEffects.toPacket();
      }

      packet.mouseSensitivityAdjustmentDuration = this.mouseSensitivityAdjustmentDuration;
      packet.mouseSensitivityAdjustmentTarget = this.mouseSensitivityAdjustmentTarget;
      return packet;
   }

   public float getHorizontalSpeedMultiplier() {
      return this.horizontalSpeedMultiplier;
   }

   public float getKnockbackMultiplier() {
      return this.knockbackMultiplier;
   }

   protected void processConfig() {
      IndexedLookupTableAssetMap<String, SoundEvent> soundEventAssetMap = SoundEvent.getAssetMap();
      if (this.soundEventIdLocal != null) {
         this.soundEventIndexLocal = soundEventAssetMap.getIndex(this.soundEventIdLocal);
      }

      if (this.soundEventIdWorld != null) {
         this.soundEventIndexWorld = soundEventAssetMap.getIndex(this.soundEventIdWorld);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ApplicationEffects{entityBottomTint="
         + this.entityBottomTint
         + ", entityTopTint="
         + this.entityTopTint
         + ", entityAnimationId='"
         + this.entityAnimationId
         + "', particles="
         + Arrays.toString((Object[])this.particles)
         + ", firstPersonParticles="
         + Arrays.toString((Object[])this.firstPersonParticles)
         + ", screenEffect='"
         + this.screenEffect
         + "', horizontalSpeedMultiplier="
         + this.horizontalSpeedMultiplier
         + ", soundEventIndexLocal="
         + this.soundEventIndexLocal
         + ", soundEventIndexWorld="
         + this.soundEventIndexWorld
         + ", movementEffects="
         + this.movementEffects
         + ", abilityEffects="
         + this.abilityEffects
         + "}";
   }
}
