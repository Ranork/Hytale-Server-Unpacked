package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import javax.annotation.Nonnull;

public class ImportedPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<ImportedPatternAsset> CODEC = BuilderCodec.builder(
         ImportedPatternAsset.class, ImportedPatternAsset::new, PatternAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.name = k, k -> k.name)
      .add()
      .build();
   private String name = "";

   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else if (this.name != null && !this.name.isEmpty()) {
         PatternAsset exportedAsset = PatternAsset.getExportedAsset(this.name);
         return exportedAsset == null ? Pattern.noPattern() : exportedAsset.build(argument);
      } else {
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log("An exported Pattern with the name does not exist: " + this.name);
         return Pattern.noPattern();
      }
   }
}
