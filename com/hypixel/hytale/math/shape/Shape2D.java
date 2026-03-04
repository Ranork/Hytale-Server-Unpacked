package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.math.vector.Vector2d;
import javax.annotation.Nonnull;

public interface Shape2D {
   default Box2D getBox(@Nonnull Vector2d position) {
      return this.getBox(position.getX(), position.getY());
   }

   Box2D getBox(double var1, double var3);

   boolean containsPosition(Vector2d var1, Vector2d var2);

   boolean containsPosition(Vector2d var1, double var2, double var4);
}
