package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockFilter;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class ReplaceBlockTypeEffect extends TriggerEffect {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<ReplaceBlockTypeEffect> CODEC = BuilderCodec.builder(ReplaceBlockTypeEffect.class, ReplaceBlockTypeEffect::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("FromBlockTypes", Codec.STRING_ARRAY),
         (effect, fromBlockTypes) -> effect.fromBlockTypes = fromBlockTypes != null ? fromBlockTypes : new String[0],
         effect -> effect.fromBlockTypes.length == 0 ? null : effect.fromBlockTypes
      )
      .add()
      .append(new KeyedCodec<>("ToBlockType", Codec.STRING), (effect, toBlockType) -> effect.toBlockType = toBlockType, effect -> effect.toBlockType)
      .add()
      .append(
         new KeyedCodec<>("Bounds", new EnumCodec<>(ReplaceBlockTypeEffect.Bounds.class), false),
         (effect, bounds) -> effect.bounds = bounds != null ? bounds : ReplaceBlockTypeEffect.Bounds.SHAPE,
         effect -> effect.bounds
      )
      .add()
      .append(new KeyedCodec<>("X", Codec.FLOAT, false), (effect, sizeX) -> effect.sizeX = sizeX != null ? sizeX : 0.0F, effect -> effect.sizeX)
      .add()
      .append(new KeyedCodec<>("Y", Codec.FLOAT, false), (effect, sizeY) -> effect.sizeY = sizeY != null ? sizeY : 0.0F, effect -> effect.sizeY)
      .add()
      .append(new KeyedCodec<>("Z", Codec.FLOAT, false), (effect, sizeZ) -> effect.sizeZ = sizeZ != null ? sizeZ : 0.0F, effect -> effect.sizeZ)
      .add()
      .append(
         new KeyedCodec<>("Offset", Vector3dUtil.CODEC, false),
         (effect, offset) -> effect.offset = offset != null ? offset : new Vector3d(),
         effect -> effect.offset
      )
      .add()
      .build();
   @Nonnull
   private String[] fromBlockTypes = new String[0];
   @Nullable
   private String toBlockType;
   @Nonnull
   private ReplaceBlockTypeEffect.Bounds bounds = ReplaceBlockTypeEffect.Bounds.SHAPE;
   private float sizeX;
   private float sizeY;
   private float sizeZ;
   @Nonnull
   private Vector3d offset = new Vector3d();

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.fromBlockTypes.length != 0 && this.toBlockType != null && !this.toBlockType.isBlank()) {
         World world = context.getStore().getExternalData().getWorld();
         if (world != null) {
            ReplaceBlockTypeEffect.TargetType toTarget = this.resolveToTarget();
            if (toTarget != null) {
               BlockFilter.BlocksAndFluids fromTypes = BlockFilter.parseBlocksAndFluids(this.fromBlockTypes);
               IntSet fromFluids = fromTypes.fluids();
               if (!fromTypes.blocks().isEmpty() || fromFluids != null && !fromFluids.isEmpty()) {
                  Vector3d min = new Vector3d();
                  Vector3d max = new Vector3d();
                  Vector3d blockCenter = new Vector3d();

                  for (VolumeEntry volume : context.getSpatialVolumes()) {
                     TriggerVolumeShape shape = volume.getShape();
                     Vector3d origin = volume.getPosition();
                     if (this.bounds == ReplaceBlockTypeEffect.Bounds.AABB) {
                        this.getConfiguredAABB(origin, min, max);
                     } else {
                        shape.getWorldAABB(origin, min, max);
                     }

                     int minBlockX = MathUtil.floor(min.x());
                     int minBlockY = MathUtil.floor(min.y());
                     int minBlockZ = MathUtil.floor(min.z());
                     int maxBlockX = MathUtil.floor(max.x());
                     int maxBlockY = MathUtil.floor(max.y());
                     int maxBlockZ = MathUtil.floor(max.z());

                     for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
                        for (int blockY = minBlockY; blockY <= maxBlockY; blockY++) {
                           for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
                              if (this.containsBlock(shape, origin, min, max, blockCenter, blockX, blockY, blockZ)) {
                                 WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
                                 if (chunk != null) {
                                    int currentBlockId = chunk.getBlock(blockX, blockY, blockZ);
                                    int currentFluidId = chunk.getFluidId(blockX, blockY, blockZ);
                                    boolean fluidMatched = fromFluids != null && fromFluids.contains(currentFluidId);
                                    if ((chunk.getFiller(blockX, blockY, blockZ) == 0 || fluidMatched)
                                       && (fromTypes.blocks().contains(currentBlockId) || fluidMatched)) {
                                       this.replaceMatchedCell(world, chunk, blockX, blockY, blockZ, toTarget);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   private ReplaceBlockTypeEffect.TargetType resolveToTarget() {
      BlockFilter.BlocksAndFluids resolvedTypes = BlockFilter.parseBlocksAndFluids(new String[]{this.toBlockType});
      if (resolvedTypes.hasInvalidBlocks()) {
         LOGGER.at(Level.FINE).log("ReplaceBlockType: unknown ToBlockType '%s'", this.toBlockType);
         return null;
      } else {
         int blockCount = resolvedTypes.blocks().size();
         int fluidCount = resolvedTypes.fluids() != null ? resolvedTypes.fluids().size() : 0;
         if (blockCount + fluidCount != 1) {
            LOGGER.at(Level.FINE).log("ReplaceBlockType: ToBlockType '%s' resolves to %d blocks and %d fluids", this.toBlockType, blockCount, fluidCount);
            return null;
         } else if (blockCount == 1) {
            IntIterator blockIterator = resolvedTypes.blocks().iterator();
            return new ReplaceBlockTypeEffect.TargetType(blockIterator.nextInt(), 0);
         } else {
            IntIterator fluidIterator = resolvedTypes.fluids().iterator();
            return new ReplaceBlockTypeEffect.TargetType(0, fluidIterator.nextInt());
         }
      }
   }

   private void replaceMatchedCell(
      @Nonnull World world, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ, @Nonnull ReplaceBlockTypeEffect.TargetType toTarget
   ) {
      boolean isFiller = chunk.getFiller(blockX, blockY, blockZ) != 0;
      if (toTarget.fluidId() != 0) {
         if (!isFiller) {
            chunk.setBlock(blockX, blockY, blockZ, 0, BlockType.EMPTY, 0, 0, 256);
         }

         setFluid(world, chunk, blockX, blockY, blockZ, toTarget.fluidId());
      } else {
         clearFluid(world, chunk, blockX, blockY, blockZ);
         if (!isFiller) {
            BlockType toBlockTypeAsset = BlockType.getAssetMap().getAsset(toTarget.blockId());
            if (toBlockTypeAsset != null) {
               int rotation = chunk.getRotationIndex(blockX, blockY, blockZ);
               chunk.setBlock(blockX, blockY, blockZ, toTarget.blockId(), toBlockTypeAsset, rotation, 0, 256);
            }
         }
      }
   }

   private static void setFluid(@Nonnull World world, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ, int fluidId) {
      Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
      if (fluid != null) {
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         ChunkColumn column = chunkStore.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
         if (column != null) {
            Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(blockY));
            FluidSection fluidSection = chunkStore.ensureAndGetComponent(section, FluidSection.getComponentType());
            fluidSection.setFluid(blockX, blockY, blockZ, fluidId, (byte)fluid.getMaxFluidLevel());
         }
      }
   }

   private static void clearFluid(@Nonnull World world, @Nonnull WorldChunk chunk, int blockX, int blockY, int blockZ) {
      if (chunk.getFluidId(blockX, blockY, blockZ) != 0) {
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         ChunkColumn column = chunkStore.getComponent(chunk.getReference(), ChunkColumn.getComponentType());
         if (column != null) {
            Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(blockY));
            FluidSection fluidSection = chunkStore.ensureAndGetComponent(section, FluidSection.getComponentType());
            fluidSection.setFluid(blockX, blockY, blockZ, 0, (byte)0);
         }
      }
   }

   private boolean containsBlock(
      @Nonnull TriggerVolumeShape shape,
      @Nonnull Vector3d origin,
      @Nonnull Vector3d min,
      @Nonnull Vector3d max,
      @Nonnull Vector3d blockCenter,
      int blockX,
      int blockY,
      int blockZ
   ) {
      blockCenter.set(blockX + 0.5, blockY + 0.5, blockZ + 0.5);
      return this.bounds != ReplaceBlockTypeEffect.Bounds.AABB
         ? shape.contains(origin, blockCenter)
         : blockCenter.x() >= min.x()
            && blockCenter.x() <= max.x()
            && blockCenter.y() >= min.y()
            && blockCenter.y() <= max.y()
            && blockCenter.z() >= min.z()
            && blockCenter.z() <= max.z();
   }

   private void getConfiguredAABB(@Nonnull Vector3d origin, @Nonnull Vector3d outMin, @Nonnull Vector3d outMax) {
      Vector3d configuredOffset = this.offset != null ? this.offset : new Vector3d();
      outMin.set(origin).add(configuredOffset);
      outMax.set(outMin).add(this.sizeX, this.sizeY, this.sizeZ);
      double minX = Math.min(outMin.x(), outMax.x());
      double minY = Math.min(outMin.y(), outMax.y());
      double minZ = Math.min(outMin.z(), outMax.z());
      double maxX = Math.max(outMin.x(), outMax.x());
      double maxY = Math.max(outMin.y(), outMax.y());
      double maxZ = Math.max(outMin.z(), outMax.z());
      outMin.set(minX, minY, minZ);
      outMax.set(maxX, maxY, maxZ);
   }

   public static enum Bounds {
      SHAPE,
      AABB;
   }

   private record TargetType(int blockId, int fluidId) {
   }
}
