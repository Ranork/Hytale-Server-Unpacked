package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class ImportedCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<ImportedCurveAsset> CODEC = BuilderCodec.builder(
         ImportedCurveAsset.class, ImportedCurveAsset::new, CurveAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.name = k, k -> k.name)
      .add()
      .build();
   private String name;

   @Override
   public Double2DoubleFunction build() {
      if (this.name != null && !this.name.isEmpty()) {
         CurveAsset exportedAsset = CurveAsset.getExportedAsset(this.name);
         return exportedAsset == null ? in -> 0.0 : exportedAsset.build();
      } else {
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log("An exported Curve with the name does not exist: " + this.name);
         return in -> 0.0;
      }
   }

   @Override
   public void cleanUp() {
   }
}
