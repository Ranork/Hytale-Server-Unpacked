package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public abstract class Directionality {
   @Nullable
   public abstract PrefabRotation getRotationAt(@Nonnull Pattern.Context var1);

   @Nonnull
   public abstract Pattern getGeneralPattern();

   @Nonnull
   public abstract Bounds3i getBoundsWith_voxelGrid(@Nonnull Scanner var1);

   @Nonnull
   public abstract List<PrefabRotation> getPossibleRotations();

   @Nonnull
   public static Directionality noDirectionality() {
      return new Directionality() {
         @Override
         public PrefabRotation getRotationAt(@Nonnull Pattern.Context context) {
            return null;
         }

         @Nonnull
         @Override
         public Pattern getGeneralPattern() {
            return ConstantPattern.INSTANCE_FALSE;
         }

         @NonNullDecl
         @Override
         public Bounds3i getBoundsWith_voxelGrid(@NonNullDecl Scanner scanner) {
            return Bounds3i.ZERO;
         }

         @Nonnull
         @Override
         public List<PrefabRotation> getPossibleRotations() {
            return Collections.emptyList();
         }
      };
   }
}
