package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ConstantCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.PositionsTwistDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class PositionsTwistDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<PositionsTwistDensityAsset> CODEC = BuilderCodec.builder(
         PositionsTwistDensityAsset.class, PositionsTwistDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset
      )
      .add()
      .append(new KeyedCodec<>("TwistCurve", CurveAsset.CODEC, true), (asset, v) -> asset.pinchCurveAsset = v, asset -> asset.pinchCurveAsset)
      .add()
      .append(new KeyedCodec<>("TwistAxis", Vector3d.CODEC, true), (asset, v) -> asset.twistAxis = v, asset -> asset.twistAxis)
      .add()
      .<Double>append(new KeyedCodec<>("MaxDistance", Codec.DOUBLE, true), (asset, v) -> asset.maxDistance = v, asset -> asset.maxDistance)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .append(new KeyedCodec<>("NormalizeDistance", Codec.BOOLEAN, true), (asset, v) -> asset.normalizeDistance = v, asset -> asset.normalizeDistance)
      .add()
      .append(new KeyedCodec<>("ZeroPositionsY", Codec.BOOLEAN, true), (asset, v) -> asset.zeroPositionsY = v, asset -> asset.zeroPositionsY)
      .add()
      .build();
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
   private CurveAsset pinchCurveAsset = new ConstantCurveAsset();
   private Vector3d twistAxis = new Vector3d();
   private double maxDistance;
   private boolean normalizeDistance;
   private boolean zeroPositionsY;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped()
         ? new ConstantValueDensity(0.0)
         : new PositionsTwistDensity(
            this.buildFirstInput(argument),
            this.positionProviderAsset.build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId)),
            this.pinchCurveAsset.build(),
            this.twistAxis,
            this.maxDistance,
            this.normalizeDistance,
            this.zeroPositionsY
         ));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      this.positionProviderAsset.cleanUp();
      this.pinchCurveAsset.cleanUp();
   }
}
