package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import javax.annotation.Nonnull;

public class GatheringEffectsConfig {
   @Nonnull
   public static final BuilderCodec<GatheringEffectsConfig> CODEC = BuilderCodec.builder(GatheringEffectsConfig.class, GatheringEffectsConfig::new)
      .append(
         new KeyedCodec<>("ParticleSystemId", Codec.STRING),
         (unbreakableBlockConfig, o) -> unbreakableBlockConfig.particleSystemId = o,
         unbreakableBlockConfig -> unbreakableBlockConfig.particleSystemId
      )
      .addValidator(ParticleSystem.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>append(
         new KeyedCodec<>("SoundEventId", Codec.STRING),
         (unbreakableBlockConfig, o) -> unbreakableBlockConfig.soundEventId = o,
         unbreakableBlockConfig -> unbreakableBlockConfig.soundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .afterDecode(GatheringEffectsConfig::processConfig)
      .build();
   protected String particleSystemId;
   protected String soundEventId;
   protected transient int soundEventIndex;

   public String getParticleSystemId() {
      return this.particleSystemId;
   }

   public String getSoundEventId() {
      return this.soundEventId;
   }

   public int getSoundEventIndex() {
      return this.soundEventIndex;
   }

   protected void processConfig() {
      if (this.soundEventId != null) {
         this.soundEventIndex = SoundEvent.getAssetMap().getIndex(this.soundEventId);
      }
   }
}
