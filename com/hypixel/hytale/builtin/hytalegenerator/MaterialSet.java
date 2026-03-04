package com.hypixel.hytale.builtin.hytalegenerator;

import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MaterialSet implements Predicate<Material> {
   private final boolean isInclusive;
   private final IntSet mask;

   public MaterialSet() {
      this.isInclusive = true;
      this.mask = IntSet.of();
   }

   public MaterialSet(boolean isInclusive, @Nonnull List<Material> elements) {
      this.isInclusive = isInclusive;
      int size = elements.size();
      if (size == 0) {
         this.mask = IntSet.of();
      } else if (size == 1) {
         Material first = elements.getFirst();
         if (first == null) {
            throw new IllegalArgumentException("element array contains null at index 0");
         } else {
            this.mask = IntSet.of(first.hashMaterialIds());
         }
      } else {
         IntSet innerSet = (IntSet)(size <= 4 ? new IntArraySet(size) : new IntOpenHashSet(size, 0.99F));

         for (int i = 0; i < size; i++) {
            Material element = elements.get(i);
            if (element == null) {
               throw new IllegalArgumentException("element array contains null at index " + i);
            }

            innerSet.add(element.hashMaterialIds());
         }

         this.mask = IntSets.unmodifiable(innerSet);
      }
   }

   public boolean test(@Nullable Material value) {
      if (value == null) {
         return false;
      } else {
         boolean contains = this.mask.contains(value.hashMaterialIds());
         return contains && this.isInclusive || !contains && !this.isInclusive;
      }
   }

   public boolean test(int hashMaterialIds) {
      boolean contains = this.mask.contains(hashMaterialIds);
      return contains && this.isInclusive || !contains && !this.isInclusive;
   }
}
