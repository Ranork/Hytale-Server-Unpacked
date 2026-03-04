package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class MovingBoxBoxCollisionEvaluator extends BlockContactData implements IBlockCollisionEvaluator {
   protected boolean touching;
   protected Box collider;
   @Nonnull
   protected final Vector3d pos;
   @Nonnull
   protected final Vector3d v;
   protected boolean checkForOnGround = true;
   private boolean computeOverlaps;
   @Nonnull
   protected final MovingBoxBoxCollisionEvaluator.Collision1D cX;
   @Nonnull
   protected final MovingBoxBoxCollisionEvaluator.Collision1D cY;
   @Nonnull
   protected final MovingBoxBoxCollisionEvaluator.Collision1D cZ;

   public MovingBoxBoxCollisionEvaluator() {
      this.pos = new Vector3d();
      this.v = new Vector3d();
      this.cX = new MovingBoxBoxCollisionEvaluator.Collision1D();
      this.cY = new MovingBoxBoxCollisionEvaluator.Collision1D();
      this.cZ = new MovingBoxBoxCollisionEvaluator.Collision1D();
   }

   @Override
   public double getCollisionStart() {
      return this.collisionStart;
   }

   @Override
   public void setCollisionData(@Nonnull BlockCollisionData data, @Nonnull CollisionConfig collisionConfig, int hitboxIndex) {
      data.setStart(this.collisionPoint, this.collisionStart);
      data.setEnd(this.collisionEnd, this.collisionNormal);
      data.setBlockData(collisionConfig);
      data.setDetailBoxIndex(hitboxIndex);
      data.setTouchingOverlapping(this.touching, this.isOverlapping());
   }

   public boolean isCheckForOnGround() {
      return this.checkForOnGround;
   }

   public void setCheckForOnGround(boolean checkForOnGround) {
      this.checkForOnGround = checkForOnGround;
   }

   public boolean isComputeOverlaps() {
      return this.computeOverlaps;
   }

   public void setComputeOverlaps(boolean computeOverlaps) {
      this.computeOverlaps = computeOverlaps;
   }

   @Nonnull
   public MovingBoxBoxCollisionEvaluator setCollider(Box collider) {
      this.collider = collider;
      return this;
   }

   @Nonnull
   public MovingBoxBoxCollisionEvaluator setMove(@Nonnull Vector3d pos, @Nonnull Vector3d v) {
      this.pos.assign(pos);
      this.v.assign(v);
      this.cX.v = v.x;
      this.cY.v = v.y;
      this.cZ.v = v.z;
      return this;
   }

   public boolean isBoundingBoxColliding(@Nonnull Box blockBoundingBox, double x, double y, double z) {
      this.cX.p = this.pos.x - x;
      this.cY.p = this.pos.y - y;
      this.cZ.p = this.pos.z - z;
      this.onGround = false;
      this.touching = false;
      this.overlapping = false;
      if (!this.cX.isColliding(blockBoundingBox.getMin().x - this.collider.getMax().x, blockBoundingBox.getMax().x - this.collider.getMin().x)) {
         return false;
      } else if (!this.cY.isColliding(blockBoundingBox.getMin().y - this.collider.getMax().y, blockBoundingBox.getMax().y - this.collider.getMin().y)) {
         return false;
      } else if (!this.cZ.isColliding(blockBoundingBox.getMin().z - this.collider.getMax().z, blockBoundingBox.getMax().z - this.collider.getMin().z)) {
         return false;
      } else if (this.cX.kind == 1 && this.cY.kind == 1 && this.cZ.kind == 1) {
         this.overlapping = true;
         if (!this.computeOverlaps) {
            return false;
         } else {
            this.collisionStart = 0.0;
            this.collisionEnd = Double.MAX_VALUE;
            this.collisionNormal.assign(0.0, 0.0, 0.0);
            if (this.cX.tLeave < this.collisionEnd) {
               this.collisionEnd = this.cX.tLeave;
               this.collisionNormal.assign(this.cX.normal, 0.0, 0.0);
            }

            if (this.cY.tLeave < this.collisionEnd) {
               this.collisionEnd = this.cY.tLeave;
               this.collisionNormal.assign(0.0, this.cY.normal, 0.0);
            }

            if (this.cZ.tLeave < this.collisionEnd) {
               this.collisionEnd = this.cZ.tLeave;
               this.collisionNormal.assign(0.0, 0.0, this.cZ.normal);
            }

            return true;
         }
      } else {
         this.collisionStart = -Double.MAX_VALUE;
         this.collisionEnd = Double.MAX_VALUE;
         if (this.cX.kind == 0) {
            this.collisionNormal.assign(this.cX.normal, 0.0, 0.0);
            this.collisionStart = this.cX.tEnter;
         }

         if (this.cY.kind == 0 && this.cY.tEnter > this.collisionStart) {
            this.collisionNormal.assign(0.0, this.cY.normal, 0.0);
            this.collisionStart = this.cY.tEnter;
         }

         if (this.cZ.kind == 0 && this.cZ.tEnter > this.collisionStart) {
            this.collisionNormal.assign(0.0, 0.0, this.cZ.normal);
            this.collisionStart = this.cZ.tEnter;
         }

         if (!(this.collisionStart > -Double.MAX_VALUE)) {
            if (this.checkForOnGround && this.cY.kind == 3) {
               this.collisionStart = MathUtil.maxValue(this.cX.tEnter, this.cY.tEnter, this.cZ.tEnter);
               this.collisionEnd = MathUtil.minValue(this.cX.tLeave, this.cY.tLeave, this.cZ.tLeave);
               this.collisionPoint.assign(this.pos);
               this.collisionPoint.addScaled(this.v, this.collisionStart);
               this.collisionNormal.assign(0.0, this.cY.normal, 0.0);
               this.onGround = true;
               this.touching = true;
            }

            return false;
         } else {
            this.collisionEnd = MathUtil.minValue(this.cX.tLeave, this.cY.tLeave, this.cZ.tLeave);
            if (this.collisionStart > this.collisionEnd) {
               return false;
            } else {
               this.collisionPoint.assign(this.pos);
               this.collisionPoint.addScaled(this.v, this.collisionStart);
               if (this.checkForOnGround && this.cY.kind == 3) {
                  this.collisionNormal.assign(0.0, this.cY.normal, 0.0);
                  this.onGround = true;
                  this.touching = true;
                  return false;
               } else {
                  this.touching = this.cX.kind >= 2 || this.cY.kind >= 2 || this.cZ.kind >= 2;
                  return !this.touching;
               }
            }
         }
      }
   }

   public boolean isTouching() {
      return this.touching;
   }

   public void setCollisionEnd(double collisionEnd) {
      this.collisionEnd = collisionEnd;
   }

   private static class Collision1D {
      protected static final int COLLISION_OUTSIDE = 0;
      protected static final int COLLISION_INSIDE = 1;
      protected static final int COLLISION_TOUCH_MIN = 2;
      protected static final int COLLISION_TOUCH_MAX = 3;
      public double p;
      public double v;
      public double min;
      public double max;
      public double tEnter;
      public double tLeave;
      public double normal;
      public int kind;
      public boolean touching;

      boolean isColliding(double min, double max) {
         this.min = min;
         this.max = max;
         this.tEnter = -Double.MAX_VALUE;
         this.tLeave = Double.MAX_VALUE;
         this.normal = 0.0;
         this.touching = false;
         double dist = min - this.p;
         if (dist >= -1.0E-5) {
            if (this.v < dist - 1.0E-5) {
               return false;
            } else {
               this.normal = -1.0;
               this.computeTouchOrOutside(max, dist, 2);
               return true;
            }
         } else {
            dist = max - this.p;
            if (dist <= 1.0E-5) {
               if (this.v > dist + 1.0E-5) {
                  return false;
               } else {
                  this.normal = 1.0;
                  this.computeTouchOrOutside(min, dist, 3);
                  return true;
               }
            } else {
               this.tEnter = 0.0;
               if (this.v < 0.0) {
                  this.tLeave = this.clampPos((min - this.p) / this.v);
                  this.normal = 1.0;
               } else if (this.v > 0.0) {
                  this.tLeave = this.clampPos((max - this.p) / this.v);
                  this.normal = -1.0;
               }

               this.kind = 1;
               return true;
            }
         }
      }

      private void computeTouchOrOutside(double border, double dist, int touchCode) {
         if (this.v != 0.0) {
            this.tEnter = MathUtil.clamp(dist / this.v, 0.0, 1.0);
            if (this.tEnter != 0.0 && this.tEnter < 1.0E-8) {
               this.tEnter = 0.0;
            }

            this.tLeave = this.clampPos((border - this.p) / this.v);
            this.kind = 0;
         } else {
            this.tEnter = 0.0;
            this.kind = touchCode;
         }
      }

      private double clampPos(double v) {
         return v >= 0.0 ? v : 0.0;
      }
   }
}
