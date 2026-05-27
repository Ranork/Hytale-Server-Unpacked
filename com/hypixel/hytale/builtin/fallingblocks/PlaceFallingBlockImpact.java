package com.hypixel.hytale.builtin.fallingblocks;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.fallingblocks.FallingBlockImpact;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class PlaceFallingBlockImpact extends FallingBlockImpact {
   public static final String ID = "Place";
   public static final BuilderCodec<PlaceFallingBlockImpact> CODEC = BuilderCodec.builder(PlaceFallingBlockImpact.class, PlaceFallingBlockImpact::new).build();

   @Override
   public void apply(
      @Nonnull WorldChunk worldChunk,
      @Nonnull World world,
      @Nonnull BlockType blockType,
      @Nonnull Vector3d position,
      @Nonnull RotationTuple rotation,
      @Nonnull Store<EntityStore> store
   ) {
      placeFallingBlock(worldChunk, world, blockType, position, rotation, store);
   }

   public static void placeFallingBlock(
      @Nonnull WorldChunk worldChunk,
      @Nonnull World world,
      @Nonnull BlockType blockType,
      @Nonnull Vector3d position,
      @Nonnull RotationTuple rotation,
      @Nonnull Store<EntityStore> store
   ) {
      int placeX = MathUtil.floor(position.x);
      int placeY = MathUtil.floor(position.y);
      int placeZ = MathUtil.floor(position.z);
      if (!worldChunk.placeBlock(placeX, placeY, placeZ, blockType.getId(), rotation, 256, true)) {
         BreakFallingBlockImpact.breakFallingBlock(world, blockType, position, rotation, store);
      }
   }
}
