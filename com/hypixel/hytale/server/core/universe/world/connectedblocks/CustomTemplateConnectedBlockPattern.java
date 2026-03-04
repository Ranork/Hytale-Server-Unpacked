package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.Optional;
import javax.annotation.Nonnull;

public abstract class CustomTemplateConnectedBlockPattern {
   public static final CodecMapCodec<CustomTemplateConnectedBlockPattern> CODEC = new CodecMapCodec<>("Type");

   public abstract Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockTypeKey(
      String var1,
      @Nonnull World var2,
      @Nonnull Vector3i var3,
      @Nonnull CustomTemplateConnectedBlockRuleSet var4,
      @Nonnull BlockType var5,
      int var6,
      @Nonnull Vector3i var7,
      boolean var8
   );
}
