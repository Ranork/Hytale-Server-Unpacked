package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.ScalerPositionProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class ScalerPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<ScalerPositionProviderAsset> CODEC = BuilderCodec.builder(
         ScalerPositionProviderAsset.class, ScalerPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Scale", Vector3dUtil.CODEC, true), (asset, v) -> asset.scale = v, asset -> asset.scale)
      .addValidator(new Validator<Vector3d>() {
         public void accept(Vector3d vector, ValidationResults results) {
            if (!ScalerPositionProviderAsset.isValidScale(vector)) {
               String msg = "Scale Vector " + vector.toString() + " has one or more zero members.";
               results.fail(msg);
            }
         }

         @Override
         public void updateSchema(SchemaContext context, Schema target) {
         }
      })
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
