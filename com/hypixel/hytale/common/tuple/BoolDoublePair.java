package com.hypixel.hytale.common.tuple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BoolDoublePair implements Comparable<BoolDoublePair> {
   private final boolean left;
   private final double right;

   public BoolDoublePair(boolean left, double right) {
      this.left = left;
      this.right = right;
   }

   public final boolean getKey() {
      return this.getLeft();
   }

   public boolean getLeft() {
      return this.left;
   }

   public final double getValue() {
      return this.getRight();
   }

   public double getRight() {
      return this.right;
   }

   public int compareTo(@Nonnull BoolDoublePair other) {
      int compare = Boolean.compare(this.left, other.left);
      return compare != 0 ? compare : Double.compare(this.right, other.right);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BoolDoublePair that = (BoolDoublePair)o;
         return this.left != that.left ? false : Double.compare(that.right, this.right) == 0;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.left ? 1 : 0;
      long temp = Double.doubleToLongBits(this.right);
      return 31 * result + (int)(temp ^ temp >>> 32);
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
   public static BoolDoublePair of(boolean left, double right) {
      return new BoolDoublePair(left, right);
   }
}
