package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ImportedPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<ImportedPositionProviderAsset> CODEC = BuilderCodec.builder(
         ImportedPositionProviderAsset.class, ImportedPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (asset, v) -> asset.name = v, asset -> asset.name)
      .add()
      .build();
   private String name = "";

   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return PositionProvider.noPositionProvider();
      } else {
         PositionProviderAsset asset = getExportedAsset(this.name);
         if (asset == null) {
            LoggerUtil.getLogger().warning("Couldn't find Positions asset exported with name: '" + this.name + "'.");
            return PositionProvider.noPositionProvider();
         } else {
            return asset.build(argument);
         }
      }
   }
}
