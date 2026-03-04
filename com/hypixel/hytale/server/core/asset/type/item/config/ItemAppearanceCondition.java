package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.range.FloatRange;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.modelvfx.config.ModelVFX;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ItemAppearanceCondition implements NetworkSerializable<com.hypixel.hytale.protocol.ItemAppearanceCondition> {
   public static final BuilderCodec<ItemAppearanceCondition> CODEC = BuilderCodec.builder(ItemAppearanceCondition.class, ItemAppearanceCondition::new)
      .append(
         new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
         (itemAppearanceCondition, modelParticles) -> itemAppearanceCondition.particles = modelParticles,
         itemAppearanceCondition -> itemAppearanceCondition.particles
      )
      .add()
      .append(
         new KeyedCodec<>("FirstPersonParticles", ModelParticle.ARRAY_CODEC),
         (itemAppearanceCondition, modelParticles) -> itemAppearanceCondition.firstPersonParticles = modelParticles,
         itemAppearanceCondition -> itemAppearanceCondition.firstPersonParticles
      )
      .add()
      .<String>append(
         new KeyedCodec<>("Model", Codec.STRING),
         (itemAppearanceCondition, s) -> itemAppearanceCondition.model = s,
         itemAppearanceCondition -> itemAppearanceCondition.model
      )
      .addValidator(CommonAssetValidator.MODEL_CHARACTER)
      .add()
      .<String>append(
         new KeyedCodec<>("Texture", Codec.STRING),
         (itemAppearanceCondition, s) -> itemAppearanceCondition.texture = s,
         itemAppearanceCondition -> itemAppearanceCondition.texture
      )
      .addValidator(CommonAssetValidator.TEXTURE_CHARACTER)
      .add()
      .<String>append(
         new KeyedCodec<>("ModelVFXId", Codec.STRING),
         (itemAppearanceCondition, s) -> itemAppearanceCondition.modelVFXId = s,
         itemAppearanceCondition -> itemAppearanceCondition.modelVFXId
      )
      .addValidator(ModelVFX.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>append(
         new KeyedCodec<>("WorldSoundEventId", Codec.STRING),
         (activationEffects, s) -> activationEffects.worldSoundEventId = s,
         activationEffects -> activationEffects.worldSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .addValidator(SoundEventValidators.LOOPING)
      .documentation("3D sound to play in the world when applying this condition.")
      .add()
      .<String>append(
         new KeyedCodec<>("LocalSoundEventId", Codec.STRING),
         (activationEffects, s) -> activationEffects.localSoundEventId = s,
         activationEffects -> activationEffects.localSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.LOOPING)
      .documentation("Local sound to play for the owner of this condition.")
      .add()
      .<FloatRange>append(
         new KeyedCodec<>("Condition", FloatRange.CODEC),
         (itemAppearanceCondition, intRange) -> itemAppearanceCondition.condition = intRange,
         itemAppearanceCondition -> itemAppearanceCondition.condition
      )
      .documentation("An array of 2 floats to define when the condition is active. 'Infinite' and '-Infinite' can be used to define bounds.")
      .addValidator(Validators.nonNull())
      .add()
      .<ValueType>append(
         new KeyedCodec<>("ConditionValueType", new EnumCodec<>(ValueType.class)),
         (itemAppearanceCondition, conditionValueType) -> itemAppearanceCondition.conditionValueType = conditionValueType,
         itemAppearanceCondition -> itemAppearanceCondition.conditionValueType
      )
      .documentation(
         "Enum to specify if the condition range must be considered as absolute values or percent. Default value is Absolute. When using ValueType.Absolute, '100' matches the max value."
      )
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(condition -> {
         if (condition.worldSoundEventId != null) {
            condition.worldSoundEventIndex = SoundEvent.getAssetMap().getIndex(condition.worldSoundEventId);
         }

         if (condition.localSoundEventId != null) {
            condition.localSoundEventIndex = SoundEvent.getAssetMap().getIndex(condition.localSoundEventId);
         }
      })
      .build();
   protected ModelParticle[] particles;
   protected ModelParticle[] firstPersonParticles;
   protected String worldSoundEventId;
   protected transient int worldSoundEventIndex = 0;
   protected String localSoundEventId;
   protected transient int localSoundEventIndex = 0;
   protected String model;
   protected String texture;
   protected FloatRange condition;
   @Nonnull
   protected ValueType conditionValueType = ValueType.Absolute;
   protected String modelVFXId;

   public ModelParticle[] getParticles() {
      return this.particles;
   }

   public String getModel() {
      return this.model;
   }

   public String getTexture() {
      return this.texture;
   }

   public FloatRange getCondition() {
      return this.condition;
   }

   public ValueType getConditionValueType() {
      return this.conditionValueType;
   }

   public String getModelVFXId() {
      return this.modelVFXId;
   }

   public String getWorldSoundEventId() {
      return this.worldSoundEventId;
   }

   public int getWorldSoundEventIndex() {
      return this.worldSoundEventIndex;
   }

   public String getLocalSoundEventId() {
      return this.localSoundEventId;
   }

   public int getLocalSoundEventIndex() {
      return this.localSoundEventIndex;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemAppearanceCondition toPacket() {
      com.hypixel.hytale.protocol.ItemAppearanceCondition packet = new com.hypixel.hytale.protocol.ItemAppearanceCondition();
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

      packet.model = this.model;
      packet.texture = this.texture;
      packet.condition = new com.hypixel.hytale.protocol.FloatRange(this.condition.getInclusiveMin(), this.condition.getInclusiveMax());
      packet.conditionValueType = this.conditionValueType;
      packet.modelVFXId = this.modelVFXId;
      packet.localSoundEventId = this.localSoundEventIndex != 0 ? this.localSoundEventIndex : this.worldSoundEventIndex;
      packet.worldSoundEventId = this.worldSoundEventIndex;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemAppearanceCondition{particles="
         + Arrays.toString((Object[])this.particles)
         + ", firstPersonParticles="
         + Arrays.toString((Object[])this.firstPersonParticles)
         + ", worldSoundEventId='"
         + this.worldSoundEventId
         + "', localSoundEventId='"
         + this.localSoundEventId
         + "', model='"
         + this.model
         + "', texture='"
         + this.texture
         + "', condition="
         + this.condition
         + ", conditionValueType="
         + this.conditionValueType
         + ", modelVFXId="
         + this.modelVFXId
         + "}";
   }
}
