package com.hypixel.hytale.math.util;

import java.util.UUID;
import javax.annotation.Nonnull;

public class HashUtil {
   public static long hash(long v) {
      v = (v >>> 30 ^ v) * -4658895280553007687L;
      v = (v >>> 27 ^ v) * -7723592293110705685L;
      return v >>> 31 ^ v;
   }

   public static long hash(long l1, long l2) {
      l1 = (hash(l1) >>> 30 ^ l1) * -4658895280553007687L;
      return hash(l2) >>> 31 ^ l1;
   }

   public static long hash(long l1, long l2, long l3) {
      l1 = (hash(l1) >>> 30 ^ l1) * -4658895280553007687L;
      l1 = (hash(l2) >>> 27 ^ l1) * -7723592293110705685L;
      return hash(l3) >>> 31 ^ l1;
   }

   public static long hash(long l1, long l2, long l3, long l4) {
      l1 = (hash(l1) >>> 30 ^ l1) * -4658895280553007687L;
      l1 = (hash(l2) >>> 27 ^ l1) * -7723592293110705685L;
      l1 = (hash(l3) >>> 30 ^ l1) * -6389720478792763523L;
      return hash(l4) >>> 31 ^ l1;
   }

   public static long rehash(long l1) {
      return hash(hash(l1));
   }

   public static long rehash(long l1, long l2) {
      return hash(hash(l1, l2));
   }

   public static long rehash(long l1, long l2, long l3) {
      return hash(hash(l1, l2, l3));
   }

   public static long rehash(long l1, long l2, long l3, long l4) {
      return hash(hash(l1, l2, l3, l4));
   }

   public static double random(long l1) {
      return hashToRandomDouble(rehash(l1));
   }

   public static double random(long l1, long l2) {
      return hashToRandomDouble(rehash(l1, l2));
   }

   public static double random(long l1, long l2, long l3) {
      return hashToRandomDouble(rehash(l1, l2, l3));
   }

   public static double random(long l1, long l2, long l3, long l4) {
      return hashToRandomDouble(rehash(l1, l2, l3, l4));
   }

   public static int randomInt(long l1, long l2, long l3, int bound) {
      long hash = rehash(l1, l2, l3);
      hash &= Long.MAX_VALUE;
      return (int)(hash % bound);
   }

   private static double hashToRandomDouble(long hash) {
      hash &= 4294967295L;
      return hash / 4.294967295E9;
   }

   public static long hashUuid(@Nonnull UUID uuid) {
      return hash(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits());
   }

   private HashUtil() {
   }
}
