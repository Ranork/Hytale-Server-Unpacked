package com.hypixel.hytale.server.core.asset.type.responsecurve.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class LogisticResponseCurve extends ResponseCurve {
   public static final BuilderCodec<LogisticResponseCurve> CODEC = BuilderCodec.builder(LogisticResponseCurve.class, LogisticResponseCurve::new, BASE_CODEC)
      .documentation("A response curve with a logistic rate of change.")
      .<Double>appendInherited(
         new KeyedCodec<>("RateOfChange", Codec.DOUBLE),
         (curve, d) -> curve.rateOfChange = d,
         curve -> curve.rateOfChange,
         (curve, parent) -> curve.rateOfChange = parent.rateOfChange
      )
      .documentation("The rate of change of the curve - similar to **Slope** in the exponential curve.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("Ceiling", Codec.DOUBLE), (curve, d) -> curve.ceiling = d, curve -> curve.ceiling, (curve, parent) -> curve.ceiling = parent.ceiling
      )
      .documentation(
         "The total height of the curve between its two plateaus. Using a negative value with vertical offsets allows the curve to act as a diminishing factor"
      )
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("HorizontalShift", Codec.DOUBLE),
         (curve, d) -> curve.horizontalShift = d,
         curve -> curve.horizontalShift,
         (curve, parent) -> curve.horizontalShift = parent.horizontalShift
      )
      .documentation("The horizontal shift to apply to the curve. This decides how far the curve is shifted left or right along the x axis.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("VerticalShift", Codec.DOUBLE),
         (curve, d) -> curve.verticalShift = d,
         curve -> curve.verticalShift,
         (curve, parent) -> curve.verticalShift = parent.verticalShift
      )
      .documentation("The vertical shift to apply to the curve. This decides how far the curve is shifted up or down along the y axis.")
      .add()
      .build();
   protected double rateOfChange = 1.0;
   protected double ceiling = 1.0;
   protected double horizontalShift = 0.5;
   protected double verticalShift = 0.0;

   public LogisticResponseCurve(double rateOfChange, double ceiling, double horizontalShift, double verticalShift) {
      this.rateOfChange = rateOfChange;
      this.ceiling = ceiling;
      this.horizontalShift = horizontalShift;
      this.verticalShift = verticalShift;
   }

   protected LogisticResponseCurve() {
   }

   @Override
   public double computeY(double x) {
      if (!(x < 0.0) && !(x > 1.0)) {
         return this.ceiling / (1.0 + Math.pow(100.0, 2.0 * this.rateOfChange * (this.horizontalShift - x))) + this.verticalShift;
      } else {
         throw new IllegalArgumentException("X must be between 0.0 and 1.0");
      }
   }

   public double getRateOfChange() {
      return this.rateOfChange;
   }

   public double getCeiling() {
      return this.ceiling;
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
      return "LogisticResponseCurve{rateOfChange="
         + this.rateOfChange
         + ", ceiling="
         + this.ceiling
         + ", horizontalShift="
         + this.horizontalShift
         + ", verticalShift="
         + this.verticalShift
         + "} "
         + super.toString();
   }
}
