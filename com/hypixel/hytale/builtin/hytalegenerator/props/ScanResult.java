package com.hypixel.hytale.builtin.hytalegenerator.props;

import javax.annotation.Nonnull;

public interface ScanResult {
   @Nonnull
   ScanResult NONE = new ScanResult() {
      @Override
      public boolean isNegative() {
         return true;
      }
   };

   boolean isNegative();

   @Nonnull
   static ScanResult noScanResult() {
      return NONE;
   }
}
