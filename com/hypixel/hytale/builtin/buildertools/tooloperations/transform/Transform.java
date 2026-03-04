package com.hypixel.hytale.builtin.buildertools.tooloperations.transform;

import com.hypixel.hytale.math.vector.Vector3i;

public interface Transform {
   Transform NONE = vec -> {};

   void apply(Vector3i var1);

   default Transform then(Transform next) {
      return Composite.of(this, next);
   }
}
