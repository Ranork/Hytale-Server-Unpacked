package com.hypixel.hytale.server.core.asset.type.fluid;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.AbstractCachedAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class FluidTicker {
   public static final BuilderCodec<FluidTicker> BASE_CODEC = BuilderCodec.abstractBuilder(FluidTicker.class)
      .appendInherited(
         new KeyedCodec<>("FlowRate", Codec.FLOAT),
         (ticker, r) -> ticker.flowRate = r,
         ticker -> ticker.flowRate,
         (ticker, parent) -> ticker.flowRate = parent.flowRate
      )
      .documentation("The tick frequency for this fluid type, in seconds")
      .addValidator(Validators.greaterThan(0.0F))
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("CanDemote", Codec.BOOLEAN),
         (ticker, o) -> ticker.canDemote = o,
         ticker -> ticker.canDemote,
         (ticker, parent) -> ticker.canDemote = parent.canDemote
      )
      .documentation("If false then the fluid will stay at its level")
      .add()
      .appendInherited(
         new KeyedCodec<>("SupportedBy", Codec.STRING),
         (ticker, o) -> ticker.supportedBy = o,
         ticker -> ticker.supportedBy,
         (ticker, parent) -> ticker.supportedBy = parent.supportedBy
      )
      .add()
      .build();
   public static final CodecMapCodec<FluidTicker> CODEC = new CodecMapCodec<>("Type", true);
   protected static final Vector2i[] ORTO_OFFSETS = new Vector2i[]{new Vector2i(-1, 0), new Vector2i(1, 0), new Vector2i(0, -1), new Vector2i(0, 1)};
   protected static final int SPREAD_NO_PATH = Integer.MAX_VALUE;
   protected static final int SPREAD_NO_CHUNK = 2147483646;
   protected static final int OFFSET_DROP_NONE = 0;
   public static final int FLUID_BLOCK_DISTANCE = 5;
   private static final double FULL_DIMENSION_THRESHOLD = 0.9;
   private static final double PARTIAL_DIMENSION_THRESHOLD = 0.6;
   private static final double FACE_BLOCK_THRESHOLD = 0.1;
   private float flowRate = 0.5F;
   private boolean canDemote = true;
   private String supportedBy;
   private transient int supportedById = 0;

   public int getSupportedById() {
      if (this.supportedById == 0) {
         if (this.supportedBy != null) {
            this.supportedById = Fluid.getAssetMap().getIndex(this.supportedBy);
         } else {
            this.supportedById = Integer.MIN_VALUE;
         }
      }

      return this.supportedById;
   }

   public BlockTickStrategy tick(
      @Nonnull CommandBuffer<ChunkStore> commandBuffer,
      @Nonnull FluidTicker.CachedAccessor cachedAccessor,
      @Nonnull FluidSection fluidSection,
      @Nonnull BlockSection blockSection,
      @Nonnull Fluid fluid,
      int fluidId,
      int worldX,
      int worldY,
      int worldZ
   ) {
      int block = blockSection.get(worldX, worldY, worldZ);
      if (!this.canOccupySolidBlocks() && isFullySolid(BlockType.getAssetMap().getAsset(block))) {
         fluidSection.setFluid(worldX, worldY, worldZ, 0, (byte)0);
         setTickingSurrounding(cachedAccessor, blockSection, worldX, worldY, worldZ);
         return BlockTickStrategy.SLEEP;
      } else {
         World world = commandBuffer.getExternalData().getWorld();
         long hash = HashUtil.rehash(worldX, worldY, worldZ, 4030921250L);
         long tick = commandBuffer.getExternalData().getWorld().getTick();
         int flowRateLimitTicks = Math.round(this.flowRate * world.getTps());
         return (hash + tick) % flowRateLimitTicks != 0L
            ? BlockTickStrategy.CONTINUE
            : this.process(world, tick, cachedAccessor, fluidSection, blockSection, fluid, fluidId, worldX, worldY, worldZ);
      }
   }

   public boolean canOccupySolidBlocks() {
      return false;
   }

   public BlockTickStrategy process(
      World world,
      long tick,
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      @Nonnull BlockSection blockSection,
      @Nonnull Fluid fluid,
      int fluidId,
      int worldX,
      int worldY,
      int worldZ
   ) {
      byte fluidLevel = fluidSection.getFluidLevel(worldX, worldY, worldZ);
      switch (this.isAlive(accessor, fluidSection, blockSection, fluid, fluidId, fluidLevel, worldX, worldY, worldZ)) {
         case ALIVE:
            return this.spread(world, tick, accessor, fluidSection, blockSection, fluid, fluidId, fluidLevel, worldX, worldY, worldZ);
         case DEMOTE:
            if (fluidLevel == 1) {
               fluidSection.setFluid(worldX, worldY, worldZ, 0, (byte)0);
               setTickingSurrounding(accessor, blockSection, worldX, worldY, worldZ);
               return BlockTickStrategy.SLEEP;
            }

            fluidSection.setFluid(worldX, worldY, worldZ, fluidId, (byte)((fluidLevel == 0 ? fluid.getMaxFluidLevel() : fluidLevel) - 1));
            setTickingSurrounding(accessor, blockSection, worldX, worldY, worldZ);
            return BlockTickStrategy.SLEEP;
         case WAIT_FOR_ADJACENT_CHUNK:
            return BlockTickStrategy.WAIT_FOR_ADJACENT_CHUNK_LOAD;
         default:
            return BlockTickStrategy.SLEEP;
      }
   }

   @Nonnull
   protected FluidTicker.AliveStatus isAlive(
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      @Nonnull BlockSection blockSection,
      Fluid fluid,
      int fluidId,
      byte fluidLevel,
      int worldX,
      int worldY,
      int worldZ
   ) {
      if (!this.canDemote) {
         return FluidTicker.AliveStatus.ALIVE;
      } else {
         int supportId = this.getSupportedById();
         BlockTypeAssetMap<String, BlockType> blockMap = BlockType.getAssetMap();
         FluidSection aboveFluidSection = ChunkUtil.chunkCoordinate(worldY + 1) == fluidSection.getY()
            ? fluidSection
            : accessor.getFluidSectionByBlock(worldX, worldY + 1, worldZ);
         if (aboveFluidSection == null) {
            return FluidTicker.AliveStatus.WAIT_FOR_ADJACENT_CHUNK;
         } else {
            int fluidAbove = aboveFluidSection.getFluidId(worldX, worldY + 1, worldZ);
            if (fluidAbove != fluidId && fluidAbove != supportId) {
               BlockType thisBlock = blockMap.getAsset(blockSection.get(worldX, worldY, worldZ));
               int thisRotation = blockSection.getRotationIndex(worldX, worldY, worldZ);
               int thisFiller = blockSection.getFiller(worldX, worldY, worldZ);
               boolean chunkNotLoaded = false;

               for (Vector2i offset : ORTO_OFFSETS) {
                  int x = offset.x;
                  int z = offset.y;
                  int blockX = x + worldX;
                  int blockZ = z + worldZ;
                  boolean isDifferentSection = !ChunkUtil.isSameChunkSection(worldX, worldY, worldZ, blockX, worldY, blockZ);
                  FluidSection otherFluidSection = isDifferentSection ? accessor.getFluidSectionByBlock(blockX, worldY, blockZ) : fluidSection;
                  BlockSection otherBlockSection = isDifferentSection ? accessor.getBlockSectionByBlock(blockX, worldY, blockZ) : blockSection;
                  if (otherFluidSection != null && otherBlockSection != null) {
                     int otherFluid = otherFluidSection.getFluidId(blockX, worldY, blockZ);
                     if (supportId != Integer.MIN_VALUE && otherFluid == supportId) {
                        BlockType sourceBlock = blockMap.getAsset(otherBlockSection.get(blockX, worldY, blockZ));
                        if (sourceBlock != null) {
                           int sourceRotation = otherBlockSection.getRotationIndex(blockX, worldY, blockZ);
                           int sourceFiller = otherBlockSection.getFiller(blockX, worldY, blockZ);
                           if (!this.blocksFluidFrom(sourceBlock, sourceRotation, x, z, sourceFiller)
                              && !this.blocksFluidFrom(thisBlock, thisRotation, -x, -z, thisFiller)) {
                              return FluidTicker.AliveStatus.ALIVE;
                           }
                        }
                     } else {
                        byte otherFluidLevel = otherFluidSection.getFluidLevel(blockX, worldY, blockZ);
                        if (otherFluid != 0 && otherFluid == fluidId && otherFluidLevel > fluidLevel) {
                           BlockType sourceBlock = blockMap.getAsset(otherBlockSection.get(blockX, worldY, blockZ));
                           if (sourceBlock != null) {
                              int sourceRotation = otherBlockSection.getRotationIndex(blockX, worldY, blockZ);
                              int sourceFiller = otherBlockSection.getFiller(blockX, worldY, blockZ);
                              if (!this.blocksFluidFrom(sourceBlock, sourceRotation, x, z, sourceFiller)
                                 && !this.blocksFluidFrom(thisBlock, thisRotation, -x, -z, thisFiller)) {
                                 return FluidTicker.AliveStatus.ALIVE;
                              }
                           }
                        }
                     }
                  } else {
                     chunkNotLoaded = true;
                  }
               }

               return chunkNotLoaded ? FluidTicker.AliveStatus.WAIT_FOR_ADJACENT_CHUNK : FluidTicker.AliveStatus.DEMOTE;
            } else {
               return FluidTicker.AliveStatus.ALIVE;
            }
         }
      }
   }

   protected abstract BlockTickStrategy spread(
      World var1, long var2, FluidTicker.Accessor var4, FluidSection var5, BlockSection var6, Fluid var7, int var8, byte var9, int var10, int var11, int var12
   );

   public static void setTickingSurrounding(@Nonnull FluidTicker.Accessor accessor, BlockSection blockSection, int worldX, int worldY, int worldZ) {
      for (int y = -1; y <= 1; y++) {
         for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
               int bx = worldX + x;
               int by = worldY + y;
               int bz = worldZ + z;
               BlockSection chunk = ChunkUtil.isSameChunkSection(worldX, worldY, worldZ, bx, by, bz)
                  ? blockSection
                  : accessor.getBlockSectionByBlock(bx, by, bz);
               if (chunk != null) {
                  chunk.setTicking(bx, by, bz, true);
               }
            }
         }
      }
   }

   protected int getSpreadOffsets(
      @Nonnull BlockTypeAssetMap<String, BlockType> blockMap,
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      BlockSection blockSection,
      int worldX,
      int worldY,
      int worldZ,
      @Nonnull Vector2i[] offsetArray,
      int fluidId,
      int maxDropDistance
   ) {
      int shortestDistanceToDrop = Integer.MAX_VALUE;
      int offsets = 0;
      if (worldY <= 0) {
         return offsets;
      } else {
         for (int i = 0; i < offsetArray.length; i++) {
            Vector2i offset = offsetArray[i];
            int distance = this.distanceToDrop(blockMap, accessor, fluidSection, blockSection, worldX, worldY, worldZ, offset, fluidId, maxDropDistance);
            if (distance == 2147483646) {
               return 2147483646;
            }

            if (distance < shortestDistanceToDrop) {
               offsets = 0;
               shortestDistanceToDrop = distance;
            }

            if (distance <= shortestDistanceToDrop && distance != Integer.MAX_VALUE) {
               offsets |= 1 << i;
            }
         }

         return offsets;
      }
   }

   protected int distanceToDrop(
      @Nonnull BlockTypeAssetMap<String, BlockType> blockMap,
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      BlockSection blockSection,
      int worldX,
      int worldY,
      int worldZ,
      @Nonnull Vector2i offset,
      int fluidId,
      int maxDropDistance
   ) {
      int ox = offset.x;
      int oz = offset.y;
      int yMinus1 = worldY - 1;
      FluidSection belowFluidSection = fluidSection;
      BlockSection belowBlockSection = blockSection;
      boolean isBelowDifferent = fluidSection.getY() != ChunkUtil.chunkCoordinate(yMinus1);
      if (isBelowDifferent) {
         belowFluidSection = accessor.getFluidSectionByBlock(worldX, yMinus1, worldZ);
         belowBlockSection = accessor.getBlockSectionByBlock(worldX, yMinus1, worldZ);
      }

      int curX = worldX;
      int curZ = worldZ;
      int supportedById = this.getSupportedById();

      for (int i = 1; i < maxDropDistance; i++) {
         int blockX = worldX + ox * i;
         int blockZ = worldZ + oz * i;
         if (!ChunkUtil.isSameChunk(curX, curZ, blockX, blockZ)) {
            curX = blockX;
            curZ = blockZ;
            fluidSection = accessor.getFluidSectionByBlock(blockX, worldY, blockZ);
            blockSection = accessor.getBlockSectionByBlock(blockX, worldY, blockZ);
            if (isBelowDifferent) {
               belowFluidSection = accessor.getFluidSectionByBlock(worldX, yMinus1, worldZ);
               belowBlockSection = accessor.getBlockSectionByBlock(worldX, yMinus1, worldZ);
            } else {
               belowFluidSection = fluidSection;
               belowBlockSection = blockSection;
            }
         }

         if (fluidSection == null || blockSection == null || belowFluidSection == null || belowBlockSection == null) {
            return 2147483646;
         }

         int otherFluidId = fluidSection.getFluidId(blockX, worldY, blockZ);
         BlockType block = blockMap.getAsset(blockSection.get(blockX, worldY, blockZ));
         if (otherFluidId != 0 && (otherFluidId != fluidId || otherFluidId == supportedById || supportedById == Integer.MIN_VALUE)
            || otherFluidId == 0 && isSolid(block)) {
            break;
         }

         BlockType belowBlock = blockMap.getAsset(belowBlockSection.get(blockX, yMinus1, blockZ));
         if (!isSolid(belowBlock)) {
            return i;
         }
      }

      return Integer.MAX_VALUE;
   }

   public static boolean isFullySolid(@Nonnull BlockType blockType) {
      DrawType drawType = blockType.getDrawType();
      return blockType.getMaterial() == BlockMaterial.Solid && (drawType == DrawType.Cube || drawType == DrawType.CubeWithModel);
   }

   public static boolean isSolid(@Nonnull BlockType blockType) {
      DrawType drawType = blockType.getDrawType();
      return drawType == DrawType.Cube || drawType == DrawType.CubeWithModel;
   }

   public boolean blocksFluidFrom(@Nonnull BlockType blockType, int rotationIndex, int offsetX, int offsetZ) {
      return this.blocksFluidFrom(blockType, rotationIndex, offsetX, offsetZ, 0);
   }

   public boolean blocksFluidFrom(@Nonnull BlockType blockType, int rotationIndex, int offsetX, int offsetZ, int filler) {
      if (blockType.getMaterial() != BlockMaterial.Solid) {
         return false;
      } else if (isFullySolid(blockType)) {
         return true;
      } else {
         int hitboxIndex = blockType.getHitboxTypeIndex();
         BlockBoundingBoxes hitboxAsset = BlockBoundingBoxes.getAssetMap().getAsset(hitboxIndex);
         if (hitboxAsset == null) {
            return true;
         } else {
            BlockBoundingBoxes.RotatedVariantBoxes rotatedHitbox = hitboxAsset.get(rotationIndex);
            Box boundingBox = rotatedHitbox.getBoundingBox();
            if (!hitboxAsset.protrudesUnitBox() && filler == 0) {
               double width = boundingBox.max.x - boundingBox.min.x;
               double height = boundingBox.max.y - boundingBox.min.y;
               double depth = boundingBox.max.z - boundingBox.min.z;
               boolean isTall = height > 0.9;
               if (!isTall) {
                  return false;
               } else {
                  boolean isFullDepth = depth > 0.9;
                  boolean isPartialWidth = width < 0.6;
                  if (!isPartialWidth || !isFullDepth) {
                     boolean isFullWidth = width > 0.9;
                     boolean isPartialDepth = depth < 0.6;
                     if (!isFullWidth || !isPartialDepth) {
                        Box[] detailBoxes = rotatedHitbox.getDetailBoxes();
                        if (detailBoxes.length > 1) {
                           return boxesBlockFace(detailBoxes, offsetX, offsetZ);
                        } else {
                           double faceCoverage = 0.0;
                           if (offsetX > 0 && boundingBox.min.x < 0.1) {
                              faceCoverage = height * depth;
                           } else if (offsetX < 0 && boundingBox.max.x > 0.9) {
                              faceCoverage = height * depth;
                           } else if (offsetZ > 0 && boundingBox.min.z < 0.1) {
                              faceCoverage = height * width;
                           } else if (offsetZ < 0 && boundingBox.max.z > 0.9) {
                              faceCoverage = height * width;
                           }

                           return faceCoverage > 0.9;
                        }
                     } else if (offsetZ == 0) {
                        return false;
                     } else {
                        return offsetZ > 0 ? boundingBox.min.z < 0.1 : boundingBox.max.z > 0.9;
                     }
                  } else if (offsetX == 0) {
                     return false;
                  } else {
                     return offsetX > 0 ? boundingBox.min.x < 0.1 : boundingBox.max.x > 0.9;
                  }
               }
            } else {
               int fillerX = FillerBlockUtil.unpackX(filler);
               int fillerY = FillerBlockUtil.unpackY(filler);
               int fillerZ = FillerBlockUtil.unpackZ(filler);
               Box[] detailBoxes = rotatedHitbox.getDetailBoxes();
               double maxCrossSectionCoverage = 0.0;

               for (Box box : detailBoxes) {
                  double clampedMinX = Math.max(box.min.x, (double)fillerX);
                  double clampedMaxX = Math.min(box.max.x, (double)(fillerX + 1));
                  double clampedMinY = Math.max(box.min.y, (double)fillerY);
                  double clampedMaxY = Math.min(box.max.y, (double)(fillerY + 1));
                  double clampedMinZ = Math.max(box.min.z, (double)fillerZ);
                  double clampedMaxZ = Math.min(box.max.z, (double)(fillerZ + 1));
                  if (!(clampedMaxX <= clampedMinX) && !(clampedMaxY <= clampedMinY) && !(clampedMaxZ <= clampedMinZ)) {
                     double boxWidth = clampedMaxX - clampedMinX;
                     double boxHeight = clampedMaxY - clampedMinY;
                     double boxDepth = clampedMaxZ - clampedMinZ;
                     double crossSectionCoverage = 0.0;
                     if (offsetX != 0) {
                        crossSectionCoverage = boxHeight * boxDepth;
                     } else if (offsetZ != 0) {
                        crossSectionCoverage = boxHeight * boxWidth;
                     }

                     maxCrossSectionCoverage = Math.max(maxCrossSectionCoverage, crossSectionCoverage);
                  }
               }

               return maxCrossSectionCoverage > 0.9;
            }
         }
      }
   }

   private static boolean boxesBlockFace(Box[] boxes, int offsetX, int offsetZ) {
      double totalArea = 0.0;

      for (Box box : boxes) {
         double areaOnFace = 0.0;
         if (offsetX > 0 && box.min.x < 0.1) {
            double height = box.max.y - box.min.y;
            double depth = box.max.z - box.min.z;
            areaOnFace = height * depth;
         } else if (offsetX < 0 && box.max.x > 0.9) {
            double height = box.max.y - box.min.y;
            double depth = box.max.z - box.min.z;
            areaOnFace = height * depth;
         } else if (offsetZ > 0 && box.min.z < 0.1) {
            double height = box.max.y - box.min.y;
            double width = box.max.x - box.min.x;
            areaOnFace = height * width;
         } else if (offsetZ < 0 && box.max.z > 0.9) {
            double height = box.max.y - box.min.y;
            double width = box.max.x - box.min.x;
            areaOnFace = height * width;
         }

         totalArea += areaOnFace;
      }

      return totalArea > 0.9;
   }

   public boolean isSelfFluid(int selfFluidId, int otherFluidId) {
      return selfFluidId == otherFluidId || otherFluidId == this.getSupportedById();
   }

   public boolean canDemote() {
      return this.canDemote;
   }

   public interface Accessor {
      @Nullable
      FluidSection getFluidSection(int var1, int var2, int var3);

      @Nullable
      default FluidSection getFluidSectionByBlock(int bx, int by, int bz) {
         return this.getFluidSection(ChunkUtil.chunkCoordinate(bx), ChunkUtil.chunkCoordinate(by), ChunkUtil.chunkCoordinate(bz));
      }

      @Nullable
      BlockSection getBlockSection(int var1, int var2, int var3);

      @Nullable
      default BlockSection getBlockSectionByBlock(int bx, int by, int bz) {
         return this.getBlockSection(ChunkUtil.chunkCoordinate(bx), ChunkUtil.chunkCoordinate(by), ChunkUtil.chunkCoordinate(bz));
      }

      @Deprecated(forRemoval = true)
      void setBlock(int var1, int var2, int var3, int var4);
   }

   public static enum AliveStatus {
      ALIVE,
      DEMOTE,
      WAIT_FOR_ADJACENT_CHUNK;
   }

   public static class CachedAccessor extends AbstractCachedAccessor implements FluidTicker.Accessor {
      private static final ThreadLocal<FluidTicker.CachedAccessor> THREAD_LOCAL = ThreadLocal.withInitial(FluidTicker.CachedAccessor::new);
      private static final int FLUID_COMPONENT = 0;
      private static final int BLOCK_COMPONENT = 1;
      private CommandBuffer<ChunkStore> commandBuffer;
      public FluidSection selfFluidSection;
      public BlockSection selfBlockSection;

      protected CachedAccessor() {
         super(2);
      }

      @Nonnull
      public static FluidTicker.CachedAccessor of(CommandBuffer<ChunkStore> commandBuffer, @Nonnull FluidSection section, BlockSection blockSection, int radius) {
         FluidTicker.CachedAccessor accessor = THREAD_LOCAL.get();
         accessor.init(commandBuffer, section, blockSection, radius);
         accessor.insertSectionComponent(0, section, section.getX(), section.getY(), section.getZ());
         accessor.insertSectionComponent(1, blockSection, section.getX(), section.getY(), section.getZ());
         return accessor;
      }

      private void init(CommandBuffer<ChunkStore> commandBuffer, @Nonnull FluidSection section, BlockSection blockSection, int radius) {
         this.init(commandBuffer, section.getX(), section.getY(), section.getZ(), radius);
         this.commandBuffer = commandBuffer;
         this.selfFluidSection = section;
         this.selfBlockSection = blockSection;
      }

      @Override
      public FluidSection getFluidSection(int cx, int cy, int cz) {
         return this.getComponentSection(cx, cy, cz, 0, FluidSection.getComponentType());
      }

      @Override
      public BlockSection getBlockSection(int cx, int cy, int cz) {
         return this.getComponentSection(cx, cy, cz, 1, BlockSection.getComponentType());
      }

      @Override
      public void setBlock(int x, int y, int z, int blockId) {
         Ref<ChunkStore> chunk = this.getChunk(ChunkUtil.chunkCoordinate(x), ChunkUtil.chunkCoordinate(z));
         if (chunk != null && chunk.isValid()) {
            this.commandBuffer.run(store -> {
               if (chunk.isValid()) {
                  WorldChunk wc = store.getComponent(chunk, WorldChunk.getComponentType());
                  if (wc != null) {
                     wc.setBlock(x, y, z, blockId);
                  }
               }
            });
         }
      }
   }
}
