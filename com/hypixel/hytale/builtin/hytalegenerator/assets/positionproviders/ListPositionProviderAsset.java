package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.ListPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class ListPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<ListPositionProviderAsset> CODEC = BuilderCodec.builder(
         ListPositionProviderAsset.class, ListPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Positions", new ArrayCodec<>(ListPositionProviderAsset.PositionAsset.CODEC, ListPositionProviderAsset.PositionAsset[]::new), true),
         (asset, v) -> asset.positions = v,
         asset -> asset.positions
      )
      .add()
      .build();
   private ListPositionProviderAsset.PositionAsset[] positions = new ListPositionProviderAsset.PositionAsset[0];

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return EmptyPositionProvider.INSTANCE;
      } else {
         ArrayList<Vector3d> list = new ArrayList<>();

         for (ListPositionProviderAsset.PositionAsset asset : this.positions) {
            Vector3d position = new Vector3d(asset.x, asset.y, asset.z);
            list.add(position);
         }

         return new ListPositionProvider(list);
      }
   }

   public static class PositionAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ListPositionProviderAsset.PositionAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, ListPositionProviderAsset.PositionAsset> CODEC = AssetBuilderCodec.builder(
            ListPositionProviderAsset.PositionAsset.class,
            ListPositionProviderAsset.PositionAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("X", Codec.DOUBLE, true), (t, x) -> t.x = x, t -> t.x)
         .add()
         .append(new KeyedCodec<>("Y", Codec.DOUBLE, true), (t, y) -> t.y = y, t -> t.y)
         .add()
         .append(new KeyedCodec<>("Z", Codec.DOUBLE, true), (t, z) -> t.z = z, t -> t.z)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private double x;
      private double y;
      private double z;

      public String getId() {
         return this.id;
      }
   }
}
