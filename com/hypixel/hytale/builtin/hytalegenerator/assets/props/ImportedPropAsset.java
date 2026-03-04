package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import javax.annotation.Nonnull;

public class ImportedPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<ImportedPropAsset> CODEC = BuilderCodec.builder(ImportedPropAsset.class, ImportedPropAsset::new, PropAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (asset, v) -> asset.name = v, asset -> asset.name)
      .add()
      .build();
   private String name = "";

   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      if (super.skip()) {
         return Prop.noProp();
      } else if (this.name != null && !this.name.isEmpty()) {
         PropAsset exportedAsset = PropAsset.getExportedAsset(this.name);
         return exportedAsset == null ? Prop.noProp() : exportedAsset.build(argument);
      } else {
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log("An exported Pattern with the name does not exist: " + this.name);
         return Prop.noProp();
      }
   }
}
