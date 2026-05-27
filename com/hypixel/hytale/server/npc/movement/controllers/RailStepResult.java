package com.hypixel.hytale.server.npc.movement.controllers;

import java.util.Arrays;
import javax.annotation.Nonnull;

public final class RailStepResult {
   private static final BlockHit[] BLOCK_HITS = new BlockHit[0];
   private static final EntityHit[] ENTITY_HITS = new EntityHit[0];
   public boolean obstructed;
   public double appliedFraction;
   @Nonnull
   private BlockHit[] passThroughBlocks = BLOCK_HITS;
   private int passThroughCount;
   @Nonnull
   private EntityHit[] entityHits = ENTITY_HITS;
   private int entityHitCount;

   public void reset() {
      this.obstructed = false;
      this.appliedFraction = 0.0;

      for (int i = 0; i < this.entityHitCount; i++) {
         this.entityHits[i].clearReferences();
      }

      this.passThroughCount = 0;
      this.entityHitCount = 0;
   }

   public int getPassThroughCount() {
      return this.passThroughCount;
   }

   @Nonnull
   public BlockHit getPassThrough(int i) {
      if (i >= 0 && i < this.passThroughCount) {
         return this.passThroughBlocks[i];
      } else {
         throw new IndexOutOfBoundsException("pass-through index " + i + " out of bounds for size " + this.passThroughCount);
      }
   }

   @Nonnull
   public BlockHit nextPassThrough() {
      if (this.passThroughCount >= this.passThroughBlocks.length) {
         int newSize = this.passThroughBlocks.length == 0 ? 4 : this.passThroughBlocks.length * 2;
         BlockHit[] grown = Arrays.copyOf(this.passThroughBlocks, newSize);

         for (int i = this.passThroughBlocks.length; i < newSize; i++) {
            grown[i] = new BlockHit();
         }

         this.passThroughBlocks = grown;
      }

      return this.passThroughBlocks[this.passThroughCount++];
   }

   public int getEntityHitCount() {
      return this.entityHitCount;
   }

   @Nonnull
   public EntityHit getEntityHit(int i) {
      if (i >= 0 && i < this.entityHitCount) {
         return this.entityHits[i];
      } else {
         throw new IndexOutOfBoundsException("entity-hit index " + i + " out of bounds for size " + this.entityHitCount);
      }
   }

   @Nonnull
   public EntityHit nextEntityHit() {
      if (this.entityHitCount >= this.entityHits.length) {
         int newSize = this.entityHits.length == 0 ? 4 : this.entityHits.length * 2;
         EntityHit[] grown = Arrays.copyOf(this.entityHits, newSize);

         for (int i = this.entityHits.length; i < newSize; i++) {
            grown[i] = new EntityHit();
         }

         this.entityHits = grown;
      }

      return this.entityHits[this.entityHitCount++];
   }
}
