package com.hypixel.hytale.builtin.fallingblocks;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.fallingblocks.FallingBlockImpact;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ExplodeInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class ExplodeFallingBlockImpact extends FallingBlockImpact {
   public static final String ID = "Explode";
   public static final BuilderCodec<ExplodeFallingBlockImpact> CODEC = BuilderCodec.builder(ExplodeFallingBlockImpact.class, ExplodeFallingBlockImpact::new)
      .build();

   @Override
   public void apply(
      @Nonnull WorldChunk worldChunk,
      @Nonnull World world,
      @Nonnull BlockType blockType,
      @Nonnull Vector3d position,
      @Nonnull RotationTuple rotation,
      @Nonnull Store<EntityStore> store
   ) {
      explodeFallingBlock(world, blockType, position, rotation, store);
   }

   public static void explodeFallingBlock(
      @Nonnull World world, @Nonnull BlockType blockType, @Nonnull Vector3d position, @Nonnull RotationTuple rotation, @Nonnull Store<EntityStore> store
   ) {
      ExplosionConfig explosionConfig = blockType.getExplosionConfig();
      if (explosionConfig != null) {
         world.execute(
            () -> ExplosionUtils.performExplosion(
               ExplodeInteraction.DAMAGE_SOURCE_EXPLOSION, position, explosionConfig, null, store, world.getChunkStore().getStore()
            )
         );
      }
   }
}
