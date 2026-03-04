package com.hypixel.hytale.builtin.hytalegenerator;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReusableList<T> {
   @Nonnull
   private final List<T> data = new ArrayList<>(0);
   private int softSize = 0;

   public void expandAndSet(T element) {
      if (this.isAtHardCapacity()) {
         this.data.add(element);
      } else {
         this.data.set(this.softSize, element);
      }

      this.softSize++;
   }

   @Nullable
   public T expandAndGet() {
      if (this.isAtHardCapacity()) {
         this.data.add(null);
         return null;
      } else {
         T result = this.data.get(this.softSize);
         this.softSize++;
         return result;
      }
   }

   public int getSoftSize() {
      return this.softSize;
   }

   public int getHardSize() {
      return this.data.size();
   }

   public boolean isAtHardCapacity() {
      return this.softSize == this.data.size();
   }

   @Nullable
   public T get(int index) {
      if (index >= this.softSize) {
         throw new IndexOutOfBoundsException();
      } else {
         return this.data.get(index);
      }
   }

   public void clear() {
      this.softSize = 0;
   }
}
