package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class BlockColorIndex {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final List<BlockColorIndex.BlockColorEntry> entries = new ArrayList<>();
   private boolean initialized = false;

   private void ensureInitialized() {
      if (!this.initialized) {
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();

         for (String key : assetMap.getAssetMap().keySet()) {
            BlockType blockType = assetMap.getAsset(key);
            if (blockType != null && this.isSolidCube(blockType)) {
               Color particleColor = blockType.getParticleColor();
               if (particleColor != null) {
                  int blockId = assetMap.getIndex(key);
                  int r = particleColor.red & 255;
                  int g = particleColor.green & 255;
                  int b = particleColor.blue & 255;
                  double[] lab = rgbToLab(r, g, b);
                  this.entries.add(new BlockColorIndex.BlockColorEntry(blockId, key, r, g, b, lab[0], lab[1], lab[2]));
               }
            }
         }

         this.entries.sort(Comparator.comparingDouble(e -> e.labL));
         this.initialized = true;
         LOGGER.at(Level.INFO).log("BlockColorIndex initialized with %d solid cube blocks", this.entries.size());
      }
   }

   private boolean isSolidCube(@Nonnull BlockType blockType) {
      DrawType drawType = blockType.getDrawType();
      Opacity opacity = blockType.getOpacity();
      return drawType == DrawType.Cube && opacity == Opacity.Solid;
   }

   public int findClosestBlock(int r, int g, int b) {
      this.ensureInitialized();
      if (this.entries.isEmpty()) {
         return -1;
      } else {
         double[] lab = rgbToLab(r, g, b);
         double targetL = lab[0];
         double targetA = lab[1];
         double targetB = lab[2];
         double minDist = Double.MAX_VALUE;
         int bestId = -1;

         for (BlockColorIndex.BlockColorEntry entry : this.entries) {
            double dist = colorDistanceLab(targetL, targetA, targetB, entry.labL, entry.labA, entry.labB);
            if (dist < minDist) {
               minDist = dist;
               bestId = entry.blockId;
            }
         }

         return bestId;
      }
   }

   public int findDarkerVariant(int blockId, float darkenAmount) {
      this.ensureInitialized();
      BlockColorIndex.BlockColorEntry source = this.findEntry(blockId);
      if (source == null) {
         return blockId;
      } else {
         double targetL = source.labL * (1.0 - darkenAmount);
         double targetA = source.labA;
         double targetB = source.labB;
         double minDist = Double.MAX_VALUE;
         int bestId = blockId;

         for (BlockColorIndex.BlockColorEntry entry : this.entries) {
            if (!(entry.labL > source.labL)) {
               double dist = colorDistanceLab(targetL, targetA, targetB, entry.labL, entry.labA, entry.labB);
               if (dist < minDist) {
                  minDist = dist;
                  bestId = entry.blockId;
               }
            }
         }

         return bestId;
      }
   }

   public int getBlockColor(int blockId) {
      this.ensureInitialized();
      BlockColorIndex.BlockColorEntry entry = this.findEntry(blockId);
      return entry == null ? -1 : entry.r << 16 | entry.g << 8 | entry.b;
   }

   public int findBlockForLerpedColor(int rA, int gA, int bA, int rB, int gB, int bB, float t) {
      this.ensureInitialized();
      double[] labA = rgbToLab(rA, gA, bA);
      double[] labB = rgbToLab(rB, gB, bB);
      double l = labA[0] + (labB[0] - labA[0]) * t;
      double a = labA[1] + (labB[1] - labA[1]) * t;
      double b = labA[2] + (labB[2] - labA[2]) * t;
      int[] rgb = labToRgb(l, a, b);
      return this.findClosestBlock(rgb[0], rgb[1], rgb[2]);
   }

   public boolean isEmpty() {
      this.ensureInitialized();
      return this.entries.isEmpty();
   }

   @Nullable
   private BlockColorIndex.BlockColorEntry findEntry(int blockId) {
      for (BlockColorIndex.BlockColorEntry entry : this.entries) {
         if (entry.blockId == blockId) {
            return entry;
         }
      }

      return null;
   }

   private static double colorDistanceLab(double l1, double a1, double b1, double l2, double a2, double b2) {
      double dL = l1 - l2;
      double dA = a1 - a2;
      double dB = b1 - b2;
      return dL * dL + dA * dA + dB * dB;
   }

   private static double[] rgbToLab(int r, int g, int b) {
      double rn = r / 255.0;
      double gn = g / 255.0;
      double bn = b / 255.0;
      rn = rn > 0.04045 ? Math.pow((rn + 0.055) / 1.055, 2.4) : rn / 12.92;
      gn = gn > 0.04045 ? Math.pow((gn + 0.055) / 1.055, 2.4) : gn / 12.92;
      bn = bn > 0.04045 ? Math.pow((bn + 0.055) / 1.055, 2.4) : bn / 12.92;
      double x = rn * 0.4124564 + gn * 0.3575761 + bn * 0.1804375;
      double y = rn * 0.2126729 + gn * 0.7151522 + bn * 0.072175;
      double z = rn * 0.0193339 + gn * 0.119192 + bn * 0.9503041;
      x /= 0.95047;
      y /= 1.0;
      z /= 1.08883;
      x = x > 0.008856 ? Math.cbrt(x) : 7.787 * x + 0.13793103448275862;
      y = y > 0.008856 ? Math.cbrt(y) : 7.787 * y + 0.13793103448275862;
      z = z > 0.008856 ? Math.cbrt(z) : 7.787 * z + 0.13793103448275862;
      double labL = 116.0 * y - 16.0;
      double labA = 500.0 * (x - y);
      double labB = 200.0 * (y - z);
      return new double[]{labL, labA, labB};
   }

   private static int[] labToRgb(double labL, double labA, double labB) {
      double y = (labL + 16.0) / 116.0;
      double x = labA / 500.0 + y;
      double z = y - labB / 200.0;
      double x3 = x * x * x;
      double y3 = y * y * y;
      double z3 = z * z * z;
      x = x3 > 0.008856 ? x3 : (x - 0.13793103448275862) / 7.787;
      y = y3 > 0.008856 ? y3 : (y - 0.13793103448275862) / 7.787;
      z = z3 > 0.008856 ? z3 : (z - 0.13793103448275862) / 7.787;
      x *= 0.95047;
      y *= 1.0;
      z *= 1.08883;
      double rn = x * 3.2404542 + y * -1.5371385 + z * -0.4985314;
      double gn = x * -0.969266 + y * 1.8760108 + z * 0.041556;
      double bn = x * 0.0556434 + y * -0.2040259 + z * 1.0572252;
      rn = rn > 0.0031308 ? 1.055 * Math.pow(rn, 0.4166666666666667) - 0.055 : 12.92 * rn;
      gn = gn > 0.0031308 ? 1.055 * Math.pow(gn, 0.4166666666666667) - 0.055 : 12.92 * gn;
      bn = bn > 0.0031308 ? 1.055 * Math.pow(bn, 0.4166666666666667) - 0.055 : 12.92 * bn;
      int r = Math.max(0, Math.min(255, (int)Math.round(rn * 255.0)));
      int g = Math.max(0, Math.min(255, (int)Math.round(gn * 255.0)));
      int b = Math.max(0, Math.min(255, (int)Math.round(bn * 255.0)));
      return new int[]{r, g, b};
   }

   private record BlockColorEntry(int blockId, String key, int r, int g, int b, double labL, double labA, double labB) {
   }
}
