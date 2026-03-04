package com.hypixel.hytale.codec.lookup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Priority {
   @Nonnull
   public static Priority DEFAULT = new Priority(-1000);
   @Nonnull
   public static Priority NORMAL = new Priority(0);
   private int level;

   public Priority(int level) {
      this.level = level;
   }

   public int getLevel() {
      return this.level;
   }

   @Nonnull
   public Priority before() {
      return this.before(1);
   }

   @Nonnull
   public Priority before(int by) {
      return new Priority(this.level - by);
   }

   @Nonnull
   public Priority after() {
      return this.after(1);
   }

   @Nonnull
   public Priority after(int by) {
      return new Priority(this.level - by);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Priority priority = (Priority)o;
         return this.level == priority.level;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.level;
   }
}
