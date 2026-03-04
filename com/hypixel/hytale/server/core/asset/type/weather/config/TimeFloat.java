package com.hypixel.hytale.server.core.asset.type.weather.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class TimeFloat {
   public static final BuilderCodec<TimeFloat> CODEC = BuilderCodec.builder(TimeFloat.class, TimeFloat::new)
      .append(new KeyedCodec<>("Hour", Codec.DOUBLE), (timeFloat, i) -> timeFloat.hour = i.floatValue(), timeFloat -> (double)timeFloat.getHour())
      .addValidator(Validators.range(0.0, 24.0))
      .add()
      .addField(new KeyedCodec<>("Value", Codec.DOUBLE), (timeFloat, d) -> timeFloat.value = d.floatValue(), timeFloat -> (double)timeFloat.value)
      .build();
   protected float hour;
   protected float value;

   public TimeFloat(float hour, float value) {
      this.hour = hour;
      this.value = value;
   }

   protected TimeFloat() {
   }

   public float getHour() {
      return this.hour;
   }

   public float getValue() {
      return this.value;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TimeFloat{hour=" + this.hour + ", value='" + this.value + "'}";
   }
}
