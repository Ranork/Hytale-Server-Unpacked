package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.event.GenerateAssetsEvent;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderField;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.exception.CodecValidationException;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.codec.validation.validator.MapKeyValidator;
import com.hypixel.hytale.codec.validation.validator.MapValueValidator;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.event.IEventBus;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.logger.util.GithubMessageUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

public abstract class AssetStore<K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> {
   public static boolean DISABLE_ASSET_COMPARE = true;
   @Nonnull
   protected final HytaleLogger logger;
   @Nonnull
   protected final Class<K> kClass;
   @Nonnull
   protected final Class<T> tClass;
   protected final String path;
   @Nonnull
   protected final String extension;
   protected final AssetCodec<K, T> codec;
   protected final Function<T, K> keyFunction;
   @Nonnull
   protected final Set<Class<? extends JsonAsset<?>>> loadsAfter;
   @Nonnull
   protected final Set<Class<? extends JsonAsset<?>>> unmodifiableLoadsAfter;
   @Nonnull
   protected final Set<Class<? extends JsonAsset<?>>> loadsBefore;
   protected final M assetMap;
   protected final Function<K, T> replaceOnRemove;
   @Nonnull
   protected final Predicate<T> isUnknown;
   protected final boolean unmodifiable;
   protected final List<T> preAddedAssets;
   protected final Class<? extends JsonAsset<?>> idProvider;
   protected final Map<Class<? extends JsonAssetWithMap<?, ?>>, Map<K, Set<Object>>> childAssetsMap = new ConcurrentHashMap<>();
   @Nonnull
   protected Set<Class<? extends JsonAssetWithMap>> loadedContainedAssetsFor = new HashSet<>();
   public static boolean DISABLE_DYNAMIC_DEPENDENCIES = false;

   public AssetStore(@Nonnull AssetStore.Builder<K, T, M, ?> builder) {
      this.kClass = builder.kClass;
      this.tClass = builder.tClass;
      this.logger = HytaleLogger.get("AssetStore|" + this.tClass.getSimpleName());
      this.path = builder.path;
      this.extension = builder.extension;
      this.codec = builder.codec;
      this.keyFunction = builder.keyFunction;
      this.isUnknown = builder.isUnknown == null ? v -> false : builder.isUnknown;
      this.loadsAfter = builder.loadsAfter;
      this.unmodifiableLoadsAfter = Collections.unmodifiableSet(builder.loadsAfter);
      this.loadsBefore = Collections.unmodifiableSet(builder.loadsBefore);
      this.assetMap = builder.assetMap;
      this.replaceOnRemove = builder.replaceOnRemove;
      this.unmodifiable = builder.unmodifiable;
      this.preAddedAssets = builder.preAddedAssets;
      this.idProvider = builder.idProvider;
      if (builder.replaceOnRemove == null && this.assetMap.requireReplaceOnRemove()) {
         throw new IllegalArgumentException(
            "AssetStore for "
               + this.tClass.getSimpleName()
               + " using an AssetMap of "
               + this.assetMap.getClass().getSimpleName()
               + " must use #setReplaceOnRemove"
         );
      }
   }

   protected abstract IEventBus getEventBus();

   public abstract void addFileMonitor(@Nonnull String var1, Path var2);

   public abstract void removeFileMonitor(Path var1);

   protected abstract void handleRemoveOrUpdate(Set<K> var1, Map<K, T> var2, @Nonnull AssetUpdateQuery var3);

   @Nonnull
   public Class<K> getKeyClass() {
      return this.kClass;
   }

   @Nonnull
   public Class<T> getAssetClass() {
      return this.tClass;
   }

   public String getPath() {
      return this.path;
   }

   @Nonnull
   public String getExtension() {
      return this.extension;
   }

   public AssetCodec<K, T> getCodec() {
      return this.codec;
   }

   public Function<T, K> getKeyFunction() {
      return this.keyFunction;
   }

   @Nonnull
   public Set<Class<? extends JsonAsset<?>>> getLoadsAfter() {
      return this.unmodifiableLoadsAfter;
   }

   public M getAssetMap() {
      return this.assetMap;
   }

   public Function<K, T> getReplaceOnRemove() {
      return this.replaceOnRemove;
   }

   public boolean isUnmodifiable() {
      return this.unmodifiable;
   }

   public List<T> getPreAddedAssets() {
      return this.preAddedAssets;
   }

   public <X extends JsonAssetWithMap> boolean hasLoadedContainedAssetsFor(Class<X> x) {
      return this.loadedContainedAssetsFor.contains(x);
   }

   public Class<? extends JsonAsset<?>> getIdProvider() {
      return this.idProvider;
   }

   @Nonnull
   public HytaleLogger getLogger() {
      return this.logger;
   }

   public void simplifyLoadBeforeDependencies() {
      for (Class<? extends JsonAsset<?>> aClass : this.loadsBefore) {
         AssetRegistry.getAssetStore((Class<T>)aClass).loadsAfter.add(this.tClass);
      }
   }

   @Deprecated
   public <D extends JsonAsset<?>> void injectLoadsAfter(Class<D> aClass) {
      if (DISABLE_DYNAMIC_DEPENDENCIES) {
         throw new IllegalArgumentException("Asset stores have already loaded! Injecting a dependency is now pointless.");
      } else {
         this.loadsAfter.add(aClass);
      }
   }

   @Nullable
   public K decodeFilePathKey(@Nonnull Path path) {
      String fileName = path.getFileName().toString();
      return this.decodeStringKey(fileName.substring(0, fileName.length() - this.extension.length()));
   }

   @Nullable
   public K decodeStringKey(String key) {
      return (K)(this.codec.getKeyCodec().getChildCodec() == Codec.STRING ? key : this.codec.getKeyCodec().getChildCodec().decode(new BsonString(key)));
   }

   @Nullable
   public K transformKey(@Nullable Object o) {
      if (o == null) {
         return null;
      } else {
         return (K)(o.getClass().equals(this.kClass) ? o : this.decodeStringKey(o.toString()));
      }
   }

   public void validate(@Nullable K key, @Nonnull ValidationResults results, ExtraInfo extraInfo) {
      if (key != null) {
         if (this.assetMap.getAsset(key) == null) {
            if (extraInfo instanceof AssetExtraInfo) {
               for (AssetExtraInfo.Data data = ((AssetExtraInfo)extraInfo).getData(); data != null; data = data.getContainerData()) {
                  if (data.containsAsset(this.tClass, key)) {
                     return;
                  }
               }
            }

            results.fail("Asset '" + key + "' of type " + this.tClass.getName() + " doesn't exist!");
         }
      }
   }

   public void validateCodecDefaults() {
      ExtraInfo extraInfo = new ExtraInfo(Integer.MAX_VALUE, AssetValidationResults::new);
      this.codec.validateDefaults(extraInfo, new HashSet<>());
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.logger, "Default Asset Validation Failed!\n");
   }

   public void logDependencies() {
      ExtraInfo extraInfo = new ExtraInfo(Integer.MAX_VALUE, AssetValidationResults::new);
      HashSet<Codec<?>> tested = new HashSet<>();
      this.codec.validateDefaults(extraInfo, tested);
      Set<Class<? extends JsonAsset<?>>> assetClasses = new HashSet<>();
      Set<Class<? extends JsonAsset<?>>> maybeLateAssetClasses = new HashSet<>();

      for (Codec<?> other : tested) {
         if (other instanceof BuilderCodec) {
            for (BuilderCodec<?> builderCodec = (BuilderCodec<?>)other; builderCodec != null; builderCodec = builderCodec.getParent()) {
               for (List<? extends BuilderField<?, ?>> value : builderCodec.getEntries().values()) {
                  for (BuilderField<?, ?> field : value) {
                     if (field.supportsVersion(extraInfo.getVersion())) {
                        List<Validator<?>> validators = field.getValidators();
                        if (validators != null) {
                           for (Validator<?> validator : validators) {
                              if (validator instanceof ArrayValidator<?> arrayValidator) {
                                 validator = arrayValidator.getValidator();
                              } else if (validator instanceof MapKeyValidator<?> arrayValidator) {
                                 validator = arrayValidator.getKeyValidator();
                              } else if (validator instanceof MapValueValidator<?> arrayValidator) {
                                 validator = arrayValidator.getValueValidator();
                              }

                              if (validator instanceof AssetKeyValidator assetKeyValidator) {
                                 assetClasses.add(assetKeyValidator.getStore().getAssetClass());
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else if (other instanceof ContainedAssetCodec<?, ?, ?> containedAssetCodec) {
            maybeLateAssetClasses.add((Class<? extends JsonAsset<?>>)containedAssetCodec.getAssetClass());
         }
      }

      HashSet<Object> missing = new HashSet<>();
      HashSet<Object> unused = new HashSet<>();

      for (Class<? extends JsonAsset<?>> assetClass : assetClasses) {
         if (!this.loadsAfter.contains(assetClass)) {
            missing.add(assetClass);
         }
      }

      for (Class<? extends JsonAsset<?>> aClass : this.loadsAfter) {
         if (!assetClasses.contains(aClass) && !maybeLateAssetClasses.contains(aClass)) {
            unused.add(aClass);
         }
      }

      if (!missing.isEmpty()) {
         this.logger.at(Level.WARNING).log("\nMissing Dependencies:" + missing.stream().map(Object::toString).collect(Collectors.joining("\n- ", "\n- ", "")));
      }

      if (!unused.isEmpty()) {
         this.logger.at(Level.WARNING).log("\nUnused Dependencies:" + unused.stream().map(Object::toString).collect(Collectors.joining("\n- ", "\n- ", "")));
      }
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssetsFromDirectory(@Nonnull String packKey, @Nonnull Path assetsPath) throws IOException {
      if (this.unmodifiable) {
         throw new UnsupportedOperationException("AssetStore is unmodifiable!");
      } else {
         Objects.requireNonNull(assetsPath, "assetsPath can't be null");
         final ArrayList<Path> files = new ArrayList<>();
         Set<FileVisitOption> optionsSet = Set.of();
         Files.walkFileTree(assetsPath, optionsSet, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Nonnull
            public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
               if (attrs.isRegularFile() && file.toString().endsWith(AssetStore.this.extension)) {
                  files.add(file);
               }

               return FileVisitResult.CONTINUE;
            }
         });
         return this.loadAssetsFromPaths(packKey, files);
      }
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssetsFromPaths(@Nonnull String packKey, @Nonnull List<Path> paths) {
      return this.loadAssetsFromPaths(packKey, paths, AssetUpdateQuery.DEFAULT);
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssetsFromPaths(@Nonnull String packKey, @Nonnull Collection<Path> paths, @Nonnull AssetUpdateQuery query) {
      return this.loadAssetsFromPaths(packKey, paths, query, false);
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssetsFromPaths(
      @Nonnull String packKey, @Nonnull Collection<Path> paths, @Nonnull AssetUpdateQuery query, boolean forceLoadAll
   ) {
      if (this.unmodifiable) {
         throw new UnsupportedOperationException("AssetStore is unmodifiable!");
      } else {
         Objects.requireNonNull(paths, "paths can't be null");
         long start = System.nanoTime();
         Set<Path> documents = new HashSet<>();

         for (Path path : paths) {
            Path normalize = path.toAbsolutePath().normalize();
            Set<K> keys = this.assetMap.getKeys(normalize);
            if (keys != null) {
               for (K key : keys) {
                  this.loadAllChildren(documents, key);
               }
            }

            documents.add(normalize);
            this.loadAllChildren(documents, this.decodeFilePathKey(path));
         }

         List<RawAsset<K>> rawAssets = new ArrayList<>(documents.size());

         for (Path p : documents) {
            rawAssets.add(new RawAsset<>(this.decodeFilePathKey(p), p));
         }

         Map<K, T> loadedAssets = Collections.synchronizedMap(new Object2ObjectLinkedOpenHashMap());
         Map<K, Path> loadedKeyToPathMap = new ConcurrentHashMap<>();
         Set<K> failedToLoadKeys = ConcurrentHashMap.newKeySet();
         Set<Path> failedToLoadPaths = ConcurrentHashMap.newKeySet();
         Map<Class<? extends JsonAssetWithMap>, AssetLoadResult> childAssetResults = new ConcurrentHashMap<>();
         this.loadAssets0(packKey, loadedAssets, rawAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, query, forceLoadAll, childAssetResults);
         long end = System.nanoTime();
         long diff = end - start;
         this.logger
            .at(Level.FINE)
            .log(
               "Loaded %d and removed %s (%s total) of %s from %s files in %s",
               loadedAssets.size(),
               failedToLoadKeys.size(),
               this.assetMap.getAssetCount(),
               this.tClass.getSimpleName(),
               paths.size(),
               FormatUtil.nanosToString(diff)
            );
         return new AssetLoadResult<>(loadedAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, childAssetResults);
      }
   }

   @Nonnull
   public AssetLoadResult<K, T> loadBuffersWithKeys(
      @Nonnull String packKey, @Nonnull List<RawAsset<K>> preLoaded, @Nonnull AssetUpdateQuery query, boolean forceLoadAll
   ) {
      long start = System.nanoTime();
      Set<Path> documents = new HashSet<>();

      for (RawAsset<K> document : preLoaded) {
         this.loadAllChildren(documents, document.getKey());
      }

      List<RawAsset<K>> rawAssets = new ArrayList<>(preLoaded.size() + documents.size());
      rawAssets.addAll(preLoaded);

      for (Path p : documents) {
         rawAssets.add(new RawAsset<>(this.decodeFilePathKey(p), p));
      }

      Map<K, T> loadedAssets = Collections.synchronizedMap(new Object2ObjectLinkedOpenHashMap());
      Map<K, Path> loadedKeyToPathMap = new ConcurrentHashMap<>();
      Set<K> failedToLoadKeys = ConcurrentHashMap.newKeySet();
      Set<Path> failedToLoadPaths = ConcurrentHashMap.newKeySet();
      Map<Class<? extends JsonAssetWithMap>, AssetLoadResult> childAssetResults = new ConcurrentHashMap<>();
      this.loadAssets0(packKey, loadedAssets, rawAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, query, forceLoadAll, childAssetResults);
      long end = System.nanoTime();
      long diff = end - start;
      this.logger
         .at(Level.FINE)
         .log(
            "Loaded %d and removed %s (%s total) of %s via loadBuffersWithKeys in %s",
            loadedAssets.size(),
            failedToLoadKeys.size(),
            this.assetMap.getAssetCount(),
            this.tClass.getSimpleName(),
            FormatUtil.nanosToString(diff)
         );
      return new AssetLoadResult<>(loadedAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, childAssetResults);
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssets(@Nonnull String packKey, @Nonnull List<T> assets) {
      return this.loadAssets(packKey, assets, AssetUpdateQuery.DEFAULT);
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssets(@Nonnull String packKey, @Nonnull List<T> assets, @Nonnull AssetUpdateQuery query) {
      return this.loadAssets(packKey, assets, query, false);
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssets(@Nonnull String packKey, @Nonnull List<T> assets, @Nonnull AssetUpdateQuery query, boolean forceLoadAll) {
      if (this.unmodifiable) {
         throw new UnsupportedOperationException("AssetStore is unmodifiable!");
      } else {
         Objects.requireNonNull(assets, "assets can't be null");
         long start = System.nanoTime();
         Map<K, T> loadedAssets = Collections.synchronizedMap(new Object2ObjectLinkedOpenHashMap());
         Set<Path> documents = new HashSet<>();
         this.loadAllChildren(loadedAssets, assets, documents);
         List<RawAsset<K>> rawAssets = new ArrayList<>(documents.size());

         for (Path p : documents) {
            rawAssets.add(new RawAsset<>(this.decodeFilePathKey(p), p));
         }

         Map<K, Path> loadedKeyToPathMap = new ConcurrentHashMap<>();
         Set<K> failedToLoadKeys = ConcurrentHashMap.newKeySet();
         Set<Path> failedToLoadPaths = ConcurrentHashMap.newKeySet();
         Map<Class<? extends JsonAssetWithMap>, AssetLoadResult> childAssetResults = new ConcurrentHashMap<>();
         this.loadAssets0(packKey, loadedAssets, rawAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, query, forceLoadAll, childAssetResults);
         long end = System.nanoTime();
         long diff = end - start;
         this.logger
            .at(Level.FINE)
            .log(
               "Loaded %d and removed %s (%s total) of %s via loadAssets in %s",
               loadedAssets.size(),
               failedToLoadKeys.size(),
               this.assetMap.getAssetCount(),
               this.tClass.getSimpleName(),
               FormatUtil.nanosToString(diff)
            );
         return new AssetLoadResult<>(loadedAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, childAssetResults);
      }
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssetsWithReferences(@Nonnull String packKey, @Nonnull Map<T, List<AssetReferences<?, ?>>> assets) {
      return this.loadAssetsWithReferences(packKey, assets, AssetUpdateQuery.DEFAULT);
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssetsWithReferences(
      @Nonnull String packKey, @Nonnull Map<T, List<AssetReferences<?, ?>>> assets, @Nonnull AssetUpdateQuery query
   ) {
      return this.loadAssetsWithReferences(packKey, assets, query, false);
   }

   @Nonnull
   public AssetLoadResult<K, T> loadAssetsWithReferences(
      @Nonnull String packKey, @Nonnull Map<T, List<AssetReferences<?, ?>>> assets, @Nonnull AssetUpdateQuery query, boolean forceLoadAll
   ) {
      if (this.unmodifiable) {
         throw new UnsupportedOperationException("AssetStore is unmodifiable!");
      } else {
         Objects.requireNonNull(assets, "assets can't be null");
         long start = System.nanoTime();
         Map<K, T> loadedAssets = Collections.synchronizedMap(new Object2ObjectLinkedOpenHashMap());
         Set<T> assetKeys = assets.keySet();
         Set<Path> documents = new HashSet<>();
         this.loadAllChildren(loadedAssets, assetKeys, documents);
         List<RawAsset<K>> rawAssets = new ArrayList<>(documents.size());

         for (Path p : documents) {
            rawAssets.add(new RawAsset<>(this.decodeFilePathKey(p), p));
         }

         Map<K, Path> loadedKeyToPathMap = new ConcurrentHashMap<>();
         Set<K> failedToLoadKeys = ConcurrentHashMap.newKeySet();
         Set<Path> failedToLoadPaths = ConcurrentHashMap.newKeySet();
         Map<Class<? extends JsonAssetWithMap>, AssetLoadResult> childAssetResults = new ConcurrentHashMap<>();
         this.loadAssets0(packKey, loadedAssets, rawAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, query, forceLoadAll, childAssetResults);

         for (Entry<T, List<AssetReferences<?, ?>>> entry : assets.entrySet()) {
            T asset = entry.getKey();
            Objects.requireNonNull(asset, "asset can't be null");
            K key = this.keyFunction.apply(asset);
            if (key == null) {
               throw new NullPointerException(String.format("key can't be null: %s", asset));
            }

            for (AssetReferences<?, ?> references : entry.getValue()) {
               references.addChildAssetReferences(this.tClass, key);
            }
         }

         long end = System.nanoTime();
         long diff = end - start;
         this.logger
            .at(Level.FINE)
            .log(
               "Loaded %d and removed %s (%s total) of %s via loadAssetsWithReferences in %s",
               loadedAssets.size(),
               failedToLoadKeys.size(),
               this.assetMap.getAssetCount(),
               this.tClass.getSimpleName(),
               FormatUtil.nanosToString(diff)
            );
         return new AssetLoadResult<>(loadedAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, childAssetResults);
      }
   }

   @Nonnull
   public Set<K> removeAssetWithPaths(@Nonnull String packKey, @Nonnull List<Path> paths) {
      return this.removeAssetWithPaths(packKey, paths, AssetUpdateQuery.DEFAULT);
   }

   @Nonnull
   public Set<K> removeAssetWithPaths(@Nonnull String packKey, @Nonnull List<Path> paths, @Nonnull AssetUpdateQuery assetUpdateQuery) {
      if (this.unmodifiable) {
         throw new UnsupportedOperationException("AssetStore is unmodifiable!");
      } else {
         Set<K> allKeys = new HashSet<>();

         for (Path path : paths) {
            Path normalize = path.toAbsolutePath().normalize();
            Set<K> keys = this.assetMap.getKeys(normalize);
            if (keys != null) {
               allKeys.addAll(keys);
            }
         }

         return this.removeAssets(packKey, false, allKeys, assetUpdateQuery);
      }
   }

   @Nonnull
   public Set<K> removeAssetWithPath(Path path) {
      return this.removeAssetWithPath(path, AssetUpdateQuery.DEFAULT);
   }

   @Nonnull
   public Set<K> removeAssetWithPath(Path path, @Nonnull AssetUpdateQuery assetUpdateQuery) {
      if (this.unmodifiable) {
         throw new UnsupportedOperationException("AssetStore is unmodifiable!");
      } else {
         Path normalize = path.toAbsolutePath().normalize();
         Set<K> keys = this.assetMap.getKeys(normalize);
         return keys != null ? this.removeAssets("Hytale:Hytale", true, keys, assetUpdateQuery) : Collections.emptySet();
      }
   }

   @Nonnull
   public Set<K> removeAssets(@Nonnull Collection<K> keys) {
      return this.removeAssets("Hytale:Hytale", true, keys, AssetUpdateQuery.DEFAULT);
   }

   @Nonnull
   public Set<K> removeAssets(@Nonnull String packKey, boolean all, @Nonnull Collection<K> keys, @Nonnull AssetUpdateQuery assetUpdateQuery) {
      if (this.unmodifiable) {
         throw new UnsupportedOperationException("AssetStore is unmodifiable!");
      } else {
         long start = System.nanoTime();
         AssetRegistry.ASSET_LOCK.writeLock().lock();

         List<Entry<String, Object>> pathsToReload;
         try {
            Set<K> toBeRemoved = new HashSet<>();
            Set<K> temp = new HashSet<>();

            for (K key : keys) {
               toBeRemoved.add(key);
               Path path = this.assetMap.getPath(key);
               if (path != null) {
                  this.logRemoveAsset(key, path);
               } else {
                  this.logRemoveAsset(key, null);
               }

               temp.clear();
               this.collectAllChildren(key, temp);
               this.logRemoveChildren(key, temp);
               toBeRemoved.addAll(temp);
            }

            if (!toBeRemoved.isEmpty()) {
               this.removeChildrenAssets(packKey, toBeRemoved);
               pathsToReload = null;
               if (all) {
                  this.assetMap.remove(toBeRemoved);
               } else {
                  pathsToReload = new ArrayList<>();
                  this.assetMap.remove(packKey, toBeRemoved, pathsToReload);
               }

               if (this.replaceOnRemove != null) {
                  Map<K, T> replacements = toBeRemoved.stream().collect(Collectors.toMap(Function.identity(), key -> {
                     T replacement = this.replaceOnRemove.apply((K)key);
                     Objects.requireNonNull(replacement, "Replacement can't be null!");
                     K replacementKey = this.keyFunction.apply(replacement);
                     if (replacementKey == null) {
                        throw new NullPointerException(key.toString());
                     } else {
                        if (!key.equals(replacementKey)) {
                           this.logger.at(Level.WARNING).log("Replacement key '%s' doesn't match key '%s'", replacementKey, key);
                        }

                        return replacement;
                     }
                  }));
                  this.assetMap.putAll("Hytale:Hytale", this.codec, replacements, Collections.emptyMap(), Collections.emptyMap());
                  this.handleRemoveOrUpdate(null, replacements, AssetUpdateQuery.DEFAULT);
                  this.loadContainedAssets("Hytale:Hytale", replacements.values(), new HashMap<>(), AssetUpdateQuery.DEFAULT, false);
               } else {
                  this.handleRemoveOrUpdate(toBeRemoved, null, assetUpdateQuery);
               }

               if (pathsToReload != null) {
                  for (Entry<String, Object> e : pathsToReload) {
                     if (e.getValue() instanceof Path) {
                        this.loadAssetsFromPaths(e.getKey(), List.of((Path)e.getValue()));
                     } else {
                        this.loadAssets(e.getKey(), List.of((T)e.getValue()));
                     }
                  }
               }

               long end = System.nanoTime();
               long diff = end - start;
               this.logger
                  .at(Level.INFO)
                  .log(
                     "Removed %d (%s total) of %s via removeAssets in %s",
                     toBeRemoved.size(),
                     this.assetMap.getAssetCount(),
                     this.tClass.getSimpleName(),
                     FormatUtil.nanosToString(diff)
                  );
               if (!toBeRemoved.isEmpty()) {
                  IEventDispatcher dispatcher = this.getEventBus().dispatchFor(RemovedAssetsEvent.class, this.tClass);
                  if (dispatcher.hasListener()) {
                     dispatcher.dispatch(new RemovedAssetsEvent<>(this.tClass, this.assetMap, toBeRemoved, this.replaceOnRemove != null));
                  }
               }

               return toBeRemoved;
            }

            pathsToReload = toBeRemoved;
         } finally {
            AssetRegistry.ASSET_LOCK.writeLock().unlock();
         }

         return pathsToReload;
      }
   }

   public void removeAssetPack(@Nonnull String name) {
      AssetRegistry.ASSET_LOCK.writeLock().lock();

      try {
         Set<K> assets = this.assetMap.getKeysForPack(name);
         if (assets != null) {
            this.removeAssets(name, false, assets, AssetUpdateQuery.DEFAULT);
            return;
         }
      } finally {
         AssetRegistry.ASSET_LOCK.writeLock().unlock();
      }
   }

   public AssetLoadResult<K, T> writeAssetToDisk(@Nonnull AssetPack pack, @Nonnull Map<Path, T> assetsByPath) throws IOException {
      return this.writeAssetToDisk(pack, assetsByPath, AssetUpdateQuery.DEFAULT);
   }

   public AssetLoadResult<K, T> writeAssetToDisk(@Nonnull AssetPack pack, @Nonnull Map<Path, T> assetsByPath, @Nonnull AssetUpdateQuery query) throws IOException {
      if (pack.isImmutable()) {
         throw new IOException("Pack is immutable");
      } else {
         for (Entry<Path, T> entry : assetsByPath.entrySet()) {
            T asset = entry.getValue();
            K id = asset.getId();
            Path assetPath = pack.getRoot().resolve("Server").resolve(this.path).resolve(entry.getKey());
            AssetExtraInfo.Data data = this.codec.getData(asset);
            Object parentId = data == null ? null : data.getParentKey();
            BsonValue bsonValue = this.codec
               .encode(asset, new AssetExtraInfo(assetPath, new AssetExtraInfo.Data(this.tClass, id, this.transformKey(parentId))));
            Files.writeString(assetPath, bsonValue.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
         }

         return this.loadAssets(pack.getName(), new ArrayList<>(assetsByPath.values()), query);
      }
   }

   @Nonnull
   public T decode(@Nonnull String packKey, @Nonnull K key, @Nonnull BsonDocument document) {
      KeyedCodec<K> parentCodec = this.codec.getParentCodec();
      K parentKey = parentCodec != null ? parentCodec.getOrNull(document) : null;
      RawJsonReader reader = RawJsonReader.fromBuffer(document.toString().toCharArray());

      try {
         AssetExtraInfo<K> extraInfo = new AssetExtraInfo<>(new AssetExtraInfo.Data(this.getAssetClass(), key, parentKey));
         if (parentKey == null) {
            reader.consumeWhiteSpace();
            T asset = this.codec.decodeJsonAsset(reader, extraInfo);
            if (asset == null) {
               throw new NullPointerException(document.toString());
            } else {
               extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.logger);
               this.logUnusedKeys(key, null, extraInfo);
               return asset;
            }
         } else {
            T parent = parentKey.equals("super") ? this.assetMap.getAsset(packKey, key) : this.assetMap.getAsset(parentKey);
            if (parent == null) {
               throw new NullPointerException(parentKey.toString());
            } else {
               reader.consumeWhiteSpace();
               T asset = this.codec.decodeAndInheritJsonAsset(reader, parent, extraInfo);
               if (asset == null) {
                  throw new NullPointerException(document.toString());
               } else {
                  extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.logger);
                  this.logUnusedKeys(key, null, extraInfo);
                  return asset;
               }
            }
         }
      } catch (IOException var10) {
         throw SneakyThrow.sneakyThrow(var10);
      }
   }

   public <CK> void addChildAssetReferences(K parentKey, Class<? extends JsonAssetWithMap<CK, ?>> childAssetClass, @Nonnull Set<CK> childKeys) {
      this.childAssetsMap
         .computeIfAbsent(childAssetClass, k -> new ConcurrentHashMap<>())
         .computeIfAbsent(parentKey, k -> ConcurrentHashMap.newKeySet())
         .addAll(childKeys);
   }

   protected void loadAssets0(
      @Nonnull String packKey,
      @Nonnull Map<K, T> loadedAssets,
      @Nonnull List<RawAsset<K>> preLoaded,
      @Nonnull Map<K, Path> loadedKeyToPathMap,
      @Nonnull Set<K> failedToLoadKeys,
      @Nonnull Set<Path> failedToLoadPaths,
      @Nonnull AssetUpdateQuery query,
      boolean forceLoadAll,
      @Nonnull Map<Class<? extends JsonAssetWithMap>, AssetLoadResult> childAssetResults
   ) {
      Map<K, Set<K>> loadedAssetChildren = new ConcurrentHashMap<>();
      this.decodeAssets(
         packKey, preLoaded, loadedAssets, loadedKeyToPathMap, loadedAssetChildren, failedToLoadKeys, failedToLoadPaths, this.assetMap, query, forceLoadAll
      );
      AssetRegistry.ASSET_LOCK.writeLock().lock();

      try {
         IEventDispatcher generateDispatcher = this.getEventBus().dispatchFor(GenerateAssetsEvent.class, this.tClass);
         if (generateDispatcher.hasListener()) {
            generateDispatcher.dispatch(new GenerateAssetsEvent<>(this.tClass, this.assetMap, loadedAssets, loadedAssetChildren));
         }

         Map<K, K> toBeRemovedMap = new HashMap<>();
         Set<K> temp = new HashSet<>();

         for (K key : failedToLoadKeys) {
            if (toBeRemovedMap.putIfAbsent(key, key) == null) {
               this.logRemoveAsset(key, null);
               temp.clear();
               this.collectAllChildren(key, temp);

               for (K k : temp) {
                  toBeRemovedMap.putIfAbsent(k, key);
               }
            }
         }

         for (Path path : failedToLoadPaths) {
            Set<K> keys = this.assetMap.getKeys(path);
            if (keys != null) {
               for (K keyx : keys) {
                  if (toBeRemovedMap.putIfAbsent(keyx, keyx) == null) {
                     this.logRemoveAsset(keyx, path);
                     temp.clear();
                     this.collectAllChildren(keyx, temp);

                     for (K k : temp) {
                        toBeRemovedMap.putIfAbsent(k, keyx);
                     }
                  }
               }
            }
         }

         this.assetMap.putAll(packKey, this.codec, loadedAssets, loadedKeyToPathMap, loadedAssetChildren);
         Set<K> toBeRemoved = toBeRemovedMap.keySet();
         if (!toBeRemoved.isEmpty()) {
            this.logRemoveChildren(toBeRemovedMap);
            this.removeChildrenAssets(packKey, toBeRemoved);
         }

         if (this.replaceOnRemove != null && !toBeRemoved.isEmpty()) {
            Map<K, T> replacements = toBeRemoved.stream()
               .filter(kx -> this.assetMap.getAsset((K)kx) != null)
               .collect(Collectors.toMap(Function.identity(), keyxx -> {
                  T replacement = this.replaceOnRemove.apply((K)keyxx);
                  Objects.requireNonNull(replacement, "Replacement can't be null!");
                  K replacementKey = this.keyFunction.apply(replacement);
                  if (replacementKey == null) {
                     throw new NullPointerException(keyxx.toString());
                  } else {
                     if (!keyxx.equals(replacementKey)) {
                        this.logger.at(Level.WARNING).log("Replacement key '%s' doesn't match key '%s'", replacementKey, keyxx);
                     }

                     return replacement;
                  }
               }));
            this.assetMap.putAll("Hytale:Hytale", this.codec, replacements, Collections.emptyMap(), Collections.emptyMap());
            replacements.putAll(loadedAssets);
            this.handleRemoveOrUpdate(null, replacements, query);
         } else {
            this.assetMap.remove(toBeRemoved);
            this.handleRemoveOrUpdate(toBeRemoved, loadedAssets, query);
         }

         this.loadContainedAssets(packKey, loadedAssets.values(), childAssetResults, query, forceLoadAll);
         this.reloadChildrenContainerAssets(packKey, loadedAssets);
         if (!loadedAssets.isEmpty()) {
            IEventDispatcher dispatcher = this.getEventBus().dispatchFor(LoadedAssetsEvent.class, this.tClass);
            if (dispatcher.hasListener()) {
               dispatcher.dispatch(new LoadedAssetsEvent<>(this.tClass, this.assetMap, loadedAssets, false, query));
            }
         }

         if (!toBeRemoved.isEmpty()) {
            IEventDispatcher dispatcher = this.getEventBus().dispatchFor(RemovedAssetsEvent.class, this.tClass);
            if (dispatcher.hasListener()) {
               dispatcher.dispatch(new RemovedAssetsEvent<>(this.tClass, this.assetMap, toBeRemoved, this.replaceOnRemove != null));
            }
         }
      } finally {
         AssetRegistry.ASSET_LOCK.writeLock().unlock();
      }
   }

   private void reloadChildrenContainerAssets(@Nonnull String packKey, @Nonnull Map<K, T> loadedAssets) {
      HashSet<Path> toReload = new HashSet<>();
      HashMap<Class<? extends JsonAssetWithMap<?, ?>>, Set<Path>> toReloadTypes = new HashMap<>();

      for (Entry<K, T> entry : loadedAssets.entrySet()) {
         K key = entry.getKey();
         Path path = this.assetMap.getPath(key);
         if (path != null) {
            this.collectChildrenInDifferentFile(key, path, toReload, toReloadTypes, loadedAssets.keySet());
         }
      }

      AssetUpdateQuery query = null;
      if (!toReload.isEmpty()) {
         query = new AssetUpdateQuery(true, AssetUpdateQuery.RebuildCache.DEFAULT);
         this.loadAssetsFromPaths(packKey, toReload, query, true);
      }

      if (!toReloadTypes.isEmpty()) {
         if (query == null) {
            query = new AssetUpdateQuery(true, AssetUpdateQuery.RebuildCache.DEFAULT);
         }

         for (Entry<Class<? extends JsonAssetWithMap<?, ?>>, Set<Path>> entryx : toReloadTypes.entrySet()) {
            AssetStore assetStore = AssetRegistry.getAssetStore((Class<T>)entryx.getKey());
            assetStore.loadAssetsFromPaths(packKey, entryx.getValue(), query, true);
         }
      }
   }

   private void collectChildrenInDifferentFile(
      K key, @Nonnull Path path, @Nonnull Set<Path> paths, @Nonnull Map<Class<? extends JsonAssetWithMap<?, ?>>, Set<Path>> typedPaths, @Nonnull Set<K> ignore
   ) {
      for (K child : this.assetMap.getChildren(key)) {
         if (!ignore.contains(child)) {
            Path childPath = this.assetMap.getPath(child);
            if (childPath != null && !path.equals(childPath)) {
               paths.add(childPath);
            } else {
               AssetExtraInfo.Data data = this.codec.getData(this.assetMap.getAsset(child));
               AssetExtraInfo.Data root = data != null ? data.getRootContainerData() : null;
               if (root != null) {
                  if (root.getAssetClass() == this.tClass) {
                     K rootKey = (K)root.getKey();
                     if (ignore.contains(rootKey)) {
                        continue;
                     }

                     Path rootPath = this.assetMap.getPath(rootKey);
                     if (!path.equals(rootPath)) {
                        paths.add(rootPath);
                        continue;
                     }
                  } else {
                     Class assetClass = root.getAssetClass();
                     if (assetClass == null) {
                        continue;
                     }

                     AssetStore assetStore = AssetRegistry.getAssetStore(assetClass);
                     Path rootPath = assetStore.getAssetMap().getPath(root.getKey());
                     if (rootPath != null) {
                        typedPaths.computeIfAbsent(assetClass, k -> new HashSet<>()).add(rootPath);
                        continue;
                     }
                  }
               }

               this.collectChildrenInDifferentFile(child, path, paths, typedPaths, ignore);
            }
         }
      }
   }

   protected void removeChildrenAssets(@Nonnull String packKey, @Nonnull Set<K> toBeRemoved) {
      for (Entry<Class<? extends JsonAssetWithMap<?, ?>>, Map<K, Set<Object>>> entry : this.childAssetsMap.entrySet()) {
         Class k = entry.getKey();
         Map<K, Set<Object>> value = entry.getValue();
         Set<Object> allChildKeys = null;

         for (K key : toBeRemoved) {
            Set<Object> childKeys = value.remove(key);
            if (childKeys != null) {
               if (allChildKeys == null) {
                  allChildKeys = new HashSet<>();
               }

               allChildKeys.addAll(childKeys);
            }
         }

         if (allChildKeys != null && !allChildKeys.isEmpty()) {
            AssetRegistry.<Object, T, M>getAssetStore(k).removeAssets(packKey, false, allChildKeys, AssetUpdateQuery.DEFAULT);
         }
      }
   }

   protected void loadContainedAssets(
      @Nonnull String packKey,
      @Nonnull Collection<T> assets,
      @Nonnull Map<Class<? extends JsonAssetWithMap>, AssetLoadResult> childAssetsResults,
      @Nonnull AssetUpdateQuery query,
      boolean forceLoadAll
   ) {
      Map<Class<? extends JsonAssetWithMap>, Map<K, List<Object>>> containedAssetsByClass = new HashMap<>();

      for (T t : assets) {
         AssetExtraInfo.Data data = this.codec.getData(t);
         if (data != null) {
            data.fetchContainedAssets(this.keyFunction.apply(t), containedAssetsByClass);
         }
      }

      for (Entry<Class<? extends JsonAssetWithMap>, Map<K, List<Object>>> entry : containedAssetsByClass.entrySet()) {
         Class<? extends JsonAssetWithMap> assetClass = entry.getKey();
         Map<K, List<Object>> containedAssets = entry.getValue();
         AssetStore assetStore = AssetRegistry.getAssetStore((Class<T>)assetClass);
         this.loadedContainedAssetsFor.add(assetClass);
         List<Object> childList = new ArrayList<>();

         for (Entry<K, List<Object>> containedEntry : containedAssets.entrySet()) {
            K key = containedEntry.getKey();

            for (Object contained : containedEntry.getValue()) {
               Object containedKey = assetStore.getKeyFunction().apply(contained);
               this.childAssetsMap
                  .computeIfAbsent(assetStore.getAssetClass(), k -> new ConcurrentHashMap<>())
                  .computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                  .add(containedKey);
               childList.add(contained);
            }
         }

         AssetLoadResult result = assetStore.loadAssets(packKey, (List<T>)childList, query, forceLoadAll);
         childAssetsResults.put(assetClass, result);
      }

      Map<Class<? extends JsonAssetWithMap>, Map<K, List<RawAsset<Object>>>> containedRawAssetsByClass = new HashMap<>();

      for (T tx : assets) {
         AssetExtraInfo.Data data = this.codec.getData(tx);
         if (data != null) {
            data.fetchContainedRawAssets(this.keyFunction.apply(tx), containedRawAssetsByClass);
         }
      }

      for (Entry<Class<? extends JsonAssetWithMap>, Map<K, List<RawAsset<Object>>>> entry : containedRawAssetsByClass.entrySet()) {
         Class<? extends JsonAssetWithMap> assetClass = entry.getKey();
         Map<K, List<RawAsset<Object>>> containedAssets = entry.getValue();
         AssetStore assetStore = AssetRegistry.getAssetStore((Class<T>)assetClass);
         this.loadedContainedAssetsFor.add(assetClass);
         List<RawAsset<?>> childList = new ArrayList<>();

         for (Entry<K, List<RawAsset<Object>>> containedEntry : containedAssets.entrySet()) {
            K key = containedEntry.getKey();

            for (RawAsset<Object> contained : containedEntry.getValue()) {
               Object containedKey = contained.getKey();
               this.childAssetsMap
                  .computeIfAbsent(assetStore.getAssetClass(), k -> new ConcurrentHashMap<>())
                  .computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                  .add(containedKey);

               RawAsset<Object> resolvedContained = switch (contained.getContainedAssetMode()) {
                  case NONE, GENERATE_ID, INJECT_PARENT, INHERIT_ID -> contained;
                  case INHERIT_ID_AND_PARENT -> {
                     Object parentKey = contained.getParentKey();
                     if (parentKey == null) {
                        yield contained;
                     } else if (assetStore.getAssetMap().getAsset(parentKey) == null && !containedAssets.containsKey(parentKey)) {
                        this.logger
                           .at(Level.WARNING)
                           .log("Failed to find inherited parent asset %s (%s) for %s", parentKey, assetStore.getAssetClass().getSimpleName(), containedKey);
                        yield contained.withResolveKeys(containedKey, null);
                     } else {
                        yield contained;
                     }
                  }
               };
               childList.add(resolvedContained);
            }
         }

         AssetLoadResult result = assetStore.loadBuffersWithKeys(packKey, childList, query, forceLoadAll);
         childAssetsResults.put(assetClass, result);
      }
   }

   protected void decodeAssets(
      @Nonnull String packKey,
      @Nonnull List<RawAsset<K>> rawAssets,
      @Nonnull Map<K, T> loadedAssets,
      @Nonnull Map<K, Path> loadedKeyToPathMap,
      @Nonnull Map<K, Set<K>> loadedAssetChildren,
      @Nonnull Set<K> failedToLoadKeys,
      @Nonnull Set<Path> failedToLoadPaths,
      @Nullable M assetMap,
      @Nonnull AssetUpdateQuery query,
      boolean forceLoadAll
   ) {
      if (!rawAssets.isEmpty()) {
         Map<K, RawAsset<K>> waitingForParent = new ConcurrentHashMap<>();
         CompletableFuture<DecodedAsset<K, T>>[] futuresArr = new CompletableFuture[rawAssets.size()];

         for (int i = 0; i < rawAssets.size(); i++) {
            futuresArr[i] = this.executeAssetDecode(
               loadedAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, assetMap, query, forceLoadAll, waitingForParent, rawAssets.get(i)
            );
         }

         CompletableFuture.allOf(futuresArr).join();

         for (CompletableFuture<DecodedAsset<K, T>> future : futuresArr) {
            DecodedAsset<K, T> decodedAsset = future.getNow(null);
            if (decodedAsset != null) {
               loadedAssets.put(decodedAsset.getKey(), decodedAsset.getAsset());
            }
         }

         List<CompletableFuture<DecodedAsset<K, T>>> futures = new ArrayList<>();

         while (!waitingForParent.isEmpty()) {
            int processedAssets = 0;

            for (Entry<K, RawAsset<K>> entry : waitingForParent.entrySet()) {
               K key = entry.getKey();
               RawAsset<K> rawAsset = entry.getValue();
               Path path = rawAsset.getPath();
               K parentKey = rawAsset.getParentKey();
               T parent = loadedAssets.get(parentKey);
               if (parent == null) {
                  if (waitingForParent.containsKey(parentKey)) {
                     continue;
                  }

                  if (assetMap == null) {
                     this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, path);
                     this.logger.at(Level.SEVERE).log("Failed to find parent '%s' for asset: %s, %s (assetMap was null)", parentKey, key, path);
                     continue;
                  }

                  parent = parentKey.equals("super") ? assetMap.getAsset(packKey, key) : assetMap.getAsset(parentKey);
                  if (parent == null) {
                     this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, path);
                     this.logger.at(Level.SEVERE).log("Failed to find parent '%s' for asset: %s,  %s", parentKey, key, path);
                     continue;
                  }
               }

               if (this.isUnknown.test(parent)) {
                  this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, path);
                  this.logger.at(Level.SEVERE).log("Parent '%s' for asset: %s,  %s is an unknown type", parentKey, key, path);
               } else {
                  processedAssets++;
                  T finalParent = parent;
                  futures.add(
                     CompletableFuture.supplyAsync(
                        () -> {
                           char[] buffer = RawJsonReader.READ_BUFFER.get();
                           RawJsonReader reader;
                           if (rawAsset.getBuffer() != null) {
                              reader = RawJsonReader.fromBuffer(rawAsset.getBuffer());
                           } else {
                              try {
                                 reader = RawJsonReader.fromPath(path, buffer);
                              } catch (IOException var26) {
                                 ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var26)).log("Failed to load asset: %s", path);
                                 return null;
                              }
                           }

                           DecodedAsset<K, T> decodedAsset = null;

                           try {
                              decodedAsset = this.decodeAssetWithParent0(
                                 loadedAssets,
                                 loadedKeyToPathMap,
                                 loadedAssetChildren,
                                 failedToLoadKeys,
                                 failedToLoadPaths,
                                 assetMap,
                                 query,
                                 forceLoadAll,
                                 rawAsset,
                                 reader,
                                 finalParent
                              );
                           } finally {
                              try {
                                 if (rawAsset.getBuffer() != null) {
                                    reader.close();
                                 } else {
                                    char[] value = reader.closeAndTakeBuffer();
                                    if (value.length > buffer.length) {
                                       RawJsonReader.READ_BUFFER.set(value);
                                    }
                                 }
                              } catch (IOException var24x) {
                                 ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var24x)).log("Failed to close asset reader: %s", path);
                              }

                              if (decodedAsset == null) {
                                 waitingForParent.remove(key);
                              }
                           }

                           return decodedAsset;
                        }
                     )
                  );
               }
            }

            CompletableFuture<DecodedAsset<K, T>>[] futuresArray = futures.toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futuresArray).join();
            futures.clear();

            for (CompletableFuture<DecodedAsset<K, T>> futurex : futuresArray) {
               DecodedAsset<K, T> decodedAsset = futurex.getNow(null);
               if (decodedAsset != null) {
                  loadedAssets.put(decodedAsset.getKey(), decodedAsset.getAsset());
                  waitingForParent.remove(decodedAsset.getKey());
               }
            }

            if (processedAssets == 0) {
               for (Entry<K, RawAsset<K>> entry : waitingForParent.entrySet()) {
                  K keyx = entry.getKey();
                  Path assetPath = entry.getValue().getPath();
                  K parentKeyx = entry.getValue().getParentKey();
                  this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, keyx, assetPath);
                  this.logger.at(Level.SEVERE).log("Failed to find parent with key '%s' for asset: %s, %s", parentKeyx, keyx, assetPath);
               }
               break;
            }
         }
      }
   }

   @Nonnull
   private CompletableFuture<DecodedAsset<K, T>> executeAssetDecode(
      @Nonnull Map<K, T> loadedAssets,
      @Nonnull Map<K, Path> loadedKeyToPathMap,
      @Nonnull Set<K> failedToLoadKeys,
      @Nonnull Set<Path> failedToLoadPaths,
      M assetMap,
      @Nonnull AssetUpdateQuery query,
      boolean forceLoadAll,
      @Nonnull Map<K, RawAsset<K>> waitingForParent,
      @Nonnull RawAsset<K> rawAsset
   ) {
      return CompletableFuture.supplyAsync(() -> {
         RawJsonReader reader;
         try {
            reader = rawAsset.toRawJsonReader(RawJsonReader.READ_BUFFER::get);
         } catch (IOException var20) {
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var20)).log("Failed to load asset: %s", rawAsset);
            return null;
         }

         AssetHolder<K> holder;
         try {
            holder = this.decodeAsset0(loadedAssets, loadedKeyToPathMap, failedToLoadKeys, failedToLoadPaths, assetMap, query, forceLoadAll, rawAsset, reader);
            if (holder instanceof RawAsset<K> waiting) {
               waitingForParent.put(waiting.getKey(), waiting);
            }
         } finally {
            try {
               if (rawAsset.getBuffer() != null) {
                  reader.close();
               } else {
                  char[] value = reader.closeAndTakeBuffer();
                  if (value.length > RawJsonReader.READ_BUFFER.get().length) {
                     RawJsonReader.READ_BUFFER.set(value);
                  }
               }
            } catch (IOException var19) {
               ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var19)).log("Failed to close asset reader: %s", this.path);
            }
         }

         return holder instanceof DecodedAsset ? (DecodedAsset)holder : null;
      });
   }

   @Nullable
   private AssetHolder<K> decodeAsset0(
      @Nonnull Map<K, T> loadedAssets,
      @Nonnull Map<K, Path> loadedKeyToPathMap,
      @Nonnull Set<K> failedToLoadKeys,
      @Nonnull Set<Path> failedToLoadPaths,
      @Nullable M assetMap,
      @Nonnull AssetUpdateQuery query,
      boolean forceLoadAll,
      @Nonnull RawAsset<K> rawAsset,
      @Nonnull RawJsonReader reader
   ) {
      Path assetPath = rawAsset.getPath();
      long start = System.nanoTime();
      K key = rawAsset.getKey();
      K parentKey = rawAsset.getParentKey();

      try {
         KeyedCodec<K> keyCodec = this.codec.getKeyCodec();
         KeyedCodec<K> parentCodec = this.codec.getParentCodec();
         if (key == null) {
            if (rawAsset.getPath() != null) {
               throw new IllegalArgumentException("Asset with path should infer its 'Id'!");
            }

            reader.mark();
            if (parentCodec != null && !rawAsset.isParentKeyResolved()) {
               String s = RawJsonReader.seekToKeyFromObjectStart(reader, keyCodec.getKey(), parentCodec.getKey());
               if (s != null) {
                  if (keyCodec.getKey().equals(s)) {
                     key = keyCodec.getChildCodec().decodeJson(reader);
                  } else if (parentCodec.getKey().equals(s)) {
                     parentKey = parentCodec.getChildCodec().decodeJson(reader);
                  }

                  s = RawJsonReader.seekToKeyFromObjectContinued(reader, keyCodec.getKey(), parentCodec.getKey());
                  if (s != null) {
                     if (keyCodec.getKey().equals(s)) {
                        key = keyCodec.getChildCodec().decodeJson(reader);
                     } else if (parentCodec.getKey().equals(s)) {
                        parentKey = parentCodec.getChildCodec().decodeJson(reader);
                     }
                  }
               }
            } else if (RawJsonReader.seekToKey(reader, keyCodec.getKey())) {
               key = keyCodec.getChildCodec().decodeJson(reader);
            }

            if (key == null) {
               throw new CodecException("Unable to find 'Id' in document!");
            }

            reader.reset();
         } else if (parentCodec != null && !rawAsset.isParentKeyResolved()) {
            reader.mark();
            if (RawJsonReader.seekToKey(reader, parentCodec.getKey())) {
               parentKey = parentCodec.getChildCodec().decodeJson(reader);
            }

            reader.reset();
         }

         if (assetPath == null) {
            assetPath = loadedKeyToPathMap.get(key);
         }

         if (parentKey != null) {
            return rawAsset.withResolveKeys(key, parentKey);
         }

         AssetExtraInfo<K> extraInfo = new AssetExtraInfo<>(assetPath, rawAsset.makeData(this.getAssetClass(), key, null));
         reader.consumeWhiteSpace();
         T asset = this.codec.decodeJsonAsset(reader, extraInfo);
         if (asset == null) {
            throw new NullPointerException(rawAsset.toString());
         }

         extraInfo.getValidationResults()
            .logOrThrowValidatorExceptions(
               this.logger, "Failed to validate asset!\n", assetPath == null ? rawAsset.getParentPath() : assetPath, rawAsset.getLineOffset()
            );
         if (!DISABLE_ASSET_COMPARE && (query == null || !query.isDisableAssetCompare()) && assetMap != null && asset.equals(assetMap.getAsset(key))) {
            this.logger.at(Level.INFO).log("Skipping asset that hasn't changed: %s", key);
            return null;
         }

         this.testKeyFormat(key, assetPath);
         if (!forceLoadAll) {
         }

         if (assetPath != null) {
            loadedKeyToPathMap.put(key, assetPath);
         }

         this.logUnusedKeys(key, assetPath, extraInfo);
         this.logLoadedAsset(key, null, assetPath);
         return new DecodedAsset<>(key, asset);
      } catch (CodecValidationException var19) {
         this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, assetPath);
         this.logger.at(Level.SEVERE).log("Failed to validate asset: %s, %s, %s", key, assetPath, var19.getMessage());
      } catch (CodecException | IOException var20) {
         this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, assetPath);
         if (GithubMessageUtil.isGithub()) {
            String pathStr = assetPath == null ? (key == null ? "unknown" : key.toString()) : assetPath.toString();
            String message;
            if (var20 instanceof CodecException codecException) {
               message = codecException.getMessage();
               if (codecException.getCause() != null) {
                  message = message + "\nCause: " + codecException.getCause().getMessage();
               }
            } else {
               message = var20.getMessage();
            }

            if (reader.getLine() == -1) {
               HytaleLoggerBackend.rawLog(GithubMessageUtil.messageError(pathStr, message));
            } else {
               HytaleLoggerBackend.rawLog(GithubMessageUtil.messageError(pathStr, reader.getLine(), reader.getColumn(), message));
            }
         }

         ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(new SkipSentryException(var20)))
            .log("Failed to decode asset: %s, %s:\n%s", key, assetPath, reader);
      } catch (Throwable var21) {
         this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, assetPath);
         if (GithubMessageUtil.isGithub()) {
            String pathStrx = assetPath == null ? (key == null ? "unknown" : key.toString()) : assetPath.toString();
            String messagex = var21.getMessage();
            HytaleLoggerBackend.rawLog(GithubMessageUtil.messageError(pathStrx, messagex));
         }

         ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var21)).log("Failed to decode asset: %s, %s", key, assetPath);
      }

      return null;
   }

   @Nullable
   private DecodedAsset<K, T> decodeAssetWithParent0(
      @Nonnull Map<K, T> loadedAssets,
      @Nonnull Map<K, Path> loadedKeyToPathMap,
      @Nonnull Map<K, Set<K>> loadedAssetChildren,
      @Nonnull Set<K> failedToLoadKeys,
      @Nonnull Set<Path> failedToLoadPaths,
      @Nullable M assetMap,
      @Nonnull AssetUpdateQuery query,
      boolean forceLoadAll,
      @Nonnull RawAsset<K> rawAsset,
      @Nonnull RawJsonReader reader,
      T parent
   ) {
      K key = rawAsset.getKey();
      if (!rawAsset.isParentKeyResolved()) {
         throw new IllegalArgumentException("Parent key is required when decoding an asset with a parent!");
      } else {
         K parentKey = rawAsset.getParentKey();
         Path assetPath = rawAsset.getPath();

         try {
            if (assetPath == null) {
               assetPath = loadedKeyToPathMap.get(key);
            }

            AssetExtraInfo<K> extraInfo = new AssetExtraInfo<>(assetPath, rawAsset.makeData(this.getAssetClass(), key, parentKey));
            reader.consumeWhiteSpace();
            T asset = this.codec.decodeAndInheritJsonAsset(reader, parent, extraInfo);
            if (asset == null) {
               throw new NullPointerException(assetPath.toString());
            }

            extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.logger);
            if (key.equals(parentKey)) {
               this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, assetPath);
               this.logger.at(Level.SEVERE).log("Failed to load asset '%s' because it is its own parent!", key);
               return null;
            }

            if (!DISABLE_ASSET_COMPARE && (query == null || !query.isDisableAssetCompare()) && assetMap != null && asset.equals(assetMap.getAsset(key))) {
               this.logger.at(Level.INFO).log("Skipping asset that hasn't changed: %s", key);
               return null;
            }

            this.testKeyFormat(key, assetPath);
            if (!forceLoadAll) {
            }

            loadedAssetChildren.computeIfAbsent(parentKey, k -> ConcurrentHashMap.newKeySet()).add(key);
            if (assetPath != null) {
               loadedKeyToPathMap.put(key, assetPath);
            }

            this.logUnusedKeys(key, assetPath, extraInfo);
            this.logLoadedAsset(key, parentKey, assetPath);
            return new DecodedAsset<>(key, asset);
         } catch (CodecValidationException var17) {
            this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, assetPath);
            this.logger.at(Level.SEVERE).log("Failed to decode asset: %s, %s, %s", key, assetPath, var17.getMessage());
         } catch (CodecException | IOException var18) {
            this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, assetPath);
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(new SkipSentryException(var18)))
               .log("Failed to decode asset: %s, %s:\n%s", key, assetPath, reader);
         } catch (Exception var19) {
            this.recordFailedToLoad(failedToLoadKeys, failedToLoadPaths, key, assetPath);
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var19)).log("Failed to decode asset: %s, %s", key, assetPath);
         }

         return null;
      }
   }

   private void loadAllChildren(@Nonnull Map<K, T> loadedAssets, @Nonnull Collection<T> assetKeys, @Nonnull Set<Path> documents) {
      for (T asset : assetKeys) {
         Objects.requireNonNull(asset, "asset can't be null");
         K key = this.keyFunction.apply(asset);
         if (key == null) {
            throw new NullPointerException(String.format("key can't be null: %s", asset));
         }

         loadedAssets.put(key, asset);
         if (this.loadAllChildren(documents, key)) {
            StringBuilder sb = new StringBuilder();
            sb.append(key).append(":\n");
            this.logChildTree(sb, "  ", key, new HashSet<>());
            this.logger.at(Level.SEVERE).log("Found a circular dependency when trying to collect all children!\n%s", sb);
         }
      }
   }

   protected boolean loadAllChildren(@Nonnull Set<Path> documents, K key) {
      Set<K> set = this.assetMap.getChildren(key);
      if (set == null) {
         return false;
      } else {
         boolean circular = false;

         for (K child : set) {
            Path childPath = this.assetMap.getPath(child);
            if (childPath != null) {
               if (documents.add(childPath)) {
                  circular |= this.loadAllChildren(documents, child);
               } else {
                  circular = true;
               }
            }
         }

         return circular;
      }
   }

   protected void collectAllChildren(K key, @Nonnull Set<K> children) {
      if (this.collectAllChildren0(key, children)) {
         StringBuilder sb = new StringBuilder();
         sb.append(key).append(":\n");
         this.logChildTree(sb, "  ", key, new HashSet<>());
         this.logger.at(Level.SEVERE).log("Found a circular dependency when trying to collect all children!\n%s", sb);
      }
   }

   private boolean collectAllChildren0(K key, @Nonnull Set<K> children) {
      Set<K> set = this.assetMap.getChildren(key);
      if (set == null) {
         return false;
      } else {
         boolean circular = false;

         for (K child : set) {
            if (children.add(child)) {
               circular |= this.collectAllChildren0(child, children);
            } else {
               circular = true;
            }
         }

         return circular;
      }
   }

   protected void logChildTree(@Nonnull StringBuilder sb, String indent, K key, @Nonnull Set<K> children) {
      Set<K> set = this.assetMap.getChildren(key);
      if (set != null) {
         for (K child : set) {
            if (children.add(child)) {
               sb.append(indent).append("- ").append(child).append('\n');
               this.logChildTree(sb, indent + "  ", child, children);
            } else {
               sb.append(indent).append("- ").append(child).append('\n').append(indent).append("  ").append("** Circular **\n");
            }
         }
      }
   }

   protected void logRemoveChildren(K parentKey, @Nonnull Set<K> toBeRemoved) {
      Path path = this.assetMap.getPath(parentKey);

      for (K child : toBeRemoved) {
         Path childPath = this.assetMap.getPath(child);
         if (childPath != null) {
            if (path != null) {
               this.logger.at(Level.WARNING).log("Removing child asset '%s' of removed asset '%s'", childPath, path);
            } else {
               this.logger.at(Level.WARNING).log("Removing child asset '%s' of removed asset '%s'", childPath, parentKey);
            }
         } else {
            this.logger.at(Level.WARNING).log("Removing child asset '%s' of removed asset '%s'", child, parentKey);
         }
      }
   }

   protected void logRemoveChildren(@Nonnull Map<K, K> toBeRemoved) {
      for (Entry<K, K> entry : toBeRemoved.entrySet()) {
         K child = entry.getKey();
         K parentKey = entry.getValue();
         Path childPath = this.assetMap.getPath(child);
         if (childPath != null) {
            Path path = this.assetMap.getPath(parentKey);
            if (path != null) {
               this.logger.at(Level.WARNING).log("Removing child asset '%s' of removed asset '%s'", childPath, path);
            } else {
               this.logger.at(Level.WARNING).log("Removing child asset '%s' of removed asset '%s'", childPath, parentKey);
            }
         } else {
            this.logger.at(Level.WARNING).log("Removing child asset '%s' of removed asset '%s'", child, parentKey);
         }
      }
   }

   protected void testKeyFormat(@Nonnull K key, @Nullable Path assetPath) {
      String keyStr = key.toString();
      if (!StringUtil.isCapitalized(keyStr, '_')) {
         String expected = StringUtil.capitalize(keyStr, '_');
         if (assetPath == null) {
            this.logger.at(Level.WARNING).log("Asset key '%s' has incorrect format! Expected: '%s'", key, expected);
         } else {
            this.logger.at(Level.WARNING).log("Asset key '%s' for file '%s' has incorrect format! Expected: '%s'", key, assetPath, expected);
         }
      }
   }

   public void logUnusedKeys(@Nonnull K key, @Nullable Path assetPath, @Nonnull AssetExtraInfo<K> extraInfo) {
      List<String> unknownKeys = extraInfo.getUnknownKeys();
      if (!unknownKeys.isEmpty()) {
         if (GithubMessageUtil.isGithub()) {
            String pathStr = assetPath == null ? key.toString() : assetPath.toString();

            for (int i = 0; i < unknownKeys.size(); i++) {
               String unknownKey = unknownKeys.get(i);
               HytaleLoggerBackend.rawLog(GithubMessageUtil.messageWarning(pathStr, "Unused key: " + unknownKey));
            }
         } else if (assetPath != null) {
            this.logger.at(Level.WARNING).log("Unused key(s) in '%s' file %s: %s", key, assetPath, String.join(", ", unknownKeys));
         } else {
            this.logger.at(Level.WARNING).log("Unused key(s) in '%s': %s", key, String.join(", ", unknownKeys));
         }
      }
   }

   protected void logLoadedAsset(K key, @Nullable K parentKey, @Nullable Path path) {
      if (path == null && parentKey == null) {
         this.logger.at(Level.FINE).log("Loaded asset: %s", key);
      } else if (path == null) {
         this.logger.at(Level.FINE).log("Loaded asset: '%s' with parent '%s'", key, parentKey);
      } else if (parentKey == null) {
         this.logger.at(Level.FINE).log("Loaded asset: '%s' from '%s'", key, path);
      } else {
         this.logger.at(Level.FINE).log("Loaded asset: '%s' from '%s' with parent '%s'", key, path, parentKey);
      }
   }

   protected void logRemoveAsset(K key, @Nullable Path path) {
      if (path == null) {
         this.logger.at(Level.FINE).log("Removed asset: '%s'", key);
      } else {
         this.logger.at(Level.FINE).log("Removed asset: '%s' from '%s'", key, path);
      }
   }

   private void recordFailedToLoad(@Nonnull Set<K> failedToLoadKeys, @Nonnull Set<Path> failedToLoadPaths, @Nullable K key, @Nullable Path path) {
      if (key != null) {
         failedToLoadKeys.add(key);
      }

      if (path != null) {
         failedToLoadPaths.add(path);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetStore{tClass=" + this.tClass + "}";
   }

   protected abstract static class Builder<K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>, B extends AssetStore.Builder<K, T, M, B>> {
      @Nonnull
      protected final Class<K> kClass;
      @Nonnull
      protected final Class<T> tClass;
      protected final M assetMap;
      protected final Set<Class<? extends JsonAsset<?>>> loadsAfter = new HashSet<>();
      protected final Set<Class<? extends JsonAsset<?>>> loadsBefore = new HashSet<>();
      protected String path;
      @Nonnull
      protected String extension = ".json";
      protected AssetCodec<K, T> codec;
      protected Function<T, K> keyFunction;
      protected Function<K, T> replaceOnRemove;
      protected Predicate<T> isUnknown;
      protected boolean unmodifiable;
      protected List<T> preAddedAssets;
      protected Class<? extends JsonAsset<?>> idProvider;

      public Builder(Class<K> kClass, Class<T> tClass, M assetMap) {
         this.kClass = Objects.requireNonNull(kClass, "key class can't be null!");
         this.tClass = Objects.requireNonNull(tClass, "asset class can't be null!");
         this.assetMap = assetMap;
      }

      @Nonnull
      public B setPath(String path) {
         this.path = Objects.requireNonNull(path, "path can't be null!");
         return (B)this;
      }

      @Nonnull
      public B setExtension(@Nonnull String extension) {
         Objects.requireNonNull(extension, "extension can't be null!");
         if (extension.length() >= 2 && extension.charAt(0) == '.') {
            this.extension = extension;
            return (B)this;
         } else {
            throw new IllegalArgumentException("Extension must start with '.' and have at least one character after");
         }
      }

      @Nonnull
      public B setCodec(AssetCodec<K, T> codec) {
         this.codec = Objects.requireNonNull(codec, "codec can't be null!");
         return (B)this;
      }

      @Nonnull
      public B setKeyFunction(Function<T, K> keyFunction) {
         this.keyFunction = Objects.requireNonNull(keyFunction, "keyFunction can't be null!");
         return (B)this;
      }

      @Nonnull
      public B setIsUnknown(Predicate<T> isUnknown) {
         this.isUnknown = Objects.requireNonNull(isUnknown, "isUnknown can't be null!");
         return (B)this;
      }

      @Nonnull
      @SafeVarargs
      public final B loadsAfter(Class<? extends JsonAsset<?>>... clazz) {
         Collections.addAll(this.loadsAfter, clazz);
         return (B)this;
      }

      @Nonnull
      @SafeVarargs
      public final B loadsBefore(Class<? extends JsonAsset<?>>... clazz) {
         Collections.addAll(this.loadsBefore, clazz);
         return (B)this;
      }

      @Nonnull
      public B setReplaceOnRemove(Function<K, T> replaceOnRemove) {
         this.replaceOnRemove = Objects.requireNonNull(replaceOnRemove, "replaceOnRemove can't be null!");
         return (B)this;
      }

      @Nonnull
      public B unmodifiable() {
         this.unmodifiable = true;
         return (B)this;
      }

      @Nonnull
      public B preLoadAssets(@Nonnull List<T> list) {
         if (this.preAddedAssets == null) {
            this.preAddedAssets = new ArrayList<>();
         }

         this.preAddedAssets.addAll(list);
         return (B)this;
      }

      @Nonnull
      public B setIdProvider(Class<? extends JsonAsset<?>> provider) {
         this.idProvider = provider;
         return (B)this;
      }

      public abstract AssetStore<K, T, M> build();
   }
}
