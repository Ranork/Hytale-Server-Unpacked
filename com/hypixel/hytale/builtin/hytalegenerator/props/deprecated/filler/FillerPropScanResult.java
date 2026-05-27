package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.filler;

import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.ScanResult;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;

public class FillerPropScanResult implements ScanResult {
   private List<Vector3i> positions;

   public FillerPropScanResult(@Nullable List<Vector3i> positions) {
      if (positions != null) {
         this.positions = positions;
      }
   }

   @Nonnull
   public static FillerPropScanResult cast(ScanResult scanResult) {
      if (!(scanResult instanceof FillerPropScanResult)) {
         throw new IllegalArgumentException("The provided ScanResult isn't compatible with this prop.");
      } else {
         return (FillerPropScanResult)scanResult;
      }
   }

   @Nullable
   public List<Vector3i> getFluidBlocks() {
      return this.positions;
   }

   @Override
   public boolean isNegative() {
      return this.positions == null || this.positions.isEmpty();
   }
}
