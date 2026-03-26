package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AndPattern extends Pattern {
   @Nonnull
   private final Pattern[] patterns;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;

   public AndPattern(@Nonnull List<Pattern> patterns) {
      if (patterns.isEmpty()) {
         this.patterns = new Pattern[0];
         this.bounds_voxelGrid = Bounds3i.ZERO;
      } else {
         this.patterns = new Pattern[patterns.size()];
         this.bounds_voxelGrid = patterns.getFirst().getBounds_voxelGrid().clone();

         for (int i = 0; i < patterns.size(); i++) {
            Pattern pattern = patterns.get(i);
            this.patterns[i] = pattern;
            this.bounds_voxelGrid.encompass(pattern.getBounds_voxelGrid());
         }
      }
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      for (Pattern pattern : this.patterns) {
         if (!pattern.matches(context)) {
            return false;
         }
      }

      return true;
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds_voxelGrid;
   }
}
