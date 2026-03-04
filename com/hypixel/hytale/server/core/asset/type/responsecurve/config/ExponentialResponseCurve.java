package com.hypixel.hytale.server.core.asset.type.responsecurve.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class ExponentialResponseCurve extends ResponseCurve {
   public static final BuilderCodec<ExponentialResponseCurve> CODEC = BuilderCodec.builder(
         ExponentialResponseCurve.class, ExponentialResponseCurve::new, BASE_CODEC
      )
      .documentation("An response curve which changes at an exponential rate.")
      .<Double>appendInherited(
         new KeyedCodec<>("Slope", Codec.DOUBLE), (curve, d) -> curve.slope = d, curve -> curve.slope, (curve, parent) -> curve.slope = parent.slope
      )
      .documentation("The slope of the curve.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("Exponent", Codec.DOUBLE),
         (curve, d) -> curve.exponent = d,
         curve -> curve.exponent,
         (curve, parent) -> curve.exponent = parent.exponent
      )
      .documentation("The exponent used to generate this curve. 1 is linear, 2 results in a quadratic parabola, and 3 in a cubic curve, etc.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("HorizontalShift", Codec.DOUBLE),
         (curve, d) -> curve.horizontalShift = d,
         curve -> curve.horizontalShift,
         (curve, parent) -> curve.horizontalShift = parent.horizontalShift
      )
      .documentation("The horizontal shift to apply to the curve. This decides how far the curve is shifted left or right along the x axis.")
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
   protected double slope = 1.0;
   protected double exponent = 1.0;
   protected double horizontalShift;
   protected double verticalShift;

   public ExponentialResponseCurve(double slope, double exponent, double horizontalShift, double verticalShift) {
      this.slope = slope;
      this.exponent = exponent;
      this.horizontalShift = horizontalShift;
      this.verticalShift = verticalShift;
   }

   public ExponentialResponseCurve(String id) {
      super(id);
   }

   protected ExponentialResponseCurve() {
   }

   @Override
   public double computeY(double x) {
      if (!(x < 0.0) && !(x > 1.0)) {
         return this.slope * Math.pow(x - this.horizontalShift, this.exponent) + this.verticalShift;
      } else {
         throw new IllegalArgumentException("X must be between 0.0 and 1.0");
      }
   }

   public double getSlope() {
      return this.slope;
   }

   public double getExponent() {
      return this.exponent;
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
      return "ExponentialResponseCurve{slope="
         + this.slope
         + ", exponent="
         + this.exponent
         + ", horizontalShift="
         + this.horizontalShift
         + ", verticalShift="
         + this.verticalShift
         + "} "
         + super.toString();
   }
}
