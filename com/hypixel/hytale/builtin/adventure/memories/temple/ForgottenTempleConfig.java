package com.hypixel.hytale.builtin.adventure.memories.temple;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import javax.annotation.Nonnull;

public class ForgottenTempleConfig {
   @Nonnull
   public static final BuilderCodec<ForgottenTempleConfig> CODEC = BuilderCodec.builder(ForgottenTempleConfig.class, ForgottenTempleConfig::new)
      .append(new KeyedCodec<>("MinYRespawn", Codec.DOUBLE), (config, o) -> config.minYRespawn = o, config -> config.minYRespawn)
      .documentation("The Y at which players are teleported back to spawn.")
      .add()
      .<String>append(new KeyedCodec<>("RespawnSound", Codec.STRING), (config, o) -> config.respawnSound = o, config -> config.respawnSound)
      .documentation("The sound ID to play when players respawn in the temple.")
      .add()
      .build();
   private double minYRespawn = 5.0;
   private String respawnSound;

   public double getMinYRespawn() {
      return this.minYRespawn;
   }

   public String getRespawnSound() {
      return this.respawnSound;
   }

   public int getRespawnSoundIndex() {
      return this.respawnSound == null ? 0 : SoundEvent.getAssetMap().getIndex(this.respawnSound);
   }
}
