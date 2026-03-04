package com.hypixel.hytale.builtin.hytalegenerator.framework.cartas;

import com.hypixel.hytale.builtin.hytalegenerator.framework.interfaces.functions.BiCarta;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class SingleElementCarta<R> extends BiCarta<R> {
   private R element;

   private SingleElementCarta() {
   }

   @Nonnull
   public static <R> SingleElementCarta<R> of(@Nonnull R element) {
      SingleElementCarta<R> c = new SingleElementCarta<>();
      c.element = element;
      return c;
   }

   @Override
   public R apply(int x, int z, @Nonnull WorkerIndexer.Id id) {
      return this.element;
   }

   @Nonnull
   @Override
   public List<R> allPossibleValues() {
      return Collections.singletonList(this.element);
   }
}
