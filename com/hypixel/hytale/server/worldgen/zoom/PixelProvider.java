package com.hypixel.hytale.server.worldgen.zoom;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class PixelProvider {
   @Nonnull
   protected final int[] pixels;
   protected final int width;
   protected final int height;

   public PixelProvider(@Nonnull BufferedImage image) {
      this.width = image.getWidth();
      this.height = image.getHeight();
      this.pixels = new int[this.width * this.height];

      for (int x = 0; x < image.getWidth(); x++) {
         for (int y = 0; y < image.getHeight(); y++) {
            this.setPixel(x, y, image.getRGB(x, y) & 16777215);
         }
      }
   }

   public PixelProvider(PixelProvider other) {
      this.pixels = Arrays.copyOf(other.pixels, other.pixels.length);
      this.width = other.width;
      this.height = other.height;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int[] getPixels() {
      return this.pixels;
   }

   public void setPixel(int x, int y, int pixel) {
      this.pixels[this.arrIndex(x, y)] = pixel;
   }

   public int getPixel(int x, int y) {
      if (x < 0) {
         x = 0;
      } else if (x >= this.width) {
         x = this.width - 1;
      }

      if (y < 0) {
         y = 0;
      } else if (y >= this.height) {
         y = this.height - 1;
      }

      return this.pixels[this.arrIndex(x, y)];
   }

   protected int arrIndex(int x, int y) {
      return x * this.height + y;
   }

   public PixelProvider copy() {
      return new PixelProvider(this);
   }

   @Nonnull
   @Override
   public String toString() {
      return "PixelProvider{pixels=int[" + this.pixels.length + "], width=" + this.width + ", height=" + this.height + "}";
   }
}
