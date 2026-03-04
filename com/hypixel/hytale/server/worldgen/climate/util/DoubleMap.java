package com.hypixel.hytale.server.worldgen.climate.util;

import java.util.Arrays;

public class DoubleMap {
   public final int width;
   public final int height;
   protected final double[] values;

   public DoubleMap(int width, int height) {
      this.width = width;
      this.height = height;
      this.values = new double[width * height];
      this.clear();
   }

   public int index(int x, int y) {
      return y * this.width + x;
   }

   public boolean validate(int index) {
      return index > -1 && index < this.values.length;
   }

   public void clear() {
      Arrays.fill(this.values, -1.0);
   }

   public double at(int x, int y) {
      return this.at(this.index(x, y));
   }

   public double at(int index) {
      return this.values[index];
   }

   public void set(int x, int y, double value) {
      this.set(this.index(x, y), value);
   }

   public void set(int index, double value) {
      this.values[index] = value;
   }
}
