package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Random;
import javax.annotation.Nonnull;

@Deprecated
public class TetrahedronCaveNodeShape extends AbstractCaveNodeShape implements IWorldBounds {
   @Nonnull
   private final Vector3d o;
   @Nonnull
   private final Vector3d a;
   @Nonnull
   private final Vector3d b;
   @Nonnull
   private final Vector3d c;
   @Nonnull
   private final Vector3d n1;
   @Nonnull
   private final Vector3d n2;
   @Nonnull
   private final Vector3d n3;
   @Nonnull
   private final Vector3d n4;
   private final int lowBoundX;
   private final int lowBoundY;
   private final int lowBoundZ;
   private final int highBoundX;
   private final int highBoundY;
   private final int highBoundZ;

   public TetrahedronCaveNodeShape(@Nonnull Vector3d o) {
      this.o = o;
      this.a = new Vector3d(10.0, 0.0, 0.0);
      this.b = new Vector3d(0.0, 10.0, 0.0);
      this.c = new Vector3d(0.0, 0.0, 10.0);
      this.n1 = this.c.cross(this.b);
      this.n2 = this.b.cross(this.a);
      this.n3 = this.a.cross(this.c);
      Vector3d ba = this.a.clone().subtract(this.b);
      Vector3d bc = this.c.clone().subtract(this.b);
      this.n4 = bc.cross(ba);
      this.lowBoundX = (int)(o.x - 10.0);
      this.lowBoundY = (int)(o.y - 10.0);
      this.lowBoundZ = (int)(o.z - 10.0);
      this.highBoundX = (int)(o.x + 10.0);
      this.highBoundY = (int)(o.y + 10.0);
      this.highBoundZ = (int)(o.z + 10.0);
   }

   @Nonnull
   @Override
   public Vector3d getStart() {
      return this.o.clone();
   }

   @Nonnull
   @Override
   public Vector3d getEnd() {
      return this.o.clone().add(this.c);
   }

   @Nonnull
   @Override
   public Vector3d getAnchor(@Nonnull Vector3d vector, double tx, double ty, double tz) {
      return CaveNodeShapeUtils.getBoxAnchor(vector, this, tx, ty, tz);
   }

   @Nonnull
   @Override
   public IWorldBounds getBounds() {
      return this;
   }

   @Override
   public int getLowBoundX() {
      return this.lowBoundX;
   }

   @Override
   public int getLowBoundZ() {
      return this.lowBoundZ;
   }

   @Override
   public int getHighBoundX() {
      return this.highBoundX;
   }

   @Override
   public int getHighBoundZ() {
      return this.highBoundZ;
   }

   @Override
   public int getLowBoundY() {
      return this.lowBoundY;
   }

   @Override
   public int getHighBoundY() {
      return this.highBoundY;
   }

   @Override
   public boolean shouldReplace(int seed, double x, double z, int y) {
      if (determine(this.o, this.n1, x, y, z) && determine(this.o, this.n2, x, y, z) && determine(this.o, this.n3, x, y, z)) {
         double ox = this.o.x + this.b.x;
         double oy = this.o.y + this.b.y;
         double oz = this.o.z + this.b.z;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public double getFloorPosition(int seed, double x, double z) {
      int y;
      for (y = this.getLowBoundY(); y < this.getHighBoundY(); y++) {
         if (this.shouldReplace(seed, x, z, y)) {
            return y;
         }
      }

      return y;
   }

   @Override
   public double getCeilingPosition(int seed, double x, double z) {
      int y;
      for (y = this.getHighBoundY(); y < this.getLowBoundY(); y--) {
         if (this.shouldReplace(seed, x, z, y)) {
            return y;
         }
      }

      return y;
   }

   private static boolean determine(@Nonnull Vector3d o, @Nonnull Vector3d n, double px, double py, double pz) {
      return determine(o.x, o.y, o.z, n, px, py, pz);
   }

   private static boolean determine(double ox, double oy, double oz, @Nonnull Vector3d n, double px, double py, double pz) {
      double x = (px - ox) * n.x;
      double y = (py - oy) * n.y;
      double z = (pz - oz) * n.z;
      return x + y + z >= 0.0;
   }

   public static class TetrahedronCaveNodeShapeGenerator implements CaveNodeShapeEnum.CaveNodeShapeGenerator {
      @Nonnull
      @Override
      public CaveNodeShape generateCaveNodeShape(
         Random random, CaveType caveType, CaveNode parentNode, CaveNodeType.CaveNodeChildEntry childEntry, @Nonnull Vector3d origin, float yaw, float pitch
      ) {
         return new TetrahedronCaveNodeShape(origin);
      }
   }
}
