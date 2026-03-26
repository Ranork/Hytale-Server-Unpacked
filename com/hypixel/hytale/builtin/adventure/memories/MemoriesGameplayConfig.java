package com.hypixel.hytale.builtin.adventure.memories;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MemoriesGameplayConfig {
   @Nonnull
   public static final String ID = "Memories";
   @Nonnull
   public static final BuilderCodec<MemoriesGameplayConfig> CODEC = BuilderCodec.builder(MemoriesGameplayConfig.class, MemoriesGameplayConfig::new)
      .appendInherited(
         new KeyedCodec<>("MemoriesAmountPerLevel", Codec.INT_ARRAY),
         (config, value) -> config.memoriesAmountPerLevel = value,
         config -> config.memoriesAmountPerLevel,
         (config, parent) -> config.memoriesAmountPerLevel = parent.memoriesAmountPerLevel
      )
      .addValidator(Validators.nonNull())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("MemoriesRecordParticles", Codec.STRING),
         (config, value) -> config.memoriesRecordParticles = value,
         config -> config.memoriesRecordParticles,
         (config, parent) -> config.memoriesRecordParticles = parent.memoriesRecordParticles
      )
      .addValidator(Validators.nonNull())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("MemoriesCatchItemId", Codec.STRING),
         (memoriesGameplayConfig, s) -> memoriesGameplayConfig.memoriesCatchItemId = s,
         memoriesGameplayConfig -> memoriesGameplayConfig.memoriesCatchItemId,
         (memoriesGameplayConfig, parent) -> memoriesGameplayConfig.memoriesCatchItemId = parent.memoriesCatchItemId
      )
      .addValidator(Validators.nonNull())
      .addValidator(Item.VALIDATOR_CACHE.getValidator())
      .add()
      .<ModelParticle>appendInherited(
         new KeyedCodec<>("MemoriesCatchEntityParticle", ModelParticle.CODEC),
         (activationEffects, s) -> activationEffects.memoriesCatchEntityParticle = s,
         activationEffects -> activationEffects.memoriesCatchEntityParticle,
         (activationEffects, parent) -> activationEffects.memoriesCatchEntityParticle = parent.memoriesCatchEntityParticle
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("MemoriesCatchParticleViewDistance", Codec.INTEGER),
         (memoriesGameplayConfig, integer) -> memoriesGameplayConfig.memoriesCatchParticleViewDistance = integer,
         memoriesGameplayConfig -> memoriesGameplayConfig.memoriesCatchParticleViewDistance,
         (memoriesGameplayConfig, parent) -> memoriesGameplayConfig.memoriesCatchParticleViewDistance = parent.memoriesCatchParticleViewDistance
      )
      .addValidator(Validators.greaterThan(16))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("MemoriesRestoreSoundEventId", Codec.STRING),
         (activationEffects, s) -> activationEffects.memoriesRestoreSoundEventId = s,
         activationEffects -> activationEffects.memoriesRestoreSoundEventId,
         (activationEffects, parent) -> activationEffects.memoriesRestoreSoundEventId = parent.memoriesRestoreSoundEventId
      )
      .addValidator(Validators.nonNull())
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("MemoriesCatchSoundEventId", Codec.STRING),
         (memoriesGameplayConfig, s) -> memoriesGameplayConfig.memoriesCatchSoundEventId = s,
         memoriesGameplayConfig -> memoriesGameplayConfig.memoriesCatchSoundEventId,
         (memoriesGameplayConfig, parent) -> memoriesGameplayConfig.memoriesCatchSoundEventId = parent.memoriesCatchSoundEventId
      )
      .addValidator(Validators.nonNull())
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .build();
   private int[] memoriesAmountPerLevel;
   private String memoriesRecordParticles;
   private String memoriesCatchItemId;
   private ModelParticle memoriesCatchEntityParticle;
   private int memoriesCatchParticleViewDistance = 64;
   private String memoriesRestoreSoundEventId;
   private String memoriesCatchSoundEventId;

   @Nullable
   public static MemoriesGameplayConfig get(@Nonnull GameplayConfig config) {
      return config.getPluginConfig().get(MemoriesGameplayConfig.class);
   }

   public int[] getMemoriesAmountPerLevel() {
      return this.memoriesAmountPerLevel;
   }

   public String getMemoriesRecordParticles() {
      return this.memoriesRecordParticles;
   }

   public String getMemoriesCatchItemId() {
      return this.memoriesCatchItemId;
   }

   public String getMemoriesRestoreSoundEventId() {
      return this.memoriesRestoreSoundEventId;
   }

   public String getMemoriesCatchSoundEventId() {
      return this.memoriesCatchSoundEventId;
   }

   public ModelParticle getMemoriesCatchEntityParticle() {
      return this.memoriesCatchEntityParticle;
   }

   public int getMemoriesCatchParticleViewDistance() {
      return this.memoriesCatchParticleViewDistance;
   }
}
