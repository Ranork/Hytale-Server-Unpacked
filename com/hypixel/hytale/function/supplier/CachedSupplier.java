package com.hypixel.hytale.function.supplier;

import java.util.function.Supplier;
import javax.annotation.Nullable;

public class CachedSupplier<T> implements Supplier<T> {
   private final Supplier<T> delegate;
   private transient volatile boolean initialized;
   @Nullable
   private transient T value;

   public CachedSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
   }

   @Nullable
   @Override
   public T get() {
      if (!this.initialized) {
         synchronized (this) {
            if (!this.initialized) {
               T t = this.delegate.get();
               this.value = t;
               this.initialized = true;
               return t;
            }
         }
      }

      return this.value;
   }

   @Nullable
   public T getValue() {
      return this.value;
   }

   public void invalidate() {
      if (this.initialized) {
         synchronized (this) {
            if (this.initialized) {
               this.value = null;
               this.initialized = false;
            }
         }
      }
   }
}
