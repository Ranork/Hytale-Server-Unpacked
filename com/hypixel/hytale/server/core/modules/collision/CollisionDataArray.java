package com.hypixel.hytale.server.core.modules.collision;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CollisionDataArray<T> {
   @Nonnull
   private final List<T> array = new ObjectArrayList();
   private final Supplier<T> supplier;
   @Nullable
   private final Consumer<T> dispose;
   private final List<T> freeList;
   private int head;

   public CollisionDataArray(Supplier<T> supplier, @Nullable Consumer<T> dispose, List<T> freeList) {
      Objects.requireNonNull(supplier, "Must provide supplier for CollisionDataArray");
      this.supplier = supplier;
      this.dispose = dispose;
      this.freeList = freeList;
      this.head = 0;
   }

   public int getCount() {
      return this.array.size() - this.head;
   }

   public T alloc() {
      T result;
      if (this.freeList.isEmpty()) {
         result = this.supplier.get();
      } else {
         int last = this.freeList.size() - 1;
         result = this.freeList.get(last);
         this.freeList.remove(last);
      }

      this.array.add(result);
      return result;
   }

   public void reset() {
      int count = this.array.size();
      if (count > 0) {
         if (this.dispose != null) {
            for (int i = 0; i < count; i++) {
               T value = this.array.get(i);
               this.dispose.accept(value);
               this.freeList.add(value);
            }
         } else {
            for (int i = 0; i < count; i++) {
               T value = this.array.get(i);
               this.freeList.add(value);
            }
         }

         this.array.clear();
         this.head = 0;
      }
   }

   @Nullable
   public T getFirst() {
      return this.head < this.array.size() ? this.array.get(this.head) : null;
   }

   @Nullable
   public T forgetFirst() {
      this.head++;
      return this.getFirst();
   }

   public boolean isEmpty() {
      return this.array.isEmpty();
   }

   public void sort(Comparator<? super T> comparator) {
      this.array.sort(comparator);
   }

   public void remove(int l) {
      int index = this.head + l;
      if (index < this.array.size()) {
         this.freeList.add(this.array.get(index));
         this.array.remove(index);
      }
   }

   public int size() {
      return this.array.size() - this.head;
   }

   public T get(int i) {
      return this.array.get(this.head + i);
   }
}
