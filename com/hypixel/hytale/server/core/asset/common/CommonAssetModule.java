package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.common.util.PatternUtil;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.function.supplier.CachedSupplier;
import com.hypixel.hytale.function.supplier.SupplierUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.interface_.Notification;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.protocol.packets.setup.AssetFinalize;
import com.hypixel.hytale.protocol.packets.setup.AssetInitialize;
import com.hypixel.hytale.protocol.packets.setup.AssetPart;
import com.hypixel.hytale.protocol.packets.setup.RemoveAssets;
import com.hypixel.hytale.protocol.packets.setup.RequestCommonAssetsRebuild;
import com.hypixel.hytale.protocol.packets.setup.WorldLoadProgress;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.AssetPackRegisterEvent;
import com.hypixel.hytale.server.core.asset.AssetPackUnregisterEvent;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.asset.common.asset.FileCommonAsset;
import com.hypixel.hytale.server.core.asset.common.events.CommonAssetMonitorEvent;
import com.hypixel.hytale.server.core.asset.common.events.SendCommonAssetsEvent;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitor;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitorHandler;
import com.hypixel.hytale.server.core.asset.monitor.EventKind;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonAssetModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(CommonAssetModule.class).depends(AssetModule.class).build();
   private static CommonAssetModule instance;
   public static final Set<Path> IGNORED_FILES = Set.of(Path.of(".DS_Store"), Path.of("Thumbs.db"));
   public static final Instant TICK_TIMESTAMP_ORIGIN = Instant.parse("0001-01-01T00:00:00Z");
   public static final String ASSET_INDEX_VERSION_IDENTIFIER = "VERSION=";
   public static final int ASSET_INDEX_HASHES_VERSION = 0;
   public static final int ASSET_INDEX_CACHE_VERSION = 1;
   public static final int MAX_FRAME = 2621440;
   private final CachedSupplier<Asset[]> assets = SupplierUtil.cache(
      () -> CommonAssetRegistry.getAllAssets()
         .stream()
         .map(List::getLast)
         .map(CommonAssetRegistry.PackAsset::asset)
         .map(CommonAsset::toPacket)
         .toArray(Asset[]::new)
   );

   public static CommonAssetModule get() {
      return instance;
   }

   public CommonAssetModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.getEventRegistry().register(SendCommonAssetsEvent.class, this::onSendCommonAssets);
      this.getEventRegistry().register((short)-32, LoadAssetEvent.class, event -> {
         for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            this.loadCommonAssets(pack, event.getBootStart());
         }
      });
      this.getEventRegistry().register((short)-32, AssetPackRegisterEvent.class, event -> this.loadCommonAssets(event.getAssetPack(), System.nanoTime()));
      this.getEventRegistry().register(AssetPackUnregisterEvent.class, event -> this.removeCommonAssets(event.getAssetPack()));
   }

   private void removeCommonAssets(@Nonnull AssetPack assetPack) {
      this.unregisterAssetMonitor(assetPack);
      List<CommonAssetRegistry.PackAsset> removedAssets = new ObjectArrayList();
      List<CommonAsset> updatedAssets = new ObjectArrayList();

      for (List<CommonAssetRegistry.PackAsset> assets : CommonAssetRegistry.getAllAssets()) {
         for (CommonAssetRegistry.PackAsset asset : assets) {
            if (asset.pack().equals(assetPack.getName())) {
               BooleanObjectPair<CommonAssetRegistry.PackAsset> removed = CommonAssetRegistry.removeCommonAssetByName(asset.pack(), asset.asset().getName());
               if (removed != null) {
                  if (removed.firstBoolean()) {
                     updatedAssets.add(((CommonAssetRegistry.PackAsset)removed.second()).asset());
                  } else {
                     removedAssets.add((CommonAssetRegistry.PackAsset)removed.second());
                  }
               }

               this.assets.invalidate();
            }
         }
      }

      this.sendRemoveAssets(removedAssets, false);
      this.sendAssets(updatedAssets, false);
      Universe.get().broadcastPacketNoCache(new RequestCommonAssetsRebuild());
   }

   public void loadCommonAssets(@Nonnull AssetPack pack, long bootTime) {
      Path assetPath = pack.getRoot();
      HytaleLogger.getLogger().at(Level.INFO).log("Loading common assets from: %s", assetPath);
      long start = System.nanoTime();
      if (this.readCommonAssetsIndexHashes(pack)) {
         int duplicateAssetCount = CommonAssetRegistry.getDuplicateAssetCount();
         if (duplicateAssetCount > 0) {
            this.getLogger().at(Level.WARNING).log("Duplicated Asset Count: %s", duplicateAssetCount);
         }

         HytaleLogger.getLogger()
            .at(Level.INFO)
            .log(
               "Loading common assets phase completed! Boot time %s, Took %s",
               FormatUtil.nanosToString(System.nanoTime() - bootTime),
               FormatUtil.nanosToString(System.nanoTime() - start)
            );
      } else {
         Path commonPath = pack.getRoot().resolve("Common");
         AssetMonitor assetMonitor = AssetModule.get().getAssetMonitor();
         if (assetMonitor != null && !pack.isImmutable() && Files.isDirectory(commonPath)) {
            assetMonitor.monitorDirectoryFiles(commonPath, new CommonAssetModule.CommonAssetMonitorHandler(pack, commonPath));
         }

         this.readCommonAssetsIndexCache(pack);

         try {
            this.walkFileTree(pack);
         } catch (IOException var10) {
            throw SneakyThrow.sneakyThrow(var10);
         }

         int duplicateAssetCount = CommonAssetRegistry.getDuplicateAssetCount();
         if (duplicateAssetCount > 0) {
            this.getLogger().at(Level.WARNING).log("Duplicated Asset Count: %s", duplicateAssetCount);
         }

         HytaleLogger.getLogger()
            .at(Level.INFO)
            .log(
               "Loading common assets phase completed! Boot time %s, Took %s",
               FormatUtil.nanosToString(System.nanoTime() - bootTime),
               FormatUtil.nanosToString(System.nanoTime() - start)
            );
         Universe.get().broadcastPacketNoCache(new RequestCommonAssetsRebuild());
      }
   }

   public <T extends CommonAsset> void addCommonAsset(String pack, @Nonnull T asset) {
      this.addCommonAsset(pack, asset, true);
   }

   public <T extends CommonAsset> void addCommonAsset(String pack, @Nonnull T asset, boolean log) {
      CommonAssetRegistry.AddCommonAssetResult result = CommonAssetRegistry.addCommonAsset(pack, asset);
      CommonAssetRegistry.PackAsset newAsset = result.getNewPackAsset();
      CommonAssetRegistry.PackAsset oldAsset = result.getPreviousNameAsset();
      if (oldAsset != null && oldAsset.asset().getHash().equals(newAsset.asset().getHash())) {
         if (log) {
            this.getLogger().at(Level.INFO).log("Didn't change: %s", asset.getName());
         }
      } else {
         if (oldAsset == null) {
            if (log) {
               this.getLogger().at(Level.INFO).log("Created: %s", newAsset);
            }
         } else if (log) {
            this.getLogger().at(Level.INFO).log("Reloaded: %s - Old Hash: %s", newAsset, oldAsset.asset().getHash());
         }

         String messageId = oldAsset == null ? "server.general.assetstore.reloadAssets" : "server.general.assetstore.reloadAssets";
         String iconPath = oldAsset == null ? "Icons/AssetNotifications/IconCheckmark.png" : "Icons/AssetNotifications/AssetReloaded.png";
         String messageColor = oldAsset == null ? "#06EE92" : "#A7AfA7";
         NotificationUtil.sendNotificationToUniverse(
            Message.translation(messageId).color(messageColor).param("class", "Common"),
            Message.raw(newAsset.pack() + ":" + newAsset.asset().getName()),
            iconPath,
            NotificationStyle.Success
         );
         if (result.getActiveAsset().equals(newAsset)) {
            this.assets.invalidate();
            BlockyAnimationCache.invalidate(newAsset.asset().getName());
            if (Universe.get().getPlayerCount() > 0) {
               this.sendAsset(newAsset.asset(), false);
            }
         }
      }
   }

   @Nullable
   public Asset[] getRequiredAssets() {
      return this.assets.get();
   }

   private boolean readCommonAssetsIndexHashes(@Nonnull AssetPack pack) {
      Path assetPath = pack.getRoot();
      Path commonPath = assetPath.resolve("Common");
      Path assetHashFile = assetPath.resolve("CommonAssetsIndex.hashes");
      if (Files.isRegularFile(assetHashFile)) {
         long loadHashesStart = System.nanoTime();
         int loadedAssetCount = 0;

         try (BufferedReader reader = Files.newBufferedReader(assetHashFile)) {
            int version = 0;
            int i = 0;

            while (true) {
               String line = reader.readLine();
               if (line == null) {
                  break;
               }

               if (line.startsWith("VERSION=")) {
                  version = Integer.parseInt(line.substring("VERSION=".length()));
                  this.getLogger().at(Level.FINEST).log("Version set to %d from CommonAssetsIndex.hashes:L%d '%s'", version, i, line);
                  if (version > 0) {
                     throw new IllegalArgumentException(String.format("Unsupported version %d in CommonAssetsIndex.hashes %d > %d", version, version, 0));
                  }
               } else {
                  String[] split = line.split(" ", 2);
                  if (split.length != 2) {
                     this.getLogger().at(Level.WARNING).log("Corrupt line in CommonAssetsIndex.hashes:L%d '%s'", i, line);
                  } else {
                     String hash = split[0];
                     if (hash.length() != 64 && !CommonAsset.HASH_PATTERN.matcher(hash).matches()) {
                        this.getLogger().at(Level.WARNING).log("Corrupt line in CommonAssetsIndex.hashes:L%d '%s'", i, line);
                     } else {
                        String name = split[1];
                        this.addCommonAsset(pack.getName(), new FileCommonAsset(commonPath.resolve(name), name, hash, null), false);
                        this.getLogger().at(Level.FINEST).log("Loaded asset info from CommonAssetsIndex.hashes:L%d '%s'", i, name);
                        loadedAssetCount++;
                     }
                  }
               }

               i++;
            }
         } catch (IOException var17) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(var17)).log("Failed to load hashes from CommonAssetsIndex.hashes");
            return false;
         }

         long loadHashesEnd = System.nanoTime();
         long loadHashesDiff = loadHashesEnd - loadHashesStart;
         this.getLogger()
            .at(Level.INFO)
            .log("Took %s to load %d assets from CommonAssetsIndex.hashes file.", FormatUtil.nanosToString(loadHashesDiff), loadedAssetCount);
         return true;
      } else {
         return false;
      }
   }

   private void readCommonAssetsIndexCache(@Nonnull AssetPack pack) {
      Path assetPath = pack.getRoot();
      Path commonPath = assetPath.resolve("Common");
      Path assetCacheFile = assetPath.resolve("CommonAssetsIndex.cache");
      if (Files.isRegularFile(assetCacheFile)) {
         long loadCacheStart = System.nanoTime();
         AtomicInteger loadedAssetCount = new AtomicInteger();
         List<CompletableFuture<Void>> futures = new ObjectArrayList();

         try (BufferedReader reader = Files.newBufferedReader(assetCacheFile)) {
            int version = 0;
            int i = 0;

            while (true) {
               String line = reader.readLine();
               if (line == null) {
                  break;
               }

               if (line.startsWith("VERSION=")) {
                  version = Integer.parseInt(line.substring("VERSION=".length()));
                  this.getLogger().at(Level.FINEST).log("Version set to %d from CommonAssetsIndex.cache:L%d '%s'", version, i, line);
                  if (version > 1) {
                     throw new IllegalArgumentException(String.format("Unsupported version %d in CommonAssetsIndex.cache %d > %d", version, version, 1));
                  }
               } else {
                  int indexOne = line.indexOf(32);
                  int indexTwo = line.indexOf(32, indexOne + 1);
                  if (indexTwo < 0) {
                     this.getLogger().at(Level.WARNING).log("Corrupt line in CommonAssetsIndex.cache:L%d '%s'", i, line);
                  } else {
                     String hash = line.substring(0, indexOne);
                     if (hash.length() != 64 && !CommonAsset.HASH_PATTERN.matcher(hash).matches()) {
                        this.getLogger().at(Level.WARNING).log("Corrupt line in CommonAssetsIndex.cache:L%d '%s'", i, line);
                     } else {
                        long timestampLong = Long.parseLong(line, indexOne + 1, indexTwo, 10);
                        Instant timestamp;
                        if (version > 0) {
                           timestamp = Instant.ofEpochSecond(timestampLong);
                        } else {
                           long timestampMillis = timestampLong / 10000L;
                           timestamp = TICK_TIMESTAMP_ORIGIN.plusMillis(timestampMillis);
                        }

                        String name = line.substring(indexTwo + 1);
                        Path file = commonPath.resolve(name);
                        int lineNumber = i;
                        futures.add(
                           CompletableFuture.supplyAsync(
                              () -> {
                                 BasicFileAttributes attributes;
                                 try {
                                    attributes = Files.readAttributes(file, BasicFileAttributes.class);
                                 } catch (IOException var10x) {
                                    return null;
                                 }

                                 if (!attributes.isRegularFile()) {
                                    return null;
                                 } else {
                                    Instant lastModified = attributes.lastModifiedTime().toInstant().truncatedTo(ChronoUnit.SECONDS);
                                    if (timestamp.equals(lastModified)) {
                                       this.addCommonAsset(pack.getName(), new FileCommonAsset(file, name, hash, null), false);
                                       this.getLogger().at(Level.FINEST).log("Loaded asset info from CommonAssetsIndex.cache:L%d '%s'", lineNumber, name);
                                       loadedAssetCount.getAndIncrement();
                                    } else {
                                       this.getLogger()
                                          .at(Level.FINEST)
                                          .log(
                                             "Skipped outdated asset from CommonAssetsIndex.cache:L%d '%s', Timestamp: %s, Last Modified: %s",
                                             lineNumber,
                                             name,
                                             timestamp,
                                             lastModified
                                          );
                                    }

                                    return null;
                                 }
                              }
                           )
                        );
                     }
                  }
               }

               i++;
            }
         } catch (IOException var24) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(var24)).log("Failed to load hashes from CommonAssetsIndex.cache");
         }

         CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
         long loadCacheEnd = System.nanoTime();
         long loadCacheDiff = loadCacheEnd - loadCacheStart;
         this.getLogger()
            .at(Level.INFO)
            .log("Took %s to load %d assets from CommonAssetsIndex.cache file.", FormatUtil.nanosToString(loadCacheDiff), loadedAssetCount.get());
      }
   }

   private void walkFileTree(@Nonnull final AssetPack pack) throws IOException {
      Path assetPath = pack.getRoot();
      Path commonPath = assetPath.resolve("Common").toAbsolutePath();
      if (Files.exists(commonPath)) {
         final int commonPathSubStringIndex = commonPath.toString().length() + 1;
         long walkFileTreeStart = System.nanoTime();
         final ObjectArrayList<CompletableFuture<Void>> futures = new ObjectArrayList();
         Files.walkFileTree(commonPath, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Nonnull
            public FileVisitResult visitFile(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) throws IOException {
               if (!attrs.isRegularFile()) {
                  return FileVisitResult.CONTINUE;
               } else {
                  Path fileName = path.getFileName();
                  if (CommonAssetModule.IGNORED_FILES.contains(fileName)) {
                     String name = PatternUtil.replaceBackslashWithForwardSlash(path.toString().substring(commonPathSubStringIndex));
                     CommonAssetModule.this.getLogger().at(Level.FINEST).log("Skipping ignored file at %s", name);
                     return FileVisitResult.CONTINUE;
                  } else if (fileName.toString().endsWith(".hash")) {
                     Files.deleteIfExists(path);
                     return FileVisitResult.CONTINUE;
                  } else {
                     String name = PatternUtil.replaceBackslashWithForwardSlash(path.toString().substring(commonPathSubStringIndex));
                     if (CommonAssetRegistry.hasCommonAsset(pack, name)) {
                        return FileVisitResult.CONTINUE;
                     } else {
                        CommonAssetModule.this.getLogger().at(Level.FINER).log("Loading asset: %s", name);
                        futures.add(CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier(() -> Files.readAllBytes(path))).thenAcceptAsync(bytes -> {
                           FileCommonAsset asset = new FileCommonAsset(path, name, bytes);
                           CommonAssetModule.this.addCommonAsset(pack.getName(), asset, false);
                           CommonAssetModule.this.getLogger().at(Level.FINER).log("Loaded asset: %s", asset);
                        }).exceptionally(throwable -> {
                           ((HytaleLogger.Api)CommonAssetModule.this.getLogger().at(Level.FINE).withCause(throwable)).log("Failed to load asset: %s", name);
                           throw SneakyThrow.sneakyThrow(throwable);
                        }));
                        return FileVisitResult.CONTINUE;
                     }
                  }
               }
            }
         });
         CompletableFuture.allOf((CompletableFuture<?>[])futures.toArray(CompletableFuture[]::new)).join();
         this.assets.invalidate();
         long walkFileTreeEnd = System.nanoTime();
         long walkFileTreeDiff = walkFileTreeEnd - walkFileTreeStart;
         this.getLogger().at(Level.INFO).log("Took %s to walk file tree and load %d assets.", FormatUtil.nanosToString(walkFileTreeDiff), futures.size());
      }
   }

   private void unregisterAssetMonitor(@Nonnull AssetPack pack) {
      AssetMonitor assetMonitor = AssetModule.get().getAssetMonitor();
      if (assetMonitor != null) {
         assetMonitor.removeMonitorDirectoryFiles(pack.getRoot().resolve("Common"), pack);
      }
   }

   private void reloadAsset(@Nonnull List<CompletableFuture<Void>> addedOrUpdatedAssets, String pack, @Nonnull Path file, @Nonnull String name) {
      this.getLogger().at(Level.FINEST).log("Reloading: %s", file);
      addedOrUpdatedAssets.add(
         CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier(() -> Files.readAllBytes(file)))
            .thenAcceptAsync(bytes -> this.addCommonAsset(pack, new FileCommonAsset(file, name, bytes)))
            .exceptionally(throwable -> {
               if (throwable instanceof NoSuchFileException) {
                  throwable = new SkipSentryException(throwable);
               }

               ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(throwable)).log("Failed to reload asset: %s", file);
               return null;
            })
      );
   }

   private void onSendCommonAssets(@Nonnull SendCommonAssetsEvent event) {
      this.sendAssetsToPlayer(event.getPacketHandler(), event.getRequestedAssets(), true);
   }

   public void sendAssetsToPlayer(@Nonnull PacketHandler packetHandler, @Nullable Asset[] requested, boolean forceRebuild) {
      List<CommonAsset> toSend = new ObjectArrayList();
      if (requested != null) {
         for (Asset toSendAsset : requested) {
            CommonAsset asset = CommonAssetRegistry.getByHash(toSendAsset.hash);
            Objects.requireNonNull(asset, toSendAsset.hash);
            toSend.add(asset);
         }
      } else {
         for (List<CommonAssetRegistry.PackAsset> asset : CommonAssetRegistry.getAllAssets()) {
            toSend.add(asset.getLast().asset());
         }
      }

      this.getLogger().at(Level.FINE).log("%s requested %d assets!", packetHandler.getIdentifier(), toSend.size());
      this.sendAssetsToPlayer(packetHandler, toSend, forceRebuild);
   }

   public void sendAssets(@Nonnull List<CommonAsset> toSend, boolean forceRebuild) {
      for (int i = 0; i < toSend.size(); i++) {
         CommonAsset thisAsset = toSend.get(i);
         byte[] allBytes = thisAsset.getBlob().join();
         byte[][] parts = ArrayUtil.split(allBytes, 2621440);
         ToClientPacket[] packets = new ToClientPacket[2 + parts.length];
         packets[0] = new AssetInitialize(thisAsset.toPacket(), allBytes.length);

         for (int partIndex = 0; partIndex < parts.length; partIndex++) {
            packets[1 + partIndex] = new AssetPart(parts[partIndex]);
         }

         packets[packets.length - 1] = new AssetFinalize();
         Universe.get().broadcastPacket(packets);
      }

      if (!toSend.isEmpty() && forceRebuild) {
         Universe.get().broadcastPacketNoCache(new RequestCommonAssetsRebuild());
      }
   }

   public void sendAssetsToPlayer(@Nonnull PacketHandler packetHandler, @Nonnull List<CommonAsset> toSend, boolean forceRebuild) {
      for (int i = 0; i < toSend.size(); i++) {
         int thisPercent = MathUtil.getPercentageOf(i, toSend.size());
         CommonAsset thisAsset = toSend.get(i);
         byte[] allBytes = thisAsset.getBlob().join();
         byte[][] parts = ArrayUtil.split(allBytes, 2621440);
         ToClientPacket[] packets = new ToClientPacket[2 + parts.length * 2];
         packets[0] = new AssetInitialize(thisAsset.toPacket(), allBytes.length);

         for (int partIndex = 0; partIndex < parts.length; partIndex++) {
            packets[1 + partIndex * 2] = new WorldLoadProgress(
               Message.translation("client.general.worldLoad.loadingAsset").param("assetName", thisAsset.getName()).getFormattedMessage(),
               thisPercent,
               100 * partIndex / parts.length
            );
            packets[1 + partIndex * 2 + 1] = new AssetPart(parts[partIndex]);
         }

         packets[packets.length - 1] = new AssetFinalize();
         packetHandler.write(packets);
      }

      if (!toSend.isEmpty() && forceRebuild) {
         packetHandler.writeNoCache(new RequestCommonAssetsRebuild());
      }
   }

   public void sendAsset(@Nonnull CommonAsset asset, boolean forceRebuild) {
      asset.getBlob().whenComplete((allBytes, throwable) -> {
         if (throwable != null) {
            this.getLogger().at(Level.WARNING).log("Failed to send asset: %s, %s", asset.getName(), asset.getHash());
         } else {
            byte[][] parts = ArrayUtil.split(allBytes, 2621440);
            ToClientPacket[] packets = new ToClientPacket[2 + (forceRebuild ? 1 : 0) + parts.length];
            packets[0] = new AssetInitialize(asset.toPacket(), allBytes.length);

            for (int i = 0; i < parts.length; i++) {
               packets[1 + i] = new AssetPart(parts[i]);
            }

            packets[1 + parts.length] = new AssetFinalize();
            if (forceRebuild) {
               packets[2 + parts.length] = new RequestCommonAssetsRebuild();
            }

            Universe.get().broadcastPacket(packets);
         }
      });
   }

   public void sendRemoveAssets(@Nonnull List<CommonAssetRegistry.PackAsset> assets, boolean forceRebuild) {
      int size = assets.size();
      Asset[] asset_ = new Asset[size];
      String messageRemovalKey = "server.general.assetstore.removedAssets";
      String color = "#FF3874";
      String icon = "Icons/AssetNotifications/Trash.png";
      Message message = Message.translation("server.general.assetstore.removedAssets").param("class", "Common").color("#FF3874");
      int packetCountThreshold = 5;
      int packetsCount = 1 + (forceRebuild ? 1 : 0) + (assets.size() < 5 ? assets.size() : 1);
      ToClientPacket[] packets = new ToClientPacket[packetsCount];
      int i = 0;

      for (CommonAssetRegistry.PackAsset asset : assets) {
         asset_[i++] = asset.asset().toPacket();
      }

      if (assets.size() < 5) {
         i = 0;

         for (CommonAssetRegistry.PackAsset asset : assets) {
            Message assetName = Message.raw(asset.pack() + ":" + asset.asset().getName()).color("#FF3874");
            packets[i++] = new Notification(
               message.getFormattedMessage(), assetName.getFormattedMessage(), "Icons/AssetNotifications/Trash.png", null, NotificationStyle.Default
            );
         }

         packets[i++] = new RemoveAssets(asset_);
         if (forceRebuild) {
            packets[i++] = new RequestCommonAssetsRebuild();
         }
      } else {
         Message secondaryMessage = Message.translation("server.general.assetstore.removedAssetsSecondaryGeneric").param("count", assets.size());
         packets[0] = new Notification(
            message.getFormattedMessage(), secondaryMessage.getFormattedMessage(), "Icons/AssetNotifications/Trash.png", null, NotificationStyle.Default
         );
         packets[1] = new RemoveAssets(asset_);
         if (forceRebuild) {
            packets[2] = new RequestCommonAssetsRebuild();
         }
      }

      Universe.get().broadcastPacket(packets);
   }

   private class CommonAssetMonitorHandler implements AssetMonitorHandler {
      private final AssetPack pack;
      private final Path commonPath;

      public CommonAssetMonitorHandler(AssetPack pack, Path commonPath) {
         this.pack = pack;
         this.commonPath = commonPath;
      }

      @Override
      public Object getKey() {
         return this.pack;
      }

      public boolean test(Path path, EventKind eventKind) {
         return !CommonAssetModule.IGNORED_FILES.contains(path.getFileName());
      }

      public void accept(Map<Path, EventKind> map) {
         List<Path> createdOrModifiedFilesToLoad = new ObjectArrayList();
         List<Path> removedFilesToUnload = new ObjectArrayList();
         List<Path> createdOrModifiedDirectories = new ObjectArrayList();
         List<Path> removedFilesAndDirectories = new ObjectArrayList();

         for (Entry<Path, EventKind> entry : map.entrySet()) {
            Path path = entry.getKey();
            EventKind eventKind = entry.getValue();
            switch (eventKind) {
               case ENTRY_CREATE:
                  if (Files.isDirectory(path)) {
                     CommonAssetModule.this.getLogger().at(Level.INFO).log("Directory Created: %s", path);

                     try (Stream<Path> stream = Files.walk(path, FileUtil.DEFAULT_WALK_TREE_OPTIONS_ARRAY)) {
                        stream.forEach(child -> {
                           BasicFileAttributes attributes;
                           try {
                              attributes = Files.readAttributes(child, BasicFileAttributes.class);
                           } catch (IOException var6) {
                              return;
                           }

                           if (attributes.isDirectory()) {
                              createdOrModifiedDirectories.add(path);
                           } else if (attributes.isRegularFile()) {
                              createdOrModifiedFilesToLoad.add(child);
                           }
                        });
                     } catch (IOException var17) {
                        ((HytaleLogger.Api)CommonAssetModule.this.getLogger().at(Level.SEVERE).withCause(var17))
                           .log("Failed to reload assets in directory: %s", path);
                     }
                  } else {
                     CommonAssetModule.this.getLogger().at(Level.INFO).log("File Created: %s", path);
                     createdOrModifiedFilesToLoad.add(path);
                  }
                  break;
               case ENTRY_DELETE:
                  CommonAssetModule.this.getLogger().at(Level.INFO).log("Deleted: %s", path);
                  removedFilesAndDirectories.add(path);
                  Path relative = PathUtil.relativize(this.commonPath, path);
                  String name = PatternUtil.replaceBackslashWithForwardSlash(relative.toString());

                  for (CommonAsset asset : CommonAssetRegistry.getCommonAssetsStartingWith(this.pack.getName(), name)) {
                     removedFilesToUnload.add(this.commonPath.resolve(asset.getName()));
                  }
                  break;
               case ENTRY_MODIFY:
                  if (Files.isDirectory(path)) {
                     CommonAssetModule.this.getLogger().at(Level.INFO).log("Directory Modified: %s", path);
                     createdOrModifiedDirectories.add(path);
                  } else {
                     CommonAssetModule.this.getLogger().at(Level.INFO).log("File Modified: %s", path);
                     createdOrModifiedFilesToLoad.add(path);
                  }
                  break;
               default:
                  throw new IllegalArgumentException("Unknown eventKind " + eventKind);
            }
         }

         if (!removedFilesAndDirectories.isEmpty() || !createdOrModifiedFilesToLoad.isEmpty() || !createdOrModifiedDirectories.isEmpty()) {
            IEventDispatcher<CommonAssetMonitorEvent, CommonAssetMonitorEvent> dispatchFor = HytaleServer.get()
               .getEventBus()
               .dispatchFor(CommonAssetMonitorEvent.class);
            if (dispatchFor.hasListener()) {
               dispatchFor.dispatch(
                  new CommonAssetMonitorEvent(
                     this.pack.getName(), createdOrModifiedFilesToLoad, removedFilesToUnload, createdOrModifiedDirectories, removedFilesAndDirectories
                  )
               );
            }
         }

         List<CompletableFuture<Void>> addedOrUpdatedAssets = new ObjectArrayList();
         List<CommonAssetRegistry.PackAsset> removedAssets = new ObjectArrayList();
         List<CommonAsset> updatedAssets = new ObjectArrayList();
         if (!removedFilesToUnload.isEmpty()) {
            CommonAssetModule.this.getLogger().at(Level.INFO).log("Removing deleted assets: %s", removedFilesToUnload);

            for (Path path : removedFilesToUnload) {
               Path relativePath = PathUtil.relativize(this.commonPath, path);
               String name = PatternUtil.replaceBackslashWithForwardSlash(relativePath.toString());
               BooleanObjectPair<CommonAssetRegistry.PackAsset> removed = CommonAssetRegistry.removeCommonAssetByName(this.pack.getName(), name);
               if (removed != null) {
                  if (removed.firstBoolean()) {
                     updatedAssets.add(((CommonAssetRegistry.PackAsset)removed.second()).asset());
                  } else {
                     removedAssets.add((CommonAssetRegistry.PackAsset)removed.second());
                  }
               }

               CommonAssetModule.this.assets.invalidate();
            }

            CommonAssetModule.this.sendRemoveAssets(removedAssets, false);
            CommonAssetModule.this.sendAssets(updatedAssets, false);
         }

         if (!createdOrModifiedFilesToLoad.isEmpty()) {
            CommonAssetModule.this.getLogger().at(Level.INFO).log("Reloading assets: %s", createdOrModifiedFilesToLoad);

            for (Path path : createdOrModifiedFilesToLoad) {
               Path relative = PathUtil.relativize(this.commonPath, path);
               String name = PatternUtil.replaceBackslashWithForwardSlash(relative.toString());
               CommonAssetModule.this.reloadAsset(addedOrUpdatedAssets, this.pack.getName(), path, name);
            }

            CompletableFuture.allOf(addedOrUpdatedAssets.toArray(CompletableFuture[]::new))
               .thenAccept(v -> Universe.get().broadcastPacketNoCache(new RequestCommonAssetsRebuild()));
         } else {
            Universe.get().broadcastPacketNoCache(new RequestCommonAssetsRebuild());
         }
      }
   }
}
