package com.hypixel.hytale.builtin.hytalegenerator.framework.cartas;

import com.hypixel.hytale.builtin.hytalegenerator.framework.interfaces.functions.TriCarta;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

public class LayeredCarta<R> extends TriCarta<R> {
   @Nonnull
   private final List<TriCarta<R>> layers;
   @Nonnull
   private final List<R> allValues;
   @Nonnull
   private final R defaultValue;

   public LayeredCarta(@Nonnull R defaultValue) {
      Objects.requireNonNull(defaultValue);
      this.layers = new ArrayList<>(1);
      this.allValues = new ArrayList<>(1);
      this.defaultValue = defaultValue;
      this.allValues.add(defaultValue);
   }

   @Override
   public R apply(int x, int y, int z, @Nonnull WorkerIndexer.Id id) {
      R result = this.defaultValue;

      for (TriCarta<R> layer : this.layers) {
         R value = layer.apply(x, y, z, id);
         if (value != null) {
            result = value;
         }
      }

      return result;
   }

   @Nonnull
   @Override
   public List<R> allPossibleValues() {
      return Collections.unmodifiableList(this.allValues);
   }

   @Nonnull
   public LayeredCarta<R> addLayer(@Nonnull TriCarta<R> layer) {
      Objects.requireNonNull(layer);
      this.layers.add(layer);
      this.allValues.addAll(layer.allPossibleValues());
      return this;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LayeredCarta{layers=" + this.layers + ", allValues=" + this.allValues + ", defaultValue=" + this.defaultValue + "}";
   }
}
