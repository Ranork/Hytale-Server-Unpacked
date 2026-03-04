package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ImportedDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<ImportedDensityAsset> CODEC = BuilderCodec.builder(
         ImportedDensityAsset.class, ImportedDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.importedNodeName = k, k -> k.importedNodeName)
      .add()
      .build();
   private String importedNodeName = "";

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         DensityAsset.Exported exported = getExportedAsset(this.importedNodeName);
         if (exported == null) {
            LoggerUtil.getLogger().warning("Couldn't find Density asset exported with name: '" + this.importedNodeName + "'. Using empty Node instead.");
            return new ConstantValueDensity(0.0);
         } else if (exported.isSingleInstance) {
            Density builtInstance = exported.threadInstances.get(argument.workerId);
            if (builtInstance == null) {
               builtInstance = exported.asset.build(argument);
               exported.threadInstances.put(argument.workerId, builtInstance);
            }

            return builtInstance;
         } else {
            return exported.asset.build(argument);
         }
      }
   }

   @Override
   public DensityAsset[] inputs() {
      DensityAsset.Exported asset = getExportedAsset(this.importedNodeName);
      if (asset == null) {
         LoggerUtil.getLogger().warning("Couldn't find Density asset exported with name: '" + this.importedNodeName + "'. Using empty Node instead.");
         return new DensityAsset[0];
      } else {
         return asset.asset.inputs();
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      DensityAsset.Exported exported = getExportedAsset(this.importedNodeName);
      if (exported != null) {
         exported.threadInstances.clear();

         for (DensityAsset input : this.inputs()) {
            input.cleanUp();
         }
      }
   }
}
