package com.hypixel.hytale.builtin.hytalegenerator.assets.propruntime;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.assignments.AssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.assignments.ConstantAssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.NoPropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assignments.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.AssignedPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PositionsPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
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
      .append(new KeyedCodec<>("PropDistribution", PropDistributionAsset.CODEC, true), (t, k) -> t.propDistributionAsset = k, t -> t.propDistributionAsset)
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
   private PropDistributionAsset propDistributionAsset = NoPropDistributionAsset.INSTANCE;

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

   public PositionProvider buildPositionProvider_deprecated(
      @Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId
   ) {
      return this.positionProviderAsset.build(new PositionProviderAsset.Argument(parentSeed, referenceBundle, workerId));
   }

   public Assignments buildAssignments_deprecated(
      @Nonnull SeedBox parentSeed, @Nonnull MaterialCache materialCache, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId
   ) {
      return this.assignmentsAsset.build(new AssignmentsAsset.Argument(parentSeed, materialCache, referenceBundle, workerId));
   }

   @Nonnull
   public PropDistribution buildPropDistribution(
      @Nonnull SeedBox parentSeed,
      @Nonnull MaterialCache materialCache,
      int runtime,
      @Nonnull ReferenceBundle referenceBundle,
      @Nonnull WorkerIndexer.Id workerId
   ) {
      if (this.propDistributionAsset == NoPropDistributionAsset.INSTANCE) {
         PositionProvider positionProvider = this.buildPositionProvider_deprecated(parentSeed, referenceBundle, workerId);
         PositionsPropDistribution propDistribution = new PositionsPropDistribution(positionProvider);
         Assignments assignments = this.buildAssignments_deprecated(parentSeed, materialCache, referenceBundle, workerId);
         return new AssignedPropDistribution(propDistribution, assignments, true);
      } else {
         return this.propDistributionAsset.build(new PropDistributionAsset.Argument(parentSeed, materialCache, referenceBundle, workerId));
      }
   }

   public int getRuntime() {
      return this.runtime;
   }

   public String getId() {
      return this.id;
   }
}
