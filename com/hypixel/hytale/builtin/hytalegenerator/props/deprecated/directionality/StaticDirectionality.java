package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class StaticDirectionality extends Directionality {
   @Nonnull
   private final List<PrefabRotation> possibleRotations;
   @Nonnull
   private final PrefabRotation rotation;
   @Nonnull
   private final Pattern pattern;

   public StaticDirectionality(@Nonnull PrefabRotation rotation, @Nonnull Pattern pattern) {
      this.rotation = rotation;
      this.pattern = pattern;
      this.possibleRotations = Collections.unmodifiableList(List.of(rotation));
   }

   @Override
   public PrefabRotation getRotationAt(@Nonnull Pattern.Context context) {
      return this.rotation;
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
      return this.possibleRotations;
   }
}
