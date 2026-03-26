package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.DelimiterDouble;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DensitySelectorProp extends Prop {
   @Nonnull
   private final List<DelimiterDouble<Prop>> propDelimiters;
   @Nonnull
   private final Density density;
   @Nonnull
   private final Bounds3i readBounds;
   @Nonnull
   private final Bounds3i writeBounds;
   @Nonnull
   private final Density.Context rDensityContext;

   public DensitySelectorProp(@Nonnull List<DelimiterDouble<Prop>> propDelimiters, @Nonnull Density density) {
      this.propDelimiters = new ArrayList<>(propDelimiters);
      this.density = density;
      this.readBounds = new Bounds3i();
      this.writeBounds = new Bounds3i();

      for (DelimiterDouble<Prop> delimiter : this.propDelimiters) {
         assert delimiter.getValue() != null;

         this.readBounds.encompass(delimiter.getValue().getReadBounds_voxelGrid());
         this.writeBounds.encompass(delimiter.getValue().getWriteBounds_voxelGrid());
      }

      this.rDensityContext = new Density.Context();
   }

   @Override
   public boolean generate(@Nonnull Prop.Context context) {
      this.rDensityContext.assign(context);
      double densityValue = this.density.process(this.rDensityContext);

      for (DelimiterDouble<Prop> delimiter : this.propDelimiters) {
         if (delimiter.getRange().contains(densityValue)) {
            assert delimiter.getValue() != null;

            return delimiter.getValue().generate(context);
         }
      }

      return false;
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.readBounds;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds;
   }
}
