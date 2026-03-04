package com.hypixel.hytale.common.util;

import javax.annotation.Nonnull;

public class BitUtil {
   public static void setNibble(@Nonnull byte[] data, int idx, byte b) {
      int fieldIdx = idx >> 1;
      byte val = data[fieldIdx];
      b = (byte)(b & 15);
      int i = idx & 1;
      b = (byte)(b << ((i ^ 1) << 2));
      val = (byte)(val & 15 << (i << 2));
      val = (byte)(val | b);
      data[fieldIdx] = val;
   }

   public static byte getNibble(@Nonnull byte[] data, int idx) {
      int fieldIdx = idx >> 1;
      byte val = data[fieldIdx];
      int i = idx & 1;
      val = (byte)(val >> ((i ^ 1) << 2));
      return (byte)(val & 15);
   }

   public static byte getAndSetNibble(@Nonnull byte[] data, int idx, byte b) {
      int fieldIdx = idx >> 1;
      byte val = data[fieldIdx];
      int i = idx & 1;
      byte oldVal = (byte)(val >> ((i ^ 1) << 2));
      oldVal = (byte)(oldVal & 15);
      b = (byte)(b & 15);
      b = (byte)(b << ((i ^ 1) << 2));
      val = (byte)(val & 15 << (i << 2));
      val = (byte)(val | b);
      data[fieldIdx] = val;
      return oldVal;
   }
}
