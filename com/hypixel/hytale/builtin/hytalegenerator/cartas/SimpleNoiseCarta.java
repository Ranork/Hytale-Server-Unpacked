package com.hypixel.hytale.builtin.hytalegenerator.cartas;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.interfaces.functions.BiCarta;
import com.hypixel.hytale.builtin.hytalegenerator.rangemaps.DoubleRange;
import com.hypixel.hytale.builtin.hytalegenerator.rangemaps.DoubleRangeMap;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.List;
import javax.annotation.Nonnull;

public class SimpleNoiseCarta<T> extends BiCarta<T> {
   @Nonnull
   private final Density density;
   @Nonnull
   private final DoubleRangeMap<T> rangeMap;
   private final T defaultValue;

   public SimpleNoiseCarta(@Nonnull Density density, T defaultValue) {
      this.density = density;
      this.defaultValue = defaultValue;
      this.rangeMap = new DoubleRangeMap<>();
   }

   @Nonnull
   public SimpleNoiseCarta<T> put(@Nonnull DoubleRange range, T value) {
      this.rangeMap.put(range, value);
      return this;
   }

   @Override
   public T apply(int x, int z, @Nonnull WorkerIndexer.Id id) {
      Density.Context context = new Density.Context();
      context.position = new Vector3d(x, 0.0, z);
      double noiseValue = this.density.process(context);
      T value = this.rangeMap.get(noiseValue);
      return value == null ? this.defaultValue : value;
   }

   @Nonnull
   @Override
   public List<T> allPossibleValues() {
      List<T> list = this.rangeMap.values();
      list.add(this.defaultValue);
      return list;
   }
}
