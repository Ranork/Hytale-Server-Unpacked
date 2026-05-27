package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockFilter;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class PlaceBlockEffect extends TriggerEffect {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<PlaceBlockEffect> CODEC = BuilderCodec.builder(PlaceBlockEffect.class, PlaceBlockEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("BlockType", Codec.STRING), (effect, blockType) -> effect.blockType = blockType, effect -> effect.blockType)
      .add()
      .append(new KeyedCodec<>("Position", Vector3dUtil.CODEC, false), (effect, position) -> effect.position = position, effect -> effect.position)
      .add()
      .append(
         new KeyedCodec<>("Origin", new EnumCodec<>(PlaceBlockEffect.Origin.class), false), (effect, origin) -> effect.origin = origin, effect -> effect.origin
      )
      .add()
      .append(
         new KeyedCodec<>("ReplaceMode", new EnumCodec<>(PlaceBlockEffect.ReplaceMode.class), false),
         (effect, replaceMode) -> effect.replaceMode = replaceMode,
         effect -> effect.replaceMode
      )
      .add()
      .build();
   @Nullable
   private String blockType;
   @Nonnull
   private Vector3d position = new Vector3d();
   @Nonnull
   private PlaceBlockEffect.Origin origin = PlaceBlockEffect.Origin.VOLUME_ORIGIN;
   @Nonnull
   private PlaceBlockEffect.ReplaceMode replaceMode = PlaceBlockEffect.ReplaceMode.ALWAYS;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.blockType != null && !this.blockType.isBlank()) {
         World world = context.getStore().getExternalData().getWorld();
         if (world != null) {
            PlaceBlockEffect.TargetType targetType = resolveTargetType(this.blockType);
            if (targetType != null) {
               Vector3d target = this.resolveTargetPosition(context);
               int blockX = MathUtil.floor(target.x());
               int blockY = MathUtil.floor(target.y());
               int blockZ = MathUtil.floor(target.z());
               WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
               if (chunk != null) {
                  if (this.replaceMode != PlaceBlockEffect.ReplaceMode.ONLY_AIR || chunk.getBlock(blockX, blockY, blockZ) == 0) {
                     if (targetType.fluidId() != 0) {
                        chunk.setBlock(blockX, blockY, blockZ, 0, BlockType.EMPTY, 0, 0, 256);
                        setFluid(world, chunk, blockX, blockY, blockZ, targetType.fluidId());
                     } else {
                        clearFluid(world, chunk, blockX, blockY, blockZ);
                        BlockType blockTypeAsset = BlockType.getAssetMap().getAsset(targetType.blockId());
                        if (blockTypeAsset != null) {
                           chunk.setBlock(blockX, blockY, blockZ, targetType.blockId(), blockTypeAsset, 0, 0, 256);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static PlaceBlockEffect.TargetType resolveTargetType(@Nonnull String blockType) {
      BlockFilter.BlocksAndFluids resolvedTypes = BlockFilter.parseBlocksAndFluids(new String[]{blockType});
      if (resolvedTypes.hasInvalidBlocks()) {
         return null;
      } else {
         int blockCount = resolvedTypes.blocks().size();
         int fluidCount = resolvedTypes.fluids() != null ? resolvedTypes.fluids().size() : 0;
         if (blockCount + fluidCount != 1) {
            LOGGER.at(Level.FINE).log("PlaceBlockEffect: target '%s' resolves to %d blocks and %d fluids", blockType, blockCount, fluidCount);
            return null;
         } else if (blockCount == 1) {
            IntIterator blockIterator = resolvedTypes.blocks().iterator();
            return new PlaceBlockEffect.TargetType(blockIterator.nextInt(), 0);
         } else {
            IntIterator fluidIterator = resolvedTypes.fluids().iterator();
            return new PlaceBlockEffect.TargetType(0, fluidIterator.nextInt());
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

   @Nonnull
   private Vector3d resolveTargetPosition(@Nonnull TriggerContext context) {
      Vector3d offset = this.position != null ? new Vector3d(this.position) : new Vector3d();

      return switch (this.origin != null ? this.origin : PlaceBlockEffect.Origin.VOLUME_ORIGIN) {
         case VOLUME_ORIGIN -> new Vector3d(context.getVolume().getPosition()).add(offset);
         case ENTITY -> {
            TransformComponent transform = context.getStore().getComponent(context.getEntityRef(), TransformComponent.getComponentType());
            Vector3d base = transform != null ? new Vector3d(transform.getPosition()) : new Vector3d(context.getVolume().getPosition());
            yield base.add(offset);
         }
         case WORLD_ABSOLUTE -> offset;
      };
   }

   public static enum Origin {
      VOLUME_ORIGIN,
      ENTITY,
      WORLD_ABSOLUTE;
   }

   public static enum ReplaceMode {
      ALWAYS,
      ONLY_AIR;
   }

   private record TargetType(int blockId, int fluidId) {
   }
}
