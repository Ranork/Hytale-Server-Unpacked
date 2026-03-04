package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.worldgen.cave.Cave;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.cave.shape.distorted.AbstractDistortedShape;
import com.hypixel.hytale.server.worldgen.cave.shape.distorted.DistortedShape;
import com.hypixel.hytale.server.worldgen.cave.shape.distorted.ShapeDistortion;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DistortedCaveNodeShape implements CaveNodeShape {
   private final CaveType caveType;
   private final DistortedShape shape;
   private final ShapeDistortion distortion;

   public DistortedCaveNodeShape(CaveType caveType, DistortedShape shape, ShapeDistortion distortion) {
      this.caveType = caveType;
      this.shape = shape;
      this.distortion = distortion;
   }

   public DistortedShape getShape() {
      return this.shape;
   }

   @Override
   public Vector3d getStart() {
      return this.shape.getStart();
   }

   @Override
   public Vector3d getEnd() {
      return this.shape.getEnd();
   }

   @Override
   public Vector3d getAnchor(Vector3d vector, double tx, double ty, double tz) {
      return this.shape.getAnchor(vector, tx, ty, tz);
   }

   @Override
   public IWorldBounds getBounds() {
      return this.shape;
   }

   @Override
   public boolean hasGeometry() {
      return this.shape.hasGeometry();
   }

   @Override
   public boolean shouldReplace(int seed, double x, double z, int y) {
      double t = this.shape.getProjection(x, z);
      if (this.shape.isValidProjection(t)) {
         double centerY = this.shape.getYAt(t);
         double shapeHeight = this.shape.getHeightAtProjection(seed, x, z, t, centerY, this.caveType, this.distortion);
         if (shapeHeight > 0.0) {
            int minY = this.getBounds().getLowBoundY();
            int floor = this.getFloor(seed, x, z, centerY, shapeHeight, minY);
            if (y < floor) {
               return false;
            }

            int maxY = this.getBounds().getHighBoundY();
            int ceiling = this.getCeiling(seed, x, z, centerY, shapeHeight, maxY);
            return y <= ceiling;
         }
      }

      return false;
   }

   @Override
   public double getFloorPosition(int seed, double x, double z) {
      double t = this.shape.getProjection(x, z);
      if (this.shape.isValidProjection(t)) {
         double centerY = this.shape.getYAt(t);
         double shapeHeight = this.shape.getHeightAtProjection(seed, x, z, t, centerY, this.caveType, this.distortion);
         if (shapeHeight > 0.0) {
            int minY = this.getBounds().getLowBoundY();
            return this.getFloor(seed, x, z, centerY, shapeHeight, minY) - 1;
         }
      }

      return -1.0;
   }

   @Override
   public double getCeilingPosition(int seed, double x, double z) {
      double t = this.shape.getProjection(x, z);
      if (this.shape.isValidProjection(t)) {
         double centerY = this.shape.getYAt(t);
         double shapeHeight = this.shape.getHeightAtProjection(seed, x, z, t, centerY, this.caveType, this.distortion);
         if (shapeHeight > 0.0) {
            int maxY = this.getBounds().getHighBoundY();
            return this.getCeiling(seed, x, z, centerY, shapeHeight, maxY) + 1;
         }
      }

      return -1.0;
   }

   @Override
   public void populateChunk(int seed, @Nonnull ChunkGeneratorExecution execution, @Nonnull Cave cave, @Nonnull CaveNode node, @Nonnull Random random) {
      GeneratedBlockChunk chunk = execution.getChunk();
      BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();
      CaveType caveType = cave.getCaveType();
      CaveNodeType caveNodeType = node.getCaveNodeType();
      IWorldBounds shapeBounds = this.getBounds();
      boolean surfaceLimited = cave.getCaveType().isSurfaceLimited();
      int environment = node.getCaveNodeType().hasEnvironment() ? node.getCaveNodeType().getEnvironment() : caveType.getEnvironment();
      int chunkLowX = ChunkUtil.minBlock(execution.getX());
      int chunkLowZ = ChunkUtil.minBlock(execution.getZ());
      int chunkHighX = ChunkUtil.maxBlock(execution.getX());
      int chunkHighZ = ChunkUtil.maxBlock(execution.getZ());
      int minX = Math.max(chunkLowX, shapeBounds.getLowBoundX());
      int minY = shapeBounds.getLowBoundY();
      int minZ = Math.max(chunkLowZ, shapeBounds.getLowBoundZ());
      int maxX = Math.min(chunkHighX, shapeBounds.getHighBoundX());
      int maxY = shapeBounds.getHighBoundY();
      int maxZ = Math.min(chunkHighZ, shapeBounds.getHighBoundZ());

      for (int x = minX; x <= maxX; x++) {
         int cx = x - chunkLowX;

         for (int z = minZ; z <= maxZ; z++) {
            int cz = z - chunkLowZ;
            int maximumY = maxY;
            boolean heightLimited = false;
            if (surfaceLimited) {
               int chunkHeight = chunk.getHeight(cx, cz);
               if (maxY >= chunkHeight) {
                  maximumY = chunkHeight;
                  heightLimited = true;
               }
            }

            int lowest = Integer.MAX_VALUE;
            int lowestPossible = Integer.MAX_VALUE;
            int highest = Integer.MIN_VALUE;
            int highestPossible = Integer.MIN_VALUE;
            double t = this.shape.getProjection(x, z);
            if (this.shape.isValidProjection(t)) {
               double centerY = this.shape.getYAt(t);
               double shapeHeight = this.shape.getHeightAtProjection(seed, x, z, t, centerY, caveType, this.distortion);
               if (!(shapeHeight <= 0.0)) {
                  int floorY = this.getFloor(seed, x, z, centerY, shapeHeight, minY);
                  int ceilingY = this.getCeiling(seed, x, z, centerY, shapeHeight, maximumY);
                  if (floorY < lowestPossible) {
                     lowestPossible = floorY;
                  }

                  if (ceilingY > highestPossible) {
                     highestPossible = ceilingY;
                  }

                  for (int y = floorY; y <= ceilingY; y++) {
                     int current = execution.getBlock(cx, y, cz);
                     int currentFluid = execution.getFluid(cx, y, cz);
                     boolean isCandidateBlock = !surfaceLimited || current != 0;
                     if (isCandidateBlock) {
                        BlockFluidEntry blockEntry = CaveNodeShapeUtils.getFillingBlock(caveType, caveNodeType, y, random);
                        if (caveType.getBlockMask().eval(current, currentFluid, blockEntry.blockId(), blockEntry.fluidId())) {
                           if (execution.setBlock(cx, y, cz, (byte)6, blockEntry, environment)) {
                              if (y < lowest) {
                                 lowest = y;
                              }

                              if (y > highest) {
                                 highest = y;
                              }
                           }

                           if (execution.setFluid(cx, y, cz, (byte)6, blockEntry.fluidId(), environment)) {
                              if (y < lowest) {
                                 lowest = y;
                              }

                              if (y > highest) {
                                 highest = y;
                              }
                           }
                        }
                     }
                  }

                  CaveNodeType.CaveNodeCoverEntry[] covers = caveNodeType.getCovers();

                  for (CaveNodeType.CaveNodeCoverEntry cover : covers) {
                     CaveNodeType.CaveNodeCoverEntry.Entry entry = cover.get(random);
                     int yx = CaveNodeShapeUtils.getCoverHeight(lowest, lowestPossible, highest, highestPossible, heightLimited, cover, entry);
                     if (yx >= 0
                        && cover.getDensityCondition().eval(seed + node.getSeedOffset(), x, z)
                        && cover.getHeightCondition().eval(seed, x, z, yx, random)
                        && cover.getMapCondition().eval(seed, x, z)
                        && CaveNodeShapeUtils.isCoverMatchingParent(cx, cz, yx, execution, cover)) {
                        execution.setBlock(cx, yx, cz, (byte)5, entry.getEntry(), environment);
                        execution.setFluid(cx, yx, cz, (byte)5, entry.getEntry().fluidId(), environment);
                     }
                  }

                  if (CaveNodeShapeUtils.invalidateCover(cx, lowest - 1, cz, CaveNodeType.CaveNodeCoverType.CEILING, execution, blockTypeMap)) {
                     BlockFluidEntry blockEntry = CaveNodeShapeUtils.getFillingBlock(caveType, caveNodeType, lowest - 1, random);
                     execution.overrideBlock(cx, lowest - 1, cz, (byte)6, blockEntry);
                     execution.overrideFluid(cx, lowest - 1, cz, (byte)6, blockEntry.fluidId());
                  }

                  if (CaveNodeShapeUtils.invalidateCover(cx, highest + 1, cz, CaveNodeType.CaveNodeCoverType.FLOOR, execution, blockTypeMap)) {
                     BlockFluidEntry blockEntry = CaveNodeShapeUtils.getFillingBlock(caveType, caveNodeType, highest + 1, random);
                     execution.overrideBlock(cx, highest + 1, cz, (byte)6, blockEntry);
                     execution.overrideFluid(cx, highest + 1, cz, (byte)6, blockEntry.fluidId());
                  }
               }
            }
         }
      }
   }

   private int getFloor(int seed, double x, double z, double centerY, double height, int minY) {
      height *= this.distortion.getFloorFactor(seed, x, z);
      double floorY = this.shape.getFloor(x, z, centerY, height);
      return Math.max(MathUtil.floor(floorY), minY);
   }

   private int getCeiling(int seed, double x, double z, double centerY, double height, int maxY) {
      height *= this.distortion.getCeilingFactor(seed, x, z);
      double ceilingY = this.shape.getCeiling(x, z, centerY, height);
      return Math.min(MathUtil.ceil(ceilingY), maxY);
   }

   public static class DistortedCaveNodeShapeGenerator implements CaveNodeShapeEnum.CaveNodeShapeGenerator {
      private final DistortedShape.Factory shapeFactory;
      private final IDoubleRange widthRange;
      private final IDoubleRange midWidthRange;
      private final IDoubleRange heightRange;
      private final IDoubleRange midHeightRange;
      private final IDoubleRange lengthRange;
      private final ShapeDistortion distortion;
      private final boolean inheritParentRadius;
      private final GeneralNoise.InterpolationFunction interpolation;

      public DistortedCaveNodeShapeGenerator(
         DistortedShape.Factory shapeFactory,
         IDoubleRange widthRange,
         IDoubleRange heightRange,
         @Nullable IDoubleRange midWidthRange,
         @Nullable IDoubleRange midHeightRange,
         @Nullable IDoubleRange lengthRange,
         boolean inheritParentRadius,
         ShapeDistortion distortion,
         @Nullable GeneralNoise.InterpolationFunction interpolation
      ) {
         this.shapeFactory = shapeFactory;
         this.widthRange = widthRange;
         this.heightRange = heightRange;
         this.midWidthRange = midWidthRange;
         this.midHeightRange = midHeightRange;
         this.lengthRange = lengthRange;
         this.distortion = distortion;
         this.inheritParentRadius = inheritParentRadius;
         this.interpolation = interpolation;
      }

      @Nonnull
      @Override
      public CaveNodeShape generateCaveNodeShape(
         Random random,
         CaveType caveType,
         @Nullable CaveNode parentNode,
         @Nonnull CaveNodeType.CaveNodeChildEntry childEntry,
         @Nonnull Vector3d position,
         float yaw,
         float pitch
      ) {
         double length = getLength(this.lengthRange, random);
         Vector3d origin = getOrigin(position, parentNode, childEntry);
         Vector3d direction = getDirection(yaw, pitch, length);
         double startWidth = getStartWidth(this.inheritParentRadius, parentNode, this.widthRange, random);
         double startHeight = getStartHeight(this.inheritParentRadius, parentNode, this.heightRange, random);
         double endWidth = this.widthRange.getValue(random);
         double endHeight = this.heightRange.getValue(random);
         double midWidth = getMiddleRadius(startWidth, endWidth, this.midWidthRange, random);
         double midHeight = getMiddleRadius(startHeight, endHeight, this.midHeightRange, random);
         DistortedShape shape = this.shapeFactory
            .create(origin, direction, length, startWidth, startHeight, midWidth, midHeight, endWidth, endHeight, this.interpolation);
         return new DistortedCaveNodeShape(caveType, shape, this.distortion);
      }

      @Nonnull
      private static Vector3d getOrigin(@Nonnull Vector3d origin, @Nullable CaveNode parentNode, @Nonnull CaveNodeType.CaveNodeChildEntry childEntry) {
         if (parentNode == null) {
            return origin;
         } else {
            Vector3d offset = CaveNodeShapeUtils.getOffset(parentNode, childEntry);
            origin.add(offset);
            return origin.add(offset);
         }
      }

      private static double getLength(@Nullable IDoubleRange lengthRange, Random random) {
         return lengthRange == null ? 0.0 : lengthRange.getValue(random);
      }

      @Nonnull
      private static Vector3d getDirection(double yaw, double pitch, double length) {
         if (length == 0.0) {
            return Vector3d.ZERO;
         } else {
            pitch = AbstractDistortedShape.clampPitch(pitch);
            return new Vector3d(TrigMathUtil.sin(pitch) * TrigMathUtil.cos(yaw), TrigMathUtil.cos(pitch), TrigMathUtil.sin(pitch) * TrigMathUtil.sin(yaw))
               .scale(length);
         }
      }

      private static double getStartWidth(boolean inheritParentRadius, @Nullable CaveNode parentNode, @Nonnull IDoubleRange fallback, Random random) {
         return inheritParentRadius ? CaveNodeShapeUtils.getEndWidth(parentNode, fallback, random) : fallback.getValue(random);
      }

      private static double getStartHeight(boolean inheritParentRadius, @Nullable CaveNode parentNode, @Nonnull IDoubleRange fallback, Random random) {
         return inheritParentRadius ? CaveNodeShapeUtils.getEndHeight(parentNode, fallback, random) : fallback.getValue(random);
      }

      private static double getMiddleRadius(double start, double end, @Nullable IDoubleRange range, Random random) {
         return range == null ? (start - end) * 0.5 + start : range.getValue(random);
      }
   }
}
