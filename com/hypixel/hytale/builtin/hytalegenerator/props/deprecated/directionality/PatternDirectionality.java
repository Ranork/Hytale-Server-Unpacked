package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.OrPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

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
   private final RngField rngField;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;

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
      this.rngField = new RngField(seed);
      this.bounds_voxelGrid = this.generalPattern.getBounds_voxelGrid().clone();
      this.bounds_voxelGrid.encompass(southPattern.getBounds_voxelGrid());
      this.bounds_voxelGrid.encompass(northPattern.getBounds_voxelGrid());
      this.bounds_voxelGrid.encompass(eastPattern.getBounds_voxelGrid());
      this.bounds_voxelGrid.encompass(westPattern.getBounds_voxelGrid());
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

   @NonNullDecl
   @Override
   public Bounds3i getBoundsWith_voxelGrid(@NonNullDecl Scanner scanner) {
      return this.bounds_voxelGrid;
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
         FastRandom random = new FastRandom(this.rngField.get(context.position.x, context.position.y, context.position.z));
         return successful.get(random.nextInt(successful.size()));
      }
   }
}
