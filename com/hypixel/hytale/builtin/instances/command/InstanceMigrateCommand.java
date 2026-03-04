package com.hypixel.hytale.builtin.instances.command;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.instances.config.InstanceWorldConfig;
import com.hypixel.hytale.builtin.instances.removal.RemovalCondition;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.migrations.ChunkColumnMigrationSystem;
import com.hypixel.hytale.server.core.modules.migrations.ChunkSectionMigrationSystem;
import com.hypixel.hytale.server.core.modules.migrations.MigrationModule;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkSavingSystems;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;

public class InstanceMigrateCommand extends AbstractAsyncCommand {
   private static final long CHUNK_UPDATE_INTERVAL = 100L;

   public InstanceMigrateCommand() {
      super("migrate", "server.commands.instances.migrate.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      InstancesPlugin instancePlugin = InstancesPlugin.get();
      List<String> instancesToMigrate = instancePlugin.getInstanceAssets();
      CompletableFuture[] futures = new CompletableFuture[instancesToMigrate.size()];
      AtomicLong chunkCount = new AtomicLong();
      AtomicLong chunksMigrated = new AtomicLong();

      for (int i = 0; i < instancesToMigrate.size(); i++) {
         String asset = instancesToMigrate.get(i);
         Path instancePath = InstancesPlugin.getInstanceAssetPath(asset);
         CompletableFuture<WorldConfig> configFuture = WorldConfig.load(instancePath.resolve("instance.bson"));
         futures[i] = CompletableFutureUtil._catch(configFuture.thenCompose(config -> migrateInstance(context, asset, config, chunkCount, chunksMigrated)));
         futures[i].join();
      }

      return CompletableFuture.allOf(futures)
         .whenComplete(
            (result, throwable) -> {
               if (throwable != null) {
                  context.sendMessage(Message.translation("server.commands.instances.migrate.failed").param("error", throwable.getMessage()));
               } else {
                  context.sendMessage(
                     Message.translation("server.commands.instances.migrate.complete").param("worlds", futures.length).param("chunks", chunkCount.get())
                  );
               }
            }
         );
   }

   @Nonnull
   private static CompletableFuture<Void> migrateInstance(
      @Nonnull CommandContext context, @Nonnull String asset, @Nonnull WorldConfig config, @Nonnull AtomicLong chunkCount, @Nonnull AtomicLong chunksMigrated
   ) {
      Path instancePath = InstancesPlugin.getInstanceAssetPath(asset);
      Universe universe = Universe.get();
      config.setUuid(UUID.randomUUID());
      config.setSavingPlayers(false);
      config.setIsAllNPCFrozen(true);
      config.setSavingConfig(false);
      config.setTicking(false);
      config.setGameMode(GameMode.Creative);
      config.setDeleteOnRemove(false);
      config.setCompassUpdating(false);
      InstanceWorldConfig.ensureAndGet(config).setRemovalConditions(RemovalCondition.EMPTY);
      config.markChanged();
      String worldName = "instance-migrate-" + InstancesPlugin.safeName(asset);
      return universe.makeWorld(worldName, instancePath, config, true).thenCompose(world -> {
         IChunkLoader loader = world.getChunkStore().getLoader();
         IChunkSaver saver = world.getChunkStore().getSaver();
         return CompletableFuture.<CompletableFuture<Void>>supplyAsync(() -> {
            ChunkStore chunkStore = world.getChunkStore();
            ChunkSavingSystems.Data data = chunkStore.getStore().getResource(ChunkStore.SAVE_RESOURCE);
            data.isSaving = false;
            return data.waitForSavingChunks();
         }, world).thenCompose(val -> (CompletionStage<Void>)val).thenComposeAsync(SneakyThrow.sneakyFunction(_void -> {
            LongSet chunks = loader.getIndexes();
            ObjectArrayList<CompletableFuture<Void>> futures = new ObjectArrayList(chunks.size());
            LongIterator iterator = chunks.iterator();

            while (iterator.hasNext()) {
               long chunkIndex = iterator.nextLong();
               chunkCount.incrementAndGet();
               int chunkX = ChunkUtil.xOfChunkIndex(chunkIndex);
               int chunkZ = ChunkUtil.zOfChunkIndex(chunkIndex);
               futures.add(CompletableFutureUtil._catch(loader.loadHolder(chunkX, chunkZ).thenComposeAsync(holder -> {
                  ComponentRegistry.Data<ChunkStore> data = ChunkStore.REGISTRY.getData();
                  ChunkStore chunkStore = world.getChunkStore();
                  Store<ChunkStore> store = chunkStore.getStore();
                  boolean shouldSave = false;
                  SystemType<ChunkStore, ChunkColumnMigrationSystem> systemType = MigrationModule.get().getChunkColumnMigrationSystem();
                  BitSet systemIndexes = data.getSystemIndexesForType(systemType);
                  int systemIndex = -1;

                  while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
                     ChunkColumnMigrationSystem system = data.getSystem(systemIndex, systemType);
                     if (system.test(ChunkStore.REGISTRY, holder.getArchetype())) {
                        system.onEntityAdd((Holder<ChunkStore>)holder, AddReason.LOAD, store);
                        shouldSave = true;
                     }
                  }

                  systemIndex = -1;

                  while ((systemIndex = systemIndexes.nextSetBit(systemIndex + 1)) >= 0) {
                     ChunkColumnMigrationSystem system = data.getSystem(systemIndex, systemType);
                     if (system.test(ChunkStore.REGISTRY, holder.getArchetype())) {
                        system.onEntityRemoved((Holder<ChunkStore>)holder, RemoveReason.REMOVE, store);
                     }
                  }

                  EntityChunk entityChunk = holder.getComponent(EntityChunk.getComponentType());
                  if (entityChunk != null && !entityChunk.getEntityHolders().isEmpty()) {
                     Store<EntityStore> entityStore = world.getEntityStore().getStore();
                     ComponentRegistry.Data<EntityStore> entityData = EntityStore.REGISTRY.getData();
                     List<Holder<EntityStore>> entities = entityChunk.getEntityHolders();
                     SystemType<EntityStore, EntityModule.MigrationSystem> systemTypex = EntityModule.get().getMigrationSystemType();
                     BitSet systemIndexesx = entityData.getSystemIndexesForType(systemTypex);
                     int systemIndexx = -1;

                     while ((systemIndexx = systemIndexesx.nextSetBit(systemIndexx + 1)) >= 0) {
                        EntityModule.MigrationSystem system = entityData.getSystem(systemIndexx, systemTypex);

                        for (int i = 0; i < entities.size(); i++) {
                           Holder<EntityStore> section = entities.get(i);
                           if (system.test(EntityStore.REGISTRY, section.getArchetype())) {
                              system.onEntityAdd(section, AddReason.LOAD, entityStore);
                              shouldSave = true;
                           }
                        }
                     }

                     systemIndexx = -1;

                     while ((systemIndexx = systemIndexesx.nextSetBit(systemIndexx + 1)) >= 0) {
                        EntityModule.MigrationSystem system = entityData.getSystem(systemIndexx, systemTypex);

                        for (int ix = 0; ix < entities.size(); ix++) {
                           Holder<EntityStore> section = entities.get(ix);
                           if (system.test(EntityStore.REGISTRY, section.getArchetype())) {
                              system.onEntityRemoved(section, RemoveReason.REMOVE, entityStore);
                           }
                        }
                     }
                  }

                  ChunkColumn chunkColumn = holder.getComponent(ChunkColumn.getComponentType());
                  if (chunkColumn != null && chunkColumn.getSectionHolders() != null) {
                     Holder<ChunkStore>[] sections = chunkColumn.getSectionHolders();
                     SystemType<ChunkStore, ChunkSectionMigrationSystem> systemTypex = MigrationModule.get().getChunkSectionMigrationSystem();
                     BitSet systemIndexesx = data.getSystemIndexesForType(systemTypex);
                     int systemIndexx = -1;

                     while ((systemIndexx = systemIndexesx.nextSetBit(systemIndexx + 1)) >= 0) {
                        ChunkSectionMigrationSystem system = data.getSystem(systemIndexx, systemTypex);

                        for (Holder<ChunkStore> section : sections) {
                           if (system.test(ChunkStore.REGISTRY, section.getArchetype())) {
                              system.onEntityAdd(section, AddReason.LOAD, store);
                              shouldSave = true;
                           }
                        }
                     }

                     systemIndexx = -1;

                     while ((systemIndexx = systemIndexesx.nextSetBit(systemIndexx + 1)) >= 0) {
                        ChunkSectionMigrationSystem system = data.getSystem(systemIndexx, systemTypex);

                        for (Holder<ChunkStore> sectionx : sections) {
                           if (system.test(ChunkStore.REGISTRY, sectionx.getArchetype())) {
                              system.onEntityRemoved(sectionx, RemoveReason.REMOVE, store);
                           }
                        }
                     }
                  }

                  return shouldSave ? saver.saveHolder(chunkX, chunkZ, (Holder<ChunkStore>)holder) : CompletableFuture.completedFuture(null);
               }, world).whenComplete((v, throwable) -> {
                  long migratedChunks = chunksMigrated.incrementAndGet();
                  long max = chunkCount.get();
                  if (migratedChunks % 100L == 0L || migratedChunks == max) {
                     context.sendMessage(Message.translation("server.commands.instances.migrate.update").param("chunks", migratedChunks).param("max", max));
                  }
               })));
            }

            return CompletableFuture.allOf((CompletableFuture<?>[])futures.toArray(CompletableFuture[]::new)).whenCompleteAsync((result, throwable) -> {
               context.sendMessage(Message.translation("server.commands.instances.migrate.worldDone").param("asset", asset));
               Universe.get().removeWorld(worldName);
            });
         }));
      });
   }
}
