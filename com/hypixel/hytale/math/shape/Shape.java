package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import javax.annotation.Nonnull;
import org.joml.Vector3dc;

public interface Shape {
   default Box getBox(@Nonnull Vector3dc position) {
      return this.getBox(position.x(), position.y(), position.z());
   }

   Box getBox(double var1, double var3, double var5);

   default boolean containsPosition(@Nonnull Vector3dc origin, @Nonnull Vector3dc position) {
      return this.containsPosition(position.x() - origin.x(), position.y() - origin.y(), position.z() - origin.z());
   }

   default boolean containsPosition(@Nonnull Vector3dc position) {
      return this.containsPosition(position.x(), position.y(), position.z());
   }

   boolean containsPosition(double var1, double var3, double var5);

   void expand(double var1);

   default boolean forEachBlock(@Nonnull Vector3dc origin, TriIntPredicate consumer) {
      return this.forEachBlock(origin.x(), origin.y(), origin.z(), consumer);
   }

   default boolean forEachBlock(@Nonnull Vector3dc origin, double epsilon, TriIntPredicate consumer) {
      return this.forEachBlock(origin.x(), origin.y(), origin.z(), epsilon, consumer);
   }

   default boolean forEachBlock(double x, double y, double z, TriIntPredicate consumer) {
      return this.forEachBlock(x, y, z, 0.0, consumer);
   }

   boolean forEachBlock(double var1, double var3, double var5, double var7, TriIntPredicate var9);

   default <T> boolean forEachBlock(@Nonnull Vector3dc origin, T t, TriIntObjPredicate<T> consumer) {
      return this.forEachBlock(origin.x(), origin.y(), origin.z(), t, consumer);
   }

   default <T> boolean forEachBlock(@Nonnull Vector3dc origin, double epsilon, T t, TriIntObjPredicate<T> consumer) {
      return this.forEachBlock(origin.x(), origin.y(), origin.z(), epsilon, t, consumer);
   }

   default <T> boolean forEachBlock(double x, double y, double z, T t, TriIntObjPredicate<T> consumer) {
      return this.forEachBlock(x, y, z, 0.0, t, consumer);
   }

   <T> boolean forEachBlock(double var1, double var3, double var5, double var7, T var9, TriIntObjPredicate<T> var10);
}
