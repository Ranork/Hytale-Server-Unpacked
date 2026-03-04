package com.hypixel.hytale.builtin.buildertools.objimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MtlParser {
   private MtlParser() {
   }

   @Nonnull
   public static Map<String, MtlParser.MtlMaterial> parse(@Nonnull Path path) throws IOException {
      Map<String, MtlParser.MtlMaterial> materials = new HashMap<>();
      String currentMaterial = null;
      float[] currentKd = null;
      String currentMapKd = null;

      try (BufferedReader reader = Files.newBufferedReader(path)) {
         String line;
         while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
               String[] parts = line.split("\\s+", 2);
               if (parts.length != 0) {
                  String var8 = parts[0];
                  switch (var8) {
                     case "newmtl":
                        if (currentMaterial != null) {
                           materials.put(currentMaterial, new MtlParser.MtlMaterial(currentMaterial, currentKd, currentMapKd));
                        }

                        currentMaterial = parts.length > 1 ? parts[1].trim() : "";
                        currentKd = null;
                        currentMapKd = null;
                        break;
                     case "Kd":
                        if (parts.length > 1) {
                           currentKd = parseColor(parts[1]);
                        }
                        break;
                     case "map_Kd":
                        if (parts.length > 1) {
                           currentMapKd = parts[1].trim();
                        }
                  }
               }
            }
         }

         if (currentMaterial != null) {
            materials.put(currentMaterial, new MtlParser.MtlMaterial(currentMaterial, currentKd, currentMapKd));
         }
      }

      return materials;
   }

   @Nullable
   private static float[] parseColor(String colorStr) {
      String[] parts = colorStr.trim().split("\\s+");
      if (parts.length < 3) {
         return null;
      } else {
         try {
            float r = Float.parseFloat(parts[0]);
            float g = Float.parseFloat(parts[1]);
            float b = Float.parseFloat(parts[2]);
            return new float[]{r, g, b};
         } catch (NumberFormatException var5) {
            return null;
         }
      }
   }

   public record MtlMaterial(@Nonnull String name, @Nullable float[] diffuseColor, @Nullable String diffuseTexturePath) {
      @Nullable
      public int[] getDiffuseColorRGB() {
         return this.diffuseColor == null
            ? null
            : new int[]{
               Math.max(0, Math.min(255, Math.round(this.diffuseColor[0] * 255.0F))),
               Math.max(0, Math.min(255, Math.round(this.diffuseColor[1] * 255.0F))),
               Math.max(0, Math.min(255, Math.round(this.diffuseColor[2] * 255.0F)))
            };
      }
   }
}
