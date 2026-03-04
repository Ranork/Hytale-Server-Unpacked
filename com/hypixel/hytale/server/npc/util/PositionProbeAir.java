package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.modules.collision.BoxBlockIntersectionEvaluator;
import com.hypixel.hytale.server.core.modules.collision.CollisionConfig;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PositionProbeAir extends PositionProbeBase {
   protected boolean inAir;
   protected boolean onSolid;
   protected boolean collideWithFluid;

   public boolean probePosition(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Box boundingBox,
      @Nonnull Vector3d position,
      @Nonnull CollisionResult collisionResult,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.collideWithFluid = (collisionResult.getConfig().getCollisionByMaterial() & 2) != 0;
      this.inAir = super.probePosition(ref, boundingBox, position, collisionResult, this, PositionProbeAir::blockTest, 6, componentAccessor);
      return this.inAir;
   }

   private boolean blockTest(int code, @Nonnull BoxBlockIntersectionEvaluator boxBlockIntersection, @Nonnull CollisionConfig config) {
      if (CollisionMath.isTouching(code)) {
         if (config.blockMaterial == BlockMaterial.Solid) {
            boolean isOnGround = boxBlockIntersection.isOnGround();
            this.onGround |= isOnGround;
            this.touchCeil = this.touchCeil | boxBlockIntersection.touchesCeil();
            this.onSolid |= isOnGround;
            if (isOnGround && config.blockY > this.groundLevel) {
               this.groundLevel = config.blockY;
            }
         }

         return false;
      } else if (CollisionMath.isOverlapping(code) && (config.blockMaterialMask & 2) != 0) {
         this.inWater = true;
         return this.collideWithFluid;
      } else {
         return true;
      }
   }

   public boolean isInAir() {
      return this.inAir;
   }

   public boolean isOnSolid() {
      return this.onSolid;
   }

   @Override
   protected void reset() {
      super.reset();
      this.inAir = false;
      this.onSolid = false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PositionProbeAir{inAir=" + this.inAir + ", onSolid=" + this.onSolid + "} " + super.toString();
   }
}
