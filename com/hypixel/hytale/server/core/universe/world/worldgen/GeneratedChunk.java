package com.hypixel.hytale.server.core.universe.world.worldgen;

import com.hypixel.hytale.common.collection.Flags;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class GeneratedChunk {
   private final GeneratedBlockChunk generatedBlockChunk;
   private final GeneratedBlockStateChunk generatedBlockStateChunk;
   private final GeneratedEntityChunk generatedEntityChunk;
   private final Holder<ChunkStore>[] sections;

   public GeneratedChunk() {
      this(new GeneratedBlockChunk(), new GeneratedBlockStateChunk(), new GeneratedEntityChunk(), makeSections());
   }

   public GeneratedChunk(
      GeneratedBlockChunk generatedBlockChunk,
      GeneratedBlockStateChunk generatedBlockStateChunk,
      GeneratedEntityChunk generatedEntityChunk,
      Holder<ChunkStore>[] sections
   ) {
      this.generatedBlockChunk = generatedBlockChunk;
      this.generatedBlockStateChunk = generatedBlockStateChunk;
      this.generatedEntityChunk = generatedEntityChunk;
      this.sections = sections;
   }

   public GeneratedBlockChunk getBlockChunk() {
      return this.generatedBlockChunk;
   }

   public GeneratedBlockStateChunk getBlockStateChunk() {
      return this.generatedBlockStateChunk;
   }

   public GeneratedEntityChunk getEntityChunk() {
      return this.generatedEntityChunk;
   }

   public Holder<ChunkStore>[] getSections() {
      return this.sections;
   }

   @Nonnull
   public Holder<ChunkStore> toWorldChunk(World world) {
      BlockChunk blockChunk = this.generatedBlockChunk.toBlockChunk(this.sections);
      BlockComponentChunk blockComponentChunk = this.generatedBlockStateChunk.toBlockComponentChunk();
      EntityChunk entityChunk = this.generatedEntityChunk.toEntityChunk();
      WorldChunk worldChunk = new WorldChunk(world, new Flags<>(ChunkFlag.NEWLY_GENERATED), blockChunk, blockComponentChunk, entityChunk);
      Holder<ChunkStore> holder = worldChunk.toHolder();
      holder.putComponent(ChunkColumn.getComponentType(), new ChunkColumn(this.sections));
      return holder;
   }

   @Nonnull
   public Holder<ChunkStore> toHolder(World world) {
      return this.toWorldChunk(world);
   }

   @Nonnull
   public static Holder<ChunkStore>[] makeSections() {
      Holder<ChunkStore>[] holders = new Holder[10];

      for (int i = 0; i < holders.length; i++) {
         holders[i] = ChunkStore.REGISTRY.newHolder();
      }

      return holders;
   }
}
