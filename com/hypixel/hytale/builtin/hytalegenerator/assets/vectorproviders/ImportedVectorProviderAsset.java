package com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.ConstantVectorProvider;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.VectorProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ImportedVectorProviderAsset extends VectorProviderAsset {
   @Nonnull
   public static final BuilderCodec<ImportedVectorProviderAsset> CODEC = BuilderCodec.builder(
         ImportedVectorProviderAsset.class, ImportedVectorProviderAsset::new, VectorProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.importedNodeName = k, k -> k.importedNodeName)
      .add()
      .build();
   private String importedNodeName = "";

   @Override
   public VectorProvider build(@Nonnull VectorProviderAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantVectorProvider(new Vector3d());
      } else {
         VectorProviderAsset.Exported exported = getExportedAsset(this.importedNodeName);
         if (exported == null) {
            LoggerUtil.getLogger().warning("Couldn't find VectorProvider asset exported with name: '" + this.importedNodeName + "'. Using empty Node instead.");
            return new ConstantVectorProvider(new Vector3d());
         } else if (exported.isSingleInstance) {
            VectorProvider builtInstance = exported.threadInstances.get(argument.workerId);
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
   public void cleanUp() {
      VectorProviderAsset.Exported exported = getExportedAsset(this.importedNodeName);
      if (exported != null) {
         exported.threadInstances.clear();
      }
   }
}
