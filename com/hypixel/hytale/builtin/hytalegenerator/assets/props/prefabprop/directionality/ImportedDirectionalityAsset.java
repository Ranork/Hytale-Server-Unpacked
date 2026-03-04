package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import javax.annotation.Nonnull;

public class ImportedDirectionalityAsset extends DirectionalityAsset {
   @Nonnull
   public static final BuilderCodec<ImportedDirectionalityAsset> CODEC = BuilderCodec.builder(
         ImportedDirectionalityAsset.class, ImportedDirectionalityAsset::new, DirectionalityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (asset, v) -> asset.name = v, asset -> asset.name)
      .add()
      .build();
   private String name = "";

   @Override
   public Directionality build(@Nonnull DirectionalityAsset.Argument argument) {
      if (this.name != null && !this.name.isEmpty()) {
         DirectionalityAsset exportedAsset = DirectionalityAsset.getExportedAsset(this.name);
         return exportedAsset == null ? Directionality.noDirectionality() : exportedAsset.build(argument);
      } else {
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log("An exported Pattern with the name does not exist: " + this.name);
         return Directionality.noDirectionality();
      }
   }
}
