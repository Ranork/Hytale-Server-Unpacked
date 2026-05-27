package com.hypixel.hytale.builtin.hytalegenerator.positionproviders.cached;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.joml.Vector3d;

public class CacheThreadMemory {
   Map<Long, Vector3d[]> sections;
   LinkedList<Long> expirationList;
   int size;

   public CacheThreadMemory(int size) {
      if (size < 0) {
         throw new IllegalArgumentException();
      } else {
         this.sections = new HashMap<>(size);
         this.expirationList = new LinkedList<>();
         this.size = size;
      }
   }
}
