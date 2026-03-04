package com.hypixel.hytale.server.core.modules.migrations;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkSavingSystems;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import joptsimple.OptionSet;

public class MigrationModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(MigrationModule.class).build();
   protected static MigrationModule instance;
   @Nonnull
   private final Map<String, Function<Path, Migration>> migrationCtors = new Object2ObjectOpenHashMap();
   private SystemType<ChunkStore, ChunkColumnMigrationSystem> chunkColumnMigrationSystem;
   private SystemType<ChunkStore, ChunkSectionMigrationSystem> chunkSectionMigrationSystem;

   public MigrationModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   public static MigrationModule get() {
      return instance;
   }

   @Override
   protected void setup() {
      this.getEventRegistry().register(BootEvent.class, event -> {
         if (Options.getOptionSet().has(Options.MIGRATIONS)) {
            this.runMigrations();
            HytaleServer.get().shutdownServer();
         }
      });
      this.chunkColumnMigrationSystem = this.getChunkStoreRegistry().registerSystemType(ChunkColumnMigrationSystem.class);
      this.chunkSectionMigrationSystem = this.getChunkStoreRegistry().registerSystemType(ChunkSectionMigrationSystem.class);
   }

   public SystemType<ChunkStore, ChunkColumnMigrationSystem> getChunkColumnMigrationSystem() {
      return this.chunkColumnMigrationSystem;
   }

   public SystemType<ChunkStore, ChunkSectionMigrationSystem> getChunkSectionMigrationSystem() {
      return this.chunkSectionMigrationSystem;
   }

   public void register(String id, Function<Path, Migration> migration) {
      this.migrationCtors.put(id, migration);
   }

   public void runMigrations() {
      OptionSet optionSet = Options.getOptionSet();
      List<String> worldsToMigrate = optionSet.has(Options.MIGRATE_WORLDS) ? optionSet.valuesOf(Options.MIGRATE_WORLDS) : null;
      Map<String, Path> migrationMap = (Map<String, Path>)Options.getOptionSet().valueOf(Options.MIGRATIONS);
      List<Migration> migrations = new ObjectArrayList();
      migrationMap.forEach((s, path) -> {
         Function<Path, Migration> migrationCtor = this.migrationCtors.get(s);
         if (migrationCtor != null) {
            migrations.add(migrationCtor.apply(path));
         }
      });
      if (!migrations.isEmpty()) {
         AtomicInteger worldsCount = new AtomicInteger();

         for (World world : Universe.get().getWorlds().values()) {
            String worldName = world.getName();
            if (worldsToMigrate == null || worldsToMigrate.contains(worldName)) {
               worldsCount.incrementAndGet();
               this.getLogger().at(Level.INFO).log("Starting to migrate world '%s'...", worldName);
               ChunkStore chunkComponentStore = world.getChunkStore();
               IChunkSaver saver = chunkComponentStore.getSaver();
               IChunkLoader loader = chunkComponentStore.getLoader();
               world.execute(
                  () -> {
                     ChunkSavingSystems.Data data = chunkComponentStore.getStore().getResource(ChunkStore.SAVE_RESOURCE);
                     data.isSaving = false;
                     data.waitForSavingChunks()
                        .whenComplete(
                           (aVoid, throwable) -> {
                              try {
                                 LongSet chunks = loader.getIndexes();
                                 this.getLogger().at(Level.INFO).log("Found %d chunks in world '%s'. Starting iteration...", chunks.size(), worldName);
                                 List<CompletableFuture<?>> futures = new ObjectArrayList(chunks.size());
                                 LongIterator iterator = chunks.iterator();

                                 while (iterator.hasNext()) {
                                    long index = iterator.nextLong();
                                    int chunkX = ChunkUtil.xOfChunkIndex(index);
                                    int chunkZ = ChunkUtil.zOfChunkIndex(index);
                                    futures.add(
                                       loader.loadHolder(chunkX, chunkZ)
                                          .thenCompose(
                                             holder -> {
                                                if (holder == null) {
                                                   return CompletableFuture.completedFuture(null);
                                                } else {
                                                   WorldChunk chunk = holder.getComponent(WorldChunk.getComponentType());
                                                   migrations.forEach(migration -> migration.run(chunk));
                                                   return chunk.getNeedsSaving()
                                                      ? saver.saveHolder(chunkX, chunkZ, (Holder<ChunkStore>)holder)
                                                      : CompletableFuture.completedFuture(null);
                                                }
                                             }
                                          )
                                    );
                                 }

                                 try {
                                    CompletableFutureUtil.joinWithProgress(
                                       futures,
                                       (value, completed, max) -> this.getLogger()
                                          .at(Level.INFO)
                                          .log("Scanning + Migrating world '%s': %.2d% (%d of %d)", worldName, value, completed, max),
                                       250,
                                       750
                                    );
                                 } catch (InterruptedException var20) {
                                    ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var20)).log("Interrupted while loading chunks:");
                                    Thread.currentThread().interrupt();
                                 }
                              } catch (Throwable var21) {
                                 ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var21)).log("Failed to migrate chunks!");
                              } finally {
                                 data.isSaving = true;
                                 this.getLogger().at(Level.INFO).log("%d world(s) left to migrate.", worldsCount.decrementAndGet());
                              }
                           }
                        );
                  }
               );
            }
         }
      }
   }
}
