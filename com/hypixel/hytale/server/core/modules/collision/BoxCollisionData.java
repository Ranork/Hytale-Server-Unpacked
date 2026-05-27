package com.hypixel.hytale.server.core.modules.collision;

import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class BoxCollisionData extends BasicCollisionData {
   public double collisionEnd;
   public final Vector3d collisionNormal = new Vector3d();

   public void setEnd(double collisionEnd, @Nonnull Vector3d collisionNormal) {
      this.collisionEnd = collisionEnd;
      this.collisionNormal.set(collisionNormal);
   }
}
