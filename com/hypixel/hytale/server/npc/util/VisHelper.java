package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public class VisHelper {
   public static final Vector3f DEBUG_COLOR_STEERING_POST = DebugUtils.COLOR_GREEN;
   public static final Vector3f DEBUG_COLOR_STEERING_PRE = DebugUtils.COLOR_RED;
   public static final Vector3f DEBUG_COLOR_AVOIDANCE = DebugUtils.COLOR_WHITE;
   public static final Vector3f DEBUG_COLOR_SEPARATION = DebugUtils.COLOR_BLUE;
   public static final double DEBUG_MIN_VECTOR_DRAW_LENGTH_SQUARED = 0.01;
   public static final double DEBUG_VECTORS_SCALE = 4.0;
   public static final float DEBUG_VECTORS_TIME = 0.05F;
   public static final float DEBUG_TRANSPARENT = 0.24000001F;
   public static final double DEBUG_SPHERE_SCALE = 1.0;

   public static void renderDebugVector(@Nonnull Vector3d position, @Nonnull Vector3d direction, @Nonnull Vector3f color, @Nonnull World world) {
      renderDebugVector(position, direction, color, 0.24000001F, world);
   }

   public static void renderDebugVector(@Nonnull Vector3d position, @Nonnull Vector3d direction, @Nonnull Vector3f color, float opacity, @Nonnull World world) {
      if (!(direction.squaredLength() < 0.01)) {
         Vector3d scaledDir = direction.clone().scale(4.0);
         DebugUtils.addArrow(world, position, scaledDir, color, opacity, 0.05F, 0);
      }
   }

   public static void renderDebugVectorTo(@Nonnull Vector3d position, @Nonnull Vector3d direction, @Nonnull Vector3f color, @Nonnull World world) {
      renderDebugVectorTo(position, direction, color, 0.24000001F, world);
   }

   public static void renderDebugVectorTo(@Nonnull Vector3d position, @Nonnull Vector3d direction, @Nonnull Vector3f color, float opacity, @Nonnull World world) {
      if (!(direction.squaredLength() < 0.01)) {
         Vector3d scaledDir = direction.clone().scale(4.0);
         Vector3d start = position.clone().subtract(scaledDir);
         DebugUtils.addArrow(world, start, scaledDir, color, opacity, 0.05F, 0);
      }
   }

   public static void renderDebugSphere(@Nonnull Vector3d position, @Nonnull Vector3f color, @Nonnull World world) {
      renderDebugSphere(position, color, 0.24000001F, world);
   }

   public static void renderDebugSphere(@Nonnull Vector3d position, @Nonnull Vector3f color, float opacity, @Nonnull World world) {
      DebugUtils.addSphere(world, position, color, opacity, 1.0, 0.05F);
   }

   public static void renderDebugSphere(@Nonnull Vector3d position, double radius, @Nonnull Vector3f color, @Nonnull World world) {
      renderDebugSphere(position, radius, color, 0.24000001F, world);
   }

   public static void renderDebugSphere(@Nonnull Vector3d position, double radius, @Nonnull Vector3f color, float opacity, @Nonnull World world) {
      DebugUtils.addSphere(world, position, color, opacity, radius, 0.05F);
   }
}
