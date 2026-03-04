package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class BlockContactData {
   protected final Vector3d collisionNormal = new Vector3d();
   protected final Vector3d collisionPoint = new Vector3d();
   protected double collisionStart;
   protected double collisionEnd;
   protected boolean onGround;
   protected int damage;
   protected boolean isSubmergeFluid;
   protected boolean overlapping;

   public void clear() {
   }

   public void assign(@Nonnull BlockContactData other) {
      this.assign(other, other.damage, other.isSubmergeFluid);
   }

   public void assign(@Nonnull BlockContactData other, int damage, boolean isSubmergedFluid) {
      this.collisionNormal.assign(other.collisionNormal);
      this.collisionPoint.assign(other.collisionPoint);
      this.collisionStart = other.collisionStart;
      this.collisionEnd = other.collisionEnd;
      this.onGround = other.onGround;
      this.overlapping = other.overlapping;
      this.setDamageAndSubmerged(damage, isSubmergedFluid);
   }

   public void setDamageAndSubmerged(int damage, boolean isSubmerge) {
      this.damage = damage;
      this.isSubmergeFluid = isSubmerge;
   }

   @Nonnull
   public Vector3d getCollisionNormal() {
      return this.collisionNormal;
   }

   @Nonnull
   public Vector3d getCollisionPoint() {
      return this.collisionPoint;
   }

   public double getCollisionStart() {
      return this.collisionStart;
   }

   public double getCollisionEnd() {
      return this.collisionEnd;
   }

   public boolean isOverlapping() {
      return this.overlapping;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public int getDamage() {
      return this.damage;
   }

   public boolean isSubmergeFluid() {
      return this.isSubmergeFluid;
   }
}
