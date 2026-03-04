package com.hypixel.fastutil.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;
import sun.misc.Unsafe;

public class TLRUtil {
   private static final Unsafe UNSAFE;
   private static final long PROBE;

   public static void localInit() {
      ThreadLocalRandom.current();
   }

   public static int getProbe() {
      return UNSAFE.getInt(Thread.currentThread(), PROBE);
   }

   public static int advanceProbe(int probe) {
      probe ^= probe << 13;
      probe ^= probe >>> 17;
      probe ^= probe << 5;
      UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
      return probe;
   }

   static {
      Unsafe instance;
      try {
         Field field = Unsafe.class.getDeclaredField("theUnsafe");
         field.setAccessible(true);
         instance = (Unsafe)field.get(null);
      } catch (Exception var5) {
         try {
            Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor();
            c.setAccessible(true);
            instance = c.newInstance();
         } catch (Exception var4) {
            throw SneakyThrow.sneakyThrow(var4);
         }
      }

      UNSAFE = instance;

      try {
         PROBE = UNSAFE.objectFieldOffset(Thread.class.getDeclaredField("threadLocalRandomProbe"));
      } catch (NoSuchFieldException var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }
}
