package com.hypixel.hytale.builtin.hytalegenerator.props.filler;

import com.hypixel.hytale.builtin.hytalegenerator.props.ScanResult;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FillerPropScanResult implements ScanResult {
   private List<Vector3i> positions;

   public FillerPropScanResult(@Nullable List<Vector3i> positions) {
      if (positions != null) {
         this.positions = positions;
      }
   }

   @Nullable
   public List<Vector3i> getFluidBlocks() {
      return this.positions;
   }

   @Nonnull
   public static FillerPropScanResult cast(ScanResult scanResult) {
      if (!(scanResult instanceof FillerPropScanResult)) {
         throw new IllegalArgumentException("The provided ScanResult isn't compatible with this prop.");
      } else {
         return (FillerPropScanResult)scanResult;
      }
   }

   @Override
   public boolean isNegative() {
      return this.positions == null || this.positions.isEmpty();
   }
}
