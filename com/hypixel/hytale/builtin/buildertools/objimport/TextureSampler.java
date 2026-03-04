package com.hypixel.hytale.builtin.buildertools.objimport;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

public final class TextureSampler {
   private static final Map<Path, BufferedImage> textureCache = new HashMap<>();

   private TextureSampler() {
   }

   @Nullable
   public static BufferedImage loadTexture(@Nonnull Path path) {
      if (!Files.exists(path)) {
         return null;
      } else {
         BufferedImage cached = textureCache.get(path);
         if (cached != null) {
            return cached;
         } else {
            try {
               BufferedImage image = ImageIO.read(path.toFile());
               if (image != null) {
                  textureCache.put(path, image);
               }

               return image;
            } catch (IOException var3) {
               return null;
            }
         }
      }
   }

   @Nonnull
   public static int[] sampleAt(@Nonnull BufferedImage texture, float u, float v) {
      u -= (float)Math.floor(u);
      v -= (float)Math.floor(v);
      v = 1.0F - v;
      int width = texture.getWidth();
      int height = texture.getHeight();
      int x = Math.min((int)(u * width), width - 1);
      int y = Math.min((int)(v * height), height - 1);
      int rgb = texture.getRGB(x, y);
      return new int[]{rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF};
   }

   public static int sampleAlphaAt(@Nonnull BufferedImage texture, float u, float v) {
      if (!texture.getColorModel().hasAlpha()) {
         return 255;
      } else {
         u -= (float)Math.floor(u);
         v -= (float)Math.floor(v);
         v = 1.0F - v;
         int width = texture.getWidth();
         int height = texture.getHeight();
         int x = Math.min((int)(u * width), width - 1);
         int y = Math.min((int)(v * height), height - 1);
         int rgba = texture.getRGB(x, y);
         return rgba >> 24 & 0xFF;
      }
   }

   public static void clearCache() {
      textureCache.clear();
   }

   @Nullable
   public static int[] getAverageColor(@Nonnull Path path) {
      if (!Files.exists(path)) {
         return null;
      } else {
         try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
               return null;
            } else {
               long totalR = 0L;
               long totalG = 0L;
               long totalB = 0L;
               int count = 0;
               int width = image.getWidth();
               int height = image.getHeight();
               boolean hasAlpha = image.getColorModel().hasAlpha();

               for (int y = 0; y < height; y++) {
                  for (int x = 0; x < width; x++) {
                     int rgba = image.getRGB(x, y);
                     if (hasAlpha) {
                        int alpha = rgba >> 24 & 0xFF;
                        if (alpha == 0) {
                           continue;
                        }
                     }

                     int r = rgba >> 16 & 0xFF;
                     int g = rgba >> 8 & 0xFF;
                     int b = rgba & 0xFF;
                     totalR += r;
                     totalG += g;
                     totalB += b;
                     count++;
                  }
               }

               return count == 0 ? null : new int[]{(int)(totalR / count), (int)(totalG / count), (int)(totalB / count)};
            }
         } catch (IOException var18) {
            return null;
         }
      }
   }
}
