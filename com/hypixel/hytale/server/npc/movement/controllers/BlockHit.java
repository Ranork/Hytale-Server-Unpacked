package com.hypixel.hytale.server.npc.movement.controllers;

import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class BlockHit {
   public int blockX;
   public int blockY;
   public int blockZ;
   public int blockTypeId;
   @Nonnull
   public final Vector3d collisionPoint = new Vector3d();
   @Nonnull
   public final Vector3d normal = new Vector3d();
   public double t;

   public void assign(int blockX, int blockY, int blockZ, int blockTypeId, @Nonnull Vector3dc collisionPoint, @Nonnull Vector3dc normal, double t) {
      this.blockX = blockX;
      this.blockY = blockY;
      this.blockZ = blockZ;
      this.blockTypeId = blockTypeId;
      this.collisionPoint.set(collisionPoint);
      this.normal.set(normal);
      this.t = t;
   }
}
