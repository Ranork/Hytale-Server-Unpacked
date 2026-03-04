package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ExportedDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<ExportedDensityAsset> CODEC = BuilderCodec.builder(
         ExportedDensityAsset.class, ExportedDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("SingleInstance", Codec.BOOLEAN, false), (asset, value) -> asset.singleInstance = value, asset -> asset.singleInstance)
      .add()
      .build();
   private boolean singleInstance = false;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (!this.isSkipped() && this.inputs().length != 0) {
         DensityAsset.Exported exported = getExportedAsset(this.exportName);
         if (exported == null) {
            LoggerUtil.getLogger()
               .severe("Couldn't find Density asset exported with name: '" + this.exportName + "'. This could indicate a defect in the HytaleGenerator assets.");
            return this.firstInput().build(argument);
         } else if (exported.isSingleInstance) {
            Density builtInstance = exported.threadInstances.get(argument.workerId);
            if (builtInstance == null) {
               builtInstance = this.firstInput().build(argument);
               exported.threadInstances.put(argument.workerId, builtInstance);
            }

            return builtInstance;
         } else {
            return this.firstInput().build(argument);
         }
      } else {
         return new ConstantValueDensity(0.0);
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      DensityAsset.Exported exported = getExportedAsset(this.exportName);
      if (exported != null) {
         exported.threadInstances.clear();

         for (DensityAsset input : this.inputs()) {
            input.cleanUp();
         }
      }
   }

   public boolean isSingleInstance() {
      return this.singleInstance;
   }
}
