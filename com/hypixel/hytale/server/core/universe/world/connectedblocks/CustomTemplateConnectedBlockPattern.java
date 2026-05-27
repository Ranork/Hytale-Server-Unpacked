package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.joml.Vector3ic;

public abstract class CustomTemplateConnectedBlockPattern {
   @Nonnull
   public static final CodecMapCodec<CustomTemplateConnectedBlockPattern> CODEC = new CodecMapCodec<>("Type");

   @Nonnull
   public abstract Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockTypeKey(
      @Nonnull String var1,
      @Nonnull ChunkStore var2,
      @Nonnull Vector3ic var3,
      @Nonnull CustomTemplateConnectedBlockRuleSet var4,
      @Nonnull BlockType var5,
      int var6,
      @Nonnull Vector3ic var7,
      boolean var8
   );
}
