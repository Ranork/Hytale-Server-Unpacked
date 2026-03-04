package com.hypixel.hytale.builtin.hytalegenerator.props.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.OrPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class PatternDirectionality extends Directionality {
   @Nonnull
   private final List<PrefabRotation> rotations;
   @Nonnull
   private final PrefabRotation south;
   @Nonnull
   private final PrefabRotation north;
   @Nonnull
   private final PrefabRotation east;
   @Nonnull
   private final PrefabRotation west;
   @Nonnull
   private final Pattern southPattern;
   @Nonnull
   private final Pattern northPattern;
   @Nonnull
   private final Pattern eastPattern;
   @Nonnull
   private final Pattern westPattern;
   @Nonnull
   private final Pattern generalPattern;
   @Nonnull
   private final SeedGenerator seedGenerator;

   public PatternDirectionality(
      @Nonnull OrthogonalDirection startingDirection,
      @Nonnull Pattern southPattern,
      @Nonnull Pattern northPattern,
      @Nonnull Pattern eastPattern,
      @Nonnull Pattern westPattern,
      int seed
   ) {
      this.southPattern = southPattern;
      this.northPattern = northPattern;
      this.eastPattern = eastPattern;
      this.westPattern = westPattern;
      this.generalPattern = new OrPattern(List.of(northPattern, southPattern, eastPattern, westPattern));
      this.seedGenerator = new SeedGenerator(seed);
      switch (startingDirection) {
         case S:
            this.south = PrefabRotation.ROTATION_0;
            this.west = PrefabRotation.ROTATION_270;
            this.north = PrefabRotation.ROTATION_180;
            this.east = PrefabRotation.ROTATION_90;
            break;
         case E:
            this.east = PrefabRotation.ROTATION_180;
            this.south = PrefabRotation.ROTATION_90;
            this.west = PrefabRotation.ROTATION_0;
            this.north = PrefabRotation.ROTATION_270;
            break;
         case W:
            this.west = PrefabRotation.ROTATION_180;
            this.north = PrefabRotation.ROTATION_90;
            this.east = PrefabRotation.ROTATION_0;
            this.south = PrefabRotation.ROTATION_270;
            break;
         default:
            this.north = PrefabRotation.ROTATION_0;
            this.east = PrefabRotation.ROTATION_270;
            this.south = PrefabRotation.ROTATION_180;
            this.west = PrefabRotation.ROTATION_90;
      }

      this.rotations = Collections.unmodifiableList(List.of(this.north, this.south, this.east, this.west));
   }

   @Nonnull
   @Override
   public Pattern getGeneralPattern() {
      return this.generalPattern;
   }

   @Nonnull
   @Override
   public Vector3i getReadRangeWith(@Nonnull Scanner scanner) {
      return scanner.readSpaceWith(this.generalPattern).getRange();
   }

   @Nonnull
   @Override
   public List<PrefabRotation> getPossibleRotations() {
      return this.rotations;
   }

   @Override
   public PrefabRotation getRotationAt(@Nonnull Pattern.Context context) {
      ArrayList<PrefabRotation> successful = new ArrayList<>(4);
      if (this.northPattern.matches(context)) {
         successful.add(this.north);
      }

      if (this.southPattern.matches(context)) {
         successful.add(this.south);
      }

      if (this.eastPattern.matches(context)) {
         successful.add(this.east);
      }

      if (this.westPattern.matches(context)) {
         successful.add(this.west);
      }

      if (successful.isEmpty()) {
         return null;
      } else {
         FastRandom random = new FastRandom(this.seedGenerator.seedAt((long)context.position.x, (long)context.position.y, (long)context.position.z));
         return successful.get(random.nextInt(successful.size()));
      }
   }
}
