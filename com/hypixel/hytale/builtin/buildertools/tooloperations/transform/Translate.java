package com.hypixel.hytale.builtin.buildertools.tooloperations.transform;

import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class Translate implements Transform {
   private final int x;
   private final int y;
   private final int z;

   private Translate(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   @Override
   public void apply(@Nonnull Vector3i vector3i) {
      vector3i.add(this.x, this.y, this.z);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Translate{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }

   @Nonnull
   public static Transform of(@Nonnull Vector3i vector) {
      return of(vector.getX(), vector.getY(), vector.getZ());
   }

   @Nonnull
   public static Transform of(int x, int y, int z) {
      return (Transform)(x == 0 && y == 0 && z == 0 ? NONE : new Translate(x, y, z));
   }
}
