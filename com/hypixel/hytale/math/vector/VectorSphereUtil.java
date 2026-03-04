package com.hypixel.hytale.math.vector;

import com.hypixel.fastutil.FastCollection;
import com.hypixel.hytale.function.consumer.IntBiObjectConsumer;
import com.hypixel.hytale.function.consumer.IntObjectConsumer;
import com.hypixel.hytale.function.consumer.IntTriObjectConsumer;
import com.hypixel.hytale.function.consumer.TriConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class VectorSphereUtil {
   public static void forEachVector(Iterable<Vector3d> vectors, double originX, double originY, double originZ, double radius, Consumer<Vector3d> consumer) {
      forEachVector(vectors, originX, originY, originZ, radius, radius, radius, consumer);
   }

   public static void forEachVector(
      Iterable<Vector3d> vectors, double originX, double originY, double originZ, double radiusX, double radiusY, double radiusZ, Consumer<Vector3d> consumer
   ) {
      forEachVector(vectors, Function.identity(), originX, originY, originZ, radiusX, radiusY, radiusZ, consumer);
   }

   public static <T> void forEachVector(
      Iterable<T> input, @Nonnull Function<T, Vector3d> func, double originX, double originY, double originZ, double radius, Consumer<T> consumer
   ) {
      forEachVector(input, func, originX, originY, originZ, radius, radius, radius, consumer);
   }

   public static <T> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radiusX,
      double radiusY,
      double radiusZ,
      Consumer<T> consumer
   ) {
      forEachVector(input, func, originX, originY, originZ, radiusX, radiusY, radiusZ, (t, c, n0) -> c.accept(t), consumer, null);
   }

   public static <T, V> void forEachVector(
      Iterable<T> input, @Nonnull Function<T, Vector3d> func, double originX, double originY, double originZ, double radius, BiConsumer<T, V> consumer, V objV
   ) {
      forEachVector(input, func, originX, originY, originZ, radius, radius, radius, consumer, objV);
   }

   public static <T, V> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radiusX,
      double radiusY,
      double radiusZ,
      BiConsumer<T, V> consumer,
      V objV
   ) {
      forEachVector(input, func, originX, originY, originZ, radiusX, radiusY, radiusZ, (t, c, objV2) -> c.accept(t, objV2), consumer, objV);
   }

   public static <T, V1, V2> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radius,
      @Nonnull TriConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      forEachVector(input, func, originX, originY, originZ, radius, radius, radius, consumer, objV1, objV2);
   }

   public static <T, V1, V2> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radiusX,
      double radiusY,
      double radiusZ,
      @Nonnull TriConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      if (input instanceof FastCollection<T> fastCollection) {
         fastCollection.forEach((objx, _func, _originX, _originY, _originZ, _radiusX, _radiusY, _radiusZ, _consumer, _objV1, _objV2) -> {
            Vector3d vectorx = (Vector3d)_func.apply(objx);
            if (isInside(_originX, _originY, _originZ, _radiusX, _radiusY, _radiusZ, vectorx)) {
               _consumer.accept(objx, _objV1, _objV2);
            }
         }, func, originX, originY, originZ, radiusX, radiusY, radiusZ, consumer, objV1, objV2);
      } else {
         for (T obj : input) {
            Vector3d vector = func.apply(obj);
            if (isInside(originX, originY, originZ, radiusX, radiusY, radiusZ, vector)) {
               consumer.accept(obj, objV1, objV2);
            }
         }
      }
   }

   public static <T> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radius,
      IntObjectConsumer<T> consumer
   ) {
      forEachVector(input, func, originX, originY, originZ, radius, radius, radius, consumer);
   }

   public static <T> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radiusX,
      double radiusY,
      double radiusZ,
      IntObjectConsumer<T> consumer
   ) {
      forEachVector(input, func, originX, originY, originZ, radiusX, radiusY, radiusZ, (i, t, c, n0) -> c.accept(i, t), consumer, null);
   }

   public static <T, V> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radius,
      IntBiObjectConsumer<T, V> consumer,
      V objV
   ) {
      forEachVector(input, func, originX, originY, originZ, radius, radius, radius, consumer, objV);
   }

   public static <T, V> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radiusX,
      double radiusY,
      double radiusZ,
      IntBiObjectConsumer<T, V> consumer,
      V objV
   ) {
      forEachVector(input, func, originX, originY, originZ, radiusX, radiusY, radiusZ, (i, t, objV1, c) -> c.accept(i, t, objV1), objV, consumer);
   }

   public static <T, V1, V2> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radius,
      @Nonnull IntTriObjectConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      forEachVector(input, func, originX, originY, originZ, radius, radius, radius, consumer, objV1, objV2);
   }

   public static <T, V1, V2> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double radiusX,
      double radiusY,
      double radiusZ,
      @Nonnull IntTriObjectConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      ObjectIterator var17 = input.int2ObjectEntrySet().iterator();

      while (var17.hasNext()) {
         Entry<T> next = (Entry<T>)var17.next();
         int key = next.getIntKey();
         T value = (T)next.getValue();
         Vector3d vector = func.apply(value);
         if (isInside(originX, originY, originZ, radiusX, radiusY, radiusZ, vector)) {
            consumer.accept(key, value, objV1, objV2);
         }
      }
   }

   public static boolean isInside(double originX, double originY, double originZ, double radius, @Nonnull Vector3d vector) {
      return isInside(originX, originY, originZ, radius, radius, radius, vector);
   }

   public static boolean isInside(double originX, double originY, double originZ, double radiusX, double radiusY, double radiusZ, @Nonnull Vector3d vector) {
      double x = vector.getX() - originX;
      double y = vector.getY() - originY;
      double z = vector.getZ() - originZ;
      double xRatio = x / radiusX;
      double yRatio = y / radiusY;
      double zRatio = z / radiusZ;
      return xRatio * xRatio + yRatio * yRatio + zRatio * zRatio <= 1.0;
   }
}
