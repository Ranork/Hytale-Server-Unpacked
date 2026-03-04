package com.hypixel.hytale.builtin.hytalegenerator.assets.curves.manual;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.NodeFunction;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.math.vector.Vector2d;
import java.util.HashSet;
import javax.annotation.Nonnull;

public class ManualCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<ManualCurveAsset> CODEC = BuilderCodec.builder(ManualCurveAsset.class, ManualCurveAsset::new, CurveAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Points", new ArrayCodec<>(PointInOutAsset.CODEC, PointInOutAsset[]::new), true), (t, k) -> t.nodes = k, t -> t.nodes)
      .addValidator((LegacyValidator<? super PointInOutAsset[]>)((v, r) -> {
         HashSet<Double> ySet = new HashSet<>(v.length);

         for (PointInOutAsset point : v) {
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
   private PointInOutAsset[] nodes = new PointInOutAsset[0];

   private ManualCurveAsset() {
   }

   @Nonnull
   public NodeFunction build() {
      NodeFunction nodeFunction = new NodeFunction();

      for (PointInOutAsset node : this.nodes) {
         Vector2d point = node.build();
         nodeFunction.addPoint(point.x, point.y);
      }

      return nodeFunction;
   }

   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
   }
}
