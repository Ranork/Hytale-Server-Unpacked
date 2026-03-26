package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class WallPattern extends Pattern {
   @Nonnull
   private final Pattern wallPattern;
   @Nonnull
   private final Pattern originPattern;
   @Nonnull
   private final List<WallPattern.WallDirection> directions;
   private final boolean matchAll;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final Vector3i rWallPosition;
   @Nonnull
   private final Pattern.Context rWallContext;

   public WallPattern(@Nonnull Pattern wallPattern, @Nonnull Pattern originPattern, @Nonnull List<WallPattern.WallDirection> wallDirections, boolean matchAll) {
      this.wallPattern = wallPattern;
      this.originPattern = originPattern;
      this.directions = new ArrayList<>(wallDirections);
      this.matchAll = matchAll;
      this.rWallPosition = new Vector3i();
      this.rWallContext = new Pattern.Context();
      this.bounds_voxelGrid = originPattern.getBounds_voxelGrid().clone();
      Bounds3i wallBounds_voxelGrid = wallPattern.getBounds_voxelGrid().clone();

      for (WallPattern.WallDirection d : this.directions) {
         switch (d) {
            case N:
               wallBounds_voxelGrid.clone().offset(new Vector3i(0, 0, -1));
               break;
            case S:
               wallBounds_voxelGrid.clone().offset(new Vector3i(0, 0, 1));
               break;
            case E:
               wallBounds_voxelGrid.clone().offset(new Vector3i(1, 0, 0));
               break;
            case W:
               wallBounds_voxelGrid.clone().offset(new Vector3i(-1, 0, 0));
         }

         this.bounds_voxelGrid.encompass(wallBounds_voxelGrid);
      }
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      for (WallPattern.WallDirection direction : this.directions) {
         boolean matches = this.matches(context, direction);
         if (this.matchAll && !matches) {
            return false;
         }

         if (matches) {
            return true;
         }
      }

      return false;
   }

   private boolean matches(@Nonnull Pattern.Context context, @Nonnull WallPattern.WallDirection direction) {
      this.rWallPosition.assign(context.position);
      switch (direction) {
         case N:
            this.rWallPosition.z--;
            break;
         case S:
            this.rWallPosition.z++;
            break;
         case E:
            this.rWallPosition.x++;
            break;
         case W:
            this.rWallPosition.x--;
      }

      this.rWallContext.assign(context);
      this.rWallContext.position = this.rWallPosition;
      return this.originPattern.matches(context) && this.wallPattern.matches(this.rWallContext);
   }

   @Nonnull
   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds_voxelGrid;
   }

   public static enum WallDirection {
      N,
      S,
      E,
      W;

      @Nonnull
      public static final Codec<WallPattern.WallDirection> CODEC = new EnumCodec<>(WallPattern.WallDirection.class, EnumCodec.EnumStyle.LEGACY);
   }
}
