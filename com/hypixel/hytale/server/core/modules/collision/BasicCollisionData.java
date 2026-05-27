package com.hypixel.hytale.server.core.modules.collision;

import java.util.Comparator;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class BasicCollisionData {
   public static final Comparator<BasicCollisionData> COLLISION_START_COMPARATOR = Comparator.comparingDouble(a -> a.collisionStart);
   public final Vector3d collisionPoint = new Vector3d();
   public double collisionStart;

   public void setStart(@Nonnull Vector3d point, double start) {
      this.collisionPoint.set(point);
      this.collisionStart = start;
   }
}
