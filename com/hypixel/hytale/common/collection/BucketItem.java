package com.hypixel.hytale.common.collection;

import javax.annotation.Nonnull;

public class BucketItem<E> {
   public E item;
   public double squaredDistance;

   @Nonnull
   public BucketItem<E> set(E reference, double squaredDistance) {
      this.item = reference;
      this.squaredDistance = squaredDistance;
      return this;
   }
}
