package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.RotatorPattern;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class OrienterProp extends Prop {
   @Nonnull
   private final List<Prop> props;
   @Nonnull
   private final List<Pattern> patterns;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final OrienterProp.SelectionMode selectionMode;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final Bounds3i readBounds;
   @Nonnull
   private final Bounds3i writeBounds;
   @Nonnull
   private final FastRandom random;
   @Nonnull
   private final Pattern.Context rPatternContext;
   @Nonnull
   private final Prop.Context rChildContext;
   @Nonnull
   private final boolean[] rHasGenerated;
   @Nonnull
   private final List<Integer> rValidPatternIndices;
   @Nonnull
   private Prop.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3i> rFirstAllValidPipe = new Pipe.One<Vector3i>() {
      public void accept(@NonNullDecl Vector3i position, @NonNullDecl Control control) {
         OrienterProp.this.rPatternContext.position = position;

         for (int i = 0; i < OrienterProp.this.patterns.size(); i++) {
            Pattern pattern = OrienterProp.this.patterns.get(i);
            if (pattern.matches(OrienterProp.this.rPatternContext)) {
               Prop prop = OrienterProp.this.props.get(i);
               OrienterProp.this.rChildContext.assign(OrienterProp.this.rContext);
               OrienterProp.this.rChildContext.position = position;
               OrienterProp.this.rHasGenerated[0] = prop.generate(OrienterProp.this.rChildContext);
               control.stop = true;
               if (OrienterProp.this.selectionMode == OrienterProp.SelectionMode.FIRST_VALID) {
                  return;
               }
            }
         }
      }
   };
   @Nonnull
   private final Pipe.One<Vector3i> rRandomValidPipe = new Pipe.One<Vector3i>() {
      public void accept(@NonNullDecl Vector3i position, @NonNullDecl Control control) {
         OrienterProp.this.rPatternContext.position = position;
         OrienterProp.this.rValidPatternIndices.clear();

         for (int i = 0; i < OrienterProp.this.patterns.size(); i++) {
            Pattern pattern = OrienterProp.this.patterns.get(i);
            if (pattern.matches(OrienterProp.this.rPatternContext)) {
               OrienterProp.this.rValidPatternIndices.add(i);
            }
         }

         if (!OrienterProp.this.rValidPatternIndices.isEmpty()) {
            OrienterProp.this.random.setSeed(OrienterProp.this.rngField.get(position.x, position.y, position.z));
            int pickedIndex = OrienterProp.this.random.nextInt(OrienterProp.this.rValidPatternIndices.size());
            Prop prop = OrienterProp.this.props.get(OrienterProp.this.rValidPatternIndices.get(pickedIndex));
            OrienterProp.this.rChildContext.assign(OrienterProp.this.rContext);
            OrienterProp.this.rChildContext.position = position;
            OrienterProp.this.rHasGenerated[0] = prop.generate(OrienterProp.this.rChildContext);
            control.stop = true;
         }
      }
   };

   public OrienterProp(
      @Nonnull List<RotationTuple> rotations,
      @Nonnull Prop prop,
      @Nonnull Pattern pattern,
      @Nonnull Scanner scanner,
      @Nonnull MaterialCache materialCache,
      @Nonnull OrienterProp.SelectionMode selectionMode,
      int seed
   ) {
      this.props = new ArrayList<>(rotations.size());
      this.patterns = new ArrayList<>(rotations.size());
      this.scanner = scanner;
      this.selectionMode = selectionMode;
      this.rngField = new RngField(seed);
      this.readBounds = new Bounds3i();
      this.writeBounds = new Bounds3i();
      this.random = new FastRandom();

      for (int i = 0; i < rotations.size(); i++) {
         Prop rotatedProp = new StaticRotatorProp(prop, rotations.get(i), materialCache);
         Pattern rotatedPattern = new RotatorPattern(pattern, rotations.get(i), materialCache);
         this.props.add(rotatedProp);
         this.patterns.add(rotatedPattern);
         Bounds3i rotatedReadBounds = scanner.getBoundsWithPattern_voxelGrid(rotatedPattern);
         Bounds3i rotatedPropReadBounds = rotatedProp.getReadBounds_voxelGrid();
         if (!rotatedPropReadBounds.isZeroVolume()) {
            rotatedReadBounds.stack(rotatedPropReadBounds);
         }

         this.readBounds.encompass(rotatedReadBounds);
         Bounds3i rotatedWriteBounds = rotatedProp.getWriteBounds_voxelGrid().clone();
         if (!rotatedWriteBounds.isZeroVolume()) {
            rotatedWriteBounds.stack(rotatedReadBounds);
         }

         this.writeBounds.encompass(rotatedWriteBounds);
      }

      this.rPatternContext = new Pattern.Context();
      this.rChildContext = new Prop.Context();
      this.rHasGenerated = new boolean[1];
      this.rValidPatternIndices = new ArrayList<>(this.patterns.size());
      this.rContext = new Prop.Context();
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      this.rContext = context;
      this.rPatternContext.assign(context);
      this.rHasGenerated[0] = false;
      if (this.selectionMode != OrienterProp.SelectionMode.FIRST_VALID && this.selectionMode != OrienterProp.SelectionMode.ALL_VALID) {
         this.scanner.scan(context.position, this.rRandomValidPipe);
      } else {
         this.scanner.scan(context.position, this.rFirstAllValidPipe);
      }

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

   public static enum SelectionMode {
      ALL_VALID,
      FIRST_VALID,
      RANDOM_VALID;
   }
}
