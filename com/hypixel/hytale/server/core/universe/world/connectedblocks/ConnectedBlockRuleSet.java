package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3ic;

public abstract class ConnectedBlockRuleSet {
   @Nonnull
   public static final CodecMapCodec<ConnectedBlockRuleSet> CODEC = new CodecMapCodec<>("Type");

   public abstract boolean onlyUpdateOnPlacement();

   public abstract Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockType(
      @Nonnull ChunkStore var1, @Nonnull Vector3ic var2, @Nonnull BlockType var3, int var4, @Nonnull Vector3ic var5, boolean var6
   );

   public void updateCachedBlockTypes(@Nonnull BlockType blockType, @Nonnull BlockTypeAssetMap<String, BlockType> assetMap) {
   }

   @Nullable
   public com.hypixel.hytale.protocol.ConnectedBlockRuleSet toPacket(@Nonnull BlockTypeAssetMap<String, BlockType> assetMap) {
      return null;
   }
}
