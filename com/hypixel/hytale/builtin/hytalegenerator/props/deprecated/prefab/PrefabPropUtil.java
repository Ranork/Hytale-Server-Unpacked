package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class PrefabPropUtil {
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
   public static Vector3i getMin(@Nonnull IPrefabBuffer prefab, @Nonnull PrefabRotation rotation) {
      int minX = prefab.getMinX(rotation);
      int minY = prefab.getMinY();
      int minZ = prefab.getMinZ(rotation);
      return new Vector3i(minX, minY, minZ);
   }

   @Nonnull
   public static Bounds3i getTotalBounds(@Nonnull IPrefabBuffer prefab) {
      Bounds3i bounds = new Bounds3i();
      bounds.min.x = prefab.getMinX();
      bounds.min.y = prefab.getMinY();
      bounds.min.z = prefab.getMinZ();
      bounds.max.x = prefab.getMaxX();
      bounds.max.y = prefab.getMaxY();
      bounds.max.z = prefab.getMaxZ();
      prefab.forEachEntity((x, z, entityWrappers, o) -> {
         if (entityWrappers != null) {
            for (int i = 0; i < entityWrappers.length; i++) {
               TransformComponent transformComp = entityWrappers[i].getComponent(TransformComponent.getComponentType());
               if (transformComp != null) {
                  Vector3d entityPosition = new Vector3d(transformComp.getPosition());
                  Vector3i entityPositionInt = new Vector3i((int)entityPosition.x, (int)entityPosition.y, (int)entityPosition.z);
                  bounds.min.x = Math.min(bounds.min.x, entityPositionInt.x);
                  bounds.min.y = Math.min(bounds.min.y, entityPositionInt.y);
                  bounds.min.z = Math.min(bounds.min.z, entityPositionInt.z);
                  bounds.max.x = Math.max(bounds.max.x, entityPositionInt.x);
                  bounds.max.y = Math.max(bounds.max.y, entityPositionInt.y);
                  bounds.max.z = Math.max(bounds.max.z, entityPositionInt.z);
               }
            }
         }
      }, null);
      return bounds;
   }

   @Nonnull
   public static Vector3i getMax(@Nonnull IPrefabBuffer prefab, @Nonnull PrefabRotation rotation) {
      int maxX = prefab.getMaxX(rotation);
      int maxY = prefab.getMaxY();
      int maxZ = prefab.getMaxZ(rotation);
      return new Vector3i(maxX, maxY, maxZ);
   }

   @Nonnull
   public static Vector3i getSize(@Nonnull PrefabBuffer.PrefabBufferAccessor prefab) {
      Vector3i min = getMin(prefab);
      Vector3i max = getMax(prefab);
      return max.sub(min);
   }

   @Nonnull
   public static Vector3i getAnchor(@Nonnull PrefabBuffer.PrefabBufferAccessor prefab) {
      int x = prefab.getAnchorX();
      int y = prefab.getAnchorY();
      int z = prefab.getAnchorZ();
      return new Vector3i(x, y, z);
   }
}
