package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PositionScanResult implements ScanResult {
   private Vector3i position;

   public PositionScanResult(@Nullable Vector3i position) {
      if (position != null) {
         this.position = position.clone();
      }
   }

   @Nullable
   public Vector3i getPosition() {
      return this.position == null ? null : this.position.clone();
   }

   @Nonnull
   public static PositionScanResult cast(ScanResult scanResult) {
      if (!(scanResult instanceof PositionScanResult)) {
         throw new IllegalArgumentException("The provided ScanResult isn't compatible with this prop.");
      } else {
         return (PositionScanResult)scanResult;
      }
   }

   @Override
   public boolean isNegative() {
      return this.position == null;
   }
}
