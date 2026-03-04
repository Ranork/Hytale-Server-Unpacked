package com.hypixel.hytale.builtin.hytalegenerator.props.prefab;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import javax.annotation.Nonnull;

public class PropPrefabUtil {
   @Nonnull
   public static Vector3i getMin(@Nonnull PrefabBuffer.PrefabBufferAccessor prefab) {
      int minX = prefab.getMinX(PrefabRotation.ROTATION_0);
      int minY = prefab.getMinY();
      int minZ = prefab.getMinZ(PrefabRotation.ROTATION_0);
      return new Vector3i(minX, minY, minZ);
   }

   @Nonnull
   public static Vector3i getMax(@Nonnull PrefabBuffer.PrefabBufferAccessor prefab) {
      int maxX = prefab.getMaxX(PrefabRotation.ROTATION_0);
      int maxY = prefab.getMaxY();
      int maxZ = prefab.getMaxZ(PrefabRotation.ROTATION_0);
      return new Vector3i(maxX, maxY, maxZ);
   }

   @Nonnull
   public static Vector3i getMin(@Nonnull PrefabBuffer.PrefabBufferAccessor prefab, @Nonnull PrefabRotation rotation) {
      int minX = prefab.getMinX(rotation);
      int minY = prefab.getMinY();
      int minZ = prefab.getMinZ(rotation);
      return new Vector3i(minX, minY, minZ);
   }

   @Nonnull
   public static Vector3i getMax(@Nonnull PrefabBuffer.PrefabBufferAccessor prefab, @Nonnull PrefabRotation rotation) {
      int maxX = prefab.getMaxX(rotation);
      int maxY = prefab.getMaxY();
      int maxZ = prefab.getMaxZ(rotation);
      return new Vector3i(maxX, maxY, maxZ);
   }

   @Nonnull
   public static Vector3i getSize(@Nonnull PrefabBuffer.PrefabBufferAccessor prefab) {
      Vector3i min = getMin(prefab);
      Vector3i max = getMax(prefab);
      return max.addScaled(min, -1);
   }

   @Nonnull
   public static Vector3i getAnchor(@Nonnull PrefabBuffer.PrefabBufferAccessor prefab) {
      int x = prefab.getAnchorX();
      int y = prefab.getAnchorY();
      int z = prefab.getAnchorZ();
      return new Vector3i(x, y, z);
   }
}
