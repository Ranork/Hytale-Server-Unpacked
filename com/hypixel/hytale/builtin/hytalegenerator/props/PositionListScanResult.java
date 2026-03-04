package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.math.vector.Vector3i;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PositionListScanResult implements ScanResult {
   private List<Vector3i> positions;

   public PositionListScanResult(@Nullable List<Vector3i> positions) {
      if (positions != null) {
         this.positions = positions;
      }
   }

   @Nullable
   public List<Vector3i> getPositions() {
      return this.positions;
   }

   @Nonnull
   public static PositionListScanResult cast(ScanResult scanResult) {
      if (!(scanResult instanceof PositionListScanResult)) {
         throw new IllegalArgumentException("The provided ScanResult isn't compatible with this prop.");
      } else {
         return (PositionListScanResult)scanResult;
      }
   }

   @Override
   public boolean isNegative() {
      return this.positions == null || this.positions.isEmpty();
   }
}
