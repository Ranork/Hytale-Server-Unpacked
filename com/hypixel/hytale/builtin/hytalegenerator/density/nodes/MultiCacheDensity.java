package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MultiCacheDensity extends Density {
   @Nonnull
   private final MultiCacheDensity.Cache cache;
   @Nonnull
   private Density input;

   public MultiCacheDensity(@Nonnull Density input, int capacity) {
      assert capacity >= 0;

      this.input = input;
      this.cache = new MultiCacheDensity.Cache(capacity);
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      MultiCacheDensity.Entry matchingEntry = this.cache.find(context.position);
      if (matchingEntry == null) {
         matchingEntry = this.cache.getNext();
         if (matchingEntry.position == null) {
            matchingEntry.position = new Vector3d();
         }

         matchingEntry.position.assign(context.position);
         matchingEntry.value = this.input.process(context);
      }

      return matchingEntry.value;
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      assert inputs.length != 0;

      assert inputs[0] != null;

      this.input = inputs[0];
   }

   private static class Cache {
      MultiCacheDensity.Entry[] entries;
      int oldestIndex;

      Cache(int size) {
         this.entries = new MultiCacheDensity.Entry[size];

         for (int i = 0; i < size; i++) {
            this.entries[i] = new MultiCacheDensity.Entry();
         }

         this.oldestIndex = 0;
      }

      MultiCacheDensity.Entry getNext() {
         MultiCacheDensity.Entry entry = this.entries[this.oldestIndex];
         this.oldestIndex++;
         if (this.oldestIndex >= this.entries.length) {
            this.oldestIndex = 0;
         }

         return entry;
      }

      @Nullable
      MultiCacheDensity.Entry find(@Nonnull Vector3d position) {
         int startIndex = this.oldestIndex - 1;
         if (startIndex < 0) {
            startIndex += this.entries.length;
         }

         int index = startIndex;

         while (!position.equals(this.entries[index].position)) {
            if (++index >= this.entries.length) {
               index = 0;
            }

            if (index == startIndex) {
               return null;
            }
         }

         return this.entries[index];
      }
   }

   private static class Entry {
      @Nullable
      Vector3d position = null;
      double value = 0.0;

      Entry() {
      }
   }
}
