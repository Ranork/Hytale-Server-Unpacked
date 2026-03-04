package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.MultiMixDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class MultiMixDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<MultiMixDensityAsset> CODEC = BuilderCodec.builder(
         MultiMixDensityAsset.class, MultiMixDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Keys", new ArrayCodec<>(MultiMixDensityAsset.KeyAsset.CODEC, MultiMixDensityAsset.KeyAsset[]::new), true),
         (asset, v) -> asset.keyAssets = v,
         asset -> asset.keyAssets
      )
      .add()
      .build();
   private MultiMixDensityAsset.KeyAsset[] keyAssets = new MultiMixDensityAsset.KeyAsset[0];

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         List<Density> densityInputs = this.buildInputs(argument, true);
         if (densityInputs.isEmpty()) {
            return new ConstantValueDensity(0.0);
         } else {
            ArrayList<MultiMixDensity.Key> keys = new ArrayList<>(this.keyAssets.length);

            for (MultiMixDensityAsset.KeyAsset keyAsset : this.keyAssets) {
               if (keyAsset.densityIndex < 0) {
                  keys.add(new MultiMixDensity.Key(keyAsset.value, null));
               } else if (keyAsset.densityIndex >= densityInputs.size() - 1) {
                  LoggerUtil.getLogger()
                     .warning(
                        "Density Index out of bounds in MultiMix node " + keyAsset.densityIndex + ", valid range is [0, " + (densityInputs.size() - 1) + "]"
                     );
                  keys.add(new MultiMixDensity.Key(keyAsset.value, null));
               } else {
                  Density density = densityInputs.get(keyAsset.densityIndex);
                  keys.add(new MultiMixDensity.Key(keyAsset.value, density));
               }
            }

            int i = 1;

            while (i < keys.size()) {
               MultiMixDensity.Key previousKey = keys.get(i - 1);
               MultiMixDensity.Key currentKey = keys.get(i);
               if (previousKey.value() == currentKey.value()) {
                  keys.remove(i);
               } else {
                  i++;
               }
            }

            i = 0;

            while (i < keys.size()) {
               if (keys.get(i).density() == null) {
                  keys.remove(i);
               } else {
                  i++;
               }
            }

            for (int ix = keys.size() - 1; ix >= 0 && keys.get(ix).density() == null; ix--) {
               keys.remove(ix);
            }

            for (int ix = keys.size() - 2; ix >= 0; ix--) {
               if (keys.get(ix).density() == null && keys.get(ix + 1).density() == null) {
                  keys.remove(ix);
               }
            }

            if (keys.isEmpty()) {
               return new ConstantValueDensity(0.0);
            } else if (keys.size() == 1) {
               return keys.getFirst().density();
            } else {
               keys.trimToSize();
               Density influenceDensity = densityInputs.getLast();
               return new MultiMixDensity(keys, influenceDensity);
            }
         }
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }

   public static class KeyAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, MultiMixDensityAsset.KeyAsset>> {
      public static final int NO_DENSITY_INDEX = 0;
      @Nonnull
      public static final AssetBuilderCodec<String, MultiMixDensityAsset.KeyAsset> CODEC = AssetBuilderCodec.builder(
            MultiMixDensityAsset.KeyAsset.class,
            MultiMixDensityAsset.KeyAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Value", Codec.DOUBLE, true), (t, value) -> t.value = value, t -> t.value)
         .add()
         .append(new KeyedCodec<>("DensityIndex", Codec.INTEGER, true), (t, value) -> t.densityIndex = value, t -> t.densityIndex)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private double value = 0.0;
      private int densityIndex = 0;

      public String getId() {
         return this.id;
      }
   }
}
