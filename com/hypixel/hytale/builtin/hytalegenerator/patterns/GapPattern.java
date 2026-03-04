package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nonnull;

public class GapPattern extends Pattern {
   private List<List<GapPattern.PositionedPattern>> axisPositionedPatterns;
   private List<GapPattern.PositionedPattern> depthPositionedPatterns;
   private double gapSize;
   private double anchorSize;
   private double anchorRoughness;
   private int depthDown;
   private int depthUp;
   private Pattern gapPattern;
   private Pattern anchorPattern;
   private SpaceSize readSpaceSize;
   @Nonnull
   private final Vector3i rChildPosition;
   @Nonnull
   private final Pattern.Context rChildContext;

   public GapPattern(
      @Nonnull List<Float> angles,
      double gapSize,
      double anchorSize,
      double anchorRoughness,
      int depthDown,
      int depthUp,
      @Nonnull Pattern gapPattern,
      @Nonnull Pattern anchorPattern
   ) {
      if (!(gapSize < 0.0) && !(anchorSize < 0.0) && !(anchorRoughness < 0.0) && depthDown >= 0 && depthUp >= 0) {
         this.gapSize = gapSize;
         this.anchorSize = anchorSize;
         this.gapPattern = gapPattern;
         this.anchorPattern = anchorPattern;
         this.anchorRoughness = anchorRoughness;
         this.depthDown = depthDown;
         this.depthUp = depthUp;
         this.rChildPosition = new Vector3i();
         this.rChildContext = new Pattern.Context();
         this.depthPositionedPatterns = this.renderDepths();
         this.axisPositionedPatterns = new ArrayList<>(angles.size());

         for (float angle : angles) {
            List<GapPattern.PositionedPattern> positions = this.renderPositions(angle);
            this.axisPositionedPatterns.add(positions);
         }

         Vector3i min = null;
         Vector3i max = null;

         for (List<GapPattern.PositionedPattern> direction : this.axisPositionedPatterns) {
            for (GapPattern.PositionedPattern pos : direction) {
               if (min == null) {
                  min = pos.position.clone();
                  max = pos.position.clone();
               } else {
                  min = Vector3i.min(min, pos.position);
                  max = Vector3i.max(max, pos.position);
               }
            }
         }

         if (max == null) {
            this.readSpaceSize = new SpaceSize(new Vector3i(), new Vector3i());
         } else {
            max.add(1, 1, 1);
            this.readSpaceSize = new SpaceSize(min, max);
         }
      } else {
         throw new IllegalArgumentException("negative sizes");
      }
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      this.rChildPosition.assign(0, 0, 0);
      this.rChildContext.assign(context);
      this.rChildContext.position = this.rChildPosition;

      for (GapPattern.PositionedPattern entry : this.depthPositionedPatterns) {
         this.rChildPosition.assign(entry.position).add(context.position);
         if (!entry.pattern.matches(this.rChildContext)) {
            return false;
         }
      }

      for (List<GapPattern.PositionedPattern> patternsInDirection : this.axisPositionedPatterns) {
         boolean matchesDirection = true;

         for (GapPattern.PositionedPattern entryx : patternsInDirection) {
            this.rChildPosition.assign(entryx.position).add(context.position);
            if (!entryx.pattern.matches(context)) {
               matchesDirection = false;
               break;
            }
         }

         if (matchesDirection) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   @Override
   public SpaceSize readSpace() {
      return this.readSpaceSize.clone();
   }

   @Nonnull
   private List<GapPattern.PositionedPattern> renderDepths() {
      ArrayList<GapPattern.PositionedPattern> positions = new ArrayList<>();
      Vector3i pointer = new Vector3i();
      int stepsDown = this.depthDown - 1;

      for (int i = 0; i < this.depthDown; i++) {
         pointer.add(0, -1, 0);
         positions.add(new GapPattern.PositionedPattern(this.gapPattern, pointer.clone()));
      }

      pointer = new Vector3i();
      int stepsUp = this.depthUp - 1;

      for (int i = 0; i < this.depthUp; i++) {
         pointer.add(0, 1, 0);
         positions.add(new GapPattern.PositionedPattern(this.gapPattern, pointer.clone()));
      }

      return positions;
   }

   @Nonnull
   private List<GapPattern.PositionedPattern> renderPositions(float angle) {
      ArrayList<GapPattern.PositionedPattern> positions = new ArrayList<>();
      positions.addAll(this.renderHalfPositions(angle));
      positions.addAll(this.renderHalfPositions((float) Math.PI + angle));
      ArrayList<GapPattern.PositionedPattern> uniquePositions = new ArrayList<>(positions.size());
      HashSet<Vector3i> positionsSet = new HashSet<>();

      for (GapPattern.PositionedPattern e : positions) {
         if (!positionsSet.contains(e.position)) {
            uniquePositions.add(e);
            positionsSet.add(e.position);
         }
      }

      return uniquePositions;
   }

   @Nonnull
   private List<GapPattern.PositionedPattern> renderHalfPositions(float angle) {
      ArrayList<GapPattern.PositionedPattern> positions = new ArrayList<>();
      double halfGap = this.gapSize / 2.0 - 1.0 - this.anchorRoughness;
      halfGap = Math.max(0.0, halfGap);
      double halfWall = this.anchorSize / 2.0;
      Vector3d pointer = new Vector3d(0.5, 0.5, 0.5);
      Vector3d mov = new Vector3d(0.0, 0.0, -1.0);
      mov.rotateY(angle);
      double stepSize = 0.5;
      mov.setLength(stepSize);
      int steps = (int)(halfGap / stepSize);

      for (int s = 0; s < steps; s++) {
         pointer.add(mov);
         positions.add(new GapPattern.PositionedPattern(this.gapPattern, pointer.toVector3i()));
      }

      positions.add(new GapPattern.PositionedPattern(this.gapPattern, new Vector3i()));
      pointer = mov.clone().setLength(halfGap).add(0.5, 0.5, 0.5);
      positions.add(new GapPattern.PositionedPattern(this.gapPattern, pointer.toVector3i()));
      Vector3d anchor = mov.clone().setLength(this.gapSize / 2.0);
      pointer = anchor.clone().add(0.5, 0.5, 0.5);
      positions.add(new GapPattern.PositionedPattern(this.anchorPattern, anchor.toVector3i()));
      mov.rotateY((float) (Math.PI / 2));
      steps = (int)(halfWall / stepSize);

      for (int s = 0; s < steps; s++) {
         pointer.add(mov);
         positions.add(new GapPattern.PositionedPattern(this.anchorPattern, pointer.toVector3i()));
      }

      Vector3d wallTip = anchor.clone().add(0.5, 0.5, 0.5);
      wallTip.add(mov.clone().setLength(halfWall));
      positions.add(new GapPattern.PositionedPattern(this.anchorPattern, wallTip.toVector3i()));
      mov.scale(-1.0);
      pointer = anchor.clone().add(0.5, 0.5, 0.5);

      for (int s = 0; s < steps; s++) {
         pointer.add(mov);
         positions.add(new GapPattern.PositionedPattern(this.anchorPattern, pointer.toVector3i()));
      }

      wallTip = anchor.clone().add(0.5, 0.5, 0.5);
      wallTip.add(mov.clone().setLength(halfWall));
      positions.add(new GapPattern.PositionedPattern(this.anchorPattern, wallTip.toVector3i()));
      return positions;
   }

   public static class PositionedPattern {
      private Vector3i position;
      private Pattern pattern;

      public PositionedPattern(@Nonnull Pattern pattern, @Nonnull Vector3i position) {
         this.pattern = pattern;
         this.position = position.clone();
      }

      public int getX() {
         return this.position.x;
      }

      public int getY() {
         return this.position.y;
      }

      public int getZ() {
         return this.position.z;
      }

      public Pattern getPattern() {
         return this.pattern;
      }

      @Nonnull
      protected GapPattern.PositionedPattern clone() {
         return new GapPattern.PositionedPattern(this.pattern, this.position.clone());
      }
   }
}
