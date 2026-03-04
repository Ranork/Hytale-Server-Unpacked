package com.hypixel.hytale.builtin.portals.components.voidevent.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoidEventStage {
   @Nonnull
   public static final BuilderCodec<VoidEventStage> CODEC = BuilderCodec.builder(VoidEventStage.class, VoidEventStage::new)
      .append(new KeyedCodec<>("SecondsInto", Codec.INTEGER), (stage, o) -> stage.secondsInto = o, stage -> stage.secondsInto)
      .documentation("How many seconds into the void event does this stage becomes the active stage.")
      .add()
      .<String>append(new KeyedCodec<>("ForcedWeather", Codec.STRING), (stage, o) -> stage.forcedWeatherId = o, stage -> stage.forcedWeatherId)
      .documentation("What weather to force during this stage.")
      .addValidator(Weather.VALIDATOR_CACHE.getValidator())
      .add()
      .build();
   private int secondsInto;
   private String forcedWeatherId;

   public int getSecondsInto() {
      return this.secondsInto;
   }

   @Nullable
   public String getForcedWeatherId() {
      return this.forcedWeatherId;
   }
}
