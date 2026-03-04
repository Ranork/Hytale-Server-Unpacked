package com.hypixel.hytale.server.core.asset.type.responsecurve;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.responsecurve.config.ResponseCurve;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ScaledXResponseCurve extends ScaledResponseCurve {
   public static final BuilderCodec<ScaledXResponseCurve> CODEC = BuilderCodec.builder(ScaledXResponseCurve.class, ScaledXResponseCurve::new)
      .documentation("A response curve scaled only on the x axis.")
      .<String>append(new KeyedCodec<>("ResponseCurve", Codec.STRING), (curve, s) -> curve.responseCurve = s, curve -> curve.responseCurve)
      .documentation("The response curve to scale")
      .addValidator(Validators.nonNull())
      .addValidator(ResponseCurve.VALIDATOR_CACHE.getValidator())
      .add()
      .<double[]>append(new KeyedCodec<>("XRange", Codec.DOUBLE_ARRAY), (curve, o) -> curve.xRange = o, curve -> new double[]{curve.xRange[0], curve.xRange[1]})
      .documentation("The range to map the x axis to. e.g. [ 0, 10 ]")
      .addValidator(Validators.doubleArraySize(2))
      .addValidator(Validators.monotonicSequentialDoubleArrayValidator())
      .add()
      .afterDecode(curve -> {
         if (curve.responseCurve != null) {
            int index = ResponseCurve.getAssetMap().getIndex(curve.responseCurve);
            curve.responseCurveReference = new ResponseCurve.Reference(index, ResponseCurve.getAssetMap().getAsset(index));
         }
      })
      .build();
   public static final double[] DEFAULT_RANGE = new double[]{0.0, 1.0};
   protected String responseCurve;
   protected ResponseCurve.Reference responseCurveReference;
   protected double[] xRange = DEFAULT_RANGE;

   public ScaledXResponseCurve(String responseCurve, double[] xRange) {
      this.responseCurve = responseCurve;
      this.xRange = xRange;
   }

   protected ScaledXResponseCurve() {
   }

   public String getResponseCurve() {
      return this.responseCurve;
   }

   public double[] getXRange() {
      return this.xRange;
   }

   @Override
   public double computeY(double x) {
      return MathUtil.clamp(this.computeNormalisedY(x), 0.0, 1.0);
   }

   protected double computeNormalisedY(double x) {
      ResponseCurve curve = this.responseCurveReference.get();
      double minX = this.xRange[0];
      double maxX = this.xRange[1];
      x = MathUtil.clamp(x, minX, maxX);
      double normalisedX = (x - minX) / (maxX - minX);
      return curve.computeY(normalisedX);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ScaledXResponseCurve{responseCurve=" + this.responseCurve + ", xRange=" + Arrays.toString(this.xRange) + "}" + super.toString();
   }
}
