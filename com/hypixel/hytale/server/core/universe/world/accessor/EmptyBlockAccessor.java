package com.hypixel.hytale.server.core.universe.world.accessor;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nullable;

public class EmptyBlockAccessor implements BlockAccessor {
   public static final EmptyBlockAccessor INSTANCE = new EmptyBlockAccessor();

   @Override
   public int getX() {
      throw new UnsupportedOperationException("Empty block accessor doesn't have a position!");
   }

   @Override
   public int getZ() {
      throw new UnsupportedOperationException("Empty block accessor doesn't have a position!");
   }

   @Override
   public ChunkAccessor getChunkAccessor() {
      throw new UnsupportedOperationException("Empty block accessor doesn't have a chunk accessor!");
   }

   @Override
   public int getBlock(int x, int y, int z) {
      return 0;
   }

   @Override
   public boolean setBlock(int x, int y, int z, int id, BlockType blockType, int rotation, int filler, int settings) {
      return false;
   }

   @Override
   public boolean breakBlock(int x, int y, int z, int filler, int settings) {
      return false;
   }

   @Override
   public boolean testBlocks(int x, int y, int z, BlockType blockTypeToTest, int rotation, TriIntPredicate predicate) {
      return false;
   }

   @Override
   public boolean testBlockTypes(int x, int y, int z, BlockType blockTypeToTest, int rotation, IChunkAccessorSync.TestBlockFunction predicate) {
      return false;
   }

   @Override
   public boolean testPlaceBlock(int x, int y, int z, BlockType blockTypeToTest, int rotation) {
      return false;
   }

   @Override
   public boolean testPlaceBlock(int x, int y, int z, BlockType blockTypeToTest, int rotation, IChunkAccessorSync.TestBlockFunction filter) {
      return false;
   }

   @Override
   public boolean setTicking(int x, int y, int z, boolean ticking) {
      return false;
   }

   @Override
   public boolean isTicking(int x, int y, int z) {
      return false;
   }

   @Nullable
   @Override
   public BlockState getState(int x, int y, int z) {
      return null;
   }

   @Nullable
   @Override
   public Holder<ChunkStore> getBlockComponentHolder(int x, int y, int z) {
      return null;
   }

   @Override
   public void setState(int x, int y, int z, BlockState state, boolean notify) {
   }

   @Override
   public int getFluidId(int x, int y, int z) {
      return 0;
   }

   @Override
   public byte getFluidLevel(int x, int y, int z) {
      return 0;
   }

   @Override
   public int getSupportValue(int x, int y, int z) {
      return 0;
   }

   @Override
   public int getFiller(int x, int y, int z) {
      return 0;
   }

   @Override
   public int getRotationIndex(int x, int y, int z) {
      return 0;
   }
}
