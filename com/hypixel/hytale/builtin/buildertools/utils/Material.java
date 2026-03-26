package com.hypixel.hytale.builtin.buildertools.utils;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Material {
   public static final Material EMPTY = new Material(0, 0, (byte)0, 0, 0, 0, null);
   private final int blockId;
   private final int fluidId;
   private final byte fluidLevel;
   private final int rotation;
   private final int support;
   private final int filler;
   @Nullable
   private final Holder<ChunkStore> holder;

   private Material(int blockId, int fluidId, byte fluidLevel, int rotation, int support, int filler, @Nullable Holder<ChunkStore> holder) {
      this.blockId = blockId;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
      this.rotation = rotation;
      this.support = support;
      this.filler = filler;
      this.holder = holder;
   }

   @Nonnull
   public static Material block(int blockId) {
      return block(blockId, 0);
   }

   @Nonnull
   public static Material block(int blockId, int rotation) {
      return blockId == 0 ? EMPTY : new Material(blockId, 0, (byte)0, rotation, 0, 0, null);
   }

   @Nonnull
   public static Material fluid(int fluidId, byte fluidLevel) {
      return fluidId == 0 ? EMPTY : new Material(0, fluidId, fluidLevel, 0, 0, 0, null);
   }

   @Nonnull
   public static Material full(int blockId, int rotation, int support, int filler, @Nullable Holder<ChunkStore> holder) {
      return blockId == 0 ? EMPTY : new Material(blockId, 0, (byte)0, rotation, support, filler, holder);
   }

   @Nonnull
   public static Material full(int blockId, int rotation, int support, int filler, @Nullable Holder<ChunkStore> holder, int fluidId, byte fluidLevel) {
      return new Material(blockId, fluidId, fluidLevel, rotation, support, filler, holder);
   }

   @Nullable
   public static Material fromKey(@Nonnull String key) {
      if (key.equalsIgnoreCase("empty")) {
         return EMPTY;
      } else {
         BlockPattern.BlockEntry blockEntry = BlockPattern.tryParseBlockTypeKey(key);
         if (blockEntry != null) {
            FluidPatternHelper.FluidInfo fluidInfo = FluidPatternHelper.getFluidInfo(blockEntry.blockTypeKey());
            if (fluidInfo != null) {
               return fluid(fluidInfo.fluidId(), fluidInfo.fluidLevel());
            }

            int blockId = BlockType.getAssetMap().getIndex(blockEntry.blockTypeKey());
            if (blockId != Integer.MIN_VALUE) {
               return block(blockId, blockEntry.rotation());
            }
         }

         FluidPatternHelper.FluidInfo fluidInfox = FluidPatternHelper.getFluidInfo(key);
         if (fluidInfox != null) {
            return fluid(fluidInfox.fluidId(), fluidInfox.fluidLevel());
         } else {
            int blockId = BlockType.getAssetMap().getIndex(key);
            return blockId != Integer.MIN_VALUE ? block(blockId) : null;
         }
      }
   }

   public boolean isFluid() {
      return this.fluidId != 0;
   }

   public boolean isBlock() {
      return this.blockId != 0 && this.fluidId == 0;
   }

   public boolean isEmpty() {
      return this.blockId == 0 && this.fluidId == 0;
   }

   public int getBlockId() {
      return this.blockId;
   }

   public int getFluidId() {
      return this.fluidId;
   }

   public byte getFluidLevel() {
      return this.fluidLevel;
   }

   public int getRotation() {
      return this.rotation;
   }

   public boolean hasRotation() {
      return this.rotation != 0;
   }

   public int getSupport() {
      return this.support;
   }

   public int getFiller() {
      return this.filler;
   }

   @Nullable
   public Holder<ChunkStore> getHolder() {
      return this.holder;
   }

   @Override
   public String toString() {
      if (this.isEmpty()) {
         return "Material[empty]";
      } else if (this.isFluid()) {
         Fluid fluid = Fluid.getAssetMap().getAsset(this.fluidId);
         return "Material[fluid=" + (fluid != null ? fluid.getId() : this.fluidId) + ", level=" + this.fluidLevel + "]";
      } else {
         BlockType block = BlockType.getAssetMap().getAsset(this.blockId);
         String rotStr = this.hasRotation() ? ", rotation=" + RotationTuple.get(this.rotation) : "";
         return "Material[block=" + (block != null ? block.getId() : this.blockId) + rotStr + "]";
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Material other)
            ? false
            : this.blockId == other.blockId
               && this.fluidId == other.fluidId
               && this.fluidLevel == other.fluidLevel
               && this.rotation == other.rotation
               && this.support == other.support
               && this.filler == other.filler
               && Objects.equals(this.holder, other.holder);
      }
   }

   @Override
   public int hashCode() {
      int result = 31 * (31 * (31 * this.blockId + this.fluidId) + this.fluidLevel) + this.rotation;
      return 31 * (31 * (31 * result + this.support) + this.filler) + Objects.hashCode(this.holder);
   }

   @Nonnull
   public static Material fromPattern(@Nonnull BlockPattern pattern, @Nonnull Random random) {
      BlockPattern.BlockEntry blockEntry = pattern.nextBlockTypeKey(random);
      if (blockEntry != null) {
         FluidPatternHelper.FluidInfo fluidInfo = FluidPatternHelper.getFluidInfo(blockEntry.blockTypeKey());
         if (fluidInfo != null) {
            return fluid(fluidInfo.fluidId(), fluidInfo.fluidLevel());
         }

         int blockId = BlockType.getAssetMap().getIndex(blockEntry.blockTypeKey());
         if (blockId != Integer.MIN_VALUE) {
            return block(blockId, blockEntry.rotation());
         }
      }

      return block(pattern.nextBlock(random));
   }
}
