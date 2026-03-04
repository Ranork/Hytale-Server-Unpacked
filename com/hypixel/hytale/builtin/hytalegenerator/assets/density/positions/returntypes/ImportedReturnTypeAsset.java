package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.ReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ImportedReturnTypeAsset extends ReturnTypeAsset {
   @Nonnull
   public static final BuilderCodec<ImportedReturnTypeAsset> CODEC = BuilderCodec.builder(
         ImportedReturnTypeAsset.class, ImportedReturnTypeAsset::new, ReturnTypeAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.importedAssetName = k, k -> k.importedAssetName)
      .add()
      .build();
   private String importedAssetName = "";

   @Override
   public ReturnType build(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
      ReturnTypeAsset asset = getExportedAsset(this.importedAssetName);
      if (asset == null) {
         Logger.getLogger("Density")
            .warning("Couldn't find ReturnType asset exported with name: '" + this.importedAssetName + "'. Using a return type that only outputs 0 instead.");
         return new ReturnType() {
            @Override
            public double get(
               double distance0,
               double distance1,
               @Nonnull Vector3d samplePoint,
               @Nullable Vector3d closestPoint0,
               Vector3d closestPoint1,
               @Nullable Density.Context context
            ) {
               return 0.0;
            }
         };
      } else {
         return asset.build(parentSeed, referenceBundle, workerId);
      }
   }
}
