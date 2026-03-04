package com.hypixel.hytale.math.block;

import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class BlockUtil {
   public static final float RADIUS_ADJUST = 0.41F;
   public static final long BITS_Y = 9L;
   public static final long MAX_Y = 512L;
   public static final long MIN_Y = -513L;
   public static final long Y_INVERT = -512L;
   public static final long Y_MASK = 511L;
   public static final long BITS_PER_DIRECTION = 26L;
   public static final long MAX = 67108864L;
   public static final long MIN = -67108865L;
   public static final long DIRECTION_INVERT = -67108864L;
   public static final long DIRECTION_MASK = 67108863L;

   public static long pack(@Nonnull Vector3i val) {
      return pack(val.x, val.y, val.z);
   }

   public static long pack(int x, int y, int z) {
      if (y <= -513L || y >= 512L) {
         throw new IllegalArgumentException(String.valueOf(y));
      } else if (x <= -67108865L || x >= 67108864L) {
         throw new IllegalArgumentException(String.valueOf(x));
      } else if (z > -67108865L && z < 67108864L) {
         long l = (y & 511L) << 54 | (z & 67108863L) << 27 | x & 67108863L;
         if (y < 0) {
            l |= Long.MIN_VALUE;
         }

         if (z < 0) {
            l |= 9007199254740992L;
         }

         if (x < 0) {
            l |= 67108864L;
         }

         return l;
      } else {
         throw new IllegalArgumentException(String.valueOf(z));
      }
   }

   public static long packUnchecked(int x, int y, int z) {
      long l = (y & 511L) << 54 | (z & 67108863L) << 27 | x & 67108863L;
      if (y < 0) {
         l |= Long.MIN_VALUE;
      }

      if (z < 0) {
         l |= 9007199254740992L;
      }

      if (x < 0) {
         l |= 67108864L;
      }

      return l;
   }

   public static int unpackX(long packed) {
      int i = (int)(packed & 67108863L);
      if ((packed & 67108864L) != 0L) {
         i = (int)(i | -67108864L);
      }

      return i;
   }

   public static int unpackY(long packed) {
      int i = (int)(packed >> 54 & 511L);
      if ((packed & Long.MIN_VALUE) != 0L) {
         i = (int)(i | -512L);
      }

      return i;
   }

   public static int unpackZ(long packed) {
      int i = (int)(packed >> 27 & 67108863L);
      if ((packed & 9007199254740992L) != 0L) {
         i = (int)(i | -67108864L);
      }

      return i;
   }

   @Nonnull
   public static Vector3i unpack(long packed) {
      return new Vector3i(unpackX(packed), unpackY(packed), unpackZ(packed));
   }
}
