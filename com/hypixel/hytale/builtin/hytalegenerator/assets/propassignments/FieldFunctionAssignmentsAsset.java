package com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.FieldFunctionAssignments;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class FieldFunctionAssignmentsAsset extends AssignmentsAsset {
   @Nonnull
   public static final BuilderCodec<FieldFunctionAssignmentsAsset> CODEC = BuilderCodec.builder(
         FieldFunctionAssignmentsAsset.class, FieldFunctionAssignmentsAsset::new, AssignmentsAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>(
            "Delimiters", new ArrayCodec<>(FieldFunctionAssignmentsAsset.DelimiterAsset.CODEC, FieldFunctionAssignmentsAsset.DelimiterAsset[]::new), true
         ),
         (asset, v) -> asset.delimiterAssets = v,
         asset -> asset.delimiterAssets
      )
      .add()
      .append(new KeyedCodec<>("FieldFunction", DensityAsset.CODEC, true), (asset, v) -> asset.densityAsset = v, asset -> asset.densityAsset)
      .add()
      .build();
   private FieldFunctionAssignmentsAsset.DelimiterAsset[] delimiterAssets = new FieldFunctionAssignmentsAsset.DelimiterAsset[0];
   private DensityAsset densityAsset = new ConstantDensityAsset();

   @Nonnull
   @Override
   public Assignments build(@Nonnull AssignmentsAsset.Argument argument) {
      if (super.skip()) {
         return Assignments.noPropDistribution(argument.runtime);
      } else {
         Density functionTree = this.densityAsset.build(DensityAsset.from(argument));
         ArrayList<FieldFunctionAssignments.FieldDelimiter> delimiterList = new ArrayList<>();

         for (FieldFunctionAssignmentsAsset.DelimiterAsset asset : this.delimiterAssets) {
            Assignments propDistribution = asset.assignmentsAsset.build(argument);
            FieldFunctionAssignments.FieldDelimiter delimiter = new FieldFunctionAssignments.FieldDelimiter(propDistribution, asset.min, asset.max);
            delimiterList.add(delimiter);
         }

         return new FieldFunctionAssignments(functionTree, delimiterList, argument.runtime);
      }
   }

   @Override
   public void cleanUp() {
      this.densityAsset.cleanUp();
   }

   public static class DelimiterAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, FieldFunctionAssignmentsAsset.DelimiterAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, FieldFunctionAssignmentsAsset.DelimiterAsset> CODEC = AssetBuilderCodec.builder(
            FieldFunctionAssignmentsAsset.DelimiterAsset.class,
            FieldFunctionAssignmentsAsset.DelimiterAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Assignments", AssignmentsAsset.CODEC, true), (t, v) -> t.assignmentsAsset = v, t -> t.assignmentsAsset)
         .add()
         .append(new KeyedCodec<>("Min", Codec.DOUBLE, true), (t, v) -> t.min = v, t -> t.min)
         .add()
         .append(new KeyedCodec<>("Max", Codec.DOUBLE, true), (t, v) -> t.max = v, t -> t.max)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private double min;
      private double max;
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
