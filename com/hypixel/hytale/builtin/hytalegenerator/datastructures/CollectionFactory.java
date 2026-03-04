package com.hypixel.hytale.builtin.hytalegenerator.datastructures;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

public class CollectionFactory {
   @Nonnull
   public static <T> Set<T> hashSetOf(@Nonnull T... elements) {
      Set<T> set = new HashSet<>(elements.length);

      for (T element : elements) {
         if (element == null) {
            throw new NullPointerException("elements can't be null");
         }

         set.add(element);
      }

      return set;
   }
}
