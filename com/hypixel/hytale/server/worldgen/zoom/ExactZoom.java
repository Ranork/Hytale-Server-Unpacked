package com.hypixel.hytale.server.worldgen.zoom;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExactZoom {
   @Nonnull
   private final PixelProvider source;
   @Nonnull
   private final PixelDistanceProvider distanceProvider;
   private final double zoomX;
   private final double zoomY;
   private final int offsetX;
   private final int offsetY;

   public ExactZoom(@Nonnull PixelProvider source, double zoomX, double zoomY, int offsetX, int offsetY) {
      this.source = source;
      this.distanceProvider = new PixelDistanceProvider(source);
      this.zoomX = zoomX;
      this.zoomY = zoomY;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
   }

   @Nonnull
   public PixelDistanceProvider getDistanceProvider() {
      return this.distanceProvider;
   }

   public boolean inBounds(double x, double y) {
      x += this.offsetX;
      y += this.offsetY;
      if (!(x < 0.0) && !(y < 0.0)) {
         x /= this.zoomX;
         y /= this.zoomY;
         return this.source.getWidth() - 1 >= x && this.source.getHeight() - 1 >= y;
      } else {
         return false;
      }
   }

   public int generate(double x, double y) {
      x += this.offsetX;
      y += this.offsetY;
      x /= this.zoomX;
      y /= this.zoomY;
      int px = Math.max(0, Math.min(MathUtil.floor(x), this.source.getWidth() - 1));
      int py = Math.max(0, Math.min(MathUtil.floor(y), this.source.getHeight() - 1));
      return this.source.getPixel(px, py);
   }

   public double distanceToNextPixel(double x, double y) {
      x += this.offsetX;
      y += this.offsetY;
      x /= this.zoomX;
      y /= this.zoomY;
      return this.zoomX * Math.sqrt(this.distanceProvider.distanceSqToDifferentPixel(x, y, (int)MathUtil.fastFloor(x), (int)MathUtil.fastFloor(y)));
   }

   public ExactZoom generateUniqueZones(Zone.UniqueCandidate[] candidates, FastRandom random, List<Zone.Unique> zones) {
      PixelProvider source = this.source.copy();

      for (int i = 0; i < candidates.length; i++) {
         Zone.UniqueCandidate candidate = candidates[i];
         Vector2i pos = selectCandidatePosition(candidate, source, random);
         if (pos == null) {
            LogUtil.getLogger().at(Level.WARNING).log("Failed to place unique zone: %s", candidate.zone());
         } else {
            int radius = candidate.zone().radius();
            int radius2 = radius * radius;

            for (int dy = -radius; dy <= radius; dy++) {
               for (int dx = -radius; dx <= radius; dx++) {
                  if (dx * dx + dy * dy <= radius2) {
                     source.setPixel(pos.x + dx, pos.y + dy, candidate.zone().color());
                  }
               }
            }

            zones.add(
               new Zone.Unique(
                  candidate.zone().zone(),
                  CompletableFuture.completedFuture(new Vector2i((int)(pos.x * this.zoomX - this.offsetX), (int)(pos.y * this.zoomY - this.offsetY)))
               )
            );
         }
      }

      return new ExactZoom(source, this.zoomX, this.zoomY, this.offsetX, this.offsetY);
   }

   public Zone.UniqueCandidate[] generateUniqueZoneCandidates(Zone.UniqueEntry[] entries, int maxPositions) {
      Zone.UniqueCandidate[] candidates = new Zone.UniqueCandidate[entries.length];
      ArrayList<Vector2i> positions = new ArrayList<>();

      for (int i = 0; i < entries.length; i++) {
         Zone.UniqueEntry entry = entries[i];
         int radius = entry.radius();
         int searchRadius = radius + entry.padding();
         positions.clear();

         for (int iy = searchRadius; iy < this.source.getHeight() - 1 - searchRadius; iy++) {
            for (int ix = searchRadius; ix < this.source.getWidth() - 1 - searchRadius; ix++) {
               if (testZoneFit(entry, this.source, ix, iy, searchRadius)) {
                  positions.add(new Vector2i(ix, iy));
               }
            }
         }

         int size = positions.size();
         if (size == 0) {
            throw new Error("No parent matches found for unique zone entry: " + entry);
         }

         if (size <= maxPositions) {
            candidates[i] = new Zone.UniqueCandidate(entry, positions.toArray(Vector2i[]::new));
         } else {
            int n = Math.max(1, size / maxPositions);
            int count = size / n;
            Vector2i[] arr = new Vector2i[count];

            for (int j = 0; j < count; j++) {
               arr[j] = positions.get(j * n);
            }

            candidates[i] = new Zone.UniqueCandidate(entry, arr);
         }
      }

      return candidates;
   }

   @Nullable
   private static Vector2i selectCandidatePosition(Zone.UniqueCandidate candidate, PixelProvider source, FastRandom random) {
      Vector2i[] positions = candidate.positions();
      int searchRadius = candidate.zone().radius() + candidate.zone().padding();

      for (int len = positions.length; len > 0; len--) {
         int index = random.nextInt(len);
         Vector2i pos = positions[index];
         if (testZoneFit(candidate.zone(), source, pos.x, pos.y, searchRadius)) {
            return pos;
         }

         Vector2i back = positions[len - 1];
         positions[len - 1] = pos;
         positions[index] = back;
      }

      return null;
   }

   private static boolean testZoneFit(Zone.UniqueEntry entry, PixelProvider source, int x, int y, int radius) {
      int radius2 = radius * radius;

      for (int dy = -radius; dy <= radius; dy++) {
         for (int dx = -radius; dx <= radius; dx++) {
            if (dx * dx + dy * dy <= radius2 && !entry.matchesParent(source.getPixel(x + dx, y + dy))) {
               return false;
            }
         }
      }

      return true;
   }

   public BufferedImage exportImage() {
      BufferedImage image = new BufferedImage(this.source.getWidth(), this.source.getHeight(), 1);

      for (int x = 0; x < this.source.getWidth(); x++) {
         for (int y = 0; y < this.source.getHeight(); y++) {
            image.setRGB(x, y, this.source.getPixel(x, y));
         }
      }

      return image;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ExactZoom{source="
         + this.source
         + ", distanceProvider="
         + this.distanceProvider
         + ", zoomX="
         + this.zoomX
         + ", zoomY="
         + this.zoomY
         + ", offsetX="
         + this.offsetX
         + ", offsetY="
         + this.offsetY
         + "}";
   }
}
