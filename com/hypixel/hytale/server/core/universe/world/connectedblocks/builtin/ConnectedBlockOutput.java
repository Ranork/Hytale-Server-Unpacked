package com.hypixel.hytale.server.core.universe.world.connectedblocks.builtin;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public class ConnectedBlockOutput {
   public static final BuilderCodec<ConnectedBlockOutput> CODEC = BuilderCodec.builder(ConnectedBlockOutput.class, ConnectedBlockOutput::new)
      .append(new KeyedCodec<>("State", Codec.STRING), (output, state) -> output.state = state, output -> output.state)
      .documentation("An optional state definition to apply to the base block type")
      .add()
      .<String>append(new KeyedCodec<>("Block", Codec.STRING), (output, blockTypeKey) -> output.blockTypeKey = blockTypeKey, output -> output.blockTypeKey)
      .documentation("An optional block ID to use instead of the base block type")
      .add()
      .build();
   protected String state;
   protected String blockTypeKey;

   protected ConnectedBlockOutput() {
   }

   public int resolve(BlockType baseBlockType, BlockTypeAssetMap<String, BlockType> assetMap) {
      String blockTypeKey = this.blockTypeKey;
      if (blockTypeKey == null) {
         blockTypeKey = baseBlockType.getId();
      }

      BlockType blockType = assetMap.getAsset(blockTypeKey);
      if (blockType == null) {
         return -1;
      } else {
         if (this.state != null) {
            String baseKey = blockType.getDefaultStateKey();
            BlockType baseBlock = baseKey == null ? blockType : BlockType.getAssetMap().getAsset(baseKey);
            if ("default".equals(this.state)) {
               blockTypeKey = baseBlock.getId();
            } else {
               blockTypeKey = baseBlock.getBlockKeyForState(this.state);
            }

            if (blockTypeKey == null) {
               return -1;
            }
         }

         int index = assetMap.getIndex(blockTypeKey);
         if (index == Integer.MIN_VALUE) {
            return -1;
         } else {
            this.blockTypeKey = blockTypeKey;
            return index;
         }
      }
   }
}
