package com.hypixel.hytale.server.worldgen;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.worldgen.cache.CoordinateCache;
import com.hypixel.hytale.server.worldgen.cache.ExtendedCoordinateCache;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.chunk.BlockPriorityChunk;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import com.hypixel.hytale.server.worldgen.chunk.populator.PrefabPopulator;
import com.hypixel.hytale.server.worldgen.climate.ClimateNoise;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabLoader;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.prefab.PrefabPasteUtil;
import com.hypixel.hytale.server.worldgen.util.cache.TimeoutCache;
import com.hypixel.hytale.server.worldgen.zone.ZoneGeneratorResult;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class ChunkGeneratorResource {
   @Nonnull
   public final Random random;
   @Nonnull
   public final Random random2;
   @Nonnull
   public final IntList coverArray;
   public final TimeoutCache<WorldGenPrefabSupplier, IPrefabBuffer> prefabs = new TimeoutCache<>(
      30L, TimeUnit.SECONDS, this::getPrefab, (key, value) -> value.release()
   );
   @Nonnull
   public final BlockPriorityChunk priorityChunk;
   @Nonnull
   public final CoordinateCache.CoordinateKey cacheCoordinateKey;
   @Nonnull
   public final ExtendedCoordinateCache.ExtendedCoordinateKey<CaveType> cacheCaveCoordinateKey;
   @Nonnull
   public final ResultBuffer.Bounds2d bounds2d;
   @Nonnull
   public final ResultBuffer.ResultBuffer2d resultBuffer2d;
   @Nonnull
   public final ResultBuffer.ResultBuffer3d resultBuffer3d;
   @Nonnull
   public final PrefabPasteUtil.PrefabPasteBuffer prefabBuffer;
   @Nonnull
   public final ZoneBiomeResult zoneBiomeResult;
   public final ClimateNoise.Buffer climateBuffer = new ClimateNoise.Buffer();
   public final PrefabPopulator prefabPopulator = new PrefabPopulator();
   public final WorldGenPrefabLoader.PrefabPathCollector prefabCollector = new WorldGenPrefabLoader.PrefabPathCollector();
   @Nonnull
   public final Vector2d cacheVector2d;
   protected ChunkGenerator chunkGenerator;

   public ChunkGeneratorResource() {
      this.random = new FastRandom(0L);
      this.random2 = new FastRandom(0L);
      this.priorityChunk = new BlockPriorityChunk();
      this.coverArray = new IntArrayList(5);
      this.cacheVector2d = new Vector2d();
      this.cacheCoordinateKey = new CoordinateCache.CoordinateKey();
      this.cacheCaveCoordinateKey = new ExtendedCoordinateCache.ExtendedCoordinateKey<>();
      this.bounds2d = new ResultBuffer.Bounds2d();
      this.resultBuffer2d = new ResultBuffer.ResultBuffer2d();
      this.resultBuffer3d = new ResultBuffer.ResultBuffer3d();
      this.prefabBuffer = new PrefabPasteUtil.PrefabPasteBuffer();
      this.zoneBiomeResult = new ZoneBiomeResult();
      this.zoneBiomeResult.setZoneResult(new ZoneGeneratorResult());
   }

   public void init(ChunkGenerator chunkGenerator) {
      this.chunkGenerator = chunkGenerator;
   }

   public void release() {
      this.prefabs.shutdown();
   }

   @Nonnull
   public Random getRandom() {
      return this.random;
   }

   @Nonnull
   protected IPrefabBuffer getPrefab(WorldGenPrefabSupplier prefabSupplier) {
      return this.chunkGenerator.getPrefabLoadingCache().getPrefabAccessor(prefabSupplier);
   }
}
