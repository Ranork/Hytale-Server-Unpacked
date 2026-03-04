package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpaceAndDepthMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final SpaceAndDepthMaterialProvider.LayerContextType layerContextType;
   @Nonnull
   private final SpaceAndDepthMaterialProvider.Layer<V>[] layers;
   @Nonnull
   private final SpaceAndDepthMaterialProvider.Condition condition;
   private final int maxDistance;

   public SpaceAndDepthMaterialProvider(
      @Nonnull SpaceAndDepthMaterialProvider.LayerContextType layerContextType,
      @Nonnull List<SpaceAndDepthMaterialProvider.Layer<V>> layers,
      @Nonnull SpaceAndDepthMaterialProvider.Condition condition,
      int maxDistance
   ) {
      this.layerContextType = layerContextType;
      this.maxDistance = maxDistance;
      this.layers = new SpaceAndDepthMaterialProvider.Layer[layers.size()];

      for (int i = 0; i < layers.size(); i++) {
         SpaceAndDepthMaterialProvider.Layer<V> l = layers.get(i);
         if (l == null) {
            LoggerUtil.getLogger().warning("Couldn't retrieve layer with index " + i);
         } else {
            this.layers[i] = l;
         }
      }

      this.condition = condition;
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      int distance = switch (this.layerContextType) {
         case DEPTH_INTO_FLOOR -> context.depthIntoFloor;
         case DEPTH_INTO_CEILING -> context.depthIntoCeiling;
      };
      if (distance > this.maxDistance) {
         return null;
      } else if (!this.condition
         .qualifies(
            context.position.x,
            context.position.y,
            context.position.z,
            context.depthIntoFloor,
            context.depthIntoCeiling,
            context.spaceAboveFloor,
            context.spaceBelowCeiling
         )) {
         return null;
      } else {
         MaterialProvider<V> material = null;
         int depthAccumulator = 0;

         for (SpaceAndDepthMaterialProvider.Layer<V> l : this.layers) {
            int layerDepth = l.getThicknessAt(
               context.position.x,
               context.position.y,
               context.position.z,
               context.depthIntoFloor,
               context.depthIntoCeiling,
               context.spaceAboveFloor,
               context.spaceBelowCeiling,
               context.distanceToBiomeEdge
            );
            int nextDepthAccumulator = depthAccumulator + layerDepth;
            if (distance > depthAccumulator && distance <= nextDepthAccumulator) {
               material = l.getMaterialProvider();
               break;
            }

            depthAccumulator = nextDepthAccumulator;
         }

         return material == null ? null : material.getVoxelTypeAt(context);
      }
   }

   public interface Condition {
      boolean qualifies(int var1, int var2, int var3, int var4, int var5, int var6, int var7);
   }

   public abstract static class Layer<V> {
      public abstract int getThicknessAt(int var1, int var2, int var3, int var4, int var5, int var6, int var7, double var8);

      @Nullable
      public abstract MaterialProvider<V> getMaterialProvider();
   }

   public static enum LayerContextType {
      DEPTH_INTO_FLOOR,
      DEPTH_INTO_CEILING;

      @Nonnull
      public static final Codec<SpaceAndDepthMaterialProvider.LayerContextType> CODEC = new EnumCodec<>(
         SpaceAndDepthMaterialProvider.LayerContextType.class, EnumCodec.EnumStyle.LEGACY
      );
   }
}
