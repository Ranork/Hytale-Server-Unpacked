package com.hypixel.hytale.math.random;

import com.hypixel.hytale.function.function.TriFunction;
import com.hypixel.hytale.math.vector.Vector3d;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class RandomExtra {
   private RandomExtra() {
   }

   public static double randomBinomial() {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      double a = random.nextDouble();
      double b = random.nextDouble();
      return a - b;
   }

   public static double randomRange(@Nonnull double[] range) {
      return randomRange(range[0], range[1]);
   }

   public static double randomRange(double from, double to) {
      return from + ThreadLocalRandom.current().nextDouble() * (to - from);
   }

   public static float randomRange(@Nonnull float[] range) {
      return randomRange(range[0], range[1]);
   }

   public static float randomRange(float from, float to) {
      return from + ThreadLocalRandom.current().nextFloat() * (to - from);
   }

   public static int randomRange(int bound) {
      return ThreadLocalRandom.current().nextInt(bound);
   }

   public static int randomRange(@Nonnull int[] range) {
      return randomRange(range[0], range[1]);
   }

   public static int randomRange(int from, int to) {
      return ThreadLocalRandom.current().nextInt(to - from + 1) + from;
   }

   public static long randomRange(long from, long to) {
      return ThreadLocalRandom.current().nextLong(to - from + 1L) + from;
   }

   public static Duration randomDuration(@Nonnull Duration from, @Nonnull Duration to) {
      return Duration.ofNanos(randomRange(from.toNanos(), to.toNanos()));
   }

   public static boolean randomBoolean() {
      return ThreadLocalRandom.current().nextBoolean();
   }

   public static <T> T randomElement(@Nonnull List<T> collection) {
      return collection.get(randomRange(collection.size()));
   }

   @Nonnull
   public static Vector3d jitter(@Nonnull Vector3d vec, double maxRange) {
      ThreadLocalRandom current = ThreadLocalRandom.current();
      vec.x = vec.x + current.nextDouble() * maxRange;
      vec.y = vec.y + current.nextDouble() * maxRange;
      vec.z = vec.z + current.nextDouble() * maxRange;
      return vec;
   }

   @Nullable
   public static <T> T randomWeightedElement(@Nonnull Collection<? extends T> elements, @Nonnull ToDoubleFunction<T> weight) {
      Iterator<? extends T> i = elements.iterator();
      double sumWeights = 0.0;

      while (i.hasNext()) {
         sumWeights += weight.applyAsDouble((T)i.next());
      }

      return randomWeightedElement(elements, weight, sumWeights);
   }

   @Nullable
   public static <T> T randomWeightedElement(@Nonnull Collection<? extends T> elements, @Nonnull ToDoubleFunction<T> weight, double sumWeights) {
      if (sumWeights == 0.0) {
         return null;
      } else {
         Iterator<? extends T> i = elements.iterator();
         T result = null;
         sumWeights *= ThreadLocalRandom.current().nextDouble();

         while (i.hasNext()) {
            result = (T)i.next();
            sumWeights -= weight.applyAsDouble(result);
            if (sumWeights < 0.0) {
               break;
            }
         }

         return result;
      }
   }

   @Nullable
   public static <T> T randomIntWeightedElement(@Nonnull Collection<? extends T> elements, @Nonnull ToIntFunction<T> weight) {
      Iterator<? extends T> i = elements.iterator();
      int sumWeights = 0;

      while (i.hasNext()) {
         sumWeights += weight.applyAsInt((T)i.next());
      }

      return randomIntWeightedElement(elements, weight, sumWeights);
   }

   @Nullable
   public static <T> T randomIntWeightedElement(@Nonnull Collection<? extends T> elements, @Nonnull ToIntFunction<T> weight, int sumWeights) {
      if (sumWeights == 0) {
         return null;
      } else {
         Iterator<? extends T> i = elements.iterator();
         T result = null;
         sumWeights = randomRange(sumWeights);

         while (i.hasNext()) {
            result = (T)i.next();
            sumWeights -= weight.applyAsInt(result);
            if (sumWeights < 0) {
               break;
            }
         }

         return result;
      }
   }

   @Nullable
   public static <T> T randomWeightedElementFiltered(@Nonnull Collection<? extends T> elements, @Nonnull Predicate<T> filter, @Nonnull ToIntFunction<T> weight) {
      Iterator<? extends T> i = elements.iterator();
      int sumWeights = 0;

      while (i.hasNext()) {
         T t = (T)i.next();
         if (filter.test(t)) {
            sumWeights += weight.applyAsInt(t);
         }
      }

      return randomWeightedElementFiltered(elements, filter, weight, sumWeights);
   }

   @Nullable
   public static <T> T randomWeightedElementFiltered(
      @Nonnull Collection<? extends T> elements, @Nonnull Predicate<T> filter, @Nonnull ToIntFunction<T> weight, int sumWeights
   ) {
      if (sumWeights == 0) {
         return null;
      } else {
         Iterator<? extends T> i = elements.iterator();
         T result = null;
         sumWeights = randomRange(sumWeights);

         while (i.hasNext()) {
            result = (T)i.next();
            if (filter.test(result)) {
               sumWeights -= weight.applyAsInt(result);
               if (sumWeights < 0) {
                  break;
               }
            }
         }

         return result;
      }
   }

   @Nullable
   public static <T> T randomWeightedElement(@Nonnull Collection<? extends T> elements, @Nonnull Predicate<T> filter, @Nonnull ToDoubleFunction<T> weight) {
      Iterator<? extends T> i = elements.iterator();
      double sumWeights = 0.0;

      while (i.hasNext()) {
         T t = (T)i.next();
         if (filter.test(t)) {
            sumWeights += weight.applyAsDouble(t);
         }
      }

      return randomWeightedElement(elements, filter, weight, sumWeights);
   }

   @Nullable
   public static <T> T randomWeightedElement(
      @Nonnull Collection<? extends T> elements, @Nonnull Predicate<T> filter, @Nonnull ToDoubleFunction<T> weight, double sumWeights
   ) {
      if (sumWeights == 0.0) {
         return null;
      } else {
         Iterator<? extends T> i = elements.iterator();
         T result = null;
         sumWeights *= ThreadLocalRandom.current().nextDouble();

         while (i.hasNext()) {
            result = (T)i.next();
            if (filter.test(result)) {
               sumWeights -= weight.applyAsDouble(result);
               if (sumWeights < 0.0) {
                  break;
               }
            }
         }

         return result;
      }
   }

   @Nullable
   public static <T, U> T randomWeightedElement(
      @Nonnull Collection<? extends T> elements, @Nonnull BiPredicate<T, U> filter, @Nonnull ToDoubleBiFunction<T, U> weight, double sumWeights, U meta
   ) {
      if (sumWeights == 0.0) {
         return null;
      } else {
         Iterator<? extends T> i = elements.iterator();
         T result = null;
         sumWeights *= ThreadLocalRandom.current().nextDouble();

         while (i.hasNext()) {
            result = (T)i.next();
            if (filter.test(result, meta)) {
               sumWeights -= weight.applyAsDouble(result, meta);
               if (sumWeights < 0.0) {
                  break;
               }
            }
         }

         return result;
      }
   }

   public static <T> void reservoirSample(@Nonnull List<T> input, @Nonnull Predicate<T> matcher, int count, @Nonnull List<T> picked) {
      int selected = 0;

      for (int i = 0; i < input.size(); i++) {
         T element = input.get(i);
         if (matcher.test(element)) {
            if (selected < count) {
               picked.add(element);
            } else {
               int j = randomRange(selected + 1);
               if (j < count) {
                  picked.set(j, element);
               }
            }

            selected++;
         }
      }
   }

   public static <E, S extends List<E>, F, T extends List<F>, G, H> void reservoirSample(
      @Nonnull S input, @Nonnull TriFunction<E, G, H, F> filter, int count, @Nonnull T picked, G g, H h
   ) {
      int selected = 0;

      for (int i = 0; i < input.size(); i++) {
         F f = filter.apply(input.get(i), g, h);
         if (f != null) {
            if (selected < count) {
               picked.add(f);
            } else {
               int j = randomRange(selected + 1);
               if (j < count) {
                  picked.set(j, f);
               }
            }

            selected++;
         }
      }
   }

   public static <E, T extends List<E>> void reservoirSample(E element, int count, @Nonnull T picked) {
      if (picked.size() < count) {
         picked.add(element);
      } else {
         int i = randomRange(count + 1);
         if (i < count) {
            picked.set(i, element);
         }
      }
   }

   public static int pickWeightedIndex(@Nonnull double[] weights) {
      double sum = 0.0;

      for (double weight : weights) {
         sum += weight;
      }

      double randomWeight = ThreadLocalRandom.current().nextDouble(sum);

      for (int i = 0; i < weights.length - 1; i++) {
         randomWeight -= weights[i];
         if (randomWeight <= 0.0) {
            return i;
         }
      }

      return weights.length - 1;
   }
}
