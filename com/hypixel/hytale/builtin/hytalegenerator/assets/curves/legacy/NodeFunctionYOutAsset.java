package com.hypixel.hytale.builtin.hytalegenerator.assets.curves.legacy;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.math.NodeFunction;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.HashSet;
import javax.annotation.Nonnull;
import org.joml.Vector2d;

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
      .addValidator(new Validator<PointYOutAsset[]>() {
         public void accept(PointYOutAsset[] v, ValidationResults r) {
            HashSet<Double> ySet = new HashSet<>(v.length);

            for (PointYOutAsset point : v) {
               if (ySet.contains(point.getY())) {
                  r.fail("More than one point with Y value: " + point.getY());
                  return;
               }

               ySet.add(point.getY());
            }
         }

         @Override
         public void updateSchema(SchemaContext context, Schema target) {
         }
      })
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
