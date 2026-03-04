package com.hypixel.hytale.server.worldgen.chunk;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zoom.FuzzyZoom;
import java.util.List;
import javax.annotation.Nonnull;

public class MaskProvider {
   protected final FuzzyZoom fuzzyZoom;

   public MaskProvider(FuzzyZoom fuzzyZoom) {
      this.fuzzyZoom = fuzzyZoom;
   }

   public double getX(int seed, double x, double y) {
      return this.fuzzyZoom.getX(seed, x, y);
   }

   public double getY(int seed, double x, double y) {
      return this.fuzzyZoom.getY(seed, x, y);
   }

   public int get(int seed, double x, double y) {
      return this.fuzzyZoom.generate(x, y);
   }

   public double distance(double x, double y) {
      return this.fuzzyZoom.distance(x, y);
   }

   public boolean inBounds(double x, double y) {
      return this.fuzzyZoom.inBounds(x, y);
   }

   public FuzzyZoom getFuzzyZoom() {
      return this.fuzzyZoom;
   }

   public Zone.UniqueCandidate[] generateUniqueZoneCandidates(Zone.UniqueEntry[] entries, int maxPositions) {
      return this.fuzzyZoom.generateUniqueZoneCandidates(entries, maxPositions);
   }

   public MaskProvider generateUniqueZones(int seed, Zone.UniqueCandidate[] candidates, FastRandom random, List<Zone.Unique> zones) {
      return new MaskProvider(this.fuzzyZoom.generateUniqueZones(candidates, random, zones));
   }

   @Nonnull
   @Override
   public String toString() {
      return "MaskProvider{fuzzyZoom=" + this.fuzzyZoom + "}";
   }
}
