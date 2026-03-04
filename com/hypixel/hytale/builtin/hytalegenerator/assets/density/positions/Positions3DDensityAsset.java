package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions;

import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ConstantCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.PositionsDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions.EuclideanDistanceFunction;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.CurveReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class Positions3DDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<Positions3DDensityAsset> CODEC = BuilderCodec.builder(
         Positions3DDensityAsset.class, Positions3DDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset
      )
      .add()
      .append(new KeyedCodec<>("DistanceCurve", CurveAsset.CODEC, true), (asset, v) -> asset.curveAsset = v, asset -> asset.curveAsset)
      .add()
      .<Double>append(new KeyedCodec<>("MaxDistance", Codec.DOUBLE, false), (asset, v) -> asset.maxDistance = v, asset -> asset.maxDistance)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
   private CurveAsset curveAsset = new ConstantCurveAsset();
   private double maxDistance = 0.0;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         PositionProvider positionsField = this.positionProviderAsset
            .build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId));
         Double2DoubleFunction curve = this.curveAsset.build();
         CurveReturnType returnType = new CurveReturnType(curve);
         return new PositionsDensity(positionsField, returnType, new EuclideanDistanceFunction(), this.maxDistance);
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      this.positionProviderAsset.cleanUp();
      this.curveAsset.cleanUp();
   }
}
