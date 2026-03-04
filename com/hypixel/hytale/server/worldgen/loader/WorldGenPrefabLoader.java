package com.hypixel.hytale.server.worldgen.loader;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabLoader;
import com.hypixel.hytale.server.worldgen.WorldGenConfig;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.prefab.PrefabStoreRoot;
import com.hypixel.hytale.server.worldgen.util.cache.TimeoutCache;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldGenPrefabLoader {
   public static final String PREFAB_FOLDER = "Prefabs";
   @Nonnull
   private final Path root;
   @Nonnull
   private final PrefabStoreRoot store;
   @Nonnull
   private final PrefabLoader[] prefabLoaders;
   @Nonnull
   private final TimeoutCache<String, WorldGenPrefabSupplier[]> cache;

   public WorldGenPrefabLoader(@Nonnull PrefabStoreRoot store, @Nonnull WorldGenConfig config) {
      Path storePath = PrefabStoreRoot.resolvePrefabStore(store, config.path());
      this.root = storePath;
      this.store = store;
      this.prefabLoaders = getPrefabLoaders(config, storePath);
      this.cache = new TimeoutCache<>(30L, TimeUnit.SECONDS, SneakyThrow.sneakyFunction(this::compute), null);
   }

   @Nonnull
   public PrefabStoreRoot getStore() {
      return this.store;
   }

   public Path getRootFolder() {
      return this.root;
   }

   @Nullable
   public WorldGenPrefabSupplier[] get(@Nonnull String prefabName) {
      return this.cache.get(prefabName);
   }

   private WorldGenPrefabSupplier[] compute(@Nonnull String key) throws IOException {
      WorldGenPrefabSupplier[] var9;
      try (WorldGenPrefabLoader.PrefabPathCollector collector = ChunkGenerator.getResource().prefabCollector) {
         collector.key = key;
         collector.loader = this;

         for (PrefabLoader loader : this.prefabLoaders) {
            collector.root = loader.getRootFolder();
            loader.resolvePrefabs(key, collector);
         }

         if (collector.list.isEmpty()) {
            throw new Error("Failed to find prefab: " + key);
         }

         var9 = collector.result();
      }

      return var9;
   }

   private static PrefabLoader[] getPrefabLoaders(@Nonnull WorldGenConfig config, @Nonnull Path prefabStorePath) {
      AssetModule assets = AssetModule.get();
      Path root = assets.getBaseAssetPack().getRoot();
      Path assetPath = FileIO.relativize(prefabStorePath, root);
      List<AssetPack> packs = AssetFileSystem.getAssetPacks(config, packRoot -> FileIO.exists(packRoot, assetPath));
      Path[] roots = AssetFileSystem.getAssetRoots(packs);
      PrefabLoader[] loaders = new PrefabLoader[roots.length];

      for (int i = 0; i < roots.length; i++) {
         loaders[i] = new PrefabLoader(FileIO.append(roots[i], assetPath));
      }

      return loaders;
   }

   public static class PrefabPathCollector implements Consumer<Path>, AutoCloseable {
      private final ObjectSet<Path> visited = new ObjectOpenCustomHashSet(FileIO.PATH_STRATEGY);
      private final ObjectList<WorldGenPrefabSupplier> list = new ObjectArrayList();
      @Nullable
      private transient String key = null;
      @Nullable
      private transient Path root = null;
      @Nullable
      private transient WorldGenPrefabLoader loader = null;

      public void accept(@Nonnull Path path) {
         if (this.key != null && this.loader != null && this.root != null) {
            Path assetPath = FileIO.relativize(path, this.root);
            if (this.visited.add(assetPath)) {
               this.list.add(new WorldGenPrefabSupplier(this.loader, this.key, path));
            }
         }
      }

      @Override
      public void close() {
         this.visited.clear();
         this.list.clear();
         this.key = null;
         this.loader = null;
      }

      public WorldGenPrefabSupplier[] result() {
         return (WorldGenPrefabSupplier[])this.list.toArray(WorldGenPrefabSupplier[]::new);
      }
   }
}
