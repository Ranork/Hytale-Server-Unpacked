package com.hypixel.hytale.server.core.asset.type.blocktype.config.fallingblocks;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public abstract class FallingBlockImpact {
   @Nonnull
   public static final CodecMapCodec<FallingBlockImpact> CODEC = new CodecMapCodec<>("Type", true);
   @Nonnull
   public static final BuilderCodec<FallingBlockImpact> BASE_CODEC = BuilderCodec.abstractBuilder(FallingBlockImpact.class).build();

   public abstract void apply(
      @Nonnull WorldChunk var1,
      @Nonnull World var2,
      @Nonnull BlockType var3,
      @Nonnull Vector3d var4,
      @Nonnull RotationTuple var5,
      @Nonnull Store<EntityStore> var6
   );
}
