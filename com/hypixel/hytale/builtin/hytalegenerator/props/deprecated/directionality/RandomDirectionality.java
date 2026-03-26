package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RandomDirectionality extends Directionality {
   @Nonnull
   private final List<PrefabRotation> rotations;
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final RngField rngField;

   public RandomDirectionality(@Nonnull Pattern pattern, int seed) {
      this.pattern = pattern;
      this.rngField = new RngField(seed);
      this.rotations = Collections.unmodifiableList(
         List.of(PrefabRotation.ROTATION_0, PrefabRotation.ROTATION_90, PrefabRotation.ROTATION_180, PrefabRotation.ROTATION_270)
      );
   }

   @Nonnull
   @Override
   public Pattern getGeneralPattern() {
      return this.pattern;
   }

   @NonNullDecl
   @Override
   public Bounds3i getBoundsWith_voxelGrid(@NonNullDecl Scanner scanner) {
      return this.pattern.getBounds_voxelGrid();
   }

   @Nonnull
   @Override
   public List<PrefabRotation> getPossibleRotations() {
      return this.rotations;
   }

   @Override
   public PrefabRotation getRotationAt(@Nonnull Pattern.Context context) {
      FastRandom random = new FastRandom(this.rngField.get(context.position.x, context.position.y, context.position.z));
      return this.rotations.get(random.nextInt(this.rotations.size()));
   }
}
