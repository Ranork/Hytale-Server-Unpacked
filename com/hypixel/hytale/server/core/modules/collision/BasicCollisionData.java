package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Comparator;
import javax.annotation.Nonnull;

public class BasicCollisionData {
   public static final Comparator<BasicCollisionData> COLLISION_START_COMPARATOR = Comparator.comparingDouble(a -> a.collisionStart);
   public final Vector3d collisionPoint = new Vector3d();
   public double collisionStart;

   public void setStart(@Nonnull Vector3d point, double start) {
      this.collisionPoint.assign(point);
      this.collisionStart = start;
   }
}
