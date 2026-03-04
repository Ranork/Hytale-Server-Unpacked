package com.hypixel.hytale.server.worldgen.container;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.supplier.IDoubleCoordinateSupplier;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.NoiseBlockArray;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LayerContainer {
   @Nonnull
   protected final BlockFluidEntry filling;
   protected final int fillingEnvironment;
   protected final LayerContainer.StaticLayer[] staticLayers;
   protected final LayerContainer.DynamicLayer[] dynamicLayers;

   public LayerContainer(int filling, int fillingEnvironment, LayerContainer.StaticLayer[] staticLayers, LayerContainer.DynamicLayer[] dynamicLayers) {
      this.filling = new BlockFluidEntry(filling, 0, 0);
      this.fillingEnvironment = fillingEnvironment;
      this.staticLayers = staticLayers;
      this.dynamicLayers = dynamicLayers;
   }

   public BlockFluidEntry getFilling() {
      return this.filling;
   }

   public int getFillingEnvironment() {
      return this.fillingEnvironment;
   }

   public LayerContainer.StaticLayer[] getStaticLayers() {
      return this.staticLayers;
   }

   public LayerContainer.DynamicLayer[] getDynamicLayers() {
      return this.dynamicLayers;
   }

   public BlockFluidEntry getTopBlockAt(int seed, int x, int z) {
      for (LayerContainer.DynamicLayer layer : this.dynamicLayers) {
         LayerContainer.DynamicLayerEntry entry = layer.getActiveEntry(seed, x, z);
         if (entry != null) {
            return entry.blockArray.getTopBlockAt(seed, x, z);
         }
      }

      return this.filling;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LayerContainer{filling="
         + this.filling
         + ", staticLayers="
         + Arrays.toString((Object[])this.staticLayers)
         + ", dynamicLayers="
         + Arrays.toString((Object[])this.dynamicLayers)
         + "}";
   }

   public static class DynamicLayer extends LayerContainer.Layer<LayerContainer.DynamicLayerEntry> {
      protected final IDoubleCoordinateSupplier offset;

      public DynamicLayer(LayerContainer.DynamicLayerEntry[] entries, ICoordinateCondition mapCondition, int environmentId, IDoubleCoordinateSupplier offset) {
         super(entries, mapCondition, environmentId);
         this.offset = offset;
      }

      public int getOffset(int seed, int x, int z) {
         return MathUtil.floor(this.offset.get(seed, x, z));
      }

      @Nonnull
      @Override
      public String toString() {
         return "DynamicLayer{entries=" + Arrays.toString((Object[])this.entries) + ", offset=" + this.offset + "}";
      }
   }

   public static class DynamicLayerEntry extends LayerContainer.LayerEntry {
      public DynamicLayerEntry(NoiseBlockArray blockArray, ICoordinateCondition mapCondition) {
         super(blockArray, mapCondition);
      }

      @Nonnull
      @Override
      public String toString() {
         return "DynamicLayerEntry{blockArray=" + this.blockArray + ", mapCondition=" + this.mapCondition + "}";
      }
   }

   public static class Layer<T extends LayerContainer.LayerEntry> {
      protected final T[] entries;
      protected final ICoordinateCondition mapCondition;
      protected final int environmentId;

      public Layer(T[] entries, ICoordinateCondition mapCondition, int environmentId) {
         this.entries = entries;
         this.mapCondition = mapCondition;
         this.environmentId = environmentId;
      }

      public int getEnvironmentId() {
         return this.environmentId;
      }

      @Nullable
      public T getActiveEntry(int seed, int x, int z) {
         if (!this.mapCondition.eval(seed, x, z)) {
            return null;
         } else {
            for (T entry : this.entries) {
               if (entry.isActive(seed, x, z)) {
                  return entry;
               }
            }

            return null;
         }
      }

      @Nonnull
      @Override
      public String toString() {
         return "Layer{entries=" + Arrays.toString((Object[])this.entries) + "}";
      }
   }

   public abstract static class LayerEntry {
      protected final NoiseBlockArray blockArray;
      protected final ICoordinateCondition mapCondition;

      public LayerEntry(NoiseBlockArray blockArray, ICoordinateCondition mapCondition) {
         this.blockArray = blockArray;
         this.mapCondition = mapCondition;
      }

      public boolean isActive(int seed, int x, int z) {
         return this.mapCondition.eval(seed, x, z);
      }

      public NoiseBlockArray getBlockArray() {
         return this.blockArray;
      }

      @Nonnull
      @Override
      public String toString() {
         return "LayerEntry{blockArray=" + this.blockArray + ", mapCondition=" + this.mapCondition + "}";
      }
   }

   public static class StaticLayer extends LayerContainer.Layer<LayerContainer.StaticLayerEntry> {
      public StaticLayer(LayerContainer.StaticLayerEntry[] entries, ICoordinateCondition mapCondition, int environmentId) {
         super(entries, mapCondition, environmentId);
      }

      @Nonnull
      @Override
      public String toString() {
         return "StaticLayer{entries=" + Arrays.toString((Object[])this.entries) + "}";
      }
   }

   public static class StaticLayerEntry extends LayerContainer.LayerEntry {
      protected final IDoubleCoordinateSupplier min;
      protected final IDoubleCoordinateSupplier max;

      public StaticLayerEntry(NoiseBlockArray blockArray, ICoordinateCondition mapCondition, IDoubleCoordinateSupplier min, IDoubleCoordinateSupplier max) {
         super(blockArray, mapCondition);
         this.min = min;
         this.max = max;
      }

      public int getMinInt(int seed, int x, int z) {
         return MathUtil.floor(this.getMinValue(seed, x, z));
      }

      public double getMinValue(int seed, int x, int z) {
         return this.min.get(seed, x, z);
      }

      public int getMaxInt(int seed, int x, int z) {
         return MathUtil.floor(this.getMaxValue(seed, x, z));
      }

      public double getMaxValue(int seed, int x, int z) {
         return this.max.get(seed, x, z);
      }

      @Nonnull
      @Override
      public String toString() {
         return "StaticLayerEntry{blockArray=" + this.blockArray + ", mapCondition=" + this.mapCondition + ", min=" + this.min + ", max=" + this.max + "}";
      }
   }
}
