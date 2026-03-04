package com.hypixel.hytale.server.core.modules.interaction.blocktrack;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Object2IntMapCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class BlockCounter implements Resource<ChunkStore> {
   public static final BuilderCodec<BlockCounter> CODEC = BuilderCodec.builder(BlockCounter.class, BlockCounter::new)
      .append(
         new KeyedCodec<>("BlockPlacementCounts", new Object2IntMapCodec<>(Codec.STRING, Object2IntOpenHashMap::new, false)),
         (o, v) -> o.blockPlacementCounts = v,
         o -> o.blockPlacementCounts
      )
      .add()
      .build();
   private Object2IntMap<String> blockPlacementCounts = new Object2IntOpenHashMap();

   public static ResourceType<ChunkStore, BlockCounter> getResourceType() {
      return InteractionModule.get().getBlockCounterResourceType();
   }

   public BlockCounter() {
      this.blockPlacementCounts.defaultReturnValue(0);
   }

   public BlockCounter(Object2IntMap<String> blockPlacementCounts) {
      this();
      this.blockPlacementCounts = blockPlacementCounts;
   }

   public void trackBlock(String blockName) {
      this.blockPlacementCounts.mergeInt(blockName, 1, Integer::sum);
   }

   public void untrackBlock(String blockName) {
      this.blockPlacementCounts.mergeInt(blockName, 0, (left, right) -> left - 1);
   }

   public int getBlockPlacementCount(String blockName) {
      return this.blockPlacementCounts.getInt(blockName);
   }

   @Override
   public Resource<ChunkStore> clone() {
      return new BlockCounter(new Object2IntOpenHashMap(this.blockPlacementCounts));
   }
}
