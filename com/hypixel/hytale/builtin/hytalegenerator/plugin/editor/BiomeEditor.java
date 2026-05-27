package com.hypixel.hytale.builtin.hytalegenerator.plugin.editor;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.asseteditor.util.BsonTransformationUtil;
import com.hypixel.hytale.builtin.hytalegenerator.Viewport;
import com.hypixel.hytale.builtin.hytalegenerator.assets.AssetManager;
import com.hypixel.hytale.builtin.hytalegenerator.assets.biomes.BiomeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.basic.BasicWorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.plugin.HandleProvider;
import com.hypixel.hytale.builtin.hytalegenerator.plugin.HytaleGenerator;
import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.joml.Vector3d;
import org.joml.Vector3i;

public interface BiomeEditor {
   HytaleLogger LOGGER = HytaleLogger.get("BiomeEditor");
   String[] BIOME_NAME_PATH = new String[]{"Name"};
   String[] BIOMES_PATH = new String[]{"Biomes"};
   String[] DEFAULT_BIOME_PATH = new String[]{"DefaultBiome"};

   static CompletableFuture<BiomeEditor.Context> setupAssets(@Nonnull BiomeEditor.Config config) {
      return AssetPackUtil.getOrCreatePack(config.group, config.pack)
         .thenApply(pack -> {
            BiomeEditor.Context context = new BiomeEditor.Context();
            context.config = config;
            context.pack = pack;
            return context;
         })
         .thenComposeAsync(context -> {
            AssetPack pack = context.pack;
            PluginIdentifier identifier = new PluginIdentifier(pack.getManifest());
            HytaleServerConfig serverConfig = HytaleServer.get().getConfig();
            HytaleServerConfig.setBoot(serverConfig, identifier, true);
            serverConfig.markChanged();
            return serverConfig.consumeHasChanged() ? HytaleServerConfig.save(serverConfig).thenApply(context) : CompletableFuture.completedFuture(context);
         })
         .thenApplyAsync(
            SneakyThrow.sneakyFunction(
               context -> {
                  ((HytaleLogger.Api)LOGGER.atInfo()).log("Creating Biome %s with Template %s", context.config.name, context.config.biomeTemplate);
                  createAssetFromTemplate(
                     context,
                     context.config.biomeTemplate,
                     BiomeAsset.class,
                     BiomeAsset.CODEC,
                     (document, cfg) -> BsonTransformationUtil.setProperty(document, BIOME_NAME_PATH, new BsonString(cfg.name))
                  );
                  return context;
               }
            )
         )
         .thenApplyAsync(
            SneakyThrow.sneakyFunction(
               context -> {
                  ((HytaleLogger.Api)LOGGER.atInfo()).log("Creating WorldStructure %s with Template %s", config.name, config.worldStructureTemplate);
                  createAssetFromTemplate(
                     context, context.config.worldStructureTemplate, WorldStructureAsset.class, BasicWorldStructureAsset.CODEC, (document, cfg) -> {
                        BsonTransformationUtil.setProperty(document, BIOMES_PATH, new BsonArray());
                        BsonTransformationUtil.setProperty(document, DEFAULT_BIOME_PATH, new BsonString(cfg.name));
                     }
                  );
                  return context;
               }
            )
         )
         .thenComposeAsync(context -> {
            ((HytaleLogger.Api)LOGGER.atInfo()).log("Creating Instance %s", config.name);
            HandleProvider provider = createWorldGenProvider();
            provider.setWorldStructureName(config.name);
            WorldConfig world = new WorldConfig();
            world.setWorldGenProvider(provider);
            world.setSavingConfig(config.enablePersistence);
            world.setSaveNewChunks(config.enablePersistence);
            world.setCanSaveChunks(config.enablePersistence);
            world.setSavingPlayers(config.enablePersistence);
            world.setDeleteOnRemove(!config.enablePersistence);
            Path path = context.pack.getRoot().resolve("Server", "Instances", config.name, "instance.bson");
            return WorldConfig.save(path, world).thenApply(context);
         })
         .whenComplete((context, error) -> {
            if (error != null) {
               ((HytaleLogger.Api)((HytaleLogger.Api)LOGGER.atWarning()).withCause(error)).log("Error occurred whilst setting up assets");
            }
         });
   }

   static CompletableFuture<Void> setupViewport(@Nonnull BiomeEditor.Config config, @Nonnull Ref<EntityStore> ref) {
      return setupViewport(setupAssets(config), ref);
   }

   static CompletableFuture<Void> setupViewport(@Nonnull CompletableFuture<BiomeEditor.Context> future, @Nonnull Ref<EntityStore> ref) {
      World sync = ref.getStore().getExternalData().getWorld();
      return future.<Viewport>thenComposeAsync(
            context -> {
               if (!ref.isValid()) {
                  throw new IllegalStateException("Ref no longer valid");
               } else {
                  Store<EntityStore> store = ref.getStore();
                  World world = store.getExternalData().getWorld();
                  TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
                  if (transform == null) {
                     throw new IllegalStateException("Entity ref does not have TransformComponent");
                  } else {
                     ((HytaleLogger.Api)LOGGER.atInfo()).log("Creating world Instance %s", context.config.name);
                     CompletableFuture<World> instance = InstancesPlugin.get()
                        .spawnInstance(
                           context.config.name, world, new Transform(new Vector3d(transform.getPosition()), new Rotation3f(transform.getRotation()))
                        );
                     ((HytaleLogger.Api)LOGGER.atInfo()).log("Teleporting player to Instance %s", context.config.name);
                     InstancesPlugin.teleportPlayerToLoadingInstance(ref, store, instance, null);
                     return instance.thenApply(context::createViewport);
                  }
               }
            },
            sync
         )
         .thenAcceptAsync(viewport -> {
            AssetManager assetManager = HytaleGenerator.get().getAssetManager();
            if (assetManager == null) {
               throw new IllegalStateException("AssetManager is not available. Unable to configure viewport");
            } else {
               assetManager.registerReloadListener(viewport::submitRefresh);
            }
         }, sync);
   }

   static <T extends JsonAssetWithMap<String, M>, M extends AssetMap<String, T>> void createAssetFromTemplate(
      @Nonnull BiomeEditor.Context context,
      @Nullable String template,
      @Nonnull Class<T> type,
      @Nonnull BuilderCodec<? extends T> defaultCodec,
      @Nonnull BiConsumer<BsonDocument, BiomeEditor.Config> templateConsumer
   ) throws IllegalStateException, IOException {
      AssetPack pack = context.pack;
      String name = context.config.name;
      AssetStore<String, T, M> store = AssetRegistry.getAssetStore(type);
      M assetMap = store.getAssetMap();
      T asset = (T)assetMap.getAsset(name);
      if (asset != null) {
         if (!pack.getName().equals(assetMap.getAssetPack(name))) {
            BsonDocument document = store.getCodec().encode(asset, ExtraInfo.THREAD_LOCAL.get()).asDocument();
            templateConsumer.accept(document, context.config);
            AssetPackUtil.exportAsset(pack, name, document, type);
         }
      } else if (template == null) {
         T value = (T)defaultCodec.getDefaultValue();
         BsonDocument document = store.getCodec().encode(value, ExtraInfo.THREAD_LOCAL.get()).asDocument();
         templateConsumer.accept(document, context.config);
         AssetPackUtil.exportAsset(pack, name, document, type);
      } else {
         Path templatePath = assetMap.getPath(template);
         if (templatePath == null) {
            throw new IllegalStateException("Template asset does not exist: " + template);
         } else {
            try (RawJsonReader reader = RawJsonReader.fromPath(templatePath, RawJsonReader.READ_BUFFER.get())) {
               BsonDocument document = RawJsonReader.readBsonDocument(reader);
               templateConsumer.accept(document, context.config);
               AssetPackUtil.exportAsset(pack, name, document, type);
            }
         }
      }
   }

   static HandleProvider createWorldGenProvider() {
      BuilderCodec<? extends IWorldGenProvider> codec = IWorldGenProvider.CODEC.getCodecFor("HytaleGenerator");
      if (codec.getDefaultValue() instanceof HandleProvider provider) {
         return provider;
      } else {
         throw new IllegalStateException("HandleProvider codec returned unexpected default value type");
      }
   }

   public static class Config {
      @Nonnull
      public String name = "";
      @Nonnull
      public String pack = "Biomes";
      @Nonnull
      public String group = "User";
      @Nullable
      public String biomeTemplate = "Basic";
      @Nullable
      public String worldStructureTemplate = "Basic";
      @Nonnull
      public transient Bounds3i viewport = BiomeEditor.Defaults.DEFAULT_VIEWPORT_BOUNDS;
      public transient boolean enablePersistence = false;
   }

   public static class Context implements Function<Object, BiomeEditor.Context> {
      protected BiomeEditor.Config config;
      protected AssetPack pack;

      @Nonnull
      public BiomeEditor.Context apply(@Nullable Object ignored) {
         return this;
      }

      @Nonnull
      public Viewport createViewport(@Nonnull World world) {
         return new Viewport(this.config.viewport, world);
      }
   }

   public static class Defaults {
      public static final BuilderCodec<BiomeEditor.Defaults> CODEC = BuilderCodec.builder(BiomeEditor.Defaults.class, BiomeEditor.Defaults::new)
         .documentation("Default configuration options for the BiomeEditor")
         .<String>append(new KeyedCodec<>("Group", Codec.STRING), (self, value) -> self.group = value, self -> self.group)
         .documentation("The asset-pack group to create new biomes under")
         .add()
         .<String>append(new KeyedCodec<>("Pack", Codec.STRING), (self, value) -> self.pack = value, self -> self.pack)
         .documentation("The asset-pack name to create new biomes under")
         .add()
         .<String>append(new KeyedCodec<>("DefaultBiome", Codec.STRING), (self, value) -> self.biome = value, self -> self.biome)
         .documentation("The Biome asset template selected as default")
         .add()
         .<String>append(new KeyedCodec<>("DefaultWorldStructure", Codec.STRING), (self, value) -> self.worldStructure = value, self -> self.worldStructure)
         .documentation("The WorldStructure asset template selected as default")
         .add()
         .<String[]>append(
            new KeyedCodec<>("AssetTemplateTags", Codec.STRING_ARRAY), (self, value) -> self.assetTemplateTags = value, self -> self.assetTemplateTags
         )
         .documentation("List of tags to match Biome and WorldStructure assets to use as templates")
         .add()
         .<Vector3i>append(new KeyedCodec<>("ViewportMin", Vector3iUtil.CODEC), (self, value) -> self.viewportMin = value, self -> self.viewportMin)
         .documentation("The min corner of the Viewport to set up in the biome editor world")
         .add()
         .<Vector3i>append(new KeyedCodec<>("ViewportMax", Vector3iUtil.CODEC), (self, value) -> self.viewportMax = value, self -> self.viewportMax)
         .documentation("The max corner of the Viewport to set up in the biome editor world")
         .add()
         .<Boolean>append(new KeyedCodec<>("EnablePersistence", Codec.BOOLEAN), (self, value) -> self.enablePersistence = value, self -> self.enablePersistence)
         .documentation("Whether the BiomdEditor world should be stored on disk after exiting it")
         .add()
         .afterDecode(defaults -> {
            defaults.biomeAssets = resolveAssetTags(BiomeAsset.class, defaults.assetTemplateTags);
            defaults.worldStructureAssets = resolveAssetTags(WorldStructureAsset.class, defaults.assetTemplateTags);
            defaults.viewportBounds = new Bounds3i(defaults.viewportMin, defaults.viewportMax);
         })
         .build();
      public static final String DEFAULT_PACK = "Biomes";
      public static final String DEFAULT_GROUP = "User";
      public static final String DEFAULT_BIOME_TEMPLATE = "Basic";
      public static final String DEFAULT_WORLD_STRUCTURE_TEMPLATE = "Basic";
      public static final String[] DEFAULT_ASSET_TEMPLATE_TAGS = new String[]{"Template"};
      public static final Vector3i VIEWPORT_MIN = new Vector3i(-128, 0, -128);
      public static final Vector3i VIEWPORT_MAX = new Vector3i(128, 0, 128);
      public static final Bounds3i DEFAULT_VIEWPORT_BOUNDS = new Bounds3i(VIEWPORT_MIN, VIEWPORT_MAX);
      public static final boolean DEFAULT_ENABLE_PERSISTENCE = false;
      @Nonnull
      public String pack = "Biomes";
      @Nonnull
      public String group = "User";
      @Nullable
      public String biome = "Basic";
      @Nullable
      public String worldStructure = "Basic";
      @Nonnull
      public String[] assetTemplateTags = DEFAULT_ASSET_TEMPLATE_TAGS;
      @Nonnull
      public Vector3i viewportMin = VIEWPORT_MIN;
      @Nonnull
      public Vector3i viewportMax = VIEWPORT_MAX;
      public boolean enablePersistence = false;
      @Nonnull
      public transient Bounds3i viewportBounds = new Bounds3i();
      @Nonnull
      public transient String[] biomeAssets = ArrayUtil.EMPTY_STRING_ARRAY;
      @Nonnull
      public transient String[] worldStructureAssets = ArrayUtil.EMPTY_STRING_ARRAY;

      protected static <T extends JsonAssetWithMap<String, M>, M extends AssetMap<String, T>> String[] resolveAssetTags(
         @Nonnull Class<T> type, @Nonnull String[] tags
      ) {
         AssetStore<String, T, M> store = AssetRegistry.getAssetStore(type);
         if (store == null) {
            return ArrayUtil.EMPTY_STRING_ARRAY;
         } else {
            M assetMap = store.getAssetMap();
            Set<String> uniqueIds = (Set<String>)(tags.length == 0 ? assetMap.getAssetMap().keySet() : new ObjectOpenHashSet());

            for (String tag : tags) {
               int tagIndex = AssetRegistry.getTagIndex(tag);
               Set<String> matchingKeys = assetMap.getKeysForTag(tagIndex);
               uniqueIds.addAll(matchingKeys);
            }

            String[] ids = uniqueIds.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
            Arrays.sort((Object[])ids);
            return ids;
         }
      }
   }
}
