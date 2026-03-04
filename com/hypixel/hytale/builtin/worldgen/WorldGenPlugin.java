package com.hypixel.hytale.builtin.worldgen;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.worldgen.BiomeDataSystem;
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldGenPlugin extends JavaPlugin {
   private static final String VERSIONS_DIR_NAME = "$Versions";
   private static final String MANIFEST_FILENAME = "manifest.json";
   private static WorldGenPlugin instance;

   public static WorldGenPlugin get() {
      return instance;
   }

   public WorldGenPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      this.getEntityStoreRegistry().registerSystem(new BiomeDataSystem());
      IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "Hytale", HytaleWorldGenProvider.class, HytaleWorldGenProvider.CODEC);
      AssetModule assets = AssetModule.get();
      if (assets.getAssetPacks().isEmpty()) {
         this.getLogger().at(Level.SEVERE).log("No asset packs loaded");
      } else {
         FileIO.setDefaultRoot(assets.getBaseAssetPack().getRoot());
         List<WorldGenPlugin.Version> packs = loadVersionPacks(assets);
         Object2ObjectOpenHashMap<String, Semver> versions = new Object2ObjectOpenHashMap();

         for (WorldGenPlugin.Version version : packs) {
            validateVersion(version, packs);
            assets.registerPack(version.getPackName(), version.path, version.manifest, false);
            Semver latest = (Semver)versions.get(version.name);
            if (latest == null || version.manifest.getVersion().compareTo(latest) > 0) {
               versions.put(version.name, version.manifest.getVersion());
            }
         }

         HytaleWorldGenProvider.CODEC.setVersions(versions);
      }
   }

   private static List<WorldGenPlugin.Version> loadVersionPacks(@Nonnull AssetModule assets) {
      Path versionsDir = getVersionsPath();
      if (!Files.exists(versionsDir)) {
         return ObjectLists.emptyList();
      } else {
         Path root = assets.getBaseAssetPack().getRoot();
         Path assetPath = root.relativize(Universe.getWorldGenPath());

         try {
            ObjectArrayList var14;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(versionsDir)) {
               ObjectArrayList<WorldGenPlugin.Version> list = new ObjectArrayList();

               for (Path path : stream) {
                  if (Files.isDirectory(path)) {
                     String name = getWorldConfigName(path, assetPath);
                     if (name != null) {
                        Path manifestPath = path.resolve("manifest.json");
                        if (Files.exists(manifestPath)) {
                           PluginManifest manifest = loadManifest(manifestPath);
                           if (manifest != null) {
                              list.add(new WorldGenPlugin.Version(name, path, manifest));
                           }
                        }
                     }
                  }
               }

               Collections.sort(list);
               var14 = list;
            }

            return var14;
         } catch (IOException var13) {
            throw new RuntimeException(var13);
         }
      }
   }

   private static void validateVersion(@Nonnull WorldGenPlugin.Version version, @Nonnull List<WorldGenPlugin.Version> versions) {
      if (version.manifest.getVersion().compareTo(HytaleWorldGenProvider.MIN_VERSION) <= 0) {
         throw new IllegalArgumentException(
            String.format(
               "Invalid $Version AssetPack: %s. Pack version number: %s must be greater than: %s",
               version.path(),
               version.manifest.getVersion(),
               HytaleWorldGenProvider.MIN_VERSION
            )
         );
      } else {
         for (WorldGenPlugin.Version other : versions) {
            if (other != version && version.name().equals(other.name()) && version.manifest.getVersion().equals(other.manifest.getVersion())) {
               throw new IllegalArgumentException(
                  String.format(
                     "$Version AssetPack: %s conflicts with pack: %s. Pack version numbers must be different. Found: %s in both",
                     version.path(),
                     other.path(),
                     version.manifest.getVersion()
                  )
               );
            }
         }
      }
   }

   @Nullable
   private static String getWorldConfigName(@Nonnull Path packPath, @Nonnull Path assetPath) {
      Path filepath = packPath.resolve(assetPath);
      if (Files.exists(filepath) && Files.isDirectory(filepath)) {
         try {
            String var6;
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(filepath)) {
               Iterator<Path> it = dirStream.iterator();
               if (!it.hasNext()) {
                  LogUtil.getLogger().at(Level.WARNING).log("WorldGen version pack: %s is empty", packPath);
                  return null;
               }

               Path path = it.next();
               if (it.hasNext()) {
                  LogUtil.getLogger().at(Level.WARNING).log("WorldGen version pack: %s contains multiple world configs", packPath);
                  return null;
               }

               if (!Files.isDirectory(path)) {
                  LogUtil.getLogger().at(Level.WARNING).log("WorldGen version pack: %s does not contain a world config directory", packPath);
                  return null;
               }

               var6 = path.getFileName().toString();
            }

            return var6;
         } catch (IOException var9) {
            throw new RuntimeException(var9);
         }
      } else {
         LogUtil.getLogger().at(Level.WARNING).log("WorldGen version pack: %s does not contain dir: %s", packPath, assetPath);
         return null;
      }
   }

   @Nullable
   private static PluginManifest loadManifest(@Nonnull Path manifestPath) throws IOException {
      if (!Files.exists(manifestPath)) {
         return null;
      } else {
         PluginManifest var6;
         try (BufferedReader reader = Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8)) {
            char[] buffer = RawJsonReader.READ_BUFFER.get();
            RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
            ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
            PluginManifest manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
            extraInfo.getValidationResults().logOrThrowValidatorExceptions(LogUtil.getLogger());
            var6 = manifest;
         }

         return var6;
      }
   }

   public static Path getVersionsPath() {
      return Universe.getWorldGenPath().resolve("$Versions");
   }

   public record Version(@Nonnull String name, @Nonnull Path path, @Nonnull PluginManifest manifest) implements Comparable<WorldGenPlugin.Version> {
      public int compareTo(WorldGenPlugin.Version o) {
         return this.manifest.getVersion().compareTo(o.manifest.getVersion());
      }

      @Nonnull
      public String getPackName() {
         String group = Objects.requireNonNullElse(this.manifest.getGroup(), "Unknown");
         String name = Objects.requireNonNullElse(this.manifest.getName(), "Unknown");
         return group + ":" + name;
      }
   }
}
