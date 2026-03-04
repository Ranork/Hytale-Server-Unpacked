package com.hypixel.hytale.builtin.instances;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.builtin.blockphysics.WorldValidationUtil;
import com.hypixel.hytale.builtin.instances.blocks.ConfigurableInstanceBlock;
import com.hypixel.hytale.builtin.instances.blocks.InstanceBlock;
import com.hypixel.hytale.builtin.instances.command.InstancesCommand;
import com.hypixel.hytale.builtin.instances.config.ExitInstance;
import com.hypixel.hytale.builtin.instances.config.InstanceDiscoveryConfig;
import com.hypixel.hytale.builtin.instances.config.InstanceEntityConfig;
import com.hypixel.hytale.builtin.instances.config.InstanceWorldConfig;
import com.hypixel.hytale.builtin.instances.config.WorldReturnPoint;
import com.hypixel.hytale.builtin.instances.event.DiscoverInstanceEvent;
import com.hypixel.hytale.builtin.instances.interactions.ExitInstanceInteraction;
import com.hypixel.hytale.builtin.instances.interactions.TeleportConfigInstanceInteraction;
import com.hypixel.hytale.builtin.instances.interactions.TeleportInstanceInteraction;
import com.hypixel.hytale.builtin.instances.page.ConfigureInstanceBlockPage;
import com.hypixel.hytale.builtin.instances.removal.IdleTimeoutCondition;
import com.hypixel.hytale.builtin.instances.removal.InstanceDataResource;
import com.hypixel.hytale.builtin.instances.removal.RemovalCondition;
import com.hypixel.hytale.builtin.instances.removal.RemovalSystem;
import com.hypixel.hytale.builtin.instances.removal.TimeoutCondition;
import com.hypixel.hytale.builtin.instances.removal.WorldEmptyCondition;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.GenerateSchemaEvent;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.asset.type.gameplay.respawn.RespawnController;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.ValidationOption;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.storage.provider.EmptyChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.provider.IChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.provider.MigrationChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.resources.EmptyResourceStorageProvider;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InstancesPlugin extends JavaPlugin {
   private static InstancesPlugin instance;
   @Nonnull
   public static final String INSTANCE_PREFIX = "instance-";
   @Nonnull
   public static final String CONFIG_FILENAME = "instance.bson";
   private ResourceType<ChunkStore, InstanceDataResource> instanceDataResourceType;
   private ComponentType<EntityStore, InstanceEntityConfig> instanceEntityConfigComponentType;
   private ComponentType<ChunkStore, InstanceBlock> instanceBlockComponentType;
   private ComponentType<ChunkStore, ConfigurableInstanceBlock> configurableInstanceBlockComponentType;

   public static InstancesPlugin get() {
      return instance;
   }

   public InstancesPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      EventRegistry eventRegistry = this.getEventRegistry();
      ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = this.getChunkStoreRegistry();
      this.getCommandRegistry().registerCommand(new InstancesCommand());
      eventRegistry.register((short)64, LoadAssetEvent.class, this::validateInstanceAssets);
      eventRegistry.register(GenerateSchemaEvent.class, InstancesPlugin::generateSchema);
      eventRegistry.registerGlobal(AddPlayerToWorldEvent.class, InstancesPlugin::onPlayerAddToWorld);
      eventRegistry.registerGlobal(DrainPlayerFromWorldEvent.class, InstancesPlugin::onPlayerDrainFromWorld);
      eventRegistry.register(PlayerConnectEvent.class, InstancesPlugin::onPlayerConnect);
      eventRegistry.registerGlobal(PlayerReadyEvent.class, InstancesPlugin::onPlayerReady);
      this.instanceBlockComponentType = chunkStoreRegistry.registerComponent(InstanceBlock.class, "Instance", InstanceBlock.CODEC);
      chunkStoreRegistry.registerSystem(new InstanceBlock.OnRemove());
      this.configurableInstanceBlockComponentType = chunkStoreRegistry.registerComponent(
         ConfigurableInstanceBlock.class, "InstanceConfig", ConfigurableInstanceBlock.CODEC
      );
      chunkStoreRegistry.registerSystem(new ConfigurableInstanceBlock.OnRemove());
      this.instanceDataResourceType = chunkStoreRegistry.registerResource(InstanceDataResource.class, "InstanceData", InstanceDataResource.CODEC);
      chunkStoreRegistry.registerSystem(new RemovalSystem());
      this.instanceEntityConfigComponentType = this.getEntityStoreRegistry()
         .registerComponent(InstanceEntityConfig.class, "Instance", InstanceEntityConfig.CODEC);
      this.getCodecRegistry(RemovalCondition.CODEC)
         .register("WorldEmpty", WorldEmptyCondition.class, WorldEmptyCondition.CODEC)
         .register("IdleTimeout", IdleTimeoutCondition.class, IdleTimeoutCondition.CODEC)
         .register("Timeout", TimeoutCondition.class, TimeoutCondition.CODEC);
      this.getCodecRegistry(Interaction.CODEC)
         .register("TeleportInstance", TeleportInstanceInteraction.class, TeleportInstanceInteraction.CODEC)
         .register("TeleportConfigInstance", TeleportConfigInstanceInteraction.class, TeleportConfigInstanceInteraction.CODEC)
         .register("ExitInstance", ExitInstanceInteraction.class, ExitInstanceInteraction.CODEC);
      this.getCodecRegistry(RespawnController.CODEC).register("ExitInstance", ExitInstance.class, ExitInstance.CODEC);
      OpenCustomUIInteraction.registerBlockEntityCustomPage(
         this, ConfigureInstanceBlockPage.class, "ConfigInstanceBlock", ConfigureInstanceBlockPage::new, () -> {
            Holder<ChunkStore> holder = ChunkStore.REGISTRY.newHolder();
            holder.ensureComponent(ConfigurableInstanceBlock.getComponentType());
            return holder;
         }
      );
      this.getCodecRegistry(WorldConfig.PLUGIN_CODEC).register(InstanceWorldConfig.class, "Instance", InstanceWorldConfig.CODEC);
   }

   @Nonnull
   public CompletableFuture<World> spawnInstance(@Nonnull String name, @Nonnull World forWorld, @Nonnull Transform returnPoint) {
      return this.spawnInstance(name, null, forWorld, returnPoint);
   }

   @Nonnull
   public CompletableFuture<World> spawnInstance(@Nonnull String name, @Nullable String worldName, @Nonnull World forWorld, @Nonnull Transform returnPoint) {
      Universe universe = Universe.get();
      Path path = universe.getPath();
      Path assetPath = getInstanceAssetPath(name);
      UUID uuid = UUID.randomUUID();
      String worldKey = worldName;
      if (worldName == null) {
         worldKey = "instance-" + safeName(name) + "-" + uuid;
      }

      Path worldPath = universe.validateWorldPath(worldKey);
      String finalWorldKey = worldKey;
      return WorldConfig.load(assetPath.resolve("instance.bson"))
         .thenApplyAsync(
            SneakyThrow.sneakyFunction(
               config -> {
                  config.setUuid(uuid);
                  if (config.getDisplayName() == null) {
                     config.setDisplayName(WorldConfig.formatDisplayName(name));
                  }

                  InstanceWorldConfig instanceConfig = InstanceWorldConfig.ensureAndGet(config);
                  instanceConfig.setReturnPoint(
                     new WorldReturnPoint(forWorld.getWorldConfig().getUuid(), returnPoint, instanceConfig.shouldPreventReconnection())
                  );
                  config.markChanged();
                  long start = System.nanoTime();
                  this.getLogger().at(Level.INFO).log("Copying instance files for %s to world %s", name, finalWorldKey);

                  try (Stream<Path> files = Files.walk(assetPath, FileUtil.DEFAULT_WALK_TREE_OPTIONS_ARRAY)) {
                     files.forEach(SneakyThrow.sneakyConsumer(filePath -> {
                        Path rel = assetPath.relativize(filePath);
                        Path toPath = worldPath.resolve(rel.toString());
                        if (Files.isDirectory(filePath)) {
                           Files.createDirectories(toPath);
                        } else {
                           if (Files.isRegularFile(filePath)) {
                              Files.copy(filePath, toPath);
                           }
                        }
                     }));
                  }

                  this.getLogger()
                     .at(Level.INFO)
                     .log("Completed instance files for %s to world %s in %s", name, finalWorldKey, FormatUtil.nanosToString(System.nanoTime() - start));
                  return config;
               }
            )
         )
         .thenCompose(config -> universe.makeWorld(finalWorldKey, worldPath, config));
   }

   public static void teleportPlayerToLoadingInstance(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull CompletableFuture<World> worldFuture,
      @Nullable Transform overrideReturn
   ) {
      World originalWorld = componentAccessor.getExternalData().getWorld();
      TransformComponent transformComponent = componentAccessor.getComponent(entityRef, TransformComponent.getComponentType());

      assert transformComponent != null;

      Transform originalPosition = transformComponent.getTransform().clone();
      InstanceEntityConfig instanceEntityConfigComponent = componentAccessor.getComponent(entityRef, InstanceEntityConfig.getComponentType());
      if (instanceEntityConfigComponent == null) {
         instanceEntityConfigComponent = componentAccessor.addComponent(entityRef, InstanceEntityConfig.getComponentType());
      }

      if (overrideReturn != null) {
         instanceEntityConfigComponent.setReturnPointOverride(new WorldReturnPoint(originalWorld.getWorldConfig().getUuid(), overrideReturn, false));
      } else {
         instanceEntityConfigComponent.setReturnPointOverride(null);
      }

      PlayerRef playerRefComponent = componentAccessor.getComponent(entityRef, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      UUIDComponent uuidComponent = componentAccessor.getComponent(entityRef, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      UUID playerUUID = uuidComponent.getUuid();
      InstanceEntityConfig finalPlayerConfig = instanceEntityConfigComponent;
      CompletableFuture.runAsync(playerRefComponent::removeFromStore, originalWorld)
         .thenCombine(worldFuture.orTimeout(1L, TimeUnit.MINUTES), (ignored, world) -> (World)world)
         .thenCompose(world -> {
            ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();
            Transform spawnPoint = spawnProvider != null ? spawnProvider.getSpawnPoint(world, playerUUID) : null;
            return world.addPlayer(playerRefComponent, spawnPoint, Boolean.TRUE, Boolean.FALSE);
         })
         .whenComplete((ret, ex) -> {
            if (ex != null) {
               ((HytaleLogger.Api)get().getLogger().at(Level.SEVERE).withCause(ex))
                  .log("Failed to send %s to instance world", playerRefComponent.getUsername());
               finalPlayerConfig.setReturnPointOverride(null);
            }

            if (ret == null) {
               if (originalWorld.isAlive()) {
                  originalWorld.addPlayer(playerRefComponent, originalPosition, Boolean.TRUE, Boolean.FALSE);
               } else {
                  World defaultWorld = Universe.get().getDefaultWorld();
                  if (defaultWorld != null) {
                     defaultWorld.addPlayer(playerRefComponent, null, Boolean.TRUE, Boolean.FALSE);
                  } else {
                     get().getLogger().at(Level.SEVERE).log("No fallback world for %s, disconnecting", playerRefComponent.getUsername());
                     playerRefComponent.getPacketHandler().disconnect("Failed to teleport - no world available");
                  }
               }
            }
         });
   }

   public static void teleportPlayerToInstance(
      @Nonnull Ref<EntityStore> playerRef,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull World targetWorld,
      @Nullable Transform overrideReturn
   ) {
      World originalWorld = componentAccessor.getExternalData().getWorld();
      WorldConfig originalWorldConfig = originalWorld.getWorldConfig();
      if (overrideReturn != null) {
         InstanceEntityConfig instanceConfig = componentAccessor.ensureAndGetComponent(playerRef, InstanceEntityConfig.getComponentType());
         instanceConfig.setReturnPointOverride(new WorldReturnPoint(originalWorldConfig.getUuid(), overrideReturn, false));
      }

      UUIDComponent uuidComponent = componentAccessor.getComponent(playerRef, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      UUID playerUUID = uuidComponent.getUuid();
      WorldConfig targetWorldConfig = targetWorld.getWorldConfig();
      ISpawnProvider spawnProvider = targetWorldConfig.getSpawnProvider();
      if (spawnProvider == null) {
         throw new IllegalStateException("Spawn provider cannot be null when teleporting player to instance!");
      } else {
         Transform spawnTransform = spawnProvider.getSpawnPoint(targetWorld, playerUUID);
         Teleport teleportComponent = Teleport.createForPlayer(targetWorld, spawnTransform);
         componentAccessor.addComponent(playerRef, Teleport.getComponentType(), teleportComponent);
      }
   }

   public static CompletableFuture<Void> exitInstance(@Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      InstanceEntityConfig entityConfig = componentAccessor.getComponent(targetRef, InstanceEntityConfig.getComponentType());
      WorldReturnPoint returnPoint = entityConfig != null ? entityConfig.getReturnPoint() : null;
      if (returnPoint == null) {
         WorldConfig config = world.getWorldConfig();
         InstanceWorldConfig instanceConfig = InstanceWorldConfig.get(config);
         returnPoint = instanceConfig != null ? instanceConfig.getReturnPoint() : null;
         if (returnPoint == null) {
            throw new IllegalArgumentException("Player is not in an instance");
         }
      }

      Universe universe = Universe.get();
      World targetWorld = universe.getWorld(returnPoint.getWorld());
      if (targetWorld == null) {
         throw new IllegalArgumentException("Missing return world");
      } else {
         Teleport teleportComponent = Teleport.createForPlayer(targetWorld, returnPoint.getReturnPoint());
         CompletableFuture<Void> future = new CompletableFuture<>();
         teleportComponent.setOnComplete(future);
         componentAccessor.addComponent(targetRef, Teleport.getComponentType(), teleportComponent);
         return future;
      }
   }

   public static void safeRemoveInstance(@Nonnull String worldName) {
      safeRemoveInstance(Universe.get().getWorld(worldName));
   }

   public static void safeRemoveInstance(@Nonnull UUID worldUUID) {
      safeRemoveInstance(Universe.get().getWorld(worldUUID));
   }

   public static void safeRemoveInstance(@Nullable World instanceWorld) {
      if (instanceWorld != null) {
         Store<ChunkStore> chunkStore = instanceWorld.getChunkStore().getStore();
         chunkStore.getResource(InstanceDataResource.getResourceType()).setHadPlayer(true);
         WorldConfig config = instanceWorld.getWorldConfig();
         InstanceWorldConfig instanceConfig = InstanceWorldConfig.get(config);
         if (instanceConfig != null) {
            instanceConfig.setRemovalConditions(WorldEmptyCondition.REMOVE_WHEN_EMPTY);
         }

         config.markChanged();
      }
   }

   @Nonnull
   public static Path getInstanceAssetPath(@Nonnull String name) {
      for (AssetPack pack : AssetModule.get().getAssetPacks()) {
         Path instancesDir = pack.getRoot().resolve("Server").resolve("Instances");
         Path path = PathUtil.resolvePathWithinDir(instancesDir, name);
         if (path == null) {
            throw new IllegalArgumentException("Invalid instance name");
         }

         if (Files.exists(path)) {
            return path;
         }
      }

      Path instancesDirx = AssetModule.get().getBaseAssetPack().getRoot().resolve("Server").resolve("Instances");
      Path pathx = PathUtil.resolvePathWithinDir(instancesDirx, name);
      if (pathx == null) {
         throw new IllegalArgumentException("Invalid instance name");
      } else {
         return pathx;
      }
   }

   public static boolean doesInstanceAssetExist(@Nonnull String name) {
      return Files.exists(getInstanceAssetPath(name).resolve("instance.bson"));
   }

   @Nonnull
   public static CompletableFuture<World> loadInstanceAssetForEdit(@Nonnull String name) {
      Path path = getInstanceAssetPath(name);
      Universe universe = Universe.get();
      return WorldConfig.load(path.resolve("instance.bson")).thenCompose(config -> {
         config.setUuid(UUID.randomUUID());
         config.setSavingPlayers(false);
         config.setIsAllNPCFrozen(true);
         config.setTicking(false);
         config.setGameMode(GameMode.Creative);
         config.setDeleteOnRemove(false);
         InstanceWorldConfig.ensureAndGet(config).setRemovalConditions(RemovalCondition.EMPTY);
         config.markChanged();
         String worldName = "instance-edit-" + safeName(name);
         return universe.makeWorld(worldName, path, config);
      });
   }

   @Nonnull
   public List<String> getInstanceAssets() {
      final List<String> instances = new ObjectArrayList();

      for (AssetPack pack : AssetModule.get().getAssetPacks()) {
         final Path path = pack.getRoot().resolve("Server").resolve("Instances");
         if (Files.isDirectory(path)) {
            try {
               Files.walkFileTree(path, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                  @Nonnull
                  public FileVisitResult preVisitDirectory(@Nonnull Path dir, @Nonnull BasicFileAttributes attrs) {
                     if (Files.exists(dir.resolve("instance.bson"))) {
                        Path relative = path.relativize(dir);
                        String name = relative.toString();
                        instances.add(name);
                        return FileVisitResult.SKIP_SUBTREE;
                     } else {
                        return FileVisitResult.CONTINUE;
                     }
                  }
               });
            } catch (IOException var6) {
               throw SneakyThrow.sneakyThrow(var6);
            }
         }
      }

      return instances;
   }

   private static void onPlayerConnect(@Nonnull PlayerConnectEvent event) {
      Holder<EntityStore> holder = event.getHolder();
      Player playerComponent = holder.getComponent(Player.getComponentType());

      assert playerComponent != null;

      PlayerConfigData playerConfig = playerComponent.getPlayerConfigData();
      InstanceEntityConfig config = InstanceEntityConfig.ensureAndGet(holder);
      String lastWorldName = playerConfig.getWorld();
      World lastWorld = Universe.get().getWorld(lastWorldName);
      WorldReturnPoint fallbackWorld = config.getReturnPoint();
      if (fallbackWorld != null && (lastWorld == null || fallbackWorld.isReturnOnReconnect())) {
         lastWorld = Universe.get().getWorld(fallbackWorld.getWorld());
         if (lastWorld != null) {
            Transform transform = fallbackWorld.getReturnPoint();
            TransformComponent transformComponent = holder.ensureAndGetComponent(TransformComponent.getComponentType());
            transformComponent.setPosition(transform.getPosition());
            Vector3f rotationClone = transformComponent.getRotation().clone();
            rotationClone.setYaw(transform.getRotation().getYaw());
            transformComponent.setRotation(rotationClone);
            HeadRotation headRotationComponent = holder.ensureAndGetComponent(HeadRotation.getComponentType());
            headRotationComponent.teleportRotation(transform.getRotation());
         }
      } else if (lastWorld != null) {
         config.setReturnPointOverride(config.getReturnPoint());
      }
   }

   private static void onPlayerAddToWorld(@Nonnull AddPlayerToWorldEvent event) {
      Holder<EntityStore> holder = event.getHolder();
      InstanceWorldConfig worldConfig = InstanceWorldConfig.get(event.getWorld().getWorldConfig());
      if (worldConfig == null) {
         InstanceEntityConfig entityConfig = holder.getComponent(InstanceEntityConfig.getComponentType());
         if (entityConfig != null && entityConfig.getReturnPoint() != null) {
            entityConfig.setReturnPoint(null);
         }
      } else {
         InstanceEntityConfig entityConfig = InstanceEntityConfig.ensureAndGet(holder);
         if (entityConfig.getReturnPointOverride() == null) {
            entityConfig.setReturnPoint(worldConfig.getReturnPoint());
         } else {
            WorldReturnPoint override = entityConfig.getReturnPointOverride();
            override.setReturnOnReconnect(worldConfig.shouldPreventReconnection());
            entityConfig.setReturnPoint(override);
            entityConfig.setReturnPointOverride(null);
         }
      }
   }

   private static void onPlayerReady(@Nonnull PlayerReadyEvent event) {
      Player player = event.getPlayer();
      World world = player.getWorld();
      if (world != null) {
         WorldConfig worldConfig = world.getWorldConfig();
         InstanceWorldConfig instanceWorldConfig = InstanceWorldConfig.get(worldConfig);
         if (instanceWorldConfig != null) {
            InstanceDiscoveryConfig discoveryConfig = instanceWorldConfig.getDiscovery();
            if (discoveryConfig != null) {
               PlayerConfigData playerConfigData = player.getPlayerConfigData();
               UUID instanceUuid = worldConfig.getUuid();
               if (discoveryConfig.alwaysDisplay() || !playerConfigData.getDiscoveredInstances().contains(instanceUuid)) {
                  Set<UUID> discoveredInstances = new HashSet<>(playerConfigData.getDiscoveredInstances());
                  discoveredInstances.add(instanceUuid);
                  playerConfigData.setDiscoveredInstances(discoveredInstances);
                  Ref<EntityStore> playerRef = event.getPlayerRef();
                  if (playerRef.isValid()) {
                     world.execute(() -> {
                        Store<EntityStore> store = world.getEntityStore().getStore();
                        showInstanceDiscovery(playerRef, store, instanceUuid, discoveryConfig);
                     });
                  }
               }
            }
         }
      }
   }

   private static void showInstanceDiscovery(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UUID instanceUuid, @Nonnull InstanceDiscoveryConfig discoveryConfig
   ) {
      DiscoverInstanceEvent.Display discoverInstanceEvent = new DiscoverInstanceEvent.Display(instanceUuid, discoveryConfig.clone());
      store.invoke(ref, discoverInstanceEvent);
      discoveryConfig = discoverInstanceEvent.getDiscoveryConfig();
      if (!discoverInstanceEvent.isCancelled() && discoverInstanceEvent.shouldDisplay()) {
         PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            String subtitleKey = discoveryConfig.getSubtitleKey();
            Message subtitle = subtitleKey != null ? Message.translation(subtitleKey) : Message.empty();
            EventTitleUtil.showEventTitleToPlayer(
               playerRefComponent,
               Message.translation(discoveryConfig.getTitleKey()),
               subtitle,
               discoveryConfig.isMajor(),
               discoveryConfig.getIcon(),
               discoveryConfig.getDuration(),
               discoveryConfig.getFadeInDuration(),
               discoveryConfig.getFadeOutDuration()
            );
            String discoverySoundEventId = discoveryConfig.getDiscoverySoundEventId();
            if (discoverySoundEventId != null) {
               int assetIndex = SoundEvent.getAssetMap().getIndex(discoverySoundEventId);
               if (assetIndex != Integer.MIN_VALUE) {
                  SoundUtil.playSoundEvent2d(ref, assetIndex, SoundCategory.UI, store);
               }
            }
         }
      }
   }

   private static void onPlayerDrainFromWorld(@Nonnull DrainPlayerFromWorldEvent event) {
      InstanceEntityConfig config = InstanceEntityConfig.removeAndGet(event.getHolder());
      if (config != null) {
         WorldReturnPoint returnPoint = config.getReturnPoint();
         if (returnPoint != null) {
            World returnWorld = Universe.get().getWorld(returnPoint.getWorld());
            if (returnWorld != null) {
               event.setWorld(returnWorld);
               event.setTransform(returnPoint.getReturnPoint());
            }
         }
      }
   }

   private static void generateSchema(@Nonnull GenerateSchemaEvent event) {
      ObjectSchema worldConfig = WorldConfig.CODEC.toSchema(event.getContext());
      Map<String, Schema> props = worldConfig.getProperties();
      props.put("UUID", Schema.anyOf(new StringSchema(), new ObjectSchema()));
      worldConfig.setTitle("Instance Configuration");
      worldConfig.setId("InstanceConfig.json");
      Schema.HytaleMetadata hytaleMetadata = worldConfig.getHytale();
      if (hytaleMetadata != null) {
         hytaleMetadata.setPath("Instances");
         hytaleMetadata.setExtension("instance.bson");
         hytaleMetadata.setUiEditorIgnore(Boolean.TRUE);
      }

      event.addSchema("InstanceConfig.json", worldConfig);
      event.addSchemaLink("InstanceConfig", List.of("Instances/**/instance.bson"), ".bson");
   }

   private void validateInstanceAssets(@Nonnull LoadAssetEvent event) {
      Path path = AssetModule.get().getBaseAssetPack().getRoot().resolve("Server").resolve("Instances");
      if (Options.getOptionSet().has(Options.VALIDATE_ASSETS) && Files.isDirectory(path) && !event.isShouldShutdown()) {
         StringBuilder errors = new StringBuilder();

         for (String name : this.getInstanceAssets()) {
            StringBuilder sb = new StringBuilder();
            Path instancePath = getInstanceAssetPath(name);
            Universe universe = Universe.get();
            WorldConfig config = WorldConfig.load(instancePath.resolve("instance.bson")).join();
            IChunkStorageProvider<?> storage = config.getChunkStorageProvider();
            config.setChunkStorageProvider(new MigrationChunkStorageProvider(new IChunkStorageProvider[]{storage}, EmptyChunkStorageProvider.INSTANCE));
            config.setResourceStorageProvider(EmptyResourceStorageProvider.INSTANCE);
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
            String worldName = "instance-validate-" + safeName(name);

            try {
               World world = universe.makeWorld(worldName, instancePath, config, false).join();
               EnumSet<ValidationOption> options = EnumSet.of(ValidationOption.BLOCK_STATES, ValidationOption.BLOCKS);
               world.validate(sb, WorldValidationUtil.blockValidator(errors, options), options);
            } catch (Exception var18) {
               sb.append("\t").append(var18.getMessage());
               ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var18)).log("Failed to validate: " + name);
            } finally {
               if (!sb.isEmpty()) {
                  errors.append("Instance: ").append(name).append('\n').append((CharSequence)sb).append('\n');
               }
            }

            if (universe.getWorld(worldName) != null) {
               universe.removeWorld(worldName);
            }
         }

         if (!errors.isEmpty()) {
            this.getLogger().at(Level.SEVERE).log("Failed to validate instances:\n" + errors);
            event.failed(true, "failed to validate instances");
         }

         HytaleLogger.getLogger()
            .at(Level.INFO)
            .log("Loading Instance assets phase completed! Boot time %s", FormatUtil.nanosToString(System.nanoTime() - event.getBootStart()));
      }
   }

   @Nonnull
   public static String safeName(@Nonnull String name) {
      return name.replace('/', '-');
   }

   @Nonnull
   public ResourceType<ChunkStore, InstanceDataResource> getInstanceDataResourceType() {
      return this.instanceDataResourceType;
   }

   @Nonnull
   public ComponentType<EntityStore, InstanceEntityConfig> getInstanceEntityConfigComponentType() {
      return this.instanceEntityConfigComponentType;
   }

   @Nonnull
   public ComponentType<ChunkStore, InstanceBlock> getInstanceBlockComponentType() {
      return this.instanceBlockComponentType;
   }

   @Nonnull
   public ComponentType<ChunkStore, ConfigurableInstanceBlock> getConfigurableInstanceBlockComponentType() {
      return this.configurableInstanceBlockComponentType;
   }
}
