package com.hypixel.hytale.common.fastutil;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HObjectOpenHashSet<K> extends ObjectOpenHashSet<K> {
   @Nullable
   public K first() {
      if (this.containsNull) {
         return (K)this.key[this.n];
      } else {
         K[] key = (K[])this.key;
         int pos = this.n;

         while (pos-- != 0) {
            if (key[pos] != null) {
               return key[pos];
            }
         }

         return null;
      }
   }

   public void pushInto(@Nonnull Collection<K> c) {
      if (this.containsNull) {
         c.add((K)this.key[this.n]);
      }

      K[] key = (K[])this.key;
      int pos = this.n;

      while (pos-- != 0) {
         if (key[pos] != null) {
            c.add(key[pos]);
         }
      }
   }
}
