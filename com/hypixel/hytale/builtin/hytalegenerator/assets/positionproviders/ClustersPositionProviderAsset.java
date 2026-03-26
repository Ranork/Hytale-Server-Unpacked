package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.DecimalBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.ClustersPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ClustersPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<ClustersPositionProviderAsset> CODEC = BuilderCodec.builder(
         ClustersPositionProviderAsset.class, ClustersPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Cluster", PositionProviderAsset.CODEC, true),
         (asset, value) -> asset.clusterPositionProviderAsset = value,
         asset -> asset.clusterPositionProviderAsset
      )
      .add()
      .append(
         new KeyedCodec<>("Distributor", PositionProviderAsset.CODEC, true),
         (asset, value) -> asset.distributorPositionProviderAsset = value,
         asset -> asset.distributorPositionProviderAsset
      )
      .add()
      .append(
         new KeyedCodec<>("ClusterBounds", DecimalBounds3dAsset.CODEC, true), (asset, v) -> asset.clusterBoundsAsset = v, asset -> asset.clusterBoundsAsset
      )
      .add()
      .build();
   @Nonnull
   private PositionProviderAsset clusterPositionProviderAsset = new ListPositionProviderAsset();
   @Nonnull
   private PositionProviderAsset distributorPositionProviderAsset = new ListPositionProviderAsset();
   @Nonnull
   private DecimalBounds3dAsset clusterBoundsAsset = new DecimalBounds3dAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return EmptyPositionProvider.INSTANCE;
      } else {
         PositionProvider clusterPositionProvider = this.clusterPositionProviderAsset.build(argument);
         PositionProvider distributorPositionProvider = this.distributorPositionProviderAsset.build(argument);
         Bounds3d clusterBounds = this.clusterBoundsAsset.build();
         return new ClustersPositionProvider(clusterPositionProvider, distributorPositionProvider, clusterBounds);
      }
   }

   @Override
   public void cleanUp() {
      this.clusterPositionProviderAsset.cleanUp();
      this.distributorPositionProviderAsset.cleanUp();
   }

   private static boolean isValidScale(@Nonnull Vector3d vector) {
      return vector.x != 0.0 && vector.y != 0.0 && vector.z != 0.0;
   }
}
