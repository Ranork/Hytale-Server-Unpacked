package com.hypixel.hytale.server.worldgen.zone;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.server.worldgen.chunk.MaskProvider;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ZonePatternGenerator {
   protected final IPointGenerator pointGenerator;
   protected final Zone[] zones;
   protected final Zone.Unique[] uniqueZones;
   protected final MaskProvider maskProvider;
   protected final ZoneColorMapping zoneColorMapping;

   public ZonePatternGenerator(
      IPointGenerator pointGenerator, Zone[] zones, Zone.Unique[] uniqueZones, MaskProvider maskProvider, ZoneColorMapping zoneColorMapping
   ) {
      this.pointGenerator = pointGenerator;
      this.zones = zones;
      this.uniqueZones = uniqueZones;
      this.maskProvider = maskProvider;
      this.zoneColorMapping = zoneColorMapping;
   }

   public Zone[] getZones() {
      return this.zones;
   }

   public Zone.Unique[] getUniqueZones() {
      return this.uniqueZones;
   }

   @Nonnull
   public ZoneGeneratorResult generate(int seed, double x, double z) {
      return this.generate(seed, x, z, new ZoneGeneratorResult());
   }

   @Nonnull
   public ZoneGeneratorResult generate(int seed, double x, double z, @Nonnull ZoneGeneratorResult result) {
      double rx = this.maskProvider.getX(seed, x, z);
      double rz = this.maskProvider.getY(seed, x, z);
      int mask = this.maskProvider.get(seed, rx, rz) & 16777215;
      Zone[] zoneArr = this.zoneColorMapping.get(mask);
      if (zoneArr == null) {
         throw new Error("Unknown zone colour mapping for mask: " + mask);
      } else {
         if (zoneArr.length == 1) {
            double distance = Double.POSITIVE_INFINITY;
            if (this.maskProvider.inBounds(rx, rz)) {
               distance = this.maskProvider.distance(rx, rz);
            }

            result.setZone(zoneArr[0]);
            result.setBorderDistance(distance);
         } else {
            this.getZone(seed, x, z, result, zoneArr);
         }

         return result;
      }
   }

   protected void getZone(int seed, double x, double z, @Nonnull ZoneGeneratorResult result, @Nonnull Zone[] zoneArr) {
      ResultBuffer.ResultBuffer2d buf = this.pointGenerator.nearest2D(seed, x, z);
      int index = ((int)HashUtil.hash(seed, buf.ix, buf.iy) & 2147483647) % zoneArr.length;
      result.setZone(zoneArr[index]);
      result.setBorderDistance(buf.distance);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ZonePatternGenerator{pointGenerator="
         + this.pointGenerator
         + ", zones="
         + Arrays.toString((Object[])this.zones)
         + ", maskProvider="
         + this.maskProvider
         + ", zoneColorMapping="
         + this.zoneColorMapping
         + "}";
   }
}
