package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.BoxBlockIntersectionEvaluator;
import com.hypixel.hytale.server.core.modules.collision.CollisionConfig;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PositionProbeWater extends PositionProbeBase {
   private double ySwim;

   public boolean probePosition(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Box boundingBox,
      @Nonnull Vector3d position,
      @Nonnull CollisionResult collisionResult,
      double swimDepth,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.ySwim = position.y + swimDepth + 1.0E-6;
      return super.probePosition(ref, boundingBox, position, collisionResult, this, PositionProbeWater::blockTest, 6, componentAccessor);
   }

   private boolean blockTest(int code, @Nonnull BoxBlockIntersectionEvaluator boxBlockIntersection, @Nonnull CollisionConfig config) {
      boolean solid = (config.blockMaterialMask & 4) != 0;
      boolean fluid = (config.blockMaterialMask & 2) != 0;
      boolean submerged = (config.blockMaterialMask & 8) != 0;
      if (solid && CollisionMath.isTouching(code)) {
         boolean isOnGround = boxBlockIntersection.isOnGround();
         this.onGround |= isOnGround;
         this.touchCeil = this.touchCeil | boxBlockIntersection.touchesCeil();
         if (isOnGround && config.blockY > this.groundLevel) {
            this.groundLevel = config.blockY;
         }
      }

      if ((!fluid || !CollisionMath.isOverlapping(code)) && !submerged) {
         return solid;
      } else {
         double yTop = config.blockY + 1;
         this.inWater = this.inWater | yTop >= this.ySwim;
         return false;
      }
   }

   @Override
   protected void reset() {
      super.reset();
      this.inWater = false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PositionProbeWater{ySwim=" + this.ySwim + "} " + super.toString();
   }
}
