package com.hypixel.hytale.procedurallib.logic.point;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import javax.annotation.Nonnull;

public class OffsetPointGenerator implements IPointGenerator {
   private final IPointGenerator generator;
   private final double offsetX;
   private final double offsetY;
   private final double offsetZ;

   public OffsetPointGenerator(IPointGenerator generator, double offsetX, double offsetY, double offsetZ) {
      this.generator = generator;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
   }

   public double getOffsetX() {
      return this.offsetX;
   }

   public double getOffsetY() {
      return this.offsetY;
   }

   public double getOffsetZ() {
      return this.offsetZ;
   }

   @Override
   public ResultBuffer.ResultBuffer2d nearest2D(int seed, double x, double y) {
      return this.generator.nearest2D(seed, x + this.offsetX, y + this.offsetY);
   }

   @Override
   public ResultBuffer.ResultBuffer3d nearest3D(int seed, double x, double y, double z) {
      return this.generator.nearest3D(seed, x + this.offsetX, y + this.offsetY, z + this.offsetZ);
   }

   @Override
   public ResultBuffer.ResultBuffer2d transition2D(int seed, double x, double y) {
      return this.generator.transition2D(seed, x + this.offsetX, y + this.offsetY);
   }

   @Override
   public ResultBuffer.ResultBuffer3d transition3D(int seed, double x, double y, double z) {
      return this.generator.transition3D(seed, x + this.offsetX, y + this.offsetY, z + this.offsetZ);
   }

   @Override
   public void collect(int seed, double minX, double minY, double maxX, double maxY, @Nonnull IPointGenerator.PointConsumer2d consumer) {
      this.generator.collect(seed, minX, minY, maxX, maxY, (x, y) -> consumer.accept(x + this.offsetX, y + this.offsetY));
   }

   @Override
   public double getInterval() {
      return this.generator.getInterval();
   }
}
