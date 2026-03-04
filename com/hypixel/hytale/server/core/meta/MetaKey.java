package com.hypixel.hytale.server.core.meta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MetaKey<T> {
   private final int id;

   MetaKey(int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MetaKey<?> metaKey = (MetaKey<?>)o;
         return this.id == metaKey.id;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.id;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MetaKey{id=" + this.id + "}";
   }
}
