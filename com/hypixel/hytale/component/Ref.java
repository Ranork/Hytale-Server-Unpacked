package com.hypixel.hytale.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Ref<ECS_TYPE> {
   public static final Ref<?>[] EMPTY_ARRAY = new Ref[0];
   @Nonnull
   private final Store<ECS_TYPE> store;
   private volatile int index;
   private volatile Throwable invalidatedBy;

   public Ref(@Nonnull Store<ECS_TYPE> store) {
      this(store, Integer.MIN_VALUE);
   }

   public Ref(@Nonnull Store<ECS_TYPE> store, int index) {
      this.store = store;
      this.index = index;
   }

   @Nonnull
   public Store<ECS_TYPE> getStore() {
      return this.store;
   }

   public int getIndex() {
      return this.index;
   }

   void setIndex(int index) {
      this.index = index;
   }

   void invalidate() {
      this.index = Integer.MIN_VALUE;
      this.invalidatedBy = new Throwable();
   }

   void invalidate(@Nullable Throwable invalidatedBy) {
      this.index = Integer.MIN_VALUE;
      this.invalidatedBy = invalidatedBy != null ? invalidatedBy : new Throwable();
   }

   public void validate(@Nonnull Store<ECS_TYPE> store) {
      if (this.store != store) {
         throw new IllegalStateException("Incorrect store for entity reference");
      } else if (this.index == Integer.MIN_VALUE) {
         throw new IllegalStateException("Invalid entity reference!", this.invalidatedBy);
      }
   }

   public void validate() {
      if (this.index == Integer.MIN_VALUE) {
         throw new IllegalStateException("Invalid entity reference!", this.invalidatedBy);
      }
   }

   public boolean isValid() {
      return this.index != Integer.MIN_VALUE;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Ref{store=" + this.store.getClass() + "@" + this.store.hashCode() + ", index=" + this.index + "}";
   }
}
