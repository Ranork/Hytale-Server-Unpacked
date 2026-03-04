package com.hypixel.hytale.server.worldgen.zoom;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.procedurallib.random.ICoordinateRandomizer;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import java.util.List;
import javax.annotation.Nonnull;

public class FuzzyZoom {
   private final ICoordinateRandomizer randomizer;
   @Nonnull
   private final ExactZoom exactZoom;

   public FuzzyZoom(ICoordinateRandomizer randomizer, @Nonnull PixelProvider source, double zoomX, double zoomY, int offsetX, int offsetY) {
      this.randomizer = randomizer;
      this.exactZoom = new ExactZoom(source, zoomX, zoomY, offsetX, offsetY);
   }

   public FuzzyZoom(ICoordinateRandomizer randomizer, ExactZoom exactZoom) {
      this.randomizer = randomizer;
      this.exactZoom = exactZoom;
   }

   public double getX(int seed, double x, double y) {
      return this.randomizer.randomDoubleX(seed, x, y);
   }

   public double getY(int seed, double x, double y) {
      return this.randomizer.randomDoubleY(seed, x, y);
   }

   public int generate(double x, double y) {
      return this.exactZoom.generate(x, y);
   }

   public double distance(double x, double y) {
      return this.exactZoom.distanceToNextPixel(x, y);
   }

   @Nonnull
   public ExactZoom getExactZoom() {
      return this.exactZoom;
   }

   public boolean inBounds(double x, double y) {
      return this.exactZoom.inBounds(x, y);
   }

   public Zone.UniqueCandidate[] generateUniqueZoneCandidates(Zone.UniqueEntry[] entries, int maxPositions) {
      return this.exactZoom.generateUniqueZoneCandidates(entries, maxPositions);
   }

   public FuzzyZoom generateUniqueZones(Zone.UniqueCandidate[] candidates, FastRandom random, List<Zone.Unique> zones) {
      return new FuzzyZoom(this.randomizer, this.exactZoom.generateUniqueZones(candidates, random, zones));
   }

   @Nonnull
   @Override
   public String toString() {
      return "FuzzyZoom{randomizer=" + this.randomizer + ", exactZoom=" + this.exactZoom + "}";
   }
}
