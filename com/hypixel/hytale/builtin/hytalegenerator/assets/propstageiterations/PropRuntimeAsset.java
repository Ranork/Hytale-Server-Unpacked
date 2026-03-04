package com.hypixel.hytale.builtin.hytalegenerator.assets.propstageiterations;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.AssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.ConstantAssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import javax.annotation.Nonnull;

public class PropRuntimeAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, PropRuntimeAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, PropRuntimeAsset> CODEC = AssetBuilderCodec.builder(
         PropRuntimeAsset.class,
         PropRuntimeAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("Runtime", Codec.INTEGER, true), (t, k) -> t.runtime = k, t -> t.runtime)
      .add()
      .append(new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (t, k) -> t.positionProviderAsset = k, t -> t.positionProviderAsset)
      .add()
      .append(new KeyedCodec<>("Assignments", AssignmentsAsset.CODEC, true), (t, k) -> t.assignmentsAsset = k, t -> t.assignmentsAsset)
      .add()
      .append(new KeyedCodec<>("Skip", Codec.BOOLEAN, false), (t, k) -> t.skip = k, t -> t.skip)
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private boolean skip = false;
   private int runtime = 0;
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
   private AssignmentsAsset assignmentsAsset = new ConstantAssignmentsAsset();

   protected PropRuntimeAsset() {
   }

   public boolean isSkip() {
      return this.skip;
   }

   @Override
   public void cleanUp() {
      this.positionProviderAsset.cleanUp();
      this.assignmentsAsset.cleanUp();
   }

   public PositionProvider buildPositionProvider(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
      return this.positionProviderAsset.build(new PositionProviderAsset.Argument(parentSeed, referenceBundle, workerId));
   }

   public Assignments buildPropDistribution(
      @Nonnull SeedBox parentSeed,
      @Nonnull MaterialCache materialCache,
      int runtime,
      @Nonnull ReferenceBundle referenceBundle,
      @Nonnull WorkerIndexer.Id workerId
   ) {
      return this.assignmentsAsset.build(new AssignmentsAsset.Argument(parentSeed, materialCache, referenceBundle, runtime, workerId));
   }

   public int getRuntime() {
      return this.runtime;
   }

   public String getId() {
      return this.id;
   }
}
