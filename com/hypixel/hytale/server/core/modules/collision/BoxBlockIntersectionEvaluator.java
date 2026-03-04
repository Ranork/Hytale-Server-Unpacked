package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class BoxBlockIntersectionEvaluator extends BlockContactData implements IBlockCollisionEvaluator {
   @Nonnull
   protected Box box = new Box();
   protected Vector3d worldUp = Vector3d.UP;
   protected boolean touchCeil;
   protected int resultCode;

   public BoxBlockIntersectionEvaluator() {
      this.setStartEnd(0.0, 1.0);
   }

   @Override
   public void setCollisionData(@Nonnull BlockCollisionData data, @Nonnull CollisionConfig collisionConfig, int hitboxIndex) {
      data.setStart(this.collisionPoint, this.collisionStart);
      data.setEnd(this.collisionEnd, this.collisionNormal);
      data.setBlockData(collisionConfig);
      data.setDetailBoxIndex(hitboxIndex);
      data.setTouchingOverlapping(CollisionMath.isTouching(this.resultCode), CollisionMath.isOverlapping(this.resultCode));
   }

   public Vector3d getWorldUp() {
      return this.worldUp;
   }

   public void setWorldUp(Vector3d worldUp) {
      this.worldUp = worldUp;
   }

   @Nonnull
   public BoxBlockIntersectionEvaluator setBox(@Nonnull Box box) {
      this.box.assign(box);
      return this;
   }

   @Nonnull
   public BoxBlockIntersectionEvaluator expandBox(double radius) {
      this.box.expand(radius);
      return this;
   }

   @Nonnull
   public BoxBlockIntersectionEvaluator setPosition(@Nonnull Vector3d pos) {
      this.collisionPoint.assign(pos);
      return this;
   }

   @Nonnull
   public BoxBlockIntersectionEvaluator setBox(@Nonnull Box box, @Nonnull Vector3d pos) {
      return this.setBox(box).setPosition(pos);
   }

   @Nonnull
   public BoxBlockIntersectionEvaluator offsetPosition(@Nonnull Vector3d offset) {
      this.collisionPoint.add(offset);
      return this;
   }

   @Nonnull
   public BoxBlockIntersectionEvaluator setStartEnd(double start, double end) {
      this.collisionStart = start;
      this.collisionEnd = end;
      return this;
   }

   public int intersectBox(@Nonnull Box otherBox, double x, double y, double z) {
      return CollisionMath.intersectAABBs(this.collisionPoint.x, this.collisionPoint.y, this.collisionPoint.z, this.box, x, y, z, otherBox);
   }

   public int intersectBoxComputeTouch(@Nonnull Box otherBox, double x, double y, double z) {
      int code = CollisionMath.intersectAABBs(this.collisionPoint.x, this.collisionPoint.y, this.collisionPoint.z, this.box, x, y, z, otherBox);
      this.resultCode = code;
      this.onGround = false;
      this.touchCeil = false;
      this.collisionNormal.assign(0.0, 0.0, 0.0);
      this.overlapping = CollisionMath.isOverlapping(this.resultCode);
      if ((code & 7) != 0) {
         if (this.worldUp.y != 0.0) {
            if ((code & 2) != 0) {
               this.collisionNormal.assign(0.0, y + otherBox.min.y < this.collisionPoint.y + this.box.min.y ? 1.0 : -1.0, 0.0);
               this.onGround = this.collisionNormal.y == this.worldUp.y;
               this.touchCeil = !this.onGround;
            } else if ((code & 1) != 0) {
               this.collisionNormal.assign(x + otherBox.min.x < this.collisionPoint.x + this.box.min.x ? 1.0 : -1.0, 0.0, 0.0);
            } else {
               this.collisionNormal.assign(0.0, 0.0, z + otherBox.min.z < this.collisionPoint.z + this.box.min.z ? 1.0 : -1.0);
            }
         } else if (this.worldUp.x != 0.0) {
            if ((code & 1) != 0) {
               this.collisionNormal.assign(x + otherBox.min.x < this.collisionPoint.x + this.box.min.x ? 1.0 : -1.0, 0.0, 0.0);
               this.onGround = this.collisionNormal.x == this.worldUp.x;
               this.touchCeil = !this.onGround;
            } else if ((code & 2) != 0) {
               this.collisionNormal.assign(0.0, y + otherBox.min.y < this.collisionPoint.y + this.box.min.y ? 1.0 : -1.0, 0.0);
            } else {
               this.collisionNormal.assign(0.0, 0.0, z + otherBox.min.z < this.collisionPoint.z + this.box.min.z ? 1.0 : -1.0);
            }
         } else if ((code & 4) != 0) {
            this.collisionNormal.assign(0.0, 0.0, z + otherBox.min.z < this.collisionPoint.z + this.box.min.z ? 1.0 : -1.0);
            this.onGround = this.collisionNormal.z == this.worldUp.z;
            this.touchCeil = !this.onGround;
         } else if ((code & 2) != 0) {
            this.collisionNormal.assign(0.0, y + otherBox.min.y < this.collisionPoint.y + this.box.min.y ? 1.0 : -1.0, 0.0);
         } else {
            this.collisionNormal.assign(x + otherBox.min.x < this.collisionPoint.x + this.box.min.x ? 1.0 : -1.0, 0.0, 0.0);
         }
      }

      return code;
   }

   public int intersectBoxComputeOnGround(@Nonnull Box otherBox, double x, double y, double z) {
      int code = CollisionMath.intersectAABBs(this.collisionPoint.x, this.collisionPoint.y, this.collisionPoint.z, this.box, x, y, z, otherBox);
      this.resultCode = code;
      this.onGround = false;
      this.touchCeil = false;
      if ((code & 7) != 0) {
         if (this.worldUp.y != 0.0 && (code & 2) != 0) {
            this.onGround = (y + otherBox.min.y - this.collisionPoint.y - this.box.min.y) * this.worldUp.y < 0.0;
            this.touchCeil = !this.onGround;
         } else if (this.worldUp.x != 0.0 && (code & 1) != 0) {
            this.onGround = (x + otherBox.min.x - this.collisionPoint.x - this.box.min.x) * this.worldUp.x < 0.0;
            this.touchCeil = !this.onGround;
         } else if (this.worldUp.z != 0.0 && (code & 4) != 0) {
            this.onGround = (z + otherBox.min.z - this.collisionPoint.z - this.box.min.z) * this.worldUp.z < 0.0;
            this.touchCeil = !this.onGround;
         }
      }

      return code;
   }

   public boolean isBoxIntersecting(@Nonnull Box otherBox, double x, double y, double z) {
      return !CollisionMath.isDisjoint(this.intersectBoxComputeTouch(otherBox, x, y, z));
   }

   public boolean isTouching() {
      return CollisionMath.isTouching(this.resultCode);
   }

   public boolean touchesCeil() {
      return this.touchCeil;
   }
}
