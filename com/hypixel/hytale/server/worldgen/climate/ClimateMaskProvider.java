package com.hypixel.hytale.server.worldgen.climate;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.random.CoordinateRandomizer;
import com.hypixel.hytale.procedurallib.random.ICoordinateRandomizer;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.MaskProvider;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zoom.ExactZoom;
import com.hypixel.hytale.server.worldgen.zoom.FuzzyZoom;
import com.hypixel.hytale.server.worldgen.zoom.PixelProvider;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class ClimateMaskProvider extends MaskProvider {
   private static final FuzzyZoom EMPTY_ZOOM = new FuzzyZoom(
      CoordinateRandomizer.EMPTY_RANDOMIZER, new ExactZoom(new PixelProvider(new BufferedImage(1, 1, 1)), 1.0, 1.0, 0, 0)
   );
   @Nonnull
   private final ICoordinateRandomizer randomizer;
   @Nonnull
   private final ClimateNoise noise;
   @Nonnull
   private final ClimateGraph graph;
   @Nonnull
   private final UniqueClimateGenerator uniqueGenerator;

   public ClimateMaskProvider(
      @Nonnull ICoordinateRandomizer randomizer, @Nonnull ClimateNoise noise, @Nonnull ClimateGraph graph, @Nonnull UniqueClimateGenerator uniqueGenerator
   ) {
      super(EMPTY_ZOOM);
      this.randomizer = randomizer;
      this.noise = noise;
      this.graph = graph;
      this.uniqueGenerator = uniqueGenerator;
   }

   private ClimateMaskProvider(@Nonnull ClimateMaskProvider other, @Nonnull UniqueClimateGenerator uniqueGenerator) {
      super(EMPTY_ZOOM);
      this.randomizer = other.randomizer;
      this.noise = other.noise;
      this.graph = other.graph;
      this.uniqueGenerator = uniqueGenerator;
   }

   @Nonnull
   public ClimateGraph getGraph() {
      return this.graph;
   }

   @Override
   public boolean inBounds(double x, double y) {
      return true;
   }

   @Override
   public double getX(int seed, double x, double y) {
      return this.randomizer.randomDoubleX(seed, x, y);
   }

   @Override
   public double getY(int seed, double x, double y) {
      return this.randomizer.randomDoubleY(seed, x, y);
   }

   @Override
   public int get(int seed, double x, double y) {
      int unique = this.uniqueGenerator.generate(MathUtil.floor(x), MathUtil.floor(y));
      if (unique != -1) {
         return unique;
      } else {
         ClimateNoise.Buffer buffer = ChunkGenerator.getResource().climateBuffer;
         int id = this.noise.generate(seed, x, y, buffer, this.graph);
         return ClimateType.color(id, this.graph);
      }
   }

   @Override
   public double distance(double x, double y) {
      ClimateNoise.Buffer buffer = ChunkGenerator.getResource().climateBuffer;
      return buffer.fade;
   }

   @Override
   public MaskProvider generateUniqueZones(
      int seed, @Nonnull Zone.UniqueCandidate[] candidates, @Nonnull FastRandom random, @Nonnull List<Zone.Unique> collector
   ) {
      return candidates.length == 0 ? this : new ClimateMaskProvider(this, this.uniqueGenerator.apply(seed, candidates, this.noise, this.graph, collector));
   }

   @Override
   public Zone.UniqueCandidate[] generateUniqueZoneCandidates(Zone.UniqueEntry[] entries, int maxPositions) {
      return Zone.UniqueCandidate.EMPTY_ARRAY;
   }

   public Zone.UniqueCandidate[] getUniqueZoneCandidates(Map<String, Zone> zoneLookup) {
      return this.uniqueGenerator.getCandidates(zoneLookup);
   }
}
