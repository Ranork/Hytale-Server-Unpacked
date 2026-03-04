package com.hypixel.hytale.builtin.hytalegenerator.assets.curves.legacy;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.NodeFunction;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.math.vector.Vector2d;
import java.util.HashSet;
import javax.annotation.Nonnull;

public class NodeFunctionYOutAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, NodeFunctionYOutAsset>>, Cleanable {
   @Nonnull
   public static final AssetBuilderCodec<String, NodeFunctionYOutAsset> CODEC = AssetBuilderCodec.builder(
         NodeFunctionYOutAsset.class,
         NodeFunctionYOutAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("Points", new ArrayCodec<>(PointYOutAsset.CODEC, PointYOutAsset[]::new), true), (t, k) -> t.nodes = k, t -> t.nodes)
      .addValidator((LegacyValidator<? super PointYOutAsset[]>)((v, r) -> {
         HashSet<Double> ySet = new HashSet<>(v.length);

         for (PointYOutAsset point : v) {
            if (ySet.contains(point.getY())) {
               r.fail("More than one point with Y value: " + point.getY());
               return;
            }

            ySet.add(point.getY());
         }
      }))
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private PointYOutAsset[] nodes = new PointYOutAsset[0];

   @Nonnull
   public NodeFunction build() {
      NodeFunction nodeFunction = new NodeFunction();

      for (PointYOutAsset node : this.nodes) {
         Vector2d point = node.build();
         nodeFunction.addPoint(point.x, point.y);
      }

      return nodeFunction;
   }

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
   }
}
