package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.function.function.BiDoubleToDoubleFunction;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.condition.ConstantBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RequiredBlockFaceSupport;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CaveNodeShapeUtils {
   public static final BiDoubleToDoubleFunction LEFT = (l, r) -> l;
   public static final BiDoubleToDoubleFunction RIGHT = (l, r) -> r;
   public static final BiDoubleToDoubleFunction MIN = Math::min;
   public static final BiDoubleToDoubleFunction MAX = Math::max;

   @Nonnull
   public static Vector3d getBoxAnchor(@Nonnull Vector3d vector, @Nonnull IWorldBounds bounds, double tx, double ty, double tz) {
      double x = bounds.fractionX(tx);
      double y = bounds.fractionY(ty);
      double z = bounds.fractionZ(tz);
      return vector.assign(x, y, z);
   }

   @Nonnull
   public static Vector3d getLineAnchor(@Nonnull Vector3d vector, @Nonnull Vector3d o, @Nonnull Vector3d v, double t) {
      double x = o.x + v.x * t;
      double y = o.y + v.y * t;
      double z = o.z + v.z * t;
      return vector.assign(x, y, z);
   }

   @Nonnull
   public static Vector3d getSphereAnchor(@Nonnull Vector3d vector, @Nonnull Vector3d origin, double rx, double ry, double rz, double tx, double ty, double tz) {
      double fx = tx * 2.0 - 1.0;
      double fy = ty * 2.0 - 1.0;
      double fz = tz * 2.0 - 1.0;
      return getRadialProjection(vector, origin.x, origin.y, origin.z, rx, ry, rz, fx, fy, fz);
   }

   @Nonnull
   public static Vector3d getPipeAnchor(
      @Nonnull Vector3d vector, @Nonnull Vector3d o, @Nonnull Vector3d v, double rx, double ry, double rz, double t, double tv, double th
   ) {
      double x = o.x + v.x * t;
      double y = o.y + v.y * t;
      double z = o.z + v.z * t;
      double len = v.length();
      double nx = v.x / len;
      double ny = v.y / len;
      double nz = v.z / len;
      double fv = 2.0 * tv - 1.0;
      double fh = 2.0 * th - 1.0;
      double fx = -ny * fv - nz * fh;
      double fy = nx * fv;
      double fz = nx * fh;
      return getRadialProjection(vector, x, y, z, rx, ry, rz, fx, fy, fz);
   }

   @Nonnull
   public static Vector3d getOffset(@Nullable CaveNode parent, @Nonnull CaveNodeType.CaveNodeChildEntry childEntry) {
      Vector3d offset = childEntry.getOffset();
      if (offset == Vector3d.ZERO) {
         return offset;
      } else {
         if (parent != null && parent.getShape() instanceof PrefabCaveNodeShape) {
            offset = offset.clone();
            ((PrefabCaveNodeShape)parent.getShape()).getPrefabRotation().rotate(offset);
         }

         return offset;
      }
   }

   public static double getEndRadius(@Nullable CaveNode node, @Nonnull IDoubleRange range, Random random) {
      if (node != null) {
         double radius = getEndRadius(node.getShape(), MIN);
         if (radius != -1.0) {
            return radius;
         }
      }

      return range.getValue(random);
   }

   public static double getEndWidth(@Nullable CaveNode node, @Nonnull IDoubleRange range, Random random) {
      if (node != null) {
         double radius = getEndRadius(node.getShape(), LEFT);
         if (radius != -1.0) {
            return radius;
         }
      }

      return range.getValue(random);
   }

   public static double getEndHeight(@Nullable CaveNode node, @Nonnull IDoubleRange range, Random random) {
      if (node != null) {
         double radius = getEndRadius(node.getShape(), RIGHT);
         if (radius != -1.0) {
            return radius;
         }
      }

      return range.getValue(random);
   }

   public static double getEndRadius(@Nonnull CaveNodeShape shape, @Nonnull BiDoubleToDoubleFunction widthHeightSelector) {
      if (shape instanceof CylinderCaveNodeShape) {
         return ((CylinderCaveNodeShape)shape).getRadius2();
      } else if (shape instanceof PipeCaveNodeShape) {
         return ((PipeCaveNodeShape)shape).getRadius2();
      } else if (shape instanceof DistortedCaveNodeShape) {
         double width = ((DistortedCaveNodeShape)shape).getShape().getWidthAt(1.0);
         double height = ((DistortedCaveNodeShape)shape).getShape().getHeightAt(1.0);
         return widthHeightSelector.apply(width, height);
      } else {
         return -1.0;
      }
   }

   @Nullable
   public static BlockFluidEntry getFillingBlock(@Nonnull CaveType cave, @Nonnull CaveNodeType node, int y, @Nonnull Random random) {
      return cave.getFluidLevel().getHeight() >= y ? cave.getFluidLevel().getBlockEntry() : node.getFilling(random);
   }

   protected static int getCoverHeight(
      int lowest,
      int lowestPossible,
      int highest,
      int highestPossible,
      boolean heightLimited,
      @Nonnull CaveNodeType.CaveNodeCoverEntry cover,
      @Nonnull CaveNodeType.CaveNodeCoverEntry.Entry entry
   ) {
      switch (cover.getType()) {
         case FLOOR:
            if (lowest != Integer.MAX_VALUE && lowestPossible == lowest) {
               return lowest - 1 + entry.getOffset();
            }

            return -1;
         case CEILING:
            if (heightLimited) {
               return -1;
            } else {
               if (highest != Integer.MIN_VALUE && highestPossible == highest) {
                  return highest + 1 - entry.getOffset();
               }

               return -1;
            }
         default:
            throw new AssertionError("Not all cases covered!");
      }
   }

   public static boolean isCoverMatchingParent(
      int cx, int cz, int y, @Nonnull ChunkGeneratorExecution execution, @Nonnull CaveNodeType.CaveNodeCoverEntry cover
   ) {
      int parentY = y + cover.getType().parentOffset;
      if (parentY >= 0 && parentY <= 319) {
         IBlockFluidCondition parentCondition = cover.getParentCondition();
         if (parentCondition == ConstantBlockFluidCondition.DEFAULT_TRUE) {
            return true;
         } else if (parentCondition == ConstantBlockFluidCondition.DEFAULT_FALSE) {
            return false;
         } else {
            int parent = execution.getBlock(cx, parentY, cz);
            int parentFluid = execution.getFluid(cx, parentY, cz);
            return parentCondition.eval(parent, parentFluid);
         }
      } else {
         return false;
      }
   }

   public static boolean invalidateCover(
      int x,
      int y,
      int z,
      CaveNodeType.CaveNodeCoverType type,
      @Nonnull ChunkGeneratorExecution execution,
      @Nonnull BlockTypeAssetMap<String, BlockType> blockTypeMap
   ) {
      if (y >= 0 && y <= 319) {
         byte priority = execution.getPriorityChunk().get(x, y, z);
         if (priority == 3) {
            return true;
         } else if (priority != 5) {
            return false;
         } else {
            int block = execution.getBlock(x, y, z);
            BlockType blockType = blockTypeMap.getAsset(block);
            Map<BlockFace, RequiredBlockFaceSupport[]> supportsMap = blockType.getSupport(execution.getRotationIndex(x, y, z));
            if (supportsMap == null) {
               return false;
            } else {
               return switch (type) {
                  case FLOOR -> supportsMap.containsKey(BlockFace.DOWN);
                  case CEILING -> supportsMap.containsKey(BlockFace.UP);
               };
            }
         }
      } else {
         return false;
      }
   }

   @Nonnull
   protected static Vector3d getRadialProjection(
      @Nonnull Vector3d vector, double x, double y, double z, double rx, double ry, double rz, double tx, double ty, double tz
   ) {
      double len2 = tx * tx + ty * ty + tz * tz;
      if (len2 == 0.0) {
         return vector.assign(x, y, z);
      } else {
         double invLen = Math.sqrt(1.0 / len2);
         double dx = Math.abs(tx) * rx * invLen;
         double dy = Math.abs(ty) * ry * invLen;
         double dz = Math.abs(tz) * rz * invLen;
         x += dx * tx;
         y += dy * ty;
         z += dz * tz;
         return vector.assign(x, y, z);
      }
   }
}
