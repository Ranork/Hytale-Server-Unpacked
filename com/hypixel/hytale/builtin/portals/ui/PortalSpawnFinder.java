package com.hypixel.hytale.builtin.portals.ui;

import com.hypixel.hytale.builtin.portals.utils.posqueries.predicates.FitsAPortal;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PortalSpawnFinder {
   private static final int MAX_ATTEMPTS_PER_WORLD = 10;
   private static final int QUALITY_ATTEMPTS = 2;
   private static final int CHECKS_PER_CHUNK = 8;
   private static final Vector3d FALLBACK_POSITION = Vector3d.ZERO;

   @Nullable
   public static Transform computeSpawnTransform(@Nonnull World world, @Nonnull List<Vector3d> hintedSpawns) {
      Vector3d spawn = guesstimateFromHints(world, hintedSpawns);
      if (spawn == null) {
         spawn = findFallbackPositionOnGround(world);
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log("Had to use fallback spawn for portal spawn (10 attempts)");
      }

      if (spawn == null) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log("Both dart and fallback spawn finder failed for portal spawn");
         return null;
      } else {
         Vector3f direction = Vector3f.lookAt(spawn).scale(-1.0F);
         direction.setPitch(0.0F);
         direction.setRoll(0.0F);
         return new Transform(spawn.clone().add(0.0, 0.5, 0.0), direction);
      }
   }

   @Nullable
   private static Vector3d guesstimateFromHints(World world, List<Vector3d> hintedSpawns) {
      for (int i = 0; i < Math.min(hintedSpawns.size(), 10); i++) {
         Vector3d hintedSpawn = hintedSpawns.get(i);
         WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(hintedSpawn.x, hintedSpawn.z));
         if (chunk != null) {
            boolean quality = i < 2;
            int scanHeight = quality ? (int)hintedSpawn.y : 319;
            Vector3d spawn = findGroundWithinChunk(chunk, scanHeight, quality);
            if (spawn != null) {
               ((HytaleLogger.Api)HytaleLogger.getLogger().atInfo()).log("Found portal spawn " + spawn + " on attempt #" + (i + 1) + " quality=" + quality);
               return spawn;
            }
         }
      }

      return null;
   }

   @Nullable
   private static Vector3d findGroundWithinChunk(@Nonnull WorldChunk chunk, int scanHeight, boolean checkIfPortalFitsNice) {
      int chunkBlockX = ChunkUtil.minBlock(chunk.getX());
      int chunkBlockZ = ChunkUtil.minBlock(chunk.getZ());
      ThreadLocalRandom random = ThreadLocalRandom.current();

      for (int i = 0; i < 8; i++) {
         int x = chunkBlockX + random.nextInt(2, 14);
         int z = chunkBlockZ + random.nextInt(2, 14);
         Vector3d point = findWithGroundBelow(chunk, x, scanHeight, z, scanHeight, false);
         if (point != null && (!checkIfPortalFitsNice || FitsAPortal.check(chunk.getWorld(), point))) {
            return point;
         }
      }

      return null;
   }

   @Nullable
   private static Vector3d findWithGroundBelow(@Nonnull WorldChunk chunk, int x, int y, int z, int scanHeight, boolean fluidsAreAcceptable) {
      World world = chunk.getWorld();
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> chunkRef = chunk.getReference();
      if (chunkRef != null && chunkRef.isValid()) {
         Store<ChunkStore> chunkStoreAccessor = chunkStore.getStore();
         ChunkColumn chunkColumnComponent = chunkStoreAccessor.getComponent(chunkRef, ChunkColumn.getComponentType());
         if (chunkColumnComponent == null) {
            return null;
         } else {
            BlockChunk blockChunkComponent = chunkStoreAccessor.getComponent(chunkRef, BlockChunk.getComponentType());
            if (blockChunkComponent == null) {
               return null;
            } else {
               for (int dy = 0; dy < scanHeight; dy++) {
                  PortalSpawnFinder.Material selfMat = getMaterial(chunkStoreAccessor, chunkColumnComponent, blockChunkComponent, x, y - dy, z);
                  PortalSpawnFinder.Material belowMat = getMaterial(chunkStoreAccessor, chunkColumnComponent, blockChunkComponent, x, y - dy - 1, z);
                  boolean selfValid = selfMat == PortalSpawnFinder.Material.AIR || fluidsAreAcceptable && selfMat == PortalSpawnFinder.Material.FLUID;
                  if (!selfValid) {
                     break;
                  }

                  if (belowMat == PortalSpawnFinder.Material.SOLID) {
                     return new Vector3d(x, y - dy, z);
                  }
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   @Nonnull
   private static PortalSpawnFinder.Material getMaterial(
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      @Nonnull ChunkColumn chunkColumnComponent,
      @Nonnull BlockChunk blockChunkComponent,
      double x,
      double y,
      double z
   ) {
      int blockX = (int)x;
      int blockY = (int)y;
      int blockZ = (int)z;
      int fluidId = WorldUtil.getFluidIdAtPosition(chunkStore, chunkColumnComponent, blockX, blockY, blockZ);
      if (fluidId != 0) {
         return PortalSpawnFinder.Material.FLUID;
      } else {
         BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(blockY);
         int blockId = blockSection.get(blockX, blockY, blockZ);
         BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
         if (blockType == null) {
            return PortalSpawnFinder.Material.UNKNOWN;
         } else {
            return switch (blockType.getMaterial()) {
               case Solid -> PortalSpawnFinder.Material.SOLID;
               case Empty -> PortalSpawnFinder.Material.AIR;
            };
         }
      }
   }

   @Nullable
   private static Vector3d findFallbackPositionOnGround(@Nonnull World world) {
      Vector3d center = FALLBACK_POSITION.clone();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(center.x, center.z);
      WorldChunk centerChunk = world.getChunk(chunkIndex);
      return centerChunk == null ? null : findWithGroundBelow(centerChunk, 0, 319, 0, 319, true);
   }

   private static enum Material {
      SOLID,
      FLUID,
      AIR,
      UNKNOWN;
   }
}
