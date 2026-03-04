package com.hypixel.hytale.server.core.universe.world.chunk.section;

import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;

public class ChunkSectionReference {
   private BlockChunk chunk;
   private BlockSection section;
   private int sectionIndex;

   public ChunkSectionReference(BlockChunk chunk, BlockSection section, int sectionIndex) {
      this.section = section;
      this.chunk = chunk;
      this.sectionIndex = sectionIndex;
   }

   public BlockChunk getChunk() {
      return this.chunk;
   }

   public BlockSection getSection() {
      return this.section;
   }

   public int getSectionIndex() {
      return this.sectionIndex;
   }
}
