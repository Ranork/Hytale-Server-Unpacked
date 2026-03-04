package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.map.CaseInsensitiveHashStrategy;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitor;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitorHandler;
import com.hypixel.hytale.server.core.asset.monitor.EventKind;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.server.npc.AllNPCsLoadedEvent;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.providerevaluators.ProviderEvaluatorTypeRegistry;
import com.hypixel.hytale.server.npc.asset.builder.validators.ValidatorTypeRegistry;
import com.hypixel.hytale.server.npc.decisionmaker.core.Evaluator;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdLib;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import com.hypixel.hytale.server.spawning.LoadedNPCEvent;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Period;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderManager {
   public static final String CONTENT_KEY = "Content";
   private static final String CLASS_KEY = "Class";
   private static final String TEST_TYPE_KEY = "TestType";
   private static final String FAIL_REASON_KEY = "FailReason";
   private static final String PLAYER_GROUP_TAG = "$player";
   private static final String SELF_GROUP_TAG = "$self";
   private static int playerGroupID;
   private static int selfGroupID;
   private final Int2ObjectConcurrentHashMap<BuilderInfo> builderCache = new Int2ObjectConcurrentHashMap<>();
   private final String elementTypeName = "NPC";
   private final String defaultFileType = NPCPlugin.FACTORY_CLASS_ROLE;
   private boolean autoReload;
   private final Map<Class<?>, BuilderFactory<?>> factoryMap = new HashMap<>();
   private final Map<String, Class<?>> categoryNames = new HashMap<>();
   @Nonnull
   private final Object2IntMap<String> nameToIndexMap;
   private final AtomicInteger nextIndex = new AtomicInteger();
   private final ReentrantReadWriteLock indexLock = new ReentrantReadWriteLock();
   private boolean setup;
   @Nullable
   public static BuilderManager SCHEMA_BUILDER_MANAGER;

   public BuilderManager() {
      this.nameToIndexMap = new Object2IntOpenCustomHashMap(CaseInsensitiveHashStrategy.getInstance());
      this.nameToIndexMap.defaultReturnValue(Integer.MIN_VALUE);
      playerGroupID = this.getOrCreateIndex("$player");
      selfGroupID = this.getOrCreateIndex("$self");
   }

   public <T> void registerFactory(@Nonnull BuilderFactory<T> factory) {
      if (factory == null) {
         throw new IllegalArgumentException();
      } else {
         Class<?> clazz = factory.getCategory();
         if (clazz == null) {
            throw new IllegalArgumentException();
         } else if (this.factoryMap.containsKey(clazz)) {
            throw new IllegalArgumentException(factory.getClass().getSimpleName());
         } else {
            this.factoryMap.put(clazz, factory);
         }
      }
   }

   public void addCategory(String name, Class<?> clazz) {
      this.categoryNames.put(name, clazz);
   }

   public String getCategoryName(@Nonnull Class<?> factoryClass) {
      for (Entry<String, Class<?>> stringClassEntry : this.categoryNames.entrySet()) {
         if (stringClassEntry.getValue() == factoryClass) {
            return stringClassEntry.getKey();
         }
      }

      return factoryClass.getSimpleName();
   }

   public int getIndex(@Nullable String name) {
      if (name != null && !name.isEmpty()) {
         this.indexLock.readLock().lock();

         int var2;
         try {
            var2 = this.nameToIndexMap.getInt(name);
         } finally {
            this.indexLock.readLock().unlock();
         }

         return var2;
      } else {
         return Integer.MIN_VALUE;
      }
   }

   public void setAutoReload(boolean autoReload) {
      this.autoReload = autoReload;
   }

   @Nullable
   public String lookupName(int index) {
      if (index < 0) {
         return null;
      } else {
         BuilderInfo info = this.builderCache.get(index);
         if (info != null) {
            return info.getKeyName();
         } else {
            this.indexLock.readLock().lock();

            String var5;
            try {
               ObjectIterator<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<String>> iterator = Object2IntMaps.fastIterator(this.nameToIndexMap);

               it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<String> entry;
               do {
                  if (!iterator.hasNext()) {
                     return null;
                  }

                  entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<String>)iterator.next();
               } while (entry.getIntValue() != index);

               var5 = (String)entry.getKey();
            } finally {
               this.indexLock.readLock().unlock();
            }

            return var5;
         }
      }
   }

   public int getOrCreateIndex(String name) {
      this.indexLock.writeLock().lock();

      int var3;
      try {
         int index = this.nameToIndexMap.getInt(name);
         if (index < 0) {
            index = this.nextIndex.getAndIncrement();
            this.nameToIndexMap.put(name, index);
            return index;
         }

         var3 = index;
      } finally {
         this.indexLock.writeLock().unlock();
      }

      return var3;
   }

   @Nullable
   public BuilderInfo tryGetBuilderInfo(int builderIndex) {
      return builderIndex < 0 ? null : this.builderCache.get(builderIndex);
   }

   public void unloadBuilders(AssetPack pack) {
      Path path = pack.getRoot().resolve(NPCPlugin.ROLE_ASSETS_PATH);
      AssetMonitor assetMonitor = AssetModule.get().getAssetMonitor();
      if (assetMonitor != null) {
         assetMonitor.removeMonitorDirectoryFiles(path, pack);
      }

      if (Files.isDirectory(path)) {
         try {
            Files.walkFileTree(path, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
               @Nonnull
               public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                  if (BuilderManager.isJsonFile(file) && !BuilderManager.isIgnoredFile(file)) {
                     String builderName = BuilderManager.builderNameFromPath(file);
                     BuilderManager.this.removeBuilder(builderName);
                     NPCPlugin.get().getLogger().at(Level.INFO).log("Deleted %s builder %s", "NPC", builderName);
                  }

                  return FileVisitResult.CONTINUE;
               }
            });
         } catch (IOException var5) {
            throw SneakyThrow.sneakyThrow(var5);
         }
      }
   }

   public boolean loadBuilders(@Nonnull AssetPack pack, final boolean includeTests) {
      Path path = pack.getRoot().resolve(NPCPlugin.ROLE_ASSETS_PATH);
      boolean valid = true;
      NPCPlugin.get().getLogger().at(Level.INFO).log("Starting to load NPC builders!");
      final Object2IntOpenHashMap<String> typeCounter = new Object2IntOpenHashMap();

      try {
         AssetMonitor assetMonitor = AssetModule.get().getAssetMonitor();
         if (assetMonitor != null && !pack.isImmutable() && Files.isDirectory(path)) {
            assetMonitor.removeMonitorDirectoryFiles(path, pack);
            assetMonitor.monitorDirectoryFiles(path, new BuilderManager.BuilderAssetMonitorHandler(pack, includeTests));
         }

         final ObjectArrayList<String> errors = new ObjectArrayList();
         if (Files.isDirectory(path)) {
            Files.walkFileTree(path, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
               @Nonnull
               public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                  if (BuilderManager.isJsonFile(file) && !BuilderManager.isIgnoredFile(file)) {
                     BuilderManager.this.loadFile(file, errors, typeCounter, includeTests, false);
                  }

                  return FileVisitResult.CONTINUE;
               }
            });
         }

         Int2ObjectOpenHashMap<BuilderInfo> loadedBuilders = new Int2ObjectOpenHashMap();

         for (BuilderInfo builderInfo : this.builderCache.values()) {
            try {
               if (this.validateBuilder(builderInfo)) {
                  loadedBuilders.put(builderInfo.getIndex(), builderInfo);
               } else {
                  valid = false;
               }
            } catch (IllegalStateException | IllegalArgumentException var12) {
               valid = false;
               errors.add(String.format("%s: %s", builderInfo.getKeyName(), var12.getMessage()));
            }
         }

         this.setup = true;
         this.validateAllLoadedBuilders(loadedBuilders, false, errors);
         if (!errors.isEmpty()) {
            valid = false;
            ObjectListIterator var17 = errors.iterator();

            while (var17.hasNext()) {
               String error = (String)var17.next();
               NPCPlugin.get().getLogger().at(Level.SEVERE).log("FAIL: " + error);
            }
         }

         errors.clear();
         this.onAllBuildersLoaded(loadedBuilders);
      } catch (IOException var13) {
         throw new SkipSentryException(new RuntimeException(var13));
      }

      StringBuilder output = new StringBuilder();
      output.append("Loaded ").append(this.builderCache.size()).append(" ").append("NPC").append(" configurations");
      ObjectIterator var15 = typeCounter.object2IntEntrySet().iterator();

      while (var15.hasNext()) {
         it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<String> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<String>)var15.next();
         output.append(", ").append((String)entry.getKey()).append(": ").append(entry.getIntValue());
      }

      NPCPlugin.get().getLogger().at(Level.INFO).log(output.toString());
      return valid;
   }

   private void finishLoadingBuilders(@Nonnull Int2ObjectOpenHashMap<BuilderInfo> loadedBuilders, @Nonnull List<String> errors) {
      this.onAllBuildersLoaded(loadedBuilders);
      this.validateAllLoadedBuilders(loadedBuilders, true, errors);
      if (!errors.isEmpty()) {
         for (String error : errors) {
            NPCPlugin.get().getLogger().at(Level.SEVERE).log(error);
         }
      }

      errors.clear();
   }

   public void assetEditorLoadFile(@Nonnull Path fileName) {
      HashSet<String> failedBuilderTexts = new HashSet<>();
      ObjectArrayList<String> errors = new ObjectArrayList();
      Int2ObjectOpenHashMap<BuilderInfo> loadedBuilders = new Int2ObjectOpenHashMap();
      HashSet<String> loadedBuilderNames = new HashSet<>();

      try {
         int builderIndex = this.loadFile(fileName, errors, null, true, true);
         if (builderIndex < 0) {
            return;
         }

         String name = builderNameFromPath(fileName);
         NPCPlugin.get().getLogger().at(Level.INFO).log("Reloaded NPC builder " + name);
         loadedBuilderNames.add(name);

         for (BuilderInfo builderInfo : this.builderCache.values()) {
            if (this.isDependant(builderInfo.getBuilder(), builderInfo.getIndex(), builderIndex)) {
               builderInfo.setNeedsValidation();
            }
         }

         if (this.autoReload) {
            this.reloadDependants(builderIndex);
         }

         BuilderInfo builder = this.builderCache.get(builderIndex);
         onBuilderReloaded(builder);
         loadedBuilders.put(builderIndex, builder);
      } catch (Throwable var10) {
         NPCPlugin.get().getLogger().at(Level.SEVERE).log("Failed to reload %s config %s: %s", "NPC", fileName, var10.getMessage());
         failedBuilderTexts.add(builderNameFromPath(fileName) + ": " + var10.getMessage());
      }

      sendReloadNotification(Message.translation("server.general.assetstore.reloadAssets").param("class", "NPC"), loadedBuilderNames);
      sendReloadNotification(Message.translation("server.general.assetstore.loadFailed").param("class", "NPC"), failedBuilderTexts);
      this.finishLoadingBuilders(loadedBuilders, errors);
   }

   public void assetEditorRemoveFile(@Nonnull Path filePath) {
      String builderName = builderNameFromPath(filePath);
      this.removeBuilder(builderName);
      NPCPlugin.get().getLogger().at(Level.INFO).log("Deleted %s builder %s", "NPC", builderName);
      sendReloadNotification(Message.translation("server.general.assetstore.removedAssets").param("class", "NPC"), Set.of(builderName));
      ObjectArrayList<String> errors = new ObjectArrayList();
      this.finishLoadingBuilders(new Int2ObjectOpenHashMap(), errors);
   }

   public int loadFile(@Nonnull Path fileName, boolean reloading, @Nonnull List<String> errors) {
      return this.loadFile(fileName, errors, null, false, reloading);
   }

   public int loadFile(
      @Nonnull Path fileName, @Nonnull List<String> errors, @Nullable Object2IntMap<String> typeCounter, boolean includeTests, boolean reloading
   ) {
      int errorCount = errors.size();

      JsonObject data;
      try (BufferedReader fileReader = Files.newBufferedReader(fileName)) {
         JsonReader reader = new JsonReader(fileReader);

         try {
            data = JsonParser.parseReader(reader).getAsJsonObject();
         } catch (Throwable var36) {
            try {
               reader.close();
            } catch (Throwable var30) {
               var36.addSuppressed(var30);
            }

            throw var36;
         }

         reader.close();
      } catch (Exception var38) {
         errors.add(fileName + ": Failed to load NPC builder: " + var38.getMessage());
         return Integer.MIN_VALUE;
      }

      String categoryName = this.defaultFileType;
      JsonElement content = data;
      BuilderManager.TestType testType = null;
      JsonElement testTypeElement = data.get("TestType");
      if (testTypeElement != null) {
         try {
            testType = Enum.valueOf(BuilderManager.TestType.class, testTypeElement.getAsString().toUpperCase());
         } catch (Exception var35) {
            errors.add(fileName + ": " + var35.getMessage());
         }

         if (!includeTests) {
            return Integer.MIN_VALUE;
         }
      }

      String keyName = builderNameFromPath(fileName);
      String componentInterface = null;
      StateMappingHelper stateHelper = new StateMappingHelper();
      JsonElement classData = data.get("Class");
      if (classData != null) {
         categoryName = classData.getAsString();
         stateHelper.readComponentDefaultLocalState(data);
         JsonElement interfaceData = data.get("Interface");
         if (interfaceData != null) {
            componentInterface = interfaceData.getAsString();
         }
      }

      Class<?> category = this.categoryNames.get(categoryName);
      if (category == null) {
         errors.add(fileName + ": Failed to load NPC builder, unknown class " + categoryName);
         return Integer.MIN_VALUE;
      } else {
         if (typeCounter != null) {
            JsonElement type = data.get("Type");
            String typeString = testType == null ? (type != null ? type.getAsString() : categoryName) : "Test";
            typeCounter.mergeInt(typeString, 1, Integer::sum);
         }

         BuilderFactory<Object> factory = this.getFactory(category);

         Builder<?> builder;
         try {
            builder = factory.createBuilder(content);
         } catch (Exception var34) {
            errors.add(fileName + ": " + var34.getMessage());
            return Integer.MIN_VALUE;
         }

         String fileNameString = fileName.toString();
         this.checkIfDeprecated(builder, factory, data, fileNameString, categoryName);
         builder.setLabel(categoryName + "|" + factory.getKeyName(data));
         builder.ignoreAttribute("TestType");
         if (testType == BuilderManager.TestType.FAILING) {
            builder.ignoreAttribute("FailReason");
         }

         builder.ignoreAttribute("Parameters");
         BuilderParameters builderParameters = new BuilderParameters(StdLib.getInstance(), fileNameString, componentInterface);

         try {
            builderParameters.readJSON(data, stateHelper);
         } catch (Exception var33) {
            errors.add(fileNameString + ": Failed to load NPC builder, 'Parameters' section invalid: " + var33.getMessage());
            return Integer.MIN_VALUE;
         }

         if (classData != null) {
            builder.ignoreAttribute("Class");
            builder.ignoreAttribute("Interface");
            builder.ignoreAttribute("DefaultState");
            builder.ignoreAttribute("ResetOnStateChange");
         }

         builderParameters.addParametersToScope();
         InternalReferenceResolver internalReferenceResolver = new InternalReferenceResolver();
         AssetExtraInfo.Data extraInfoData = new AssetExtraInfo.Data(null, keyName, null);
         AssetExtraInfo<Object> extraInfo = new AssetExtraInfo<>(extraInfoData);
         ObjectArrayList<Evaluator<?>> evaluators = new ObjectArrayList();
         BuilderValidationHelper validationHelper = new BuilderValidationHelper(
            fileNameString,
            new FeatureEvaluatorHelper(builder.canRequireFeature()),
            internalReferenceResolver,
            stateHelper,
            new InstructionContextHelper(InstructionType.Component),
            extraInfo,
            evaluators,
            errors
         );

         try {
            builder.readConfig(null, content, this, builderParameters, validationHelper);
         } catch (Exception var32) {
            errors.add(fileNameString + ": Failed to load NPC: " + var32.getMessage());
            return Integer.MIN_VALUE;
         }

         internalReferenceResolver.validateInternalReferences(fileNameString, errors);
         extraInfoData.loadContainedAssets(reloading);
         ObjectListIterator entry = evaluators.iterator();

         while (entry.hasNext()) {
            Evaluator<?> evaluator = (Evaluator<?>)entry.next();
            evaluator.initialise();
         }

         internalReferenceResolver.optimise();
         builderParameters.disposeCompileContext();
         stateHelper.validate(fileNameString, errors);
         stateHelper.optimise();
         BuilderInfo entryx = this.tryGetBuilderInfo(this.getIndex(keyName));
         if (entryx != null && entryx.getPath() != null) {
            try {
               if (!Files.isSameFile(fileName, entryx.getPath())) {
                  NPCPlugin.get()
                     .getLogger()
                     .at(Level.WARNING)
                     .log("Replacing asset '%s' of file '%s' with other file '%s'", keyName, entryx.getPath(), fileName);
               }
            } catch (IOException var31) {
            }
         }

         if (testType == BuilderManager.TestType.FAILING) {
            JsonElement failReasonElement = data.get("FailReason");
            if (failReasonElement == null) {
               errors.add(fileName + ": Missing fail reason!");
               return Integer.MIN_VALUE;
            } else if (errors.size() == errorCount) {
               errors.add(fileName + ": Should have failed validation: " + failReasonElement.getAsString());
               return Integer.MIN_VALUE;
            } else if (errors.size() - errorCount > 1) {
               errors.add(fileName + ": Should have failed validation: " + failReasonElement.getAsString() + ", but additional errors were also detected.");
               return Integer.MIN_VALUE;
            } else {
               String error = errors.removeLast();
               if (!error.contains(failReasonElement.getAsString())) {
                  errors.add(fileName + ": Should have failed validation: " + failReasonElement.getAsString() + ", but was instead: " + error);
                  return Integer.MIN_VALUE;
               } else {
                  if (NPCPlugin.get().isLogFailingTestErrors()) {
                     NPCPlugin.get().getLogger().at(Level.WARNING).log("Expected test failure: " + error);
                  }

                  return Integer.MIN_VALUE;
               }
            }
         } else {
            return errors.size() > errorCount ? Integer.MIN_VALUE : this.cacheBuilder(keyName, builder, fileName);
         }
      }
   }

   public boolean validateBuilder(@Nonnull BuilderInfo builderInfo) {
      if (builderInfo.isValidated()) {
         return builderInfo.isValid();
      } else if (!builderInfo.canBeValidated()) {
         return false;
      } else {
         Builder<?> builder = builderInfo.getBuilder();
         return builder.getDependencies().isEmpty() && !builder.hasDynamicDependencies()
            ? builderInfo.setValidated(true)
            : this.validateBuilder(builderInfo, new IntOpenHashSet(), new IntArrayList());
      }
   }

   @Nonnull
   public <T> BuilderFactory<T> getFactory(@Nonnull Class<?> clazz) {
      if (clazz == null) {
         throw new IllegalArgumentException("No factory class supplied!");
      } else {
         BuilderFactory<T> factory = (BuilderFactory<T>)this.factoryMap.get(clazz);
         if (factory == null) {
            throw new NullPointerException(String.format("Factory for type '%s' is not registered!", clazz.getSimpleName()));
         } else if (factory.getCategory() != clazz) {
            throw new IllegalArgumentException(
               String.format("Factory class mismatch! Expected %s, was %s", clazz.getSimpleName(), factory.getCategory().getSimpleName())
            );
         } else {
            return factory;
         }
      }
   }

   @Nonnull
   public BuilderInfo getCachedBuilderInfo(int index, @Nonnull Class<?> classType) {
      if (index < 0) {
         throw new SkipSentryException(new IllegalArgumentException("Builder asset can't have negative index " + index));
      } else {
         BuilderInfo builderInfo = this.tryGetCachedBuilderInfo(index, classType);
         if (builderInfo == null) {
            throw new SkipSentryException(new IllegalArgumentException(String.format("Asset '%s' (%s) is not available", this.lookupName(index), index)));
         } else {
            return builderInfo;
         }
      }
   }

   @Nullable
   public <T> Builder<T> tryGetCachedValidBuilder(int index, @Nonnull Class<?> classType) {
      BuilderInfo builderInfo = this.tryGetCachedBuilderInfo(index, classType);
      return (Builder<T>)(builderInfo != null && builderInfo.isValid() ? builderInfo.getBuilder() : null);
   }

   public <T> Builder<T> getCachedBuilder(int index, @Nonnull Class<?> classType) {
      BuilderInfo builderInfo = this.getCachedBuilderInfo(index, classType);
      return (Builder<T>)builderInfo.getBuilder();
   }

   public boolean isEmpty() {
      return this.builderCache.isEmpty();
   }

   @Nonnull
   public Int2ObjectMap<BuilderInfo> getAllBuilders() {
      Int2ObjectOpenHashMap<BuilderInfo> builders = new Int2ObjectOpenHashMap();

      for (BuilderInfo builder : this.builderCache.values()) {
         builders.put(builder.getIndex(), builder);
      }

      return builders;
   }

   public <T extends Collection<?>> T collectMatchingBuilders(
      T collection, @Nonnull Predicate<BuilderInfo> filter, @Nonnull BiConsumer<BuilderInfo, T> consumer
   ) {
      for (BuilderInfo builderInfo : this.builderCache.values()) {
         if (filter.test(builderInfo)) {
            consumer.accept(builderInfo, collection);
         }
      }

      return collection;
   }

   @Nonnull
   public Object2IntMap<String> getNameToIndexMap() {
      Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap();
      if (!this.setup) {
         return Object2IntMaps.unmodifiable(map);
      } else {
         this.indexLock.readLock().lock();

         try {
            ObjectIterator<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<String>> iterator = Object2IntMaps.fastIterator(this.nameToIndexMap);

            while (iterator.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<String> next = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<String>)iterator.next();
               map.put((String)next.getKey(), next.getIntValue());
            }
         } finally {
            this.indexLock.readLock().unlock();
         }

         return Object2IntMaps.unmodifiable(map);
      }
   }

   @Nullable
   public <T> BuilderInfo findMatchingBuilder(@Nonnull BiPredicate<BuilderInfo, T> filter, T t) {
      for (BuilderInfo builderInfo : this.builderCache.values()) {
         if (filter.test(builderInfo, t)) {
            return builderInfo;
         }
      }

      return null;
   }

   @Nullable
   public BuilderInfo getBuilderInfo(Builder<?> builder) {
      return this.findMatchingBuilder((builderInfo, b) -> builderInfo.getBuilder() == b, builder);
   }

   public List<String> getTemplateNames() {
      return this.collectMatchingBuilders(new ObjectArrayList(), builderInfo -> true, (builderInfo, strings) -> strings.add(builderInfo.getKeyName()));
   }

   public void forceValidation(int builderIndex) {
      BuilderInfo builderInfo = this.tryGetBuilderInfo(builderIndex);
      if (builderInfo != null) {
         IntSet dependencies = this.computeAllDependencies(builderInfo.getBuilder(), builderInfo.getIndex());
         builderInfo.setForceValidation();
         IntIterator i = dependencies.iterator();

         while (i.hasNext()) {
            builderInfo = this.tryGetBuilderInfo(i.nextInt());
            if (builderInfo != null) {
               builderInfo.setForceValidation();
            }
         }
      }
   }

   public void checkIfDeprecated(
      @Nonnull Builder<?> builder, @Nonnull BuilderFactory<?> builderFactory, @Nonnull JsonElement element, String fileName, String context
   ) {
      if (builder.isDeprecated()) {
         NPCPlugin.get()
            .getLogger()
            .at(Level.WARNING)
            .log(
               "Builder %s of type %s is deprecated and should be replaced in %s: %s",
               builderFactory.getKeyName(element),
               this.getCategoryName(builderFactory.getCategory()),
               context,
               fileName
            );
      }
   }

   @Nonnull
   public Schema generateSchema(@Nonnull SchemaContext context) {
      Schema var18;
      try {
         SCHEMA_BUILDER_MANAGER = this;
         BuilderFactory<?> roleFactory = this.factoryMap.get(Role.class);
         Schema schema = roleFactory.toSchema(context, true);
         ObjectSchema check = new ObjectSchema();
         check.setRequired("Class", "Type");
         StringSchema keys = new StringSchema();
         keys.setEnum(this.categoryNames.keySet().toArray(String[]::new));
         check.setProperties(Map.of("Class", keys));
         check.setProperties(Map.of("Type", StringSchema.constant("Component")));
         Schema dynamicComponent = new Schema();
         dynamicComponent.setIf(check);
         Schema[] subSchemas = new Schema[this.categoryNames.size()];
         int index = 0;

         for (Entry<String, Class<?>> cats : this.categoryNames.entrySet()) {
            BuilderFactory<Object> factory = this.getFactory(cats.getValue());
            Schema s = factory.toSchema(context, true);
            Schema cond = new Schema();
            ObjectSchema classCheck = new ObjectSchema();
            classCheck.setProperties(Map.of("Class", StringSchema.constant(cats.getKey())));
            cond.setIf(classCheck);
            cond.setThen(s);
            cond.setElse(false);
            subSchemas[index++] = cond;
         }

         dynamicComponent.setThen(Schema.anyOf(subSchemas));
         dynamicComponent.setElse(false);
         schema.getThen().setAnyOf(ArrayUtil.append(schema.getThen().getAnyOf(), dynamicComponent));
         var18 = schema;
      } finally {
         SCHEMA_BUILDER_MANAGER = null;
      }

      return var18;
   }

   @Nonnull
   public List<BuilderDescriptor> generateDescriptors() {
      ObjectArrayList<BuilderDescriptor> builderDescriptors = new ObjectArrayList();

      for (BuilderFactory<?> builderFactory : this.factoryMap.values()) {
         String categoryName = this.getCategoryName(builderFactory.getCategory());
         Builder<?> defaultBuilder = builderFactory.tryCreateDefaultBuilder();
         if (defaultBuilder != null) {
            try {
               builderDescriptors.add(defaultBuilder.getDescriptor(categoryName, categoryName, this));
            } catch (NullPointerException | IllegalStateException var11) {
               NPCPlugin.get().getLogger().at(Level.SEVERE).log("Failed to build descriptor for %s %s: %s", categoryName, categoryName, var11.getMessage());
            }
         }

         for (String builderName : builderFactory.getBuilderNames()) {
            Builder<?> builder = builderFactory.createBuilder(builderName);
            Objects.requireNonNull(builder, "Unable to create builder for descriptor generation");
            String name = builderName != null && !builderName.isEmpty() ? builderName : categoryName;
            if (!name.equals("Component")) {
               try {
                  builderDescriptors.add(builder.getDescriptor(name, categoryName, this));
               } catch (NullPointerException | IllegalStateException var12) {
                  NPCPlugin.get().getLogger().at(Level.SEVERE).log("Failed to build descriptor for %s %s: %s", categoryName, name, var12.getMessage());
               }
            }
         }
      }

      return builderDescriptors;
   }

   public static void saveDescriptors(List<BuilderDescriptor> builderDescriptors, @Nonnull Path fileName) {
      try (BufferedWriter fileWriter = Files.newBufferedWriter(fileName)) {
         GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
         gsonBuilder.registerTypeAdapter(Duration.class, (JsonSerializer)(src, typeOfSrc, context) -> new JsonPrimitive(src.toString()));
         gsonBuilder.registerTypeAdapter(Period.class, (JsonSerializer)(src, typeOfSrc, context) -> new JsonPrimitive(src.toString()));
         ValidatorTypeRegistry.registerTypes(gsonBuilder);
         ProviderEvaluatorTypeRegistry.registerTypes(gsonBuilder);
         Gson gson = gsonBuilder.create();
         gson.toJson(builderDescriptors, fileWriter);
      } catch (IOException var7) {
         NPCPlugin.get().getLogger().at(Level.SEVERE).log("Failed to write builder descriptors to %s", fileName);
      }
   }

   @Nullable
   public Builder<Role> tryGetCachedValidRole(int builderIndex) {
      return this.tryGetCachedValidBuilder(builderIndex, Role.class);
   }

   public void validateAllLoadedBuilders(@Nonnull Int2ObjectMap<BuilderInfo> loadedBuilders, boolean validateDependents, @Nonnull List<String> errors) {
      NPCPlugin.get().getLogger().at(Level.INFO).log("Validating loaded NPC configurations...");
      validateAllSpawnableNPCs(loadedBuilders, errors);
      if (validateDependents) {
         Int2ObjectOpenHashMap<BuilderInfo> dependents = new Int2ObjectOpenHashMap();
         loadedBuilders.forEach(
            (index, builderInfo) -> {
               for (BuilderInfo info : this.builderCache.values()) {
                  int builderIndex = info.getIndex();
                  Builder<?> builder = info.getBuilder();

                  boolean isDependent;
                  try {
                     isDependent = this.isDependant(builder, builderIndex, index);
                  } catch (IllegalStateException | IllegalArgumentException | SkipSentryException var10) {
                     NPCPlugin.get()
                        .getLogger()
                        .at(Level.WARNING)
                        .log("Could not check if builder %s was dependent: %s", this.lookupName(info.getIndex()), var10.getMessage());
                     continue;
                  }

                  if (builder.isSpawnable() && isDependent) {
                     dependents.put(builderIndex, info);
                  }
               }
            }
         );
         validateAllSpawnableNPCs(dependents, errors);
      }

      NPCPlugin.get().getLogger().at(Level.INFO).log("Validation complete.");
   }

   public void onAllBuildersLoaded(@Nonnull Int2ObjectMap<BuilderInfo> loadedBuilders) {
      if (!loadedBuilders.isEmpty()) {
         IEventDispatcher<AllNPCsLoadedEvent, AllNPCsLoadedEvent> dispatcher = HytaleServer.get().getEventBus().dispatchFor(AllNPCsLoadedEvent.class);
         if (dispatcher.hasListener()) {
            dispatcher.dispatch(new AllNPCsLoadedEvent(this.getAllBuilders(), loadedBuilders));
         }

         this.getAllBuilders().forEach((index, builderInfo) -> {
            if (builderInfo.needsValidation()) {
               NPCPlugin.get().testAndValidateRole(builderInfo);
            }
         });
      }
   }

   public static void onBuilderReloaded(@Nonnull BuilderInfo builderInfo) {
      builderInfo.getBuilder().clearDynamicDependencies();
      NPCPlugin.reloadNPCsWithRole(builderInfo.getIndex());
   }

   public static int getPlayerGroupID() {
      return playerGroupID;
   }

   public static int getSelfGroupID() {
      return selfGroupID;
   }

   protected static void onBuilderAdded(@Nonnull BuilderInfo builderInfo) {
      if (builderInfo.getBuilder().isSpawnable()) {
         IEventDispatcher<LoadedNPCEvent, LoadedNPCEvent> dispatcher = HytaleServer.get().getEventBus().dispatchFor(LoadedNPCEvent.class);
         if (dispatcher.hasListener()) {
            dispatcher.dispatch(new LoadedNPCEvent(builderInfo));
         }
      }
   }

   protected boolean isDependant(@Nonnull Builder<?> builder, int builderIndex, int dependencyIndex) {
      return builderIndex == dependencyIndex ? true : this.computeAllDependencies(builder, builderIndex).contains(dependencyIndex);
   }

   protected int cacheBuilder(String name, Builder<?> builder, Path path) {
      this.indexLock.writeLock().lock();

      int index;
      try {
         index = this.nameToIndexMap.getInt(name);
         if (index >= 0) {
            this.removeBuilder(index);
         } else {
            index = this.nextIndex.getAndIncrement();
            this.nameToIndexMap.put(name, index);
         }
      } finally {
         this.indexLock.writeLock().unlock();
      }

      BuilderInfo builderInfo = new BuilderInfo(index, name, builder, path);
      this.builderCache.put(index, builderInfo);
      onBuilderAdded(builderInfo);
      return index;
   }

   private void removeBuilder(int index) {
      BuilderInfo builder = this.builderCache.remove(index);
      if (builder != null) {
         builder.setRemoved();
      }
   }

   private void removeBuilder(String name) {
      int index = this.getIndex(name);
      if (index >= 0) {
         this.removeBuilder(index);
      }
   }

   @Nullable
   private Builder<?> tryGetCachedBuilder(int index) {
      BuilderInfo entry = this.tryGetBuilderInfo(index);
      return entry == null ? null : entry.getBuilder();
   }

   @Nullable
   private BuilderInfo tryGetCachedBuilderInfo(int index, @Nonnull Class<?> classType) {
      BuilderInfo entry = this.tryGetBuilderInfo(index);
      if (entry == null) {
         return null;
      } else {
         Builder<?> cachedBuilder = entry.getBuilder();
         if (cachedBuilder.category() != classType) {
            throw new IllegalArgumentException(
               String.format(
                  "Asset '%s'(%s) is different type. Is '%s' but should be '%s'",
                  this.lookupName(index),
                  index,
                  cachedBuilder.category().getName(),
                  classType.getName()
               )
            );
         } else {
            return entry;
         }
      }
   }

   private static void validateAllSpawnableNPCs(@Nonnull Int2ObjectMap<BuilderInfo> builders, @Nonnull List<String> errors) {
      builders.forEach(
         (index, builderInfo) -> {
            Builder<?> builder = builderInfo.getBuilder();
            if (builder.isSpawnable() && builder instanceof SpawnableWithModelBuilder<?> spawnableBuilder) {
               ExecutionContext context = new ExecutionContext(builder.getBuilderParameters().createScope());
               String fileName = builderInfo.getPath().toString();

               String modelName;
               try {
                  modelName = spawnableBuilder.getSpawnModelName(context, spawnableBuilder.createModifierScope(context));
               } catch (IllegalStateException | SkipSentryException var12) {
                  errors.add(String.format("%s: %s", fileName, var12.getMessage()));
                  builderInfo.setValidated(false);
                  return;
               }

               ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelName);
               if (modelAsset == null) {
                  errors.add(String.format("%s: Model %s does not exist.", fileName, modelName));
                  builderInfo.setValidated(false);
               } else {
                  Model model = Model.createScaledModel(modelAsset, modelAsset.getMaxScale());
                  Builder<?> builderInstance = builderInfo.getBuilder();
                  NPCLoadTimeValidationHelper validationHelper = new NPCLoadTimeValidationHelper(fileName, model, !builderInstance.isSpawnable());
                  if (!builderInstance.validate(fileName, validationHelper, context, context.getScope(), errors)
                     || !validationHelper.validateMotionControllers(errors)
                     || !validationHelper.getValueStoreValidator().validate(errors)) {
                     builderInfo.setValidated(false);
                  }
               }
            }
         }
      );
   }

   private static void sendReloadNotification(Message message, @Nonnull Set<String> builders) {
      if (!builders.isEmpty()) {
         NotificationUtil.sendNotificationToUniverse(message, Message.raw(builders.toString()), NotificationStyle.Warning);
      }
   }

   private static boolean isIgnoredFile(@Nonnull Path path) {
      return !path.getFileName().toString().isEmpty() && path.getFileName().toString().charAt(0) == '!';
   }

   private static boolean isJsonFile(@Nonnull Path path) {
      return Files.isRegularFile(path) && path.toString().endsWith(".json");
   }

   private static boolean isJsonFileName(@Nonnull Path path, EventKind eventKind) {
      return path.toString().endsWith(".json");
   }

   @Nonnull
   private static String builderNameFromPath(@Nonnull Path path) {
      String fileName = path.getFileName().toString();
      if (fileName.startsWith("NPCRole-")) {
         fileName = fileName.split("-")[1];
      }

      int endIndex = fileName.lastIndexOf(46);
      return endIndex >= 0 ? fileName.substring(0, endIndex) : fileName;
   }

   @Nonnull
   private String buildPathString(@Nonnull IntArrayList path, int index) {
      if (path.isEmpty()) {
         return "";
      } else {
         StringBuilder result = new StringBuilder();
         result.append(" (Path: ");
         IntIterator i = path.iterator();

         while (i.hasNext()) {
            result.append(this.lookupName(i.nextInt())).append(" -> ");
         }

         result.append(this.lookupName(index)).append(')');
         return result.toString();
      }
   }

   private boolean validateBuilder(@Nonnull BuilderInfo builderInfo, @Nonnull IntSet validatedDependencies, @Nonnull IntArrayList path) {
      int index = builderInfo.getIndex();
      if (path.contains(index)) {
         NPCPlugin.get()
            .getLogger()
            .at(Level.SEVERE)
            .log(
               "Builder '%s' validation failed: Cyclic reference detected for builder '%s'%s",
               this.lookupName(path.getInt(0)),
               this.lookupName(index),
               this.buildPathString(path, index)
            );
         return builderInfo.setValidated(false);
      } else {
         path.add(index);

         IntSet dependencies;
         try {
            dependencies = this.computeAllDependencies(builderInfo.getBuilder(), builderInfo.getIndex());
         } catch (IllegalStateException | IllegalArgumentException | SkipSentryException var10) {
            NPCPlugin.get().getLogger().at(Level.SEVERE).log("Builder '%s' validation failed: %s", this.lookupName(path.getInt(0)), var10.getMessage());
            return builderInfo.setValidated(false);
         }

         boolean valid = true;
         IntIterator i = dependencies.iterator();

         while (i.hasNext()) {
            int dependency = i.nextInt();
            if (path.contains(dependency)) {
               NPCPlugin.get()
                  .getLogger()
                  .at(Level.SEVERE)
                  .log(
                     "Builder '%s' validation failed: Cyclic reference detected for builder '%s'%s",
                     this.lookupName(path.getInt(0)),
                     this.lookupName(dependency),
                     this.buildPathString(path, index)
                  );
               return builderInfo.setValidated(false);
            }

            if (validatedDependencies.add(dependency)) {
               BuilderInfo childBuilder = this.builderCache.get(dependency);
               if (childBuilder == null) {
                  NPCPlugin.get()
                     .getLogger()
                     .at(Level.SEVERE)
                     .log(
                        "Builder '%s' validation failed: Reference to unknown builder '%s'%s",
                        this.lookupName(path.getInt(0)),
                        this.lookupName(dependency),
                        this.buildPathString(path, dependency)
                     );
                  valid = false;
               } else if (!childBuilder.isValidated()) {
                  valid = this.validateBuilder(childBuilder, validatedDependencies, path);
               } else if (!childBuilder.isValid()) {
                  NPCPlugin.get()
                     .getLogger()
                     .at(Level.SEVERE)
                     .log(
                        "Builder '%s' validation failed: Reference to invalid builder '%s'%s",
                        this.lookupName(path.getInt(0)),
                        childBuilder.getKeyName(),
                        this.buildPathString(path, dependency)
                     );
                  valid = false;
               }
            }
         }

         path.removeInt(path.size() - 1);
         return builderInfo.setValidated(valid);
      }
   }

   @Nonnull
   private IntSet computeAllDependencies(@Nonnull Builder<?> builder, int builderIndex) {
      return this.computeAllDependencies(builder, builderIndex, new IntOpenHashSet(), new IntArrayList());
   }

   @Nonnull
   private IntSet computeAllDependencies(@Nonnull Builder<?> builder, int builderIndex, @Nonnull IntSet dependencies, @Nonnull IntArrayList path) {
      if (path.contains(builderIndex)) {
         throw new SkipSentryException(new IllegalArgumentException("Cyclic reference detected for builder: " + this.lookupName(builderIndex)));
      } else {
         path.add(builderIndex);
         this.iterateDependencies(builder.getDependencies().iterator(), dependencies, path);
         if (builder.hasDynamicDependencies()) {
            this.iterateDependencies(builder.getDynamicDependencies().iterator(), dependencies, path);
         }

         path.removeInt(path.size() - 1);
         return dependencies;
      }
   }

   private void iterateDependencies(@Nonnull IntIterator iterator, @Nonnull IntSet dependencies, @Nonnull IntArrayList path) {
      while (iterator.hasNext()) {
         int dependency = iterator.nextInt();
         if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
            Builder<?> child = this.tryGetCachedBuilder(dependency);
            if (child == null) {
               throw new SkipSentryException(new IllegalStateException("Reference to unknown builder: " + this.lookupName(dependency)));
            }

            this.computeAllDependencies(child, dependency, dependencies, path);
         }
      }
   }

   private void reloadDependants(int dependency) {
      for (BuilderInfo builderInfo : this.builderCache.values()) {
         int index = builderInfo.getIndex();
         String keyName = builderInfo.getKeyName();
         Builder<?> builder = builderInfo.getBuilder();

         try {
            if (builder.isSpawnable() && this.isDependant(builder, index, dependency)) {
               NPCPlugin.get()
                  .getLogger()
                  .at(Level.INFO)
                  .log("Reloading entities of type '%s' because dependency '%s' changed", keyName, this.lookupName(dependency));
               onBuilderReloaded(builderInfo);
            }
         } catch (Throwable var8) {
            NPCPlugin.get().getLogger().at(Level.INFO).log("Failed to reload entities of type '%s': %s", keyName, var8.getMessage());
         }
      }
   }

   private class BuilderAssetMonitorHandler implements AssetMonitorHandler {
      private final AssetPack pack;
      private final boolean includeTests;

      public BuilderAssetMonitorHandler(AssetPack pack, boolean includeTests) {
         this.pack = pack;
         this.includeTests = includeTests;
      }

      @Override
      public Object getKey() {
         return this.pack;
      }

      public boolean test(Path path, EventKind eventKind) {
         return BuilderManager.isJsonFileName(path, eventKind);
      }

      public void accept(Map<Path, EventKind> map) {
         Int2ObjectOpenHashMap<BuilderInfo> loadedBuilders = new Int2ObjectOpenHashMap();
         HashSet<String> loadedBuilderNames = new HashSet<>();
         HashSet<String> failedBuilderTexts = new HashSet<>();
         HashSet<String> deletedBuilderNames = new HashSet<>();
         ObjectArrayList<String> errors = new ObjectArrayList();

         for (Entry<Path, EventKind> entry : map.entrySet()) {
            Path path = entry.getKey();
            EventKind eventKind = entry.getValue();
            if (eventKind != EventKind.ENTRY_CREATE && eventKind != EventKind.ENTRY_MODIFY) {
               if (eventKind == EventKind.ENTRY_DELETE) {
                  String builderName = BuilderManager.builderNameFromPath(path);
                  BuilderManager.this.removeBuilder(builderName);
                  NPCPlugin.get().getLogger().at(Level.INFO).log("Deleted %s builder %s", "NPC", builderName);
                  deletedBuilderNames.add(builderName);
               }
            } else if (Files.isRegularFile(path) && !BuilderManager.isIgnoredFile(path)) {
               try {
                  int builderIndex = BuilderManager.this.loadFile(path, errors, null, this.includeTests, true);
                  if (builderIndex >= 0) {
                     String name = BuilderManager.builderNameFromPath(path);
                     NPCPlugin.get().getLogger().at(Level.INFO).log("Reloaded NPC builder " + name);
                     loadedBuilderNames.add(name);

                     for (BuilderInfo builderInfo : BuilderManager.this.builderCache.values()) {
                        try {
                           if (BuilderManager.this.isDependant(builderInfo.getBuilder(), builderInfo.getIndex(), builderIndex)) {
                              builderInfo.setNeedsValidation();
                           }
                        } catch (IllegalArgumentException | IllegalStateException var16) {
                           NPCPlugin.get()
                              .getLogger()
                              .at(Level.WARNING)
                              .log(
                                 "Could not check if builder %s was dependent: %s", BuilderManager.this.lookupName(builderInfo.getIndex()), var16.getMessage()
                              );
                        }
                     }

                     if (BuilderManager.this.autoReload) {
                        BuilderManager.this.reloadDependants(builderIndex);
                     }

                     BuilderInfo builder = BuilderManager.this.builderCache.get(builderIndex);
                     BuilderManager.onBuilderReloaded(builder);
                     loadedBuilders.put(builderIndex, builder);
                  }
               } catch (Throwable var17) {
                  NPCPlugin.get().getLogger().at(Level.SEVERE).log("Failed to reload %s config %s: %s", "NPC", path, var17.getMessage());
                  failedBuilderTexts.add(BuilderManager.builderNameFromPath(path) + ": " + var17.getMessage());
               }
            }
         }

         BuilderManager.sendReloadNotification(Message.translation("server.general.assetstore.reloadAssets").param("class", "NPC"), loadedBuilderNames);
         BuilderManager.sendReloadNotification(Message.translation("server.general.assetstore.loadFailed").param("class", "NPC"), failedBuilderTexts);
         BuilderManager.sendReloadNotification(Message.translation("server.general.assetstore.removedAssets").param("class", "NPC"), deletedBuilderNames);
         BuilderManager.this.finishLoadingBuilders(loadedBuilders, errors);
      }
   }

   private static enum TestType {
      NORMAL,
      FAILING;
   }
}
