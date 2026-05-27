package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;

public class PositionScanResult implements ScanResult {
   private Vector3i position;

   public PositionScanResult(@Nullable Vector3i position) {
      if (position != null) {
         this.position = new Vector3i(position);
      }
   }

   @Nullable
   public Vector3i getPosition() {
      return this.position == null ? null : new Vector3i(this.position);
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
