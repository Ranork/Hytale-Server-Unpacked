package com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.WeightedAssignments;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public class WeightedAssignmentsAsset extends AssignmentsAsset {
   @Nonnull
   public static final BuilderCodec<WeightedAssignmentsAsset> CODEC = BuilderCodec.builder(
         WeightedAssignmentsAsset.class, WeightedAssignmentsAsset::new, AssignmentsAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("SkipChance", Codec.DOUBLE, true), (asset, v) -> asset.skipChance = v, asset -> asset.skipChance)
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, v) -> asset.seed = v, asset -> asset.seed)
      .add()
      .append(
         new KeyedCodec<>(
            "WeightedAssignments", new ArrayCodec<>(WeightedAssignmentsAsset.WeightedAssets.CODEC, WeightedAssignmentsAsset.WeightedAssets[]::new), true
         ),
         (asset, v) -> asset.weightedAssets = v,
         asset -> asset.weightedAssets
      )
      .add()
      .build();
   private WeightedAssignmentsAsset.WeightedAssets[] weightedAssets = new WeightedAssignmentsAsset.WeightedAssets[0];
   private String seed = "";
   private double skipChance = 0.0;

   @Nonnull
   @Override
   public Assignments build(@Nonnull AssignmentsAsset.Argument argument) {
      if (super.skip()) {
         return Assignments.noPropDistribution(argument.runtime);
      } else {
         WeightedMap<Assignments> weightMap = new WeightedMap<>();

         for (WeightedAssignmentsAsset.WeightedAssets asset : this.weightedAssets) {
            weightMap.add(asset.assignmentsAsset.build(argument), asset.weight);
         }

         SeedBox childSeed = argument.parentSeed.child(this.seed);
         return new WeightedAssignments(weightMap, childSeed.createSupplier().get(), this.skipChance, argument.runtime);
      }
   }

   @Override
   public void cleanUp() {
      for (WeightedAssignmentsAsset.WeightedAssets weightedAsset : this.weightedAssets) {
         weightedAsset.cleanUp();
      }
   }

   public static class WeightedAssets implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, WeightedAssignmentsAsset.WeightedAssets>> {
      @Nonnull
      public static final AssetBuilderCodec<String, WeightedAssignmentsAsset.WeightedAssets> CODEC = AssetBuilderCodec.builder(
            WeightedAssignmentsAsset.WeightedAssets.class,
            WeightedAssignmentsAsset.WeightedAssets::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, v) -> t.weight = v, t -> t.weight)
         .add()
         .append(new KeyedCodec<>("Assignments", AssignmentsAsset.CODEC, true), (t, v) -> t.assignmentsAsset = v, t -> t.assignmentsAsset)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private double weight = 1.0;
      private AssignmentsAsset assignmentsAsset = new ConstantAssignmentsAsset();

      public String getId() {
         return this.id;
      }

      @Override
      public void cleanUp() {
         this.assignmentsAsset.cleanUp();
      }
   }
}
