package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class LocatorProp extends Prop {
   @Nonnull
   private final Bounds3i readBounds;
   @Nonnull
   private final Bounds3i writeBounds;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final Prop prop;
   private final int placementCap;
   @Nonnull
   private final Pattern.Context rPatternContext;
   @Nonnull
   private final Prop.Context rPropContext;
   @Nonnull
   private final int[] rPlacedCount;
   @Nonnull
   private final boolean[] rHasGenerated;
   @Nonnull
   private Prop.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3i> rScannerPipe = new Pipe.One<Vector3i>() {
      public void accept(@NonNullDecl Vector3i position, @NonNullDecl Control control) {
         if (LocatorProp.this.rPlacedCount[0] >= LocatorProp.this.placementCap) {
            control.stop = true;
         } else {
            LocatorProp.this.rPatternContext.position = position;
            if (LocatorProp.this.pattern.matches(LocatorProp.this.rPatternContext)) {
               LocatorProp.this.rPropContext.assign(LocatorProp.this.rContext);
               LocatorProp.this.rPropContext.position = position;
               LocatorProp.this.rHasGenerated[0] = LocatorProp.this.rHasGenerated[0] | LocatorProp.this.prop.generate(LocatorProp.this.rPropContext);
               LocatorProp.this.rPlacedCount[0]++;
            }
         }
      }
   };

   public LocatorProp(@Nonnull Prop prop, @Nonnull Pattern pattern, @Nonnull Scanner scanner, int placementCap) {
      this.prop = prop;
      this.scanner = scanner;
      this.pattern = pattern;
      this.placementCap = placementCap;
      this.readBounds = scanner.getBoundsWithPattern_voxelGrid(pattern);
      Bounds3i propReadBounds = prop.getReadBounds_voxelGrid().clone();
      if (!propReadBounds.isZeroVolume()) {
         this.readBounds.stack(propReadBounds);
      }

      this.writeBounds = prop.getWriteBounds_voxelGrid().clone();
      if (!this.readBounds.isZeroVolume()) {
         this.writeBounds.stack(this.readBounds);
      }

      this.rPatternContext = new Pattern.Context();
      this.rPropContext = new Prop.Context();
      this.rPlacedCount = new int[1];
      this.rHasGenerated = new boolean[1];
      this.rContext = new Prop.Context();
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      this.rContext = context;
      this.rPatternContext.assign(context);
      this.rHasGenerated[0] = false;
      this.rPlacedCount[0] = 0;
      this.scanner.scan(context.position, this.rScannerPipe);
      return this.rHasGenerated[0];
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
