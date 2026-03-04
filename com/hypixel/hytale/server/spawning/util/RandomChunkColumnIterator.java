package com.hypixel.hytale.server.spawning.util;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RandomChunkColumnIterator {
   @Nonnull
   private final ChunkColumnMask availablePositions = new ChunkColumnMask();
   @Nullable
   private final ChunkColumnMask initialPositions;
   @Nonnull
   private final Random random = new Random();
   private final long seed;
   private int currentIndex;
   private int lastSavedIteratorPosition;

   public RandomChunkColumnIterator() {
      this.initialPositions = null;
      this.seed = this.random.nextLong();
   }

   public RandomChunkColumnIterator(@Nonnull ChunkColumnMask initialPositions) {
      this.initialPositions = initialPositions;
      if (initialPositions.isEmpty()) {
         throw new IllegalArgumentException();
      } else {
         this.seed = this.random.nextLong();
      }
   }

   public RandomChunkColumnIterator(ChunkColumnMask initialPositions, @Nonnull WorldChunk chunk) {
      this.initialPositions = initialPositions;
      this.seed = (chunk.getX() * 151L + chunk.getZ()) * 131L;
   }

   public int getCurrentIndex() {
      return this.currentIndex;
   }

   public int getCurrentX() {
      return ChunkUtil.xFromColumn(this.currentIndex);
   }

   public int getCurrentZ() {
      return ChunkUtil.zFromColumn(this.currentIndex);
   }

   @Nullable
   public ChunkColumnMask getInitialPositions() {
      return this.initialPositions;
   }

   public int nextPosition() {
      if (this.availablePositions.isEmpty()) {
         this.reset();
      }

      int start = this.random.nextInt(1024);
      int index = this.availablePositions.nextSetBit(start);
      if (index == -1) {
         index = this.availablePositions.previousSetBit(start);
      }

      return this.nextPosition(index);
   }

   public int nextPositionAvoidBorders() {
      if (this.availablePositions.isEmpty()) {
         this.reset();
      }

      int index = this.random.nextInt(1024);
      int start;
      int end;
      if (this.availablePositions.get(index)) {
         start = this.availablePositions.previousClearBit(index) + 1;
         end = this.availablePositions.nextClearBit(index) - 1;
      } else {
         end = this.availablePositions.previousSetBit(index);
         start = this.availablePositions.nextSetBit(index);
         if (end != -1 && (start == -1 || index - end <= start - index)) {
            start = this.availablePositions.previousClearBit(end) + 1;
         } else {
            end = this.availablePositions.nextClearBit(start) - 1;
         }
      }

      int range = end - start + 1;
      if (range > 3) {
         start += 1 + this.random.nextInt(range - 2);
      } else if (range > 1) {
         start += this.random.nextInt(range);
      }

      if (!this.availablePositions.get(start)) {
         throw new IllegalArgumentException();
      } else {
         return this.nextPosition(start);
      }
   }

   public void saveIteratorPosition() {
      this.lastSavedIteratorPosition = this.positionsLeft();
   }

   public boolean isAtSavedIteratorPosition() {
      return this.positionsLeft() == this.lastSavedIteratorPosition;
   }

   public int positionsLeft() {
      return this.availablePositions.cardinality();
   }

   public void markPositionVisited(int index) {
      this.availablePositions.clear(index);
   }

   public void markPositionVisited() {
      this.markPositionVisited(this.currentIndex);
   }

   private void reset() {
      this.random.setSeed(this.seed);
      if (this.initialPositions == null) {
         this.availablePositions.set();
      } else {
         if (this.initialPositions.isEmpty()) {
            throw new IllegalArgumentException();
         }

         this.availablePositions.copyFrom(this.initialPositions);
      }
   }

   private int nextPosition(int index) {
      if (index == -1) {
         throw new IllegalArgumentException();
      } else {
         this.markPositionVisited(index);
         return this.currentIndex = index;
      }
   }
}
