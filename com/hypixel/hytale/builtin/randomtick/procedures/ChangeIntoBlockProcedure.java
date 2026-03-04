package com.hypixel.hytale.builtin.randomtick.procedures;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.RandomTickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ChangeIntoBlockProcedure implements RandomTickProcedure {
   public static final BuilderCodec<ChangeIntoBlockProcedure> CODEC = BuilderCodec.builder(ChangeIntoBlockProcedure.class, ChangeIntoBlockProcedure::new)
      .appendInherited(new KeyedCodec<>("TargetBlock", Codec.STRING), (o, i) -> o.targetBlock = i, o -> o.targetBlock, (o, p) -> o.targetBlock = p.targetBlock)
      .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   private String targetBlock;

   @Override
   public void onRandomTick(
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> commandBuffer,
      BlockSection blockSection,
      int worldX,
      int worldY,
      int worldZ,
      int blockId,
      BlockType blockType
   ) {
      int targetBlockId = BlockType.getAssetMap().getIndex(this.targetBlock);
      if (targetBlockId != Integer.MIN_VALUE) {
         blockSection.set(ChunkUtil.indexBlock(worldX, worldY, worldZ), targetBlockId, 0, 0);
      }
   }
}
