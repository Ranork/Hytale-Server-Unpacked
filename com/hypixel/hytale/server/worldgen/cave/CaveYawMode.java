package com.hypixel.hytale.server.worldgen.cave;

import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import javax.annotation.Nullable;

public enum CaveYawMode {
   NODE {
      @Override
      public float combine(float parentYaw, @Nullable PrefabRotation parentRotation) {
         return parentYaw;
      }
   },
   SUM {
      @Override
      public float combine(float parentYaw, @Nullable PrefabRotation parentRotation) {
         return parentRotation == null ? parentYaw : parentYaw + parentRotation.getYaw();
      }
   },
   PREFAB {
      @Override
      public float combine(float parentYaw, @Nullable PrefabRotation parentRotation) {
         return parentRotation == null ? parentYaw : parentRotation.getYaw();
      }
   };

   public abstract float combine(float var1, @Nullable PrefabRotation var2);
}
