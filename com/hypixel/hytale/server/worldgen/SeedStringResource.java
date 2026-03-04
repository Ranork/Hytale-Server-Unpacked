package com.hypixel.hytale.server.worldgen;

import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabLoader;
import com.hypixel.hytale.server.worldgen.loader.prefab.BlockPlacementMaskRegistry;
import com.hypixel.hytale.server.worldgen.loader.util.FileMaskCache;
import com.hypixel.hytale.server.worldgen.prefab.PrefabStoreRoot;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class SeedStringResource implements SeedResource {
   @Nonnull
   protected final FileMaskCache<IIntCondition> biomeMaskRegistry;
   @Nonnull
   protected final BlockPlacementMaskRegistry blockMaskRegistry;
   @Nonnull
   protected WorldGenConfig config;
   @Nonnull
   protected WorldGenPrefabLoader loader;
   @Nonnull
   protected final Map<String, AtomicInteger> uniqueIds;

   public SeedStringResource(@Nonnull PrefabStoreRoot prefabStore, @Nonnull WorldGenConfig config) {
      this.config = config;
      this.loader = new WorldGenPrefabLoader(prefabStore, config);
      this.biomeMaskRegistry = new FileMaskCache<>();
      this.blockMaskRegistry = new BlockPlacementMaskRegistry();
      this.uniqueIds = new Object2ObjectOpenHashMap();
   }

   @Nonnull
   public String getUniqueName(@Nonnull String prefix) {
      return prefix + this.uniqueIds.computeIfAbsent(prefix, k -> new AtomicInteger(0)).getAndIncrement();
   }

   @Nonnull
   public WorldGenPrefabLoader getLoader() {
      return this.loader;
   }

   public void setPrefabConfig(@Nonnull WorldGenConfig config, @Nonnull PrefabStoreRoot prefabStore) {
      if (config != this.config || prefabStore != this.loader.getStore()) {
         LogUtil.getLogger().at(Level.INFO).log("Set prefab-loader config: path=%s, store=%s", config.path(), prefabStore.name());
         this.config = config;
         this.loader = new WorldGenPrefabLoader(prefabStore, config);
      }
   }

   @Nonnull
   @Override
   public ResultBuffer.Bounds2d localBounds2d() {
      return ChunkGenerator.getResource().bounds2d;
   }

   @Nonnull
   @Override
   public ResultBuffer.ResultBuffer2d localBuffer2d() {
      return ChunkGenerator.getResource().resultBuffer2d;
   }

   @Nonnull
   @Override
   public ResultBuffer.ResultBuffer3d localBuffer3d() {
      return ChunkGenerator.getResource().resultBuffer3d;
   }

   @Override
   public void writeSeedReport(String seedReport) {
      LogUtil.getLogger().at(Level.FINE).log(seedReport);
   }

   @Nonnull
   public FileMaskCache<IIntCondition> getBiomeMaskRegistry() {
      return this.biomeMaskRegistry;
   }

   @Nonnull
   public BlockPlacementMaskRegistry getBlockMaskRegistry() {
      return this.blockMaskRegistry;
   }
}
