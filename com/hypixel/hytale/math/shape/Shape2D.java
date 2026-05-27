package com.hypixel.hytale.math.shape;

import javax.annotation.Nonnull;
import org.joml.Vector2d;

public interface Shape2D {
   default Box2D getBox(@Nonnull Vector2d position) {
      return this.getBox(position.x(), position.y());
   }

   Box2D getBox(double var1, double var3);

   boolean containsPosition(Vector2d var1, Vector2d var2);

   boolean containsPosition(Vector2d var1, double var2, double var4);
}
