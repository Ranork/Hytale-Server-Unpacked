package com.hypixel.hytale.builtin.hytalegenerator.plugin.editor;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;
import org.bson.json.JsonWriterSettings;

public interface AssetPackUtil {
   String ASSET_STORE_TYPE = "Server";
   String INSTANCES_PATH = "Instances";
   Semver DEFAULT_VERSION = new Semver(1L, 0L, 0L);
   JsonWriterSettings JSON_SETTINGS = JsonWriterSettings.builder().indent(true).build();

   @Nonnull
   static CompletableFuture<AssetPack> getOrCreatePack(@Nonnull String group, @Nonnull String name) {
      String packId = group + ":" + name;
      AssetModule module = AssetModule.get();
      AssetPack pack = module.getAssetPack(packId);
      return pack != null
         ? CompletableFuture.completedFuture(pack)
         : CompletableFuture.supplyAsync(
            SneakyThrow.sneakySupplier(
               () -> {
                  PluginManifest manifest = new PluginManifest(
                     group, name, DEFAULT_VERSION, null, List.of(), null, null, SemverRange.WILDCARD, Map.of(), Map.of(), Map.of(), List.of(), false
                  );
                  Path packPath = PluginManager.MODS_PATH.resolve(group + "." + name);
                  Path manifestPath = packPath.resolve("manifest.json");
                  if (!Files.exists(packPath)) {
                     Files.createDirectories(packPath);
                  }

                  if (!Files.exists(manifestPath)) {
                     BsonDocument data = PluginManifest.CODEC.encode(manifest, new ExtraInfo()).asDocument();
                     String json = data.toJson(JSON_SETTINGS);
                     Files.writeString(manifestPath, json);
                  }

                  module.registerPack(packId, packPath, manifest, AssetPack.PackSource.RUNTIME);
                  return Objects.requireNonNull(module.getAssetPack(packId));
               }
            )
         );
   }

   static <T extends JsonAssetWithMap<String, M>, M extends AssetMap<String, T>, V extends T> void exportAsset(
      @Nonnull AssetPack pack, @Nonnull String name, @Nonnull BsonDocument asset, @Nonnull Class<T> type
   ) throws IOException {
      if (pack.isImmutable()) {
         throw new IOException(String.format("Cannot write asset to immutable AssetPack: %s", pack.getName()));
      } else {
         AssetStore<String, T, M> store = AssetRegistry.getAssetStore(type);
         if (store == null) {
            throw new IllegalArgumentException(String.format("AssetStore not found for type '%s'", type.getName()));
         } else {
            Path storePath = getStorePath(pack.getRoot(), "Server", store);
            Path filepath = storePath.resolve(name + store.getExtension());
            if (!Files.exists(filepath)) {
               if (!Files.exists(storePath)) {
                  Files.createDirectories(storePath);
                  store.addFileMonitor(pack.getName(), storePath);
               }

               if (!Files.exists(filepath.getParent())) {
                  Files.createDirectories(filepath.getParent());
               }

               String json = asset.toJson(JSON_SETTINGS);
               Files.writeString(filepath, json, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
               store.loadAssetsFromPaths(pack.getName(), List.of(filepath));
            }
         }
      }
   }

   static Path getStorePath(@Nonnull Path root, @Nonnull String type, @Nonnull AssetStore<?, ?, ?> store) {
      String separator = root.getFileSystem().getSeparator();
      String path = store.getPath().replace("\\", separator).replace("/", separator);
      return root.resolve(type).resolve(path);
   }
}
