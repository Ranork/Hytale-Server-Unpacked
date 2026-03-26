package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.Jitter3dPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class Jitter3dPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<Jitter3dPositionProviderAsset> CODEC = BuilderCodec.builder(
         Jitter3dPositionProviderAsset.class, Jitter3dPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Magnitude", Codec.DOUBLE, true), (asset, value) -> asset.magnitude = value, asset -> asset.magnitude)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, value) -> asset.seed = value, asset -> asset.seed)
      .add()
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset
      )
      .add()
      .build();
   private double magnitude = 0.0;
   @Nonnull
   private String seed = "";
   @Nonnull
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return EmptyPositionProvider.INSTANCE;
      } else {
         SeedBox seedBox = argument.parentSeed.child(this.seed);
         PositionProvider positionProvider = this.positionProviderAsset.build(argument);
         return new Jitter3dPositionProvider(this.magnitude, seedBox.createSupplier().get(), positionProvider);
      }
   }

   @Override
   public void cleanUp() {
      this.positionProviderAsset.cleanUp();
   }

   private static boolean isValidScale(@Nonnull Vector3d vector) {
      return vector.x != 0.0 && vector.y != 0.0 && vector.z != 0.0;
   }
}
