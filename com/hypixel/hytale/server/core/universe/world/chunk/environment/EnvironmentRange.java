package com.hypixel.hytale.server.core.universe.world.chunk.environment;

import javax.annotation.Nonnull;

public class EnvironmentRange {
   private int min;
   private int max;
   private int id;

   public EnvironmentRange(int id) {
      this(0, 2147483646, id);
   }

   public EnvironmentRange(int min, int max, int id) {
      this.min = min;
      this.max = max;
      this.id = id;
   }

   public int getMin() {
      return this.min;
   }

   void setMin(int min) {
      this.min = min;
   }

   public int getMax() {
      return this.max;
   }

   void setMax(int max) {
      this.max = max;
   }

   public int getId() {
      return this.id;
   }

   void setId(int id) {
      this.id = id;
   }

   public int height() {
      return this.max - this.min + 1;
   }

   @Nonnull
   public EnvironmentRange copy() {
      return new EnvironmentRange(this.min, this.max, this.id);
   }

   @Nonnull
   @Override
   public String toString() {
      return "EnvironmentRange{min=" + this.min + ", max=" + this.max + ", id='" + this.id + "'}";
   }
}
