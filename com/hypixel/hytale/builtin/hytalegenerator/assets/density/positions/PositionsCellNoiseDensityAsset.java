package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions;

import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions.DistanceFunctionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions.EuclideanDistanceFunctionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.CurveReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.ReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.PositionsDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions.DistanceFunction;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.ReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class PositionsCellNoiseDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<PositionsCellNoiseDensityAsset> CODEC = BuilderCodec.builder(
         PositionsCellNoiseDensityAsset.class, PositionsCellNoiseDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset
      )
      .add()
      .append(new KeyedCodec<>("ReturnType", ReturnTypeAsset.CODEC, true), (asset, v) -> asset.returnTypeAsset = v, asset -> asset.returnTypeAsset)
      .add()
      .append(
         new KeyedCodec<>("DistanceFunction", DistanceFunctionAsset.CODEC, true),
         (asset, v) -> asset.distanceFunctionAsset = v,
         asset -> asset.distanceFunctionAsset
      )
      .add()
      .<Double>append(new KeyedCodec<>("MaxDistance", Codec.DOUBLE, true), (asset, v) -> asset.maxDistance = v, asset -> asset.maxDistance)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
   private ReturnTypeAsset returnTypeAsset = new CurveReturnTypeAsset();
   private DistanceFunctionAsset distanceFunctionAsset = new EuclideanDistanceFunctionAsset();
   private double maxDistance = 0.0;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         PositionProvider positionsField = this.positionProviderAsset
            .build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId));
         ReturnType returnType = this.returnTypeAsset.build(argument.parentSeed, argument.referenceBundle, argument.workerId);
         returnType.setMaxDistance(this.maxDistance);
         DistanceFunction distanceFunction = this.distanceFunctionAsset.build(argument.parentSeed, this.maxDistance);
         return new PositionsDensity(positionsField, returnType, distanceFunction, this.maxDistance);
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      this.positionProviderAsset.cleanUp();
      this.returnTypeAsset.cleanUp();
   }
}
