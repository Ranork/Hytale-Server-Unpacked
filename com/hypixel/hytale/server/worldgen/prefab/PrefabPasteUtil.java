package com.hypixel.hytale.server.worldgen.prefab;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.PrefabWeights;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGeneratorExecution;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabPasteUtil {
   public static final int MAX_RECURSION_DEPTH = 10;

   public static void generate(
      @Nonnull PrefabPasteUtil.PrefabPasteBuffer buffer, PrefabRotation rotation, @Nonnull WorldGenPrefabSupplier supplier, int x, int y, int z, int cx, int cz
   ) {
      buffer.supplier = supplier;
      buffer.posWorld.assign(x, y, z);
      buffer.posChunk.assign(cx, y, cz);
      buffer.rotation = rotation;
      generate0(buffer, supplier);
      buffer.reset();
   }

   private static void generate0(@Nonnull PrefabPasteUtil.PrefabPasteBuffer _buffer, @Nonnull WorldGenPrefabSupplier supplier) {
      if (_buffer.fitHeightmap) {
         _buffer.originHeight = _buffer.execution.getChunkGenerator().getHeight(_buffer.seed, _buffer.posWorld.x, _buffer.posWorld.z);
         _buffer.posChunk.y = _buffer.originHeight;
         _buffer.posWorld.y = _buffer.originHeight;
      }

      supplier.get()
         .forEach(
            (cx, cz, blocks, buffer) -> {
               int bx = cx + buffer.posChunk.x;
               int bz = cz + buffer.posChunk.z;
               if (!ChunkUtil.isWithinLocalChunk(bx, bz)) {
                  return false;
               } else {
                  if (buffer.fitHeightmap) {
                     buffer.yOffset = buffer.execution.getChunkGenerator().getHeight(buffer.seed, buffer.posWorld.x + cx, buffer.posWorld.z + cz)
                        - buffer.originHeight;
                  } else {
                     buffer.yOffset = 0;
                  }

                  return true;
               }
            },
            (cx, cy, cz, block, holder, supportValue, rotation, filler, buffer, fluidId, fluidLevel) -> {
               if (buffer.blockMask != BlockMaskCondition.DEFAULT_FALSE) {
                  int bx = cx + buffer.posChunk.x;
                  int by = cy + buffer.posChunk.y + buffer.yOffset;
                  int bz = cz + buffer.posChunk.z;
                  if (by >= 0 && by < 320) {
                     if (buffer.blockMask != BlockMaskCondition.DEFAULT_TRUE) {
                        int currentBlock = buffer.execution.getBlock(bx, by, bz);
                        int currentFluid = buffer.execution.getFluid(bx, by, bz);
                        if (!buffer.blockMask.eval(currentBlock, currentFluid, block, fluidId)) {
                           return;
                        }
                     }

                     buffer.execution.setBlock(bx, by, bz, buffer.priority, block, holder != null ? holder.clone() : null, supportValue, rotation, filler);
                     buffer.execution.setFluid(bx, by, bz, buffer.priority, fluidId, buffer.environmentId);
                     buffer.execution.setEnvironment(bx, by, bz, buffer.environmentId);
                  }
               }
            },
            (cx, cz, entityWrappers, buffer) -> {
               Holder<EntityStore>[] clone = new Holder[entityWrappers.length];

               for (int i = 0; i < entityWrappers.length; i++) {
                  clone[i] = entityWrappers[i].clone();
               }

               Vector3i offset = new Vector3i(buffer.posWorld.x, buffer.posWorld.y + buffer.yOffset, buffer.posWorld.z);
               buffer.execution.getEntityChunk().addEntities(offset, buffer.rotation, clone, buffer.specificSeed);
            },
            (cx, cy, cz, path, fitHeightmap, inheritSeed, inheritHeightCondition, weights, rotation, buffer) -> {
               if (buffer.depth < 10) {
                  buffer.depth++;
                  int _localX = buffer.posChunk.x;
                  int _localY = buffer.posChunk.y;
                  int _localZ = buffer.posChunk.z;
                  int _worldX = buffer.posWorld.x;
                  int _worldY = buffer.posWorld.y;
                  int _worldZ = buffer.posWorld.z;
                  int _yOffset = buffer.yOffset;
                  int _originHeight = buffer.originHeight;
                  int _specificSeed = buffer.specificSeed;
                  PrefabRotation _rotation = buffer.rotation;
                  boolean _fitHeightmap = buffer.fitHeightmap;
                  generateChild(cx, cy, cz, path, fitHeightmap, inheritSeed, inheritHeightCondition, weights, rotation, buffer, buffer.childRandom);
                  buffer.posChunk.assign(_localX, _localY, _localZ);
                  buffer.posWorld.assign(_worldX, _worldY, _worldZ);
                  buffer.yOffset = _yOffset;
                  buffer.originHeight = _originHeight;
                  buffer.rotation = _rotation;
                  buffer.specificSeed = _specificSeed;
                  buffer.fitHeightmap = _fitHeightmap;
                  buffer.depth--;
               }
            },
            _buffer
         );
   }

   private static void generateChild(
      int cx,
      int cy,
      int cz,
      String path,
      boolean fitHeightmap,
      boolean inheritSeed,
      boolean inheritHeightCondition,
      @Nonnull PrefabWeights weights,
      @Nonnull PrefabRotation rotation,
      @Nonnull PrefabPasteUtil.PrefabPasteBuffer buffer,
      Random random
   ) {
      int parentSpecificSeed = buffer.specificSeed;
      boolean parentFitHeightmap = buffer.fitHeightmap;
      buffer.posChunk.add(cx, cy, cz);
      buffer.posWorld.add(cx, cy, cz);
      buffer.fitHeightmap = fitHeightmap;
      buffer.rotation = buffer.rotation.add(rotation);
      if (!inheritSeed) {
         buffer.specificSeed = parentSpecificSeed;
      } else {
         buffer.specificSeed = (int)HashUtil.hash(parentSpecificSeed, cx, cy, cz);
         if (buffer.specificSeed == parentSpecificSeed) {
            buffer.specificSeed++;
         }
      }

      if (parentFitHeightmap) {
         int yOffset = buffer.execution.getChunkGenerator().getHeight(buffer.seed, buffer.posWorld.x, buffer.posWorld.z) - buffer.originHeight;
         buffer.posChunk.y += yOffset;
         buffer.posWorld.y += yOffset;
      }

      if (buffer.posChunk.y >= 0 && buffer.posChunk.y < 320 && ChunkUtil.isWithinLocalChunk(buffer.posChunk.x, buffer.posChunk.z)) {
         buffer.execution.setBlock(buffer.posChunk.x, buffer.posChunk.y, buffer.posChunk.z, buffer.priority, 0, null);
      }

      if (!inheritHeightCondition || buffer.spawnCondition.eval(buffer.seed, buffer.posWorld.x, buffer.posWorld.z, buffer.posWorld.y, random)) {
         WorldGenPrefabSupplier[] prefabSuppliers = buffer.supplier.getLoader().get(path);
         if (prefabSuppliers != null && prefabSuppliers.length != 0) {
            WorldGenPrefabSupplier prefabSupplier = nextPrefab(buffer.childRandom, prefabSuppliers, weights);
            generate0(buffer, prefabSupplier);
         }
      }
   }

   @Nonnull
   private static WorldGenPrefabSupplier nextPrefab(@Nonnull Random random, @Nonnull WorldGenPrefabSupplier[] prefabSuppliers, @Nonnull PrefabWeights weights) {
      if (prefabSuppliers.length == 1) {
         return prefabSuppliers[0];
      } else {
         WorldGenPrefabSupplier prefab = null;
         if (weights.size() > 0) {
            prefab = weights.get(prefabSuppliers, WorldGenPrefabSupplier::getPrefabName, random);
         }

         return prefab == null ? nextRandomPrefab(random, prefabSuppliers) : prefab;
      }
   }

   @Nonnull
   private static WorldGenPrefabSupplier nextRandomPrefab(@Nonnull Random random, @Nonnull WorldGenPrefabSupplier[] prefabSuppliers) {
      return prefabSuppliers[random.nextInt(prefabSuppliers.length)];
   }

   public static class PrefabPasteBuffer extends PrefabBufferCall {
      @Nullable
      public ChunkGeneratorExecution execution;
      public final Vector3i posWorld = new Vector3i();
      public final Vector3i posChunk = new Vector3i();
      public final Random childRandom = new FastRandom(0L);
      public int originHeight;
      public int yOffset;
      public int seed;
      public int specificSeed;
      public boolean fitHeightmap;
      public boolean deepSearch;
      public BlockMaskCondition blockMask;
      public int environmentId;
      public byte priority;
      public ICoordinateCondition heightCondition;
      public ICoordinateRndCondition spawnCondition;
      @Nullable
      public WorldGenPrefabSupplier supplier;
      private int depth;

      public PrefabPasteBuffer() {
         this.random = new FastRandom(0L);
         this.reset();
      }

      public void setSeed(int worldSeed, long externalSeed) {
         this.seed = worldSeed;
         this.specificSeed = (int)externalSeed;
         this.random.setSeed(externalSeed);
         this.childRandom.setSeed(externalSeed);
      }

      void reset() {
         this.execution = null;
         this.fitHeightmap = false;
         this.deepSearch = false;
         this.blockMask = BlockMaskCondition.DEFAULT_TRUE;
         this.environmentId = Integer.MIN_VALUE;
         this.heightCondition = DefaultCoordinateCondition.DEFAULT_TRUE;
         this.spawnCondition = DefaultCoordinateRndCondition.DEFAULT_TRUE;
         this.supplier = null;
         this.depth = 0;
      }
   }
}
