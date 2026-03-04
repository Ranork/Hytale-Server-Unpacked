package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class SurfacePattern extends Pattern {
   @Nonnull
   private final Pattern wallPattern;
   @Nonnull
   private final Pattern originPattern;
   @Nonnull
   private final SpaceSize readSpaceSize;
   @Nonnull
   private final List<Vector3i> surfacePositions;
   @Nonnull
   private final List<Vector3i> originPositions;
   @Nonnull
   private final Vector3i rChildPosition;
   @Nonnull
   private final Pattern.Context rChildContext;

   public SurfacePattern(
      @Nonnull Pattern surfacePattern,
      @Nonnull Pattern originPattern,
      double surfaceRadius,
      double originRadius,
      @Nonnull SurfacePattern.Facing facing,
      int surfaceGap,
      int originGap
   ) {
      this.wallPattern = surfacePattern;
      this.originPattern = originPattern;
      this.rChildPosition = new Vector3i();
      this.rChildContext = new Pattern.Context();
      int surfaceY = -1 - surfaceGap;
      this.surfacePositions = new ArrayList<>(1);

      for (int x = -((int)surfaceRadius) - 1; x <= (int)surfaceRadius + 1; x++) {
         for (int z = -((int)surfaceRadius) - 1; z <= (int)surfaceRadius + 1; z++) {
            if (!(Calculator.distance(x, z, 0.0, 0.0) > surfaceRadius)) {
               Vector3i position = new Vector3i(x, surfaceY, z);
               this.surfacePositions.add(position);
            }
         }
      }

      int originY = originGap;
      this.originPositions = new ArrayList<>(1);

      for (int x = -((int)originRadius) - 1; x <= (int)originRadius + 1; x++) {
         for (int zx = -((int)originRadius) - 1; zx <= (int)originRadius + 1; zx++) {
            if (!(Calculator.distance(x, zx, 0.0, 0.0) > originRadius)) {
               Vector3i position = new Vector3i(x, originY, zx);
               this.originPositions.add(position);
            }
         }
      }

      for (Vector3i pos : this.surfacePositions) {
         this.applyFacing(pos, facing);
      }

      for (Vector3i pos : this.originPositions) {
         this.applyFacing(pos, facing);
      }

      SpaceSize floorSpace = surfacePattern.readSpace();

      for (Vector3i pos : this.surfacePositions) {
         floorSpace = SpaceSize.merge(floorSpace, new SpaceSize(pos));
      }

      floorSpace = SpaceSize.stack(floorSpace, surfacePattern.readSpace());
      SpaceSize originSpace = originPattern.readSpace();

      for (Vector3i pos : this.originPositions) {
         originSpace = SpaceSize.merge(originSpace, new SpaceSize(pos));
      }

      originSpace = SpaceSize.stack(originSpace, originPattern.readSpace());
      this.readSpaceSize = SpaceSize.merge(floorSpace, originSpace);
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      this.rChildPosition.assign(context.position);
      this.rChildContext.assign(context);
      this.rChildContext.position = this.rChildPosition;

      for (Vector3i pos : this.originPositions) {
         this.rChildPosition.assign(pos).add(context.position);
         if (!this.originPattern.matches(this.rChildContext)) {
            return false;
         }
      }

      for (Vector3i posx : this.surfacePositions) {
         this.rChildPosition.assign(posx).add(context.position);
         if (!this.wallPattern.matches(this.rChildContext)) {
            return false;
         }
      }

      return true;
   }

   private void applyFacing(@Nonnull Vector3i pos, @Nonnull SurfacePattern.Facing facing) {
      switch (facing) {
         case D:
            this.toD(pos);
            break;
         case E:
            this.toE(pos);
            break;
         case W:
            this.toW(pos);
            break;
         case S:
            this.toS(pos);
            break;
         case N:
            this.toN(pos);
      }
   }

   private void toD(@Nonnull Vector3i pos) {
      pos.y = -pos.y;
   }

   private void toN(@Nonnull Vector3i pos) {
      int y = pos.y;
      pos.y = pos.z;
      pos.z = y;
   }

   private void toS(@Nonnull Vector3i pos) {
      this.toN(pos);
      pos.z = -pos.z;
   }

   private void toW(@Nonnull Vector3i pos) {
      int y = pos.y;
      pos.y = -pos.x;
      pos.x = y;
   }

   private void toE(@Nonnull Vector3i pos) {
      this.toW(pos);
      pos.x = -pos.x;
   }

   @Nonnull
   @Override
   public SpaceSize readSpace() {
      return this.readSpaceSize.clone();
   }

   public static enum Facing {
      U,
      D,
      E,
      W,
      S,
      N;

      @Nonnull
      public static Codec<SurfacePattern.Facing> CODEC = new EnumCodec<>(SurfacePattern.Facing.class, EnumCodec.EnumStyle.LEGACY);
   }
}
