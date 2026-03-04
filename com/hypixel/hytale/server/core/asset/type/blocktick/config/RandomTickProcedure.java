package com.hypixel.hytale.server.core.asset.type.blocktick.config;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public interface RandomTickProcedure {
   CodecMapCodec<RandomTickProcedure> CODEC = new CodecMapCodec<>("Type");

   void onRandomTick(Store<ChunkStore> var1, CommandBuffer<ChunkStore> var2, BlockSection var3, int var4, int var5, int var6, int var7, BlockType var8);
}
