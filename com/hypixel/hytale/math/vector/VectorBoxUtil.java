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

public class VectorBoxUtil {
   public static void forEachVector(Iterable<Vector3d> vectors, double originX, double originY, double originZ, double apothem, Consumer<Vector3d> consumer) {
      forEachVector(vectors, originX, originY, originZ, apothem, apothem, apothem, consumer);
   }

   public static void forEachVector(
      Iterable<Vector3d> vectors,
      double originX,
      double originY,
      double originZ,
      double apothemX,
      double apothemY,
      double apothemZ,
      Consumer<Vector3d> consumer
   ) {
      forEachVector(vectors, Function.identity(), originX, originY, originZ, apothemX, apothemY, apothemZ, consumer);
   }

   public static void forEachVector(
      Iterable<Vector3d> vectors,
      double originX,
      double originY,
      double originZ,
      double apothemXMin,
      double apothemYMin,
      double apothemZMin,
      double apothemXMax,
      double apothemYMax,
      double apothemZMax,
      Consumer<Vector3d> consumer
   ) {
      forEachVector(
         vectors, Function.identity(), originX, originY, originZ, apothemXMin, apothemYMin, apothemZMin, apothemXMax, apothemYMax, apothemZMax, consumer
      );
   }

   public static <T> void forEachVector(
      Iterable<T> input, @Nonnull Function<T, Vector3d> func, double originX, double originY, double originZ, double apothem, Consumer<T> consumer
   ) {
      forEachVector(input, func, originX, originY, originZ, apothem, apothem, apothem, consumer);
   }

   public static <T> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemX,
      double apothemY,
      double apothemZ,
      Consumer<T> consumer
   ) {
      forEachVector(input, func, originX, originY, originZ, -apothemX, -apothemY, -apothemZ, apothemX, apothemY, apothemZ, consumer);
   }

   public static <T> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemXMin,
      double apothemYMin,
      double apothemZMin,
      double apothemXMax,
      double apothemYMax,
      double apothemZMax,
      Consumer<T> consumer
   ) {
      forEachVector(
         input,
         func,
         originX,
         originY,
         originZ,
         apothemXMin,
         apothemYMin,
         apothemZMin,
         apothemXMax,
         apothemYMax,
         apothemZMax,
         (t, c, n0) -> c.accept(t),
         consumer,
         null
      );
   }

   public static <T, V> void forEachVector(
      Iterable<T> input, @Nonnull Function<T, Vector3d> func, double originX, double originY, double originZ, double apothem, BiConsumer<T, V> consumer, V objV
   ) {
      forEachVector(input, func, originX, originY, originZ, apothem, apothem, apothem, consumer, objV);
   }

   public static <T, V> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemX,
      double apothemY,
      double apothemZ,
      BiConsumer<T, V> consumer,
      V objV
   ) {
      forEachVector(input, func, originX, originY, originZ, -apothemX, -apothemY, -apothemZ, apothemX, apothemY, apothemZ, consumer, objV);
   }

   public static <T, V> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemXMin,
      double apothemYMin,
      double apothemZMin,
      double apothemXMax,
      double apothemYMax,
      double apothemZMax,
      BiConsumer<T, V> consumer,
      V objV
   ) {
      forEachVector(
         input,
         func,
         originX,
         originY,
         originZ,
         apothemXMin,
         apothemYMin,
         apothemZMin,
         apothemXMax,
         apothemYMax,
         apothemZMax,
         (t, objV1, c) -> c.accept(t, objV1),
         objV,
         consumer
      );
   }

   public static <T, V1, V2> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothem,
      @Nonnull TriConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      forEachVector(input, func, originX, originY, originZ, apothem, apothem, apothem, consumer, objV1, objV2);
   }

   public static <T, V1, V2> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemX,
      double apothemY,
      double apothemZ,
      @Nonnull TriConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      forEachVector(input, func, originX, originY, originZ, -apothemX, -apothemY, -apothemZ, apothemX, apothemY, apothemZ, consumer, objV1, objV2);
   }

   public static <T, V1, V2> void forEachVector(
      Iterable<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemXMin,
      double apothemYMin,
      double apothemZMin,
      double apothemXMax,
      double apothemYMax,
      double apothemZMax,
      @Nonnull TriConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      if (input instanceof FastCollection) {
         ((FastCollection)input)
            .forEach(
               (objx, _func, _originX, _originY, _originZ, _apothemXMin, _apothemYMin, _apothemZMin, _apothemXMax, _apothemYMax, _apothemZMax, _consumer, _objV1, _objV2) -> {
                  Vector3d vectorx = (Vector3d)_func.apply(objx);
                  if (isInside(_originX, _originY, _originZ, _apothemXMin, _apothemYMin, _apothemZMin, _apothemXMax, _apothemYMax, _apothemZMax, vectorx)) {
                     _consumer.accept(objx, _objV1, _objV2);
                  }
               },
               func,
               originX,
               originY,
               originZ,
               apothemXMin,
               apothemYMin,
               apothemZMin,
               apothemXMax,
               apothemYMax,
               apothemZMax,
               consumer,
               objV1,
               objV2
            );
      } else {
         for (T obj : input) {
            Vector3d vector = func.apply(obj);
            if (isInside(originX, originY, originZ, apothemXMin, apothemYMin, apothemZMin, apothemXMax, apothemYMax, apothemZMax, vector)) {
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
      double apothem,
      IntObjectConsumer<T> consumer
   ) {
      forEachVector(input, func, originX, originY, originZ, apothem, apothem, apothem, consumer);
   }

   public static <T> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemX,
      double apothemY,
      double apothemZ,
      IntObjectConsumer<T> consumer
   ) {
      forEachVector(input, func, originX, originY, originZ, -apothemX, -apothemY, -apothemZ, apothemX, apothemY, apothemZ, consumer);
   }

   public static <T> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemXMin,
      double apothemYMin,
      double apothemZMin,
      double apothemXMax,
      double apothemYMax,
      double apothemZMax,
      IntObjectConsumer<T> consumer
   ) {
      forEachVector(
         input,
         func,
         originX,
         originY,
         originZ,
         apothemXMin,
         apothemYMin,
         apothemZMin,
         apothemXMax,
         apothemYMax,
         apothemZMax,
         (i, t, c, n0) -> c.accept(i, t),
         consumer,
         null
      );
   }

   public static <T, V> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothem,
      IntBiObjectConsumer<T, V> consumer,
      V objV
   ) {
      forEachVector(input, func, originX, originY, originZ, apothem, apothem, apothem, consumer, objV);
   }

   public static <T, V> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemX,
      double apothemY,
      double apothemZ,
      IntBiObjectConsumer<T, V> consumer,
      V objV
   ) {
      forEachVector(input, func, originX, originY, originZ, -apothemX, -apothemY, -apothemZ, apothemX, apothemY, apothemZ, consumer, objV);
   }

   public static <T, V> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemXMin,
      double apothemYMin,
      double apothemZMin,
      double apothemXMax,
      double apothemYMax,
      double apothemZMax,
      IntBiObjectConsumer<T, V> consumer,
      V objV
   ) {
      forEachVector(
         input,
         func,
         originX,
         originY,
         originZ,
         apothemXMin,
         apothemYMin,
         apothemZMin,
         apothemXMax,
         apothemYMax,
         apothemZMax,
         (i, t, objV1, c) -> c.accept(i, t, objV1),
         objV,
         consumer
      );
   }

   public static <T, V1, V2> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothem,
      @Nonnull IntTriObjectConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      forEachVector(input, func, originX, originY, originZ, apothem, apothem, apothem, consumer, objV1, objV2);
   }

   public static <T, V1, V2> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemX,
      double apothemY,
      double apothemZ,
      @Nonnull IntTriObjectConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      forEachVector(input, func, originX, originY, originZ, -apothemX, -apothemY, -apothemZ, apothemX, apothemY, apothemZ, consumer, objV1, objV2);
   }

   public static <T, V1, V2> void forEachVector(
      @Nonnull Int2ObjectMap<T> input,
      @Nonnull Function<T, Vector3d> func,
      double originX,
      double originY,
      double originZ,
      double apothemXMin,
      double apothemYMin,
      double apothemZMin,
      double apothemXMax,
      double apothemYMax,
      double apothemZMax,
      @Nonnull IntTriObjectConsumer<T, V1, V2> consumer,
      V1 objV1,
      V2 objV2
   ) {
      ObjectIterator var23 = input.int2ObjectEntrySet().iterator();

      while (var23.hasNext()) {
         Entry<T> next = (Entry<T>)var23.next();
         int key = next.getIntKey();
         T value = (T)next.getValue();
         Vector3d vector = func.apply(value);
         if (isInside(originX, originY, originZ, apothemXMin, apothemYMin, apothemZMin, apothemXMax, apothemYMax, apothemZMax, vector)) {
            consumer.accept(key, value, objV1, objV2);
         }
      }
   }

   public static boolean isInside(double originX, double originY, double originZ, double apothem, @Nonnull Vector3d vector) {
      return isInside(originX, originY, originZ, apothem, apothem, apothem, vector);
   }

   public static boolean isInside(double originX, double originY, double originZ, double apothemX, double apothemY, double apothemZ, @Nonnull Vector3d vector) {
      return isInside(originX, originY, originZ, -apothemX, -apothemY, -apothemZ, apothemX, apothemY, apothemZ, vector);
   }

   public static boolean isInside(
      double originX, double originY, double originZ, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax, @Nonnull Vector3d vector
   ) {
      double x = vector.getX() - originX;
      double y = vector.getY() - originY;
      double z = vector.getZ() - originZ;
      return x >= xMin && x <= xMax && y >= yMin && y <= yMax && z >= zMin && z <= zMax;
   }
}
