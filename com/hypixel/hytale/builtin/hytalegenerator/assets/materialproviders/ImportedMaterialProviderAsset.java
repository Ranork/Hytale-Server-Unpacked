package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import javax.annotation.Nonnull;

public class ImportedMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<ImportedMaterialProviderAsset> CODEC = BuilderCodec.builder(
         ImportedMaterialProviderAsset.class, ImportedMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.name = k, k -> k.name)
      .add()
      .build();
   private String name = "";

   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.noMaterialProvider();
      } else if (this.name != null && !this.name.isEmpty()) {
         MaterialProviderAsset exportedAsset = MaterialProviderAsset.getExportedAsset(this.name);
         return exportedAsset == null ? MaterialProvider.noMaterialProvider() : exportedAsset.build(argument);
      } else {
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log("An exported Material Provider with the name does not exist: " + this.name);
         return MaterialProvider.noMaterialProvider();
      }
   }
}
