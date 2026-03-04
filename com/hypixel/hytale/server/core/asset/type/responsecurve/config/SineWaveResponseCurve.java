package com.hypixel.hytale.server.core.asset.type.responsecurve.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.TrigMathUtil;
import javax.annotation.Nonnull;

public class SineWaveResponseCurve extends ResponseCurve {
   public static final BuilderCodec<SineWaveResponseCurve> CODEC = BuilderCodec.builder(SineWaveResponseCurve.class, SineWaveResponseCurve::new, BASE_CODEC)
      .documentation("A response curve with a sine wave shape.")
      .<Double>appendInherited(
         new KeyedCodec<>("Amplitude", Codec.DOUBLE),
         (curve, d) -> curve.amplitude = d,
         curve -> curve.amplitude,
         (curve, parent) -> curve.amplitude = parent.amplitude
      )
      .documentation("The vertical distance between the horizontal axis and the max/min value of the function.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("Frequency", Codec.DOUBLE),
         (curve, d) -> curve.frequency = d,
         curve -> curve.frequency,
         (curve, parent) -> curve.frequency = parent.frequency
      )
      .documentation("The frequency of the sine wave's repetition (e.g. set to 1, the full pattern will appear once in the 0-1 range, twice with 2, etc).")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("HorizontalShift", Codec.DOUBLE),
         (curve, d) -> curve.horizontalShift = d,
         curve -> curve.horizontalShift,
         (curve, parent) -> curve.horizontalShift = parent.horizontalShift
      )
      .documentation("The horizontal shift to apply to the curve,. This decides how far the curve is shifted left or right along the x axis.")
      .addValidator(Validators.range(-1.0, 1.0))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("VerticalShift", Codec.DOUBLE),
         (curve, d) -> curve.verticalShift = d,
         curve -> curve.verticalShift,
         (curve, parent) -> curve.verticalShift = parent.verticalShift
      )
      .documentation("The vertical shift to apply to the curve. This decides how far the curve is shifted up or down along the y axis.")
      .addValidator(Validators.range(-1.0, 1.0))
      .add()
      .build();
   protected double amplitude = 1.0;
   protected double frequency = 0.5;
   protected double horizontalShift;
   protected double verticalShift;

   protected SineWaveResponseCurve() {
   }

   @Override
   public double computeY(double x) {
      if (!(x < 0.0) && !(x > 1.0)) {
         return this.amplitude * TrigMathUtil.sin((float) (Math.PI * 2) * this.frequency * x + this.horizontalShift) + this.verticalShift;
      } else {
         throw new IllegalArgumentException("X must be between 0.0 and 1.0");
      }
   }

   public double getAmplitude() {
      return this.amplitude;
   }

   public double getFrequency() {
      return this.frequency;
   }

   public double getHorizontalShift() {
      return this.horizontalShift;
   }

   public double getVerticalShift() {
      return this.verticalShift;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SineWaveResponseCurve{amplitude="
         + this.amplitude
         + ", frequency="
         + this.frequency
         + ", horizontalShift="
         + this.horizontalShift
         + ", verticalShift="
         + this.verticalShift
         + "} "
         + super.toString();
   }
}
