package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.Optional;
import javax.annotation.Nullable;

public abstract class ConnectedBlockRuleSet {
   public static final CodecMapCodec<ConnectedBlockRuleSet> CODEC = new CodecMapCodec<>("Type");

   public abstract boolean onlyUpdateOnPlacement();

   public abstract Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockType(
      World var1, Vector3i var2, BlockType var3, int var4, Vector3i var5, boolean var6
   );

   public void updateCachedBlockTypes(BlockType blockType, BlockTypeAssetMap<String, BlockType> assetMap) {
   }

   @Nullable
   public com.hypixel.hytale.protocol.ConnectedBlockRuleSet toPacket(BlockTypeAssetMap<String, BlockType> assetMap) {
      return null;
   }
}
