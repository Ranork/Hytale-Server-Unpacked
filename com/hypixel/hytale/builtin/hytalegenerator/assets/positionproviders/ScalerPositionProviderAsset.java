package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.ScalerPositionProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ScalerPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<ScalerPositionProviderAsset> CODEC = BuilderCodec.builder(
         ScalerPositionProviderAsset.class, ScalerPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Scale", Vector3d.CODEC, true), (asset, v) -> asset.scale = v, asset -> asset.scale)
      .addValidator((LegacyValidator<? super Vector3d>)((vector, result) -> {
         if (!isValidScale(vector)) {
            String msg = "Scale Vector " + vector.toString() + " has one or more zero members.";
            result.fail(msg);
         }
      }))
      .add()
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset
      )
      .add()
      .build();
   @Nonnull
   private Vector3d scale = new Vector3d();
   @Nonnull
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (!super.skip() && isValidScale(this.scale)) {
         PositionProvider positionProvider = this.positionProviderAsset.build(argument);
         return new ScalerPositionProvider(this.scale, positionProvider);
      } else {
         return EmptyPositionProvider.INSTANCE;
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
