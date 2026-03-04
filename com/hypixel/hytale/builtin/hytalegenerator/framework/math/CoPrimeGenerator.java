package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

import java.util.Random;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

public class CoPrimeGenerator {
   public static long[] generateCoPrimes(long seed, int bucketSize, int numberOfBuckets, long floor) {
      if (bucketSize >= 1 && numberOfBuckets >= 1) {
         Random rand = new Random(seed);
         int[] primes = new int[bucketSize * numberOfBuckets];
         fillWithPrimes(primes);
         int[][] buckets = new int[numberOfBuckets][bucketSize];
         long[] output = new long[numberOfBuckets];
         IntStream.range(0, output.length).forEach(ix -> output[ix] = 1L);
         int indexOfBucket = 0;
         int indexOfPrime = 0;

         for (int indexInsideBucket = 0; indexOfPrime < primes.length; indexOfPrime++) {
            buckets[indexOfBucket][indexInsideBucket] = primes[indexOfPrime];
            if (indexOfBucket == numberOfBuckets - 1) {
               indexInsideBucket++;
            }

            indexOfBucket = (indexOfBucket + 1) % numberOfBuckets;
         }

         for (int i = 0; i < numberOfBuckets; i++) {
            while (output[i] < floor) {
               output[i] *= buckets[i][rand.nextInt(bucketSize)];
            }
         }

         return output;
      } else {
         throw new IllegalArgumentException("invalid sizes");
      }
   }

   public static void fillWithPrimes(@Nonnull int[] bucket) {
      int number = 2;

      for (int index = 0; index < bucket.length; number++) {
         if (isPrime(number)) {
            bucket[index] = number;
            index++;
         }
      }
   }

   public static boolean isPrime(int number) {
      for (int i = 2; i < number; i++) {
         if (number % i == 0) {
            return false;
         }
      }

      return true;
   }
}
