package com.hypixel.hytale.math.iterator;

import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;

public class LineIterator implements Iterator<Vector3i> {
   private final int x_inc;
   private final int y_inc;
   private final int z_inc;
   private final int l;
   private final int m;
   private final int n;
   private final int dx2;
   private final int dy2;
   private final int dz2;
   private int i;
   private int err1;
   private int err2;
   private int pointX;
   private int pointY;
   private int pointZ;

   public LineIterator(int x1, int y1, int z1, int x2, int y2, int z2) {
      this.pointX = x1;
      this.pointY = y1;
      this.pointZ = z1;
      int dx = x2 - x1;
      int dy = y2 - y1;
      int dz = z2 - z1;
      this.x_inc = dx < 0 ? -1 : 1;
      this.y_inc = dy < 0 ? -1 : 1;
      this.z_inc = dz < 0 ? -1 : 1;
      this.l = Math.abs(dx);
      this.m = Math.abs(dy);
      this.n = Math.abs(dz);
      this.dx2 = this.l << 1;
      this.dy2 = this.m << 1;
      this.dz2 = this.n << 1;
      if (this.l >= this.m && this.l >= this.n) {
         this.err1 = this.dy2 - this.l;
         this.err2 = this.dz2 - this.l;
      } else if (this.m >= this.l && this.m >= this.n) {
         this.err1 = this.dx2 - this.m;
         this.err2 = this.dz2 - this.m;
      } else {
         this.err1 = this.dx2 - this.n;
         this.err2 = this.dy2 - this.n;
      }
   }

   @Override
   public boolean hasNext() {
      if (this.l >= this.m && this.l >= this.n) {
         return this.i <= this.l;
      } else {
         return this.m >= this.l && this.m >= this.n ? this.i <= this.m : this.i <= this.n;
      }
   }

   @Nonnull
   public Vector3i next() {
      if (this.l >= this.m && this.l >= this.n) {
         if (this.i == this.l) {
            this.i++;
            return new Vector3i(this.pointX, this.pointY, this.pointZ);
         } else if (this.i > this.l) {
            throw new NoSuchElementException();
         } else {
            Vector3i vector3i = new Vector3i(this.pointX, this.pointY, this.pointZ);
            this.pointX = this.pointX + this.x_inc;
            if (this.err1 > 0) {
               this.pointY = this.pointY + this.y_inc;
               this.err1 = this.err1 - this.dx2;
            }

            this.err1 = this.err1 + this.dy2;
            if (this.err2 > 0) {
               this.pointZ = this.pointZ + this.z_inc;
               this.err2 = this.err2 - this.dx2;
            }

            this.err2 = this.err2 + this.dz2;
            this.i++;
            return vector3i;
         }
      } else if (this.m >= this.l && this.m >= this.n) {
         if (this.i == this.m) {
            this.i++;
            return new Vector3i(this.pointX, this.pointY, this.pointZ);
         } else if (this.i > this.m) {
            throw new NoSuchElementException();
         } else {
            Vector3i vector3ix = new Vector3i(this.pointX, this.pointY, this.pointZ);
            if (this.err1 > 0) {
               this.pointX = this.pointX + this.x_inc;
               this.err1 = this.err1 - this.dy2;
            }

            this.err1 = this.err1 + this.dx2;
            this.pointY = this.pointY + this.y_inc;
            if (this.err2 > 0) {
               this.pointZ = this.pointZ + this.z_inc;
               this.err2 = this.err2 - this.dy2;
            }

            this.err2 = this.err2 + this.dz2;
            this.i++;
            return vector3ix;
         }
      } else if (this.i == this.n) {
         this.i++;
         return new Vector3i(this.pointX, this.pointY, this.pointZ);
      } else if (this.i > this.n) {
         throw new NoSuchElementException();
      } else {
         Vector3i vector3ixx = new Vector3i(this.pointX, this.pointY, this.pointZ);
         if (this.err1 > 0) {
            this.pointX = this.pointX + this.x_inc;
            this.err1 = this.err1 - this.dz2;
         }

         this.err1 = this.err1 + this.dx2;
         if (this.err2 > 0) {
            this.pointY = this.pointY + this.y_inc;
            this.err2 = this.err2 - this.dz2;
         }

         this.err2 = this.err2 + this.dy2;
         this.pointZ = this.pointZ + this.z_inc;
         this.i++;
         return vector3ixx;
      }
   }
}
