package com.hypixel.hytale.math.hitdetection;

import com.hypixel.hytale.math.vector.Vector4d;
import javax.annotation.Nonnull;

public class Vector4dBufferList {
   private Vector4d[] vectors;
   private int size;

   public Vector4dBufferList(int size) {
      this.vectors = new Vector4d[size];

      for (int i = 0; i < size; i++) {
         this.vectors[i] = new Vector4d();
      }

      this.size = 0;
   }

   public Vector4d next() {
      return this.vectors[this.size++];
   }

   public void clear() {
      this.size = 0;
   }

   public int size() {
      return this.size;
   }

   public Vector4d get(int i) {
      return this.vectors[i];
   }

   public boolean isEmpty() {
      return this.size == 0;
   }

   @Nonnull
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Vector4dBufferList{vectors=[\n");

      for (int i = 0; i < this.size; i++) {
         sb.append(this.vectors[i]).append(",\n");
      }

      sb.append("], size=").append(this.size).append('}');
      return sb.toString();
   }
}
