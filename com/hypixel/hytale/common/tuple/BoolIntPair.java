package com.hypixel.hytale.common.tuple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BoolIntPair implements Comparable<BoolIntPair> {
   private final boolean left;
   private final int right;

   public BoolIntPair(boolean left, int right) {
      this.left = left;
      this.right = right;
   }

   public final boolean getKey() {
      return this.getLeft();
   }

   public boolean getLeft() {
      return this.left;
   }

   public final int getValue() {
      return this.getRight();
   }

   public int getRight() {
      return this.right;
   }

   public int compareTo(@Nonnull BoolIntPair other) {
      int compare = Boolean.compare(this.left, other.left);
      return compare != 0 ? compare : Integer.compare(this.right, other.right);
   }

   @Override
   public int hashCode() {
      int result = this.left ? 1 : 0;
      return 31 * result + this.right;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BoolIntPair that = (BoolIntPair)o;
         return this.left != that.left ? false : this.right == that.right;
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "(" + this.getLeft() + "," + this.getRight() + ")";
   }

   @Nonnull
   public String toString(@Nonnull String format) {
      return String.format(format, this.getLeft(), this.getRight());
   }

   @Nonnull
   public static BoolIntPair of(boolean left, int right) {
      return new BoolIntPair(left, right);
   }
}
