package com.hypixel.hytale.server.worldgen.cave;

import com.hypixel.hytale.server.worldgen.util.condition.flag.ConstantInt2Flags;
import com.hypixel.hytale.server.worldgen.util.condition.flag.Int2FlagsCondition;

public class CaveBiomeMaskFlags {
   public static final Int2FlagsCondition DEFAULT_ALLOW = new ConstantInt2Flags(7);
   public static final Int2FlagsCondition DEFAULT_DENY = new ConstantInt2Flags(0);
   public static final int GENERATE = 1;
   public static final int POPULATE = 2;
   public static final int CONTINUE = 4;

   public static boolean canGenerate(int value) {
      return test(value, 1);
   }

   public static boolean canPopulate(int value) {
      return test(value, 2);
   }

   public static boolean canContinue(int value) {
      return test(value, 4);
   }

   public static boolean test(int value, int flag) {
      return (value & flag) == flag;
   }

   public static class Defaults {
      public static final int DEFAULT_RESULT = 4;
      public static final int DISALLOW_ALL = 0;
      public static final int ALLOW_ALL = 7;
   }
}
