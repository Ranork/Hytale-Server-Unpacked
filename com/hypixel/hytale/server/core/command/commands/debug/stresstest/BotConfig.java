package com.hypixel.hytale.server.core.command.commands.debug.stresstest;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector2d;

public class BotConfig {
   public final double radius;
   public final Vector2d flyYHeight;
   public final double flySpeed;
   public final Transform spawn;
   public final int viewRadius;

   public BotConfig(double radius, Vector2d flyYHeight, double flySpeed, Transform spawn, int viewRadius) {
      this.radius = radius;
      this.flyYHeight = flyYHeight;
      this.flySpeed = flySpeed;
      this.spawn = spawn;
      this.viewRadius = viewRadius;
   }
}
