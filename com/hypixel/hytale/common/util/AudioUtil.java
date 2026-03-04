package com.hypixel.hytale.common.util;

public class AudioUtil {
   public static final float MIN_DECIBEL_VOLUME = -100.0F;
   public static final float MAX_DECIBEL_VOLUME = 10.0F;
   public static final float MIN_SEMITONE_PITCH = -12.0F;
   public static final float MAX_SEMITONE_PITCH = 12.0F;

   public static float decibelsToLinearGain(float decibels) {
      return decibels <= -100.0F ? 0.0F : (float)Math.pow(10.0, decibels / 20.0F);
   }

   public static float linearGainToDecibels(float linearGain) {
      return linearGain <= 0.0F ? -100.0F : (float)(Math.log(linearGain) / Math.log(10.0) * 20.0);
   }

   public static float semitonesToLinearPitch(float semitones) {
      return (float)(1.0 / Math.pow(2.0, -semitones / 12.0F));
   }

   public static float linearPitchToSemitones(float linearPitch) {
      return (float)(Math.log(linearPitch) / Math.log(2.0) * 12.0);
   }
}
