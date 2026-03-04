package com.hypixel.hytale.builtin.hytalegenerator.props.directionality;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import javax.annotation.Nonnull;

public class RotatedPosition {
   public final int x;
   public final int y;
   public final int z;
   @Nonnull
   public final PrefabRotation rotation;

   public RotatedPosition(int x, int y, int z, @Nonnull PrefabRotation rotation) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.rotation = rotation;
   }

   @Nonnull
   public RotatedPosition getRelativeTo(@Nonnull RotatedPosition other) {
      Vector3i vec = new Vector3i(this.x, this.y, this.z);
      other.rotation.rotate(vec);
      int x = vec.x + other.x;
      int y = vec.y + other.y;
      int z = vec.z + other.z;
      PrefabRotation rotation = this.rotation.add(other.rotation);
      return new RotatedPosition(x, y, z, rotation);
   }

   @Nonnull
   public Vector3i toVector3i() {
      return new Vector3i(this.x, this.y, this.z);
   }
}
