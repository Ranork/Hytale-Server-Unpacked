package com.hypixel.hytale.builtin.randomtick;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.RandomTickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RandomTickSystem extends EntityTickingSystem<ChunkStore> {
   private final ComponentType<ChunkStore, BlockSection> blockSelectionComponentType = BlockSection.getComponentType();
   private final ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType = ChunkSection.getComponentType();
   private final Query<ChunkStore> query = Query.and(this.blockSelectionComponentType, this.chunkSectionComponentType);

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
      @Nonnull Store<ChunkStore> store,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      BlockSection blockSection = archetypeChunk.getComponent(index, this.blockSelectionComponentType);

      assert blockSection != null;

      if (!blockSection.isSolidAir()) {
         ChunkSection chunkSection = archetypeChunk.getComponent(index, this.chunkSectionComponentType);

         assert chunkSection != null;

         RandomTick config = commandBuffer.getResource(RandomTick.getResourceType());
         World world = store.getExternalData().getWorld();
         int interval = 32768 / config.getBlocksPerSectionPerTickStable();
         long baseSeed = HashUtil.hash(world.getTick() / interval, chunkSection.getX(), chunkSection.getY(), chunkSection.getZ());
         long randomSeed = (baseSeed << 1 | 1L) & 32767L;
         long randomSeed2 = baseSeed >> 16 & 32767L;
         long startIndex = world.getTick() % interval * config.getBlocksPerSectionPerTickStable();
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();

         for (int i = 0; i < config.getBlocksPerSectionPerTickStable(); i++) {
            int blockIndex = (int)((startIndex + i) * randomSeed + randomSeed2 & 32767L);
            int localX = ChunkUtil.xFromIndex(blockIndex);
            int localY = ChunkUtil.yFromIndex(blockIndex);
            int localZ = ChunkUtil.zFromIndex(blockIndex);
            int blockId = blockSection.get(blockIndex);
            if (blockId != 0) {
               BlockType blockType = assetMap.getAsset(blockId);
               if (blockType != null) {
                  RandomTickProcedure randomTickProcedure = blockType.getRandomTickProcedure();
                  if (randomTickProcedure != null) {
                     randomTickProcedure.onRandomTick(
                        store,
                        commandBuffer,
                        blockSection,
                        ChunkUtil.worldCoordFromLocalCoord(chunkSection.getX(), localX),
                        ChunkUtil.worldCoordFromLocalCoord(chunkSection.getY(), localY),
                        ChunkUtil.worldCoordFromLocalCoord(chunkSection.getZ(), localZ),
                        blockId,
                        blockType
                     );
                  }
               }
            }
         }

         Random rng = config.getRandom();

         for (int ix = 0; ix < config.getBlocksPerSectionPerTickUnstable(); ix++) {
            int blockIndex = rng.nextInt(32768);
            int localX = ChunkUtil.xFromIndex(blockIndex);
            int localY = ChunkUtil.yFromIndex(blockIndex);
            int localZ = ChunkUtil.zFromIndex(blockIndex);
            int blockId = blockSection.get(blockIndex);
            if (blockId != 0) {
               BlockType blockType = assetMap.getAsset(blockId);
               if (blockType != null) {
                  RandomTickProcedure randomTickProcedure = blockType.getRandomTickProcedure();
                  if (randomTickProcedure != null) {
                     randomTickProcedure.onRandomTick(
                        store,
                        commandBuffer,
                        blockSection,
                        ChunkUtil.worldCoordFromLocalCoord(chunkSection.getX(), localX),
                        ChunkUtil.worldCoordFromLocalCoord(chunkSection.getY(), localY),
                        ChunkUtil.worldCoordFromLocalCoord(chunkSection.getZ(), localZ),
                        blockId,
                        blockType
                     );
                  }
               }
            }
         }
      }
   }

   @Nullable
   @Override
   public Query<ChunkStore> getQuery() {
      return this.query;
   }
}
