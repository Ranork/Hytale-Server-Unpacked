package com.hypixel.hytale.builtin.portals.utils;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class BlockTypeUtils {
   @Nullable
   public static BlockType getBlockForState(@Nonnull BlockType blockType, @Nonnull String state) {
      String baseKey = blockType.getDefaultStateKey();
      BlockType baseBlock = baseKey == null ? blockType : BlockType.getAssetMap().getAsset(baseKey);
      if ("default".equals(state)) {
         return baseBlock;
      } else {
         return baseBlock != null ? baseBlock.getBlockForState(state) : null;
      }
   }
}
