package com.hypixel.hytale.server.core.asset;

import com.hypixel.hytale.assetstore.AssetLoadResult;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.iterator.AssetStoreIterator;
import com.hypixel.hytale.assetstore.iterator.CircularDependencyException;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.AmbienceFXPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import com.hypixel.hytale.server.core.asset.type.audiocategory.AudioCategoryPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.audiocategory.config.AudioCategory;
import com.hypixel.hytale.server.core.asset.type.blockbreakingdecal.BlockBreakingDecalPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blockbreakingdecal.config.BlockBreakingDecal;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxesPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blockparticle.BlockParticleSetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blockparticle.config.BlockParticleSet;
import com.hypixel.hytale.server.core.asset.type.blockset.BlockSetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.core.asset.type.blocksound.BlockSoundSetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.BlockGroupPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blocktype.BlockTypePacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockMigration;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.PrefabListAsset;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.EntityEffectPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.environment.EnvironmentPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.equalizereffect.EqualizerEffectPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.equalizereffect.config.EqualizerEffect;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.fluid.FluidTypePacketGenerator;
import com.hypixel.hytale.server.core.asset.type.fluidfx.FluidFXPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.fluidfx.config.FluidFX;
import com.hypixel.hytale.server.core.asset.type.gamemode.GameModeType;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.item.FieldcraftCategoryPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.ItemCategoryPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.ResourceTypePacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.BlockGroup;
import com.hypixel.hytale.server.core.asset.type.item.config.BuilderToolItemReferenceAsset;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.FieldcraftCategory;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemCategory;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemReticleConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.asset.type.item.config.ResourceType;
import com.hypixel.hytale.server.core.asset.type.itemanimation.ItemPlayerAnimationsPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import com.hypixel.hytale.server.core.asset.type.itemsound.ItemSoundSetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.itemsound.config.ItemSoundSet;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.modelvfx.ModelVFXPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.modelvfx.config.ModelVFX;
import com.hypixel.hytale.server.core.asset.type.particle.ParticleSpawnerPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.particle.ParticleSystemPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSpawner;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.portalworld.PortalType;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.asset.type.responsecurve.config.ExponentialResponseCurve;
import com.hypixel.hytale.server.core.asset.type.responsecurve.config.ResponseCurve;
import com.hypixel.hytale.server.core.asset.type.reverbeffect.ReverbEffectPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.reverbeffect.config.ReverbEffect;
import com.hypixel.hytale.server.core.asset.type.soundevent.SoundEventPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundset.SoundSetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.soundset.config.SoundSet;
import com.hypixel.hytale.server.core.asset.type.tagpattern.TagPatternPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.AndPatternOp;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.EqualsTagOp;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.NotPatternOp;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.OrPatternOp;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.TagPattern;
import com.hypixel.hytale.server.core.asset.type.trail.TrailPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.trail.config.Trail;
import com.hypixel.hytale.server.core.asset.type.weather.WeatherPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.asset.type.wordlist.WordList;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionConfig;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.UnarmedInteractions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.item.CraftingRecipePacketGenerator;
import com.hypixel.hytale.server.core.modules.item.ItemPacketGenerator;
import com.hypixel.hytale.server.core.modules.item.ItemQualityPacketGenerator;
import com.hypixel.hytale.server.core.modules.item.ItemReticleConfigPacketGenerator;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfigPacketGenerator;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.CustomConnectedBlockTemplateAsset;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

public class AssetRegistryLoader {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static void init() {
   }

   public static void preLoadAssets(@Nonnull LoadAssetEvent event) {
      try {
         preLoadAssets0(event);
      } catch (Throwable var2) {
         event.failed(true, "failed to validate assets");
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static void loadAssets(@Nullable LoadAssetEvent event, @Nonnull AssetPack assetPack) {
      AssetRegistry.ASSET_LOCK.writeLock().lock();

      try {
         loadAssets0(event, assetPack);
         AssetRegistry.HAS_INIT = true;
      } catch (Throwable var6) {
         if (event != null) {
            event.failed(true, "failed to validate assets");
         }

         throw SneakyThrow.sneakyThrow(var6);
      } finally {
         AssetRegistry.ASSET_LOCK.writeLock().unlock();
      }
   }

   private static void preLoadAssets0(@Nonnull LoadAssetEvent event) {
      AssetStore.DISABLE_DYNAMIC_DEPENDENCIES = true;
      Collection<AssetStore<?, ?, ?>> values = AssetRegistry.getStoreMap().values();
      LOGGER.at(Level.INFO).log("Loading %s asset stores...", values.size());

      for (AssetStore<?, ?, ?> assetStore : values) {
         assetStore.simplifyLoadBeforeDependencies();
      }

      boolean failedToLoadAsset = false;
      LOGGER.at(Level.INFO).log("Pre-adding assets...");
      AssetStoreIterator iterator = new AssetStoreIterator(values);

      label71: {
         try {
            while (iterator.hasNext()) {
               if (HytaleServer.get().isShuttingDown()) {
                  LOGGER.at(Level.INFO).log("Aborted asset loading due to server shutdown!");
                  break label71;
               }

               AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>> assetStore = (AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>>)iterator.next();
               if (assetStore == null) {
                  throw new CircularDependencyException(values, iterator);
               }

               long start = System.nanoTime();
               Class<? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>> assetClass = assetStore.getAssetClass();

               try {
                  List<?> preAddedAssets = assetStore.getPreAddedAssets();
                  if (preAddedAssets != null && !preAddedAssets.isEmpty()) {
                     AssetLoadResult loadResult = assetStore.loadAssets("Hytale:Hytale", preAddedAssets);
                     failedToLoadAsset |= loadResult.hasFailed();
                  }
               } catch (Exception var14) {
                  failedToLoadAsset = true;
                  long end = System.nanoTime();
                  long diff = end - start;
                  if (iterator.isBeingWaitedFor(assetStore)) {
                     throw new RuntimeException(
                        String.format("Failed to pre-add %s took %s", assetClass.getSimpleName(), FormatUtil.nanosToString(diff)), var14
                     );
                  }

                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var14))
                     .log("Failed to pre-add %s took %s", assetClass.getSimpleName(), FormatUtil.nanosToString(diff));
               }
            }
         } catch (Throwable var15) {
            try {
               iterator.close();
            } catch (Throwable var13) {
               var15.addSuppressed(var13);
            }

            throw var15;
         }

         iterator.close();
         if (failedToLoadAsset) {
            event.failed(Options.getOptionSet().has(Options.VALIDATE_ASSETS), "failed to validate internal assets");
         }

         return;
      }

      iterator.close();
   }

   private static void loadAssets0(@Nullable LoadAssetEvent event, @Nonnull AssetPack assetPack) {
      AssetStore.DISABLE_DYNAMIC_DEPENDENCIES = true;
      Path serverAssetDirectory = assetPack.getRoot().resolve("Server");
      HytaleLogger.getLogger().at(Level.INFO).log("Loading assets from: %s", serverAssetDirectory);
      long startAll = System.nanoTime();
      boolean shouldFail = Options.getOptionSet().has(Options.VALIDATE_ASSETS) || !Constants.shouldSkipModValidation();
      boolean failedToLoadAsset = false;
      LOGGER.at(Level.INFO).log("Loading assets from %s", serverAssetDirectory);
      Collection<AssetStore<?, ?, ?>> values = AssetRegistry.getStoreMap().values();

      try (AssetStoreIterator iterator = new AssetStoreIterator(values)) {
         while (iterator.hasNext()) {
            if (HytaleServer.get().isShuttingDown()) {
               LOGGER.at(Level.INFO).log("Aborted asset loading due to server shutdown!");
               return;
            }

            AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>> assetStore = (AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>>)iterator.next();
            if (assetStore == null) {
               throw new CircularDependencyException(values, iterator);
            }

            long start = System.nanoTime();
            Class<? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>> assetClass = assetStore.getAssetClass();

            try {
               String path = assetStore.getPath();
               if (path != null) {
                  Path assetsPath = serverAssetDirectory.resolve(path);
                  if (Files.isDirectory(assetsPath)) {
                     AssetLoadResult<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>> loadResult = assetStore.loadAssetsFromDirectory(
                        assetPack.getName(), assetsPath
                     );
                     failedToLoadAsset |= loadResult.hasFailed();
                  }
               }
            } catch (Exception var19) {
               failedToLoadAsset = true;
               if (event != null) {
                  event.failed(shouldFail, "Asset pack " + assetPack.getName() + " failed to load " + assetClass.getSimpleName() + " - " + var19.getMessage());
               }

               long end = System.nanoTime();
               long diff = end - start;
               if (iterator.isBeingWaitedFor(assetStore)) {
                  throw new RuntimeException(
                     String.format("Failed to load %s from path '%s' took %s", assetClass.getSimpleName(), assetStore.getPath(), FormatUtil.nanosToString(diff)),
                     var19
                  );
               }

               ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var19))
                  .log("Failed to load %s from path '%s' took %s", assetClass.getSimpleName(), assetStore.getPath(), FormatUtil.nanosToString(diff));
            }
         }
      }

      for (AssetStore<?, ?, ?> assetStore : values) {
         if (assetPack.getName().equals("Hytale:Hytale")) {
            assetStore.validateCodecDefaults();
         }

         String path = assetStore.getPath();
         if (path != null) {
            Path assetsPath = serverAssetDirectory.resolve(path);
            if (Files.isDirectory(assetsPath) && !assetPack.isImmutable()) {
               assetStore.addFileMonitor(assetPack.getName(), assetsPath);
            }
         }
      }

      long endAll = System.nanoTime();
      long diffAll = endAll - startAll;
      LOGGER.at(Level.INFO).log("Took %s to load all assets", FormatUtil.nanosToString(diffAll));
      if (failedToLoadAsset && event != null) {
         if ("Hytale:Hytale".equals(assetPack.getName())) {
            event.failed(shouldFail, "Assets " + assetPack.getName() + " failed to load.");
         } else {
            event.failed(shouldFail, "Mod " + assetPack.getName() + " failed to load. Check for mod updates or contact the mod author.");
         }
      }
   }

   public static void sendAssets(@Nonnull PacketHandler packetHandler) {
      Consumer<ToClientPacket[]> packetConsumer = packetHandler::write;
      Consumer<ToClientPacket> singlePacketConsumer = packetHandler::write;
      HytaleAssetStore.SETUP_PACKET_CONSUMERS.add(singlePacketConsumer);

      try {
         for (AssetStore<?, ?, ?> assetStore : AssetRegistry.getStoreMap().values()) {
            ((HytaleAssetStore)assetStore).sendAssets(packetConsumer);
         }
      } finally {
         HytaleAssetStore.SETUP_PACKET_CONSUMERS.remove(singlePacketConsumer);
      }
   }

   @Nonnull
   public static Map<String, Schema> generateSchemas(@Nonnull SchemaContext context, @Nonnull BsonDocument vsCodeConfig) {
      AssetStore[] values = AssetRegistry.getStoreMap().values().toArray(AssetStore[]::new);
      Arrays.sort(values, Comparator.comparing(storex -> storex.getAssetClass().getSimpleName()));
      BsonArray vsCodeSchemas = new BsonArray();
      BsonDocument vsCodeFiles = new BsonDocument();
      vsCodeConfig.put("json.schemas", vsCodeSchemas);
      vsCodeConfig.put("files.associations", vsCodeFiles);
      vsCodeConfig.put("editor.tabSize", new BsonInt32(2));

      for (AssetStore store : values) {
         Class assetClass = store.getAssetClass();
         String name = assetClass.getSimpleName();
         AssetCodec codec = store.getCodec();
         context.addFileReference(name + ".json", codec);
      }

      HashMap<String, Schema> schemas = new HashMap<>();

      for (AssetStore store : values) {
         Class assetClass = store.getAssetClass();
         String path = store.getPath();
         String name = assetClass.getSimpleName();
         AssetCodec codec = store.getCodec();
         Schema schema = codec.toSchema(context);
         if (codec instanceof AssetCodecMapCodec) {
            schema.setTitle(name);
         }

         schema.setId(name + ".json");
         Schema.HytaleMetadata hytale = schema.getHytale();
         hytale.setPath(path);
         hytale.setExtension(store.getExtension());
         Class idProvider = store.getIdProvider();
         if (idProvider != null) {
            hytale.setIdProvider(idProvider.getSimpleName());
         }

         List preload = store.getPreAddedAssets();
         if (preload != null && !preload.isEmpty()) {
            String[] internal = new String[preload.size()];

            for (int i = 0; i < preload.size(); i++) {
               Object p = preload.get(i);
               Object k = store.getKeyFunction().apply(p);
               internal[i] = k.toString();
            }

            hytale.setInternalKeys(internal);
         }

         BsonDocument config = new BsonDocument();
         config.put(
            "fileMatch",
            new BsonArray(
               List.of(new BsonString("/Server/" + path + "/*" + store.getExtension()), new BsonString("/Server/" + path + "/**/*" + store.getExtension()))
            )
         );
         config.put("url", new BsonString("./Schema/" + name + ".json"));
         vsCodeSchemas.add(config);
         if (!store.getExtension().equals(".json")) {
            vsCodeFiles.put("*" + store.getExtension(), new BsonString("json"));
         }

         schemas.put(name + ".json", schema);
      }

      HytaleServer.get()
         .getEventBus()
         .<Void, GenerateSchemaEvent>dispatchFor(GenerateSchemaEvent.class)
         .dispatch(new GenerateSchemaEvent(schemas, context, vsCodeConfig));
      Schema definitions = new Schema();
      definitions.setDefinitions(context.getDefinitions());
      definitions.setId("common.json");
      schemas.put("common.json", definitions);
      Schema otherDefinitions = new Schema();
      otherDefinitions.setDefinitions(context.getOtherDefinitions());
      otherDefinitions.setId("other.json");
      schemas.put("other.json", otherDefinitions);
      return schemas;
   }

   public static void writeSchemas(LoadAssetEvent event) {
      if (Options.getOptionSet().has(Options.GENERATE_SCHEMA)) {
         try {
            AssetPack pack = AssetModule.get().getBaseAssetPack();
            if (pack.isImmutable()) {
               LOGGER.at(Level.SEVERE).log("Not generating schema due launcher assets");
               HytaleServer.get().shutdownServer(ShutdownReason.VALIDATE_ERROR.withMessage("Not generating scheme due launcher assets"));
               return;
            }

            BsonDocument vsCodeConfig = new BsonDocument();
            Path assetDirectory = pack.getRoot();
            Path schemaDir = assetDirectory.resolve("Schema");
            Files.createDirectories(schemaDir);

            try (Stream<Path> stream = Files.walk(schemaDir, 1)) {
               stream.filter(v -> v.toString().endsWith(".json")).forEach(SneakyThrow.sneakyConsumer(Files::delete));
            }

            SchemaContext context = new SchemaContext();
            Map<String, Schema> schemas = generateSchemas(context, vsCodeConfig);

            for (Entry<String, Schema> schema : schemas.entrySet()) {
               BsonUtil.writeDocument(schemaDir.resolve(schema.getKey()), Schema.CODEC.encode(schema.getValue(), EmptyExtraInfo.EMPTY).asDocument(), false)
                  .join();
            }

            Files.createDirectories(assetDirectory.resolve(".vscode"));
            BsonUtil.writeDocument(assetDirectory.resolve(".vscode/settings.json"), vsCodeConfig, false).join();
         } catch (Throwable var11) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var11)).log("Schema generation failed");
            HytaleServer.get().shutdownServer(ShutdownReason.CRASH.withMessage("Schema generation failed"));
            return;
         }

         HytaleServer.get().shutdownServer(ShutdownReason.SHUTDOWN.withMessage("Schema generated"));
      }
   }

   static {
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 AmbienceFX.class, new IndexedAssetMap()
                              )
                              .setPath("Audio/AmbienceFX"))
                           .setCodec(AmbienceFX.CODEC))
                        .setKeyFunction(AmbienceFX::getId))
                     .setReplaceOnRemove(AmbienceFX::new))
                  .setPacketGenerator(new AmbienceFXPacketGenerator())
                  .loadsAfter(
                     Weather.class,
                     Environment.class,
                     FluidFX.class,
                     SoundEvent.class,
                     BlockSoundSet.class,
                     TagPattern.class,
                     AudioCategory.class,
                     ReverbEffect.class,
                     EqualizerEffect.class
                  ))
               .preLoadAssets(Collections.singletonList(AmbienceFX.EMPTY)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              BlockBoundingBoxes.class, new IndexedLookupTableAssetMap<>(BlockBoundingBoxes[]::new)
                           )
                           .setPath("Item/Block/Hitboxes"))
                        .setCodec(BlockBoundingBoxes.CODEC))
                     .setKeyFunction(BlockBoundingBoxes::getId))
                  .setReplaceOnRemove(BlockBoundingBoxes::getUnitBoxFor))
               .setPacketGenerator(new BlockBoundingBoxesPacketGenerator())
               .preLoadAssets(Collections.singletonList(BlockBoundingBoxes.UNIT_BOX)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              BlockSet.class, new IndexedLookupTableAssetMap<>(BlockSet[]::new)
                           )
                           .setPath("Item/Block/Sets"))
                        .setCodec(BlockSet.CODEC))
                     .setKeyFunction(BlockSet::getId))
                  .setReplaceOnRemove(BlockSet::new))
               .setPacketGenerator(new BlockSetPacketGenerator())
               .loadsBefore(Item.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 BlockSoundSet.class, new IndexedLookupTableAssetMap<>(BlockSoundSet[]::new)
                              )
                              .setPath("Item/Block/Sounds"))
                           .setCodec(BlockSoundSet.CODEC))
                        .setKeyFunction(BlockSoundSet::getId))
                     .setReplaceOnRemove(BlockSoundSet::new))
                  .setPacketGenerator(new BlockSoundSetPacketGenerator())
                  .loadsAfter(SoundEvent.class))
               .preLoadAssets(Collections.singletonList(BlockSoundSet.EMPTY_BLOCK_SOUND_SET)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              ItemSoundSet.class, new IndexedLookupTableAssetMap<>(ItemSoundSet[]::new)
                           )
                           .setPath("Audio/ItemSounds"))
                        .setCodec(ItemSoundSet.CODEC))
                     .setKeyFunction(ItemSoundSet::getId))
                  .setReplaceOnRemove(ItemSoundSet::new))
               .setPacketGenerator(new ItemSoundSetPacketGenerator())
               .loadsAfter(SoundEvent.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           BlockParticleSet.class, new DefaultAssetMap()
                        )
                        .setPath("Item/Block/Particles"))
                     .setCodec(BlockParticleSet.CODEC))
                  .setKeyFunction(BlockParticleSet::getId))
               .setPacketGenerator(new BlockParticleSetPacketGenerator())
               .loadsAfter(ParticleSystem.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        BlockBreakingDecal.class, new DefaultAssetMap()
                     )
                     .setPath("Item/Block/BreakingDecals"))
                  .setCodec(BlockBreakingDecal.CODEC))
               .setKeyFunction(BlockBreakingDecal::getId))
            .setPacketGenerator(new BlockBreakingDecalPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        Integer.class, BlockMigration.class, new DefaultAssetMap()
                     )
                     .setPath("Item/Block/Migrations"))
                  .setCodec(BlockMigration.CODEC))
               .setKeyFunction(BlockMigration::getId))
            .build()
      );
      BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = new BlockTypeAssetMap<>(BlockType[]::new, BlockType::getGroup);
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                    BlockType.class, blockTypeAssetMap
                                 )
                                 .setPath("Item/Block/Blocks"))
                              .setCodec(BlockType.CODEC))
                           .setKeyFunction(BlockType::getId))
                        .setPacketGenerator(new BlockTypePacketGenerator())
                        .loadsAfter(
                           BlockBoundingBoxes.class,
                           BlockSoundSet.class,
                           SoundEvent.class,
                           BlockParticleSet.class,
                           BlockBreakingDecal.class,
                           CustomConnectedBlockTemplateAsset.class,
                           PrefabListAsset.class,
                           BlockTypeListAsset.class
                        ))
                     .setNotificationItemFunction(item -> new ItemStack(item, 1).toPacket())
                     .setReplaceOnRemove(BlockType::getUnknownFor))
                  .preLoadAssets(Arrays.asList(BlockType.EMPTY, BlockType.UNKNOWN, BlockType.DEBUG_CUBE, BlockType.DEBUG_MODEL)))
               .setIdProvider(Item.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 Fluid.class, new IndexedLookupTableAssetMap<>(Fluid[]::new)
                              )
                              .setPath("Item/Block/Fluids"))
                           .setCodec(Fluid.CODEC))
                        .setKeyFunction(Fluid::getId))
                     .setReplaceOnRemove(Fluid::getUnknownFor))
                  .setPacketGenerator(new FluidTypePacketGenerator())
                  .loadsAfter(FluidFX.class, BlockSoundSet.class, BlockParticleSet.class, SoundEvent.class))
               .preLoadAssets(List.of(Fluid.EMPTY, Fluid.UNKNOWN)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        ItemPlayerAnimations.class, new DefaultAssetMap()
                     )
                     .setPath("Item/Animations"))
                  .setCodec(ItemPlayerAnimations.CODEC))
               .setKeyFunction(ItemPlayerAnimations::getId))
            .setPacketGenerator(new ItemPlayerAnimationsPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 Environment.class, new IndexedLookupTableAssetMap<>(Environment[]::new)
                              )
                              .setPath("Environments"))
                           .setCodec(Environment.CODEC))
                        .setKeyFunction(Environment::getId))
                     .setReplaceOnRemove(Environment::getUnknownFor))
                  .setPacketGenerator(new EnvironmentPacketGenerator())
                  .loadsAfter(Weather.class, FluidFX.class, ParticleSystem.class))
               .preLoadAssets(Collections.singletonList(Environment.UNKNOWN)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 FluidFX.class, new IndexedLookupTableAssetMap<>(FluidFX[]::new)
                              )
                              .setPath("Item/Block/FluidFX"))
                           .setCodec(FluidFX.CODEC))
                        .setKeyFunction(FluidFX::getId))
                     .setReplaceOnRemove(FluidFX::getUnknownFor))
                  .setPacketGenerator(new FluidFXPacketGenerator())
                  .loadsAfter(ParticleSystem.class))
               .preLoadAssets(Collections.singletonList(FluidFX.EMPTY_FLUID_FX)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        ItemCategory.class, new DefaultAssetMap(Collections.synchronizedMap(new Object2ObjectLinkedOpenHashMap()))
                     )
                     .setPath("Item/Category/CreativeLibrary"))
                  .setCodec(ItemCategory.CODEC))
               .setKeyFunction(ItemCategory::getId))
            .setPacketGenerator(new ItemCategoryPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        FieldcraftCategory.class, new DefaultAssetMap(Collections.synchronizedMap(new Object2ObjectLinkedOpenHashMap()))
                     )
                     .setPath("Item/Category/Fieldcraft"))
                  .setCodec(FieldcraftCategory.CODEC))
               .setKeyFunction(FieldcraftCategory::getId))
            .setPacketGenerator(new FieldcraftCategoryPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(ItemDropList.class, new DefaultAssetMap())
                     .setPath("Drops"))
                  .setCodec(ItemDropList.CODEC))
               .setKeyFunction(ItemDropList::getId))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(WordList.class, new DefaultAssetMap())
                     .setPath("WordLists"))
                  .setCodec(WordList.CODEC))
               .setKeyFunction(WordList::getId))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              ItemReticleConfig.class, new IndexedLookupTableAssetMap<>(ItemReticleConfig[]::new)
                           )
                           .setPath("Item/Reticles"))
                        .setCodec(ItemReticleConfig.CODEC))
                     .setKeyFunction(ItemReticleConfig::getId))
                  .setReplaceOnRemove(ItemReticleConfig::new))
               .setPacketGenerator(new ItemReticleConfigPacketGenerator())
               .preLoadAssets(Collections.singletonList(ItemReticleConfig.DEFAULT)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           ItemToolSpec.class, new DefaultAssetMap()
                        )
                        .setPath("Item/Unarmed/Gathering"))
                     .setCodec(ItemToolSpec.CODEC))
                  .setKeyFunction(ItemToolSpec::getGatherType))
               .loadsAfter(SoundEvent.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(PortalType.class, new DefaultAssetMap())
                     .setPath("PortalTypes"))
                  .setCodec(PortalType.CODEC))
               .setKeyFunction(PortalType::getId))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              Item.class, new DefaultAssetMap()
                           )
                           .setPath("Item/Items"))
                        .setCodec(Item.CODEC))
                     .setKeyFunction(Item::getId))
                  .setPacketGenerator(new ItemPacketGenerator())
                  .loadsAfter(
                     ItemCategory.class,
                     ItemPlayerAnimations.class,
                     UnarmedInteractions.class,
                     ResourceType.class,
                     BlockType.class,
                     EntityEffect.class,
                     ItemQuality.class,
                     ItemReticleConfig.class,
                     SoundEvent.class,
                     PortalType.class,
                     ItemSoundSet.class
                  ))
               .setNotificationItemFunction(item -> new ItemStack(item, 1).toPacket())
               .preLoadAssets(Collections.singletonList(Item.UNKNOWN)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           CraftingRecipe.class, new DefaultAssetMap()
                        )
                        .setPath("Item/Recipes"))
                     .setCodec(CraftingRecipe.CODEC))
                  .setKeyFunction(CraftingRecipe::getId))
               .setPacketGenerator(new CraftingRecipePacketGenerator())
               .loadsAfter(Item.class, BlockType.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              ModelAsset.class, new DefaultAssetMap()
                           )
                           .setPath("Models"))
                        .setCodec(ModelAsset.CODEC))
                     .setKeyFunction(ModelAsset::getId))
                  .loadsAfter(ParticleSystem.class, SoundEvent.class, Trail.class))
               .preLoadAssets(List.of(ModelAsset.DEBUG)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           ParticleSpawner.class, new DefaultAssetMap()
                        )
                        .setPath("Particles"))
                     .setExtension(".particlespawner"))
                  .setCodec(ParticleSpawner.CODEC))
               .setKeyFunction(ParticleSpawner::getId))
            .setPacketGenerator(new ParticleSpawnerPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              ParticleSystem.class, new DefaultAssetMap()
                           )
                           .setPath("Particles"))
                        .setExtension(".particlesystem"))
                     .setCodec(ParticleSystem.CODEC))
                  .setKeyFunction(ParticleSystem::getId))
               .setPacketGenerator(new ParticleSystemPacketGenerator())
               .loadsAfter(ParticleSpawner.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(Trail.class, new DefaultAssetMap())
                     .setPath("Entity/Trails"))
                  .setCodec(Trail.CODEC))
               .setKeyFunction(Trail::getId))
            .setPacketGenerator(new TrailPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           Projectile.class, new DefaultAssetMap()
                        )
                        .setPath("Projectiles"))
                     .setCodec(Projectile.CODEC))
                  .setKeyFunction(Projectile::getId))
               .loadsAfter(SoundEvent.class, ModelAsset.class, ParticleSystem.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              EntityEffect.class, new IndexedLookupTableAssetMap<>(EntityEffect[]::new)
                           )
                           .setPath("Entity/Effects"))
                        .setCodec(EntityEffect.CODEC))
                     .setKeyFunction(EntityEffect::getId))
                  .setReplaceOnRemove(EntityEffect::new))
               .setPacketGenerator(new EntityEffectPacketGenerator())
               .loadsAfter(
                  ModelAsset.class, ParticleSystem.class, EntityStatType.class, ModelVFX.class, DamageCause.class, CameraEffect.class, SoundEvent.class
               ))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           ModelVFX.class, new IndexedLookupTableAssetMap<>(ModelVFX[]::new)
                        )
                        .setPath("Entity/ModelVFX"))
                     .setCodec(ModelVFX.CODEC))
                  .setKeyFunction(ModelVFX::getId))
               .setReplaceOnRemove(ModelVFX::new))
            .setPacketGenerator(new ModelVFXPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           GameModeType.class, new DefaultAssetMap()
                        )
                        .setPath("Entity/GameMode"))
                     .setCodec(GameModeType.CODEC))
                  .setKeyFunction(GameModeType::getId))
               .loadsAfter(Interaction.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(ResourceType.class, new DefaultAssetMap())
                     .setPath("Item/ResourceTypes"))
                  .setCodec(ResourceType.CODEC))
               .setKeyFunction(ResourceType::getId))
            .setPacketGenerator(new ResourceTypePacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 Weather.class, new IndexedLookupTableAssetMap<>(Weather[]::new)
                              )
                              .setPath("Weathers"))
                           .setCodec(Weather.CODEC))
                        .setKeyFunction(Weather::getId))
                     .setReplaceOnRemove(Weather::new))
                  .setPacketGenerator(new WeatherPacketGenerator())
                  .loadsAfter(ParticleSystem.class))
               .preLoadAssets(Collections.singletonList(Weather.UNKNOWN)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           GameplayConfig.class, new DefaultAssetMap()
                        )
                        .setPath("GameplayConfigs"))
                     .setCodec(GameplayConfig.CODEC))
                  .setKeyFunction(GameplayConfig::getId))
               .loadsAfter(
                  Item.class,
                  SoundEvent.class,
                  SoundSet.class,
                  BlockType.class,
                  EntityEffect.class,
                  HitboxCollisionConfig.class,
                  DamageCause.class,
                  RepulsionConfig.class,
                  ParticleSystem.class,
                  AmbienceFX.class
               ))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 SoundEvent.class, new IndexedLookupTableAssetMap<>(SoundEvent[]::new)
                              )
                              .setPath("Audio/SoundEvents"))
                           .setCodec(SoundEvent.CODEC))
                        .setKeyFunction(SoundEvent::getId))
                     .setReplaceOnRemove(SoundEvent::new))
                  .setPacketGenerator(new SoundEventPacketGenerator())
                  .preLoadAssets(Collections.singletonList(SoundEvent.EMPTY_SOUND_EVENT)))
               .loadsAfter(AudioCategory.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 SoundSet.class, new IndexedLookupTableAssetMap<>(SoundSet[]::new)
                              )
                              .setPath("Audio/SoundSets"))
                           .setCodec(SoundSet.CODEC))
                        .setKeyFunction(SoundSet::getId))
                     .setReplaceOnRemove(SoundSet::new))
                  .setPacketGenerator(new SoundSetPacketGenerator())
                  .loadsAfter(SoundEvent.class))
               .preLoadAssets(Collections.singletonList(SoundSet.EMPTY_SOUND_SET)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              AudioCategory.class, new IndexedLookupTableAssetMap<>(AudioCategory[]::new)
                           )
                           .setPath("Audio/AudioCategories"))
                        .setCodec(AudioCategory.CODEC))
                     .setKeyFunction(AudioCategory::getId))
                  .setReplaceOnRemove(AudioCategory::new))
               .setPacketGenerator(new AudioCategoryPacketGenerator())
               .preLoadAssets(Collections.singletonList(AudioCategory.EMPTY_AUDIO_CATEGORY)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              ReverbEffect.class, new IndexedLookupTableAssetMap<>(ReverbEffect[]::new)
                           )
                           .setPath("Audio/Reverb"))
                        .setCodec(ReverbEffect.CODEC))
                     .setKeyFunction(ReverbEffect::getId))
                  .setReplaceOnRemove(ReverbEffect::new))
               .setPacketGenerator(new ReverbEffectPacketGenerator())
               .preLoadAssets(Collections.singletonList(ReverbEffect.EMPTY_REVERB_EFFECT)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              EqualizerEffect.class, new IndexedLookupTableAssetMap<>(EqualizerEffect[]::new)
                           )
                           .setPath("Audio/EQ"))
                        .setCodec(EqualizerEffect.CODEC))
                     .setKeyFunction(EqualizerEffect::getId))
                  .setReplaceOnRemove(EqualizerEffect::new))
               .setPacketGenerator(new EqualizerEffectPacketGenerator())
               .preLoadAssets(Collections.singletonList(EqualizerEffect.EMPTY_EQUALIZER_EFFECT)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           ResponseCurve.class, new IndexedLookupTableAssetMap<>(ResponseCurve[]::new)
                        )
                        .setPath("ResponseCurves"))
                     .setCodec(ResponseCurve.CODEC))
                  .setKeyFunction(ResponseCurve::getId))
               .setReplaceOnRemove(ExponentialResponseCurve::new))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 ItemQuality.class, new IndexedLookupTableAssetMap<>(ItemQuality[]::new)
                              )
                              .setPath("Item/Qualities"))
                           .setCodec(ItemQuality.CODEC))
                        .setKeyFunction(ItemQuality::getId))
                     .setPacketGenerator(new ItemQualityPacketGenerator())
                     .setReplaceOnRemove(ItemQuality::new))
                  .loadsAfter(ParticleSystem.class))
               .preLoadAssets(Collections.singletonList(ItemQuality.DEFAULT_ITEM_QUALITY)))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              DamageCause.class, new IndexedLookupTableAssetMap<>(DamageCause[]::new)
                           )
                           .setPath("Entity/Damage"))
                        .setCodec(DamageCause.CODEC))
                     .setKeyFunction(DamageCause::getId))
                  .setReplaceOnRemove(DamageCause::new))
               .loadsBefore(Item.class, Interaction.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           ProjectileConfig.class, new DefaultAssetMap()
                        )
                        .setPath("ProjectileConfigs"))
                     .setCodec(ProjectileConfig.CODEC))
                  .setKeyFunction(ProjectileConfig::getId))
               .loadsAfter(Interaction.class, SoundEvent.class, ModelAsset.class, ParticleSystem.class))
            .setPacketGenerator(new ProjectileConfigPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           BlockGroup.class, new DefaultAssetMap()
                        )
                        .setPath("Item/Groups"))
                     .setCodec(BlockGroup.CODEC))
                  .setKeyFunction(BlockGroup::getId))
               .loadsAfter(BlockType.class, Item.class))
            .setPacketGenerator(new BlockGroupPacketGenerator())
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           BuilderToolItemReferenceAsset.class, new DefaultAssetMap()
                        )
                        .setPath("Item/PlayerToolsMenuConfig"))
                     .setCodec(BuilderToolItemReferenceAsset.CODEC))
                  .setKeyFunction(BuilderToolItemReferenceAsset::getId))
               .loadsAfter(BlockType.class, Item.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        BlockTypeListAsset.class, new DefaultAssetMap()
                     )
                     .setPath("BlockTypeList"))
                  .setKeyFunction(BlockTypeListAsset::getId))
               .setCodec(BlockTypeListAsset.CODEC))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(PrefabListAsset.class, new DefaultAssetMap())
                     .setPath("PrefabList"))
                  .setKeyFunction(PrefabListAsset::getId))
               .setCodec(PrefabListAsset.CODEC))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              CameraEffect.class, new IndexedLookupTableAssetMap<>(CameraEffect[]::new)
                           )
                           .loadsBefore(GameplayConfig.class, Interaction.class))
                        .setPath("Camera/CameraEffect"))
                     .setCodec(CameraEffect.CODEC))
                  .setKeyFunction(CameraEffect::getId))
               .setReplaceOnRemove(CameraEffect.MissingCameraEffect::new))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           TagPattern.class, new IndexedLookupTableAssetMap<>(TagPattern[]::new)
                        )
                        .setPath("TagPatterns"))
                     .setCodec(TagPattern.CODEC))
                  .setKeyFunction(TagPattern::getId))
               .setReplaceOnRemove(EqualsTagOp::new))
            .setPacketGenerator(new TagPatternPacketGenerator())
            .build()
      );
      TagPattern.CODEC.register("Equals", EqualsTagOp.class, EqualsTagOp.CODEC);
      TagPattern.CODEC.register("And", AndPatternOp.class, AndPatternOp.CODEC);
      TagPattern.CODEC.register("Or", OrPatternOp.class, OrPatternOp.CODEC);
      TagPattern.CODEC.register("Not", NotPatternOp.class, NotPatternOp.CODEC);
   }
}
