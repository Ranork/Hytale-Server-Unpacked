package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabAlignment;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabRowSplitMode;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabStackingAxis;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.WorldGenType;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.environment.config.WeatherForecast;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.FlatWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabEditSessionManager {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private static final Message MESSAGE_COMMANDS_PREFAB_EDIT_SESSION_MANAGER_EXISTING_EDIT_SESSION = Message.translation(
      "server.commands.prefabeditsessionmanager.existingEditSession"
   );
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_SOMETHING_WENT_WRONG = Message.translation("server.commands.editprefab.somethingWentWrong");
   public static final float NOON_TIME = 0.5F;
   public static final String DEFAULT_NEW_WORLD_ZERO_COORDINATE_BLOCK_NAME = "Rock_Stone";
   public static final String DEFAULT_ENVIRONMENT = "Zone1_Sunny";
   private static final String PREFAB_SELECTOR_TOOL_ID = "EditorTool_PrefabEditing_SelectPrefab";
   public static final String DEFAULT_CHUNK_ENVIRONMENT = "Env_Zone1_Plains";
   public static final String PREFAB_EDITING_WORLD_NAME_PREFIX = "prefabEditor-";
   @Nonnull
   public static final Color DEFAULT_TINT = new Color((byte)91, (byte)-98, (byte)40);
   private static final long PROGRESS_UPDATE_INTERVAL_NANOS = 100000000L;
   public static final String DEFAULT_GRASS_TINT_HEX = "#5B9E28";
   @Nonnull
   private final Map<UUID, PrefabEditSession> activeEditSessions = new Object2ObjectOpenHashMap();
   @Nonnull
   private final HashSet<Path> prefabsBeingEdited = new HashSet<>();
   @Nonnull
   private final Map<UUID, UUID> inProgressTeleportations = new Object2ObjectOpenHashMap();
   @Nonnull
   private final HashSet<UUID> inProgressLoading = new HashSet<>();
   @Nonnull
   private final HashSet<UUID> cancelledLoading = new HashSet<>();

   public PrefabEditSessionManager(@Nonnull JavaPlugin plugin) {
      EventRegistry eventRegistry = plugin.getEventRegistry();
      eventRegistry.registerGlobal(AddPlayerToWorldEvent.class, this::onPlayerAddedToWorld);
      eventRegistry.registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
   }

   private void onPlayerReady(@Nonnull PlayerReadyEvent event) {
      Ref<EntityStore> playerRef = event.getPlayer().getReference();
      if (playerRef != null && playerRef.isValid()) {
         Store<EntityStore> store = playerRef.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(() -> {
            UUIDComponent uuidComponent = store.getComponent(playerRef, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            UUID playerUUID = uuidComponent.getUuid();
            if (this.inProgressTeleportations.containsKey(playerUUID)) {
               this.inProgressTeleportations.remove(playerUUID);
               MovementStatesComponent movementStatesComponent = store.getComponent(playerRef, MovementStatesComponent.getComponentType());

               assert movementStatesComponent != null;

               MovementStates movementStates = movementStatesComponent.getMovementStates();
               Player playerComponent = store.getComponent(playerRef, Player.getComponentType());

               assert playerComponent != null;

               playerComponent.applyMovementStates(playerRef, new SavedMovementStates(true), movementStates, store);
               PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());
               if (playerRefComponent != null) {
                  this.givePrefabSelectorTool(playerComponent, playerRefComponent);
               }
            }
         });
      }
   }

   private void givePrefabSelectorTool(@Nonnull Player playerComponent, @Nonnull PlayerRef playerRef) {
      Inventory inventory = playerComponent.getInventory();
      ItemContainer hotbar = inventory.getHotbar();
      int hotbarSize = hotbar.getCapacity();

      for (short slot = 0; slot < hotbarSize; slot++) {
         ItemStack itemStack = hotbar.getItemStack(slot);
         if (itemStack != null && !itemStack.isEmpty() && "EditorTool_PrefabEditing_SelectPrefab".equals(itemStack.getItemId())) {
            inventory.setActiveHotbarSlot((byte)slot);
            playerRef.getPacketHandler().writeNoCache(new SetActiveSlot(-1, (byte)slot));
            return;
         }
      }

      short emptySlot = -1;

      for (short slotx = 0; slotx < hotbarSize; slotx++) {
         ItemStack itemStack = hotbar.getItemStack(slotx);
         if (itemStack == null || itemStack.isEmpty()) {
            emptySlot = slotx;
            break;
         }
      }

      if (emptySlot == -1) {
         emptySlot = 0;
      }

      hotbar.setItemStackForSlot(emptySlot, new ItemStack("EditorTool_PrefabEditing_SelectPrefab"));
      inventory.setActiveHotbarSlot((byte)emptySlot);
      playerRef.getPacketHandler().writeNoCache(new SetActiveSlot(-1, (byte)emptySlot));
   }

   public void onPlayerAddedToWorld(@Nonnull AddPlayerToWorldEvent event) {
      World world = event.getWorld();
      if (world.getName().startsWith("prefabEditor-")) {
         world.execute(() -> {
            Holder<EntityStore> playerHolder = event.getHolder();
            UUIDComponent uuidComponent = playerHolder.getComponent(UUIDComponent.getComponentType());

            assert uuidComponent != null;

            this.inProgressTeleportations.put(uuidComponent.getUuid(), world.getWorldConfig().getUuid());
         });
      }
   }

   public void updatePathOfLoadedPrefab(@Nonnull Path oldPath, @Nonnull Path newPath) {
      this.prefabsBeingEdited.remove(oldPath);
      this.prefabsBeingEdited.add(newPath);
   }

   public boolean isEditingAPrefab(@Nonnull UUID playerUUID) {
      return this.activeEditSessions.containsKey(playerUUID);
   }

   public PrefabEditSession getPrefabEditSession(@Nonnull UUID playerUUID) {
      return this.activeEditSessions.get(playerUUID);
   }

   @Nonnull
   public Map<UUID, PrefabEditSession> getActiveEditSessions() {
      return this.activeEditSessions;
   }

   void populateActiveEditSession(@Nonnull UUID playerUuid, @Nonnull PrefabEditSession editSession) {
      this.activeEditSessions.put(playerUuid, editSession);
   }

   void populatePrefabsBeingEdited(@Nonnull Path prefabPath) {
      this.prefabsBeingEdited.add(prefabPath);
   }

   void scheduleAnchorEntityRecreation(@Nonnull PrefabEditSession editSession) {
      CompletableFuture.runAsync(() -> {
         World world = Universe.get().getWorld(editSession.getWorldName());
         if (world != null) {
            world.execute(() -> {
               for (PrefabEditingMetadata metadata : editSession.getLoadedPrefabMetadata().values()) {
                  metadata.recreateAnchorEntity(world);
               }
            });
         }
      });
   }

   public boolean hasInProgressLoading(@Nonnull UUID playerUuid) {
      return this.inProgressLoading.contains(playerUuid);
   }

   public void cancelLoading(@Nonnull UUID playerUuid) {
      this.cancelledLoading.add(playerUuid);
   }

   public boolean isLoadingCancelled(@Nonnull UUID playerUuid) {
      return this.cancelledLoading.contains(playerUuid);
   }

   public void clearLoadingState(@Nonnull UUID playerUuid) {
      this.inProgressLoading.remove(playerUuid);
      this.cancelledLoading.remove(playerUuid);
   }

   @Nonnull
   public CompletableFuture<Void> createEditSessionForNewPrefab(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player editor,
      @Nonnull PrefabEditorCreationSettings settings,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      PrefabEditorCreationContext prefabEditorCreationContext = settings.finishProcessing(editor, playerRefComponent, true);
      if (prefabEditorCreationContext == null) {
         playerRefComponent.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_SOMETHING_WENT_WRONG);
         return CompletableFuture.completedFuture(null);
      } else {
         return this.createEditSession(ref, prefabEditorCreationContext, true, componentAccessor);
      }
   }

   @Nullable
   public CompletableFuture<Void> loadPrefabAndCreateEditSession(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player editor,
      @Nonnull PrefabEditorCreationSettings settings,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.loadPrefabAndCreateEditSession(ref, editor, settings, componentAccessor, null);
   }

   @Nullable
   public CompletableFuture<Void> loadPrefabAndCreateEditSession(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player editor,
      @Nonnull PrefabEditorCreationSettings settings,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nullable Consumer<PrefabLoadingState> progressCallback
   ) {
      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      UUID playerUuid = playerRefComponent.getUuid();
      if (this.inProgressLoading.contains(playerUuid)) {
         PrefabLoadingState loadingState = new PrefabLoadingState();
         loadingState.addError("server.commands.editprefab.error.loadingInProgress");
         this.notifyProgress(progressCallback, loadingState);
         playerRefComponent.sendMessage(Message.translation("server.commands.editprefab.error.loadingInProgress"));
         return null;
      } else {
         this.inProgressLoading.add(playerUuid);
         this.cancelledLoading.remove(playerUuid);
         PrefabLoadingState loadingState = new PrefabLoadingState();
         loadingState.setPhase(PrefabLoadingState.Phase.INITIALIZING);
         this.notifyProgress(progressCallback, loadingState);
         PrefabEditorCreationContext prefabEditorCreationContext = settings.finishProcessing(editor, playerRefComponent, false);
         if (prefabEditorCreationContext == null) {
            loadingState.addError("server.commands.editprefab.error.processingFailed");
            this.notifyProgress(progressCallback, loadingState);
            playerRefComponent.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_SOMETHING_WENT_WRONG);
            return null;
         } else if (prefabEditorCreationContext.getPrefabPaths().isEmpty()) {
            loadingState.addError("server.commands.editprefab.error.noPrefabsFound");
            this.notifyProgress(progressCallback, loadingState);
            playerRefComponent.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_SOMETHING_WENT_WRONG);
            return null;
         } else {
            loadingState.setTotalPrefabs(prefabEditorCreationContext.getPrefabPaths().size());
            this.notifyProgress(progressCallback, loadingState);
            return this.createEditSession(ref, prefabEditorCreationContext, false, componentAccessor, loadingState, progressCallback);
         }
      }
   }

   private void notifyProgress(@Nullable Consumer<PrefabLoadingState> progressCallback, @Nonnull PrefabLoadingState loadingState) {
      if (progressCallback != null) {
         PrefabLoadingState.Phase phase = loadingState.getCurrentPhase();
         if (phase != PrefabLoadingState.Phase.LOADING_PREFABS && phase != PrefabLoadingState.Phase.PASTING_PREFABS) {
            progressCallback.accept(loadingState);
         } else {
            long now = System.nanoTime();
            if (now - loadingState.getLastNotifyTimeNanos() >= 100000000L) {
               loadingState.setLastNotifyTimeNanos(now);
               progressCallback.accept(loadingState);
            }
         }
      }
   }

   @Nonnull
   private CompletableFuture<Void> createEditSession(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PrefabEditorCreationContext context,
      boolean createNewPrefab,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.createEditSession(ref, context, createNewPrefab, componentAccessor, null, null);
   }

   @Nonnull
   private CompletableFuture<Void> createEditSession(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PrefabEditorCreationContext context,
      boolean createNewPrefab,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nullable PrefabLoadingState loadingState,
      @Nullable Consumer<PrefabLoadingState> progressCallback
   ) {
      World sourceWorld = componentAccessor.getExternalData().getWorld();
      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      UUID playerUUID = playerRefComponent.getUuid();
      if (this.activeEditSessions.containsKey(playerUUID)) {
         if (loadingState != null) {
            loadingState.addError("server.commands.editprefab.error.existingSession");
            this.notifyProgress(progressCallback, loadingState);
         }

         playerRefComponent.sendMessage(MESSAGE_COMMANDS_PREFAB_EDIT_SESSION_MANAGER_EXISTING_EDIT_SESSION);
         return CompletableFuture.completedFuture(null);
      } else {
         for (Path prefabPath : context.getPrefabPaths()) {
            if (this.prefabsBeingEdited.contains(prefabPath)) {
               if (loadingState != null) {
                  loadingState.addError("server.commands.editprefab.error.alreadyBeingEdited", prefabPath.toString());
                  this.notifyProgress(progressCallback, loadingState);
               }

               playerRefComponent.sendMessage(
                  Message.translation("server.commands.prefabeditsessionmanager.alreadyBeingEdited").param("path", prefabPath.toString())
               );
               return CompletableFuture.completedFuture(null);
            }
         }

         if (loadingState != null) {
            loadingState.setPhase(PrefabLoadingState.Phase.CREATING_WORLD);
            this.notifyProgress(progressCallback, loadingState);
         }

         WorldConfig config = new WorldConfig();
         boolean enableTicking = context.isWorldTickingEnabled();
         config.setBlockTicking(enableTicking);
         config.setSpawningNPC(false);
         config.setIsSpawnMarkersEnabled(false);
         config.setObjectiveMarkersEnabled(false);
         config.setGameMode(GameMode.Creative);
         config.setDeleteOnRemove(true);
         config.setUuid(UUID.randomUUID());
         config.setGameTimePaused(true);
         config.setIsAllNPCFrozen(true);
         config.setSavingPlayers(true);
         config.setCanSaveChunks(true);
         config.setTicking(enableTicking);
         config.setForcedWeather(this.getWeatherFromEnvironment(context.getEnvironment()));
         String worldName = this.getWorldName(context);

         try {
            Files.createDirectories(this.getSavePath(context));
         } catch (IOException var17) {
            if (loadingState != null) {
               loadingState.addError("server.commands.editprefab.error.createDirectoryFailed", var17.getMessage());
               this.notifyProgress(progressCallback, loadingState);
            }

            playerRefComponent.sendMessage(Message.translation("server.commands.instances.createDirectory.failed").param("errormsg", var17.getMessage()));
            return CompletableFuture.completedFuture(null);
         }

         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Transform transform = transformComponent.getTransform().clone();
         PrefabEditSession prefabEditSession = new PrefabEditSession(worldName, playerUUID, sourceWorld.getWorldConfig().getUuid(), transform);
         CompletableFuture<World> future;
         if (createNewPrefab) {
            future = this.getPrefabCreatingCompletableFuture(context, prefabEditSession, config);
         } else {
            future = this.getPrefabLoadingCompletableFuture(context, prefabEditSession, config, loadingState, progressCallback, playerUUID);
         }

         if (future == null) {
            if (loadingState != null) {
               loadingState.addError("server.commands.editprefab.error.loadFailed");
               this.notifyProgress(progressCallback, loadingState);
            }

            return CompletableFuture.completedFuture(null);
         } else {
            return future.exceptionally(
                  throwable -> {
                     if (this.isLoadingCancelled(playerUUID)) {
                        return null;
                     } else {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Error occurred during prefab editor session creation");
                        if (loadingState != null) {
                           loadingState.addError("server.commands.editprefab.error.exception", throwable.getMessage());
                           this.notifyProgress(progressCallback, loadingState);
                        }

                        playerRefComponent.sendMessage(
                           Message.translation("server.commands.editprefab.error.exception")
                              .param("details", throwable.getMessage() != null ? throwable.getMessage() : "Unknown error")
                        );
                        return null;
                     }
                  }
               )
               .thenAcceptAsync(
                  targetWorld -> {
                     if (!this.isLoadingCancelled(playerUUID)) {
                        if (targetWorld != null) {
                           if (loadingState != null) {
                              loadingState.setPhase(PrefabLoadingState.Phase.FINALIZING);
                              this.notifyProgress(progressCallback, loadingState);
                           }

                           Vector3i spawnPoint = prefabEditSession.getSpawnPoint();
                           targetWorld.getWorldConfig().setSpawnProvider(new GlobalSpawnProvider(new Transform(spawnPoint)));
                           CompletableFuture.runAsync(
                              () -> targetWorld.getEntityStore().getStore().replaceResource(PrefabEditSession.getResourceType(), prefabEditSession),
                              targetWorld
                           );
                           CompletableFuture.runAsync(() -> {
                              Teleport teleportComponent = Teleport.createForPlayer(targetWorld, new Transform(spawnPoint));
                              componentAccessor.putComponent(ref, Teleport.getComponentType(), teleportComponent);
                           }, sourceWorld);
                        }
                     }
                  }
               )
               .thenRun(
                  () -> {
                     if (!this.isLoadingCancelled(playerUUID)) {
                        this.prefabsBeingEdited.addAll(context.getPrefabPaths());
                        this.activeEditSessions.put(playerUUID, prefabEditSession);
                        if (loadingState != null) {
                           loadingState.markComplete();
                           this.notifyProgress(progressCallback, loadingState);
                        }

                        playerRefComponent.sendMessage(
                           Message.translation("server.commands.prefabeditsessionmanager.success." + (createNewPrefab ? "new" : "load"))
                        );
                     }
                  }
               )
               .whenComplete((result, throwable) -> this.inProgressLoading.remove(playerUUID));
         }
      }
   }

   @Nonnull
   private CompletableFuture<World> getWorldCreatingFuture(@Nonnull PrefabEditorCreationContext context, @Nonnull WorldConfig config) {
      return Universe.get().makeWorld(this.getWorldName(context), this.getSavePath(context), config, true);
   }

   @Nonnull
   private String getWorldName(@Nonnull PrefabEditorCreationContext context) {
      return "prefabEditor-" + context.getEditorRef().getUsername();
   }

   @Nonnull
   private String getWeatherFromEnvironment(@Nullable String environmentId) {
      if (environmentId != null && !environmentId.isEmpty()) {
         Environment environment = Environment.getAssetMap().getAsset(environmentId);
         if (environment == null) {
            return "Zone1_Sunny";
         } else {
            IWeightedMap<WeatherForecast> forecast = environment.getWeatherForecast(12);
            if (forecast != null && forecast.size() != 0) {
               String[] bestWeatherId = new String[]{null};
               double[] highestWeight = new double[]{Double.NEGATIVE_INFINITY};
               forecast.forEachEntry((weatherForecast, weight) -> {
                  if (weight > highestWeight[0]) {
                     highestWeight[0] = weight;
                     bestWeatherId[0] = weatherForecast.getWeatherId();
                  }
               });
               return bestWeatherId[0] != null ? bestWeatherId[0] : "Zone1_Sunny";
            } else {
               return "Zone1_Sunny";
            }
         }
      } else {
         return "Zone1_Sunny";
      }
   }

   @Nonnull
   private Path getSavePath(@Nonnull PrefabEditorCreationContext context) {
      return Constants.UNIVERSE_PATH.resolve("worlds").resolve(this.getWorldName(context));
   }

   private void applyWorldGenWorldConfig(@Nonnull PrefabEditorCreationContext context, int yLevelToPastePrefabsAt, @Nonnull WorldConfig worldConfig) {
      String environment = context.getEnvironment() != null ? context.getEnvironment() : "Env_Zone1_Plains";
      Color tint = DEFAULT_TINT;
      if (context.getGrassTint() != null && !context.getGrassTint().isEmpty()) {
         Color parsed = ColorParseUtil.parseColor(context.getGrassTint());
         if (parsed != null) {
            tint = parsed;
         }
      }

      if (context.getWorldGenType().equals(WorldGenType.FLAT)) {
         int yLevelForFlatWorldExclusive = Math.max(1, yLevelToPastePrefabsAt - context.getBlocksAboveSurface());
         FlatWorldGenProvider.Layer topLayer = new FlatWorldGenProvider.Layer();
         topLayer.blockType = "Soil_Grass";
         topLayer.to = yLevelForFlatWorldExclusive;
         topLayer.from = yLevelForFlatWorldExclusive - 1;
         topLayer.environment = environment;
         FlatWorldGenProvider.Layer airLayer = new FlatWorldGenProvider.Layer();
         airLayer.blockType = "Empty";
         airLayer.to = 320;
         airLayer.from = yLevelForFlatWorldExclusive;
         airLayer.environment = environment;
         if (yLevelForFlatWorldExclusive - 2 >= 0) {
            FlatWorldGenProvider.Layer bottomLayer = new FlatWorldGenProvider.Layer();
            bottomLayer.blockType = "Soil_Clay";
            bottomLayer.to = yLevelForFlatWorldExclusive - 1;
            bottomLayer.from = 0;
            bottomLayer.environment = environment;
            worldConfig.setWorldGenProvider(new FlatWorldGenProvider(tint, new FlatWorldGenProvider.Layer[]{airLayer, topLayer, bottomLayer}));
         } else {
            worldConfig.setWorldGenProvider(new FlatWorldGenProvider(tint, new FlatWorldGenProvider.Layer[]{airLayer, topLayer}));
         }
      } else {
         worldConfig.setWorldGenProvider(new VoidWorldGenProvider(tint, environment));
      }
   }

   @Nonnull
   private CompletableFuture<World> getPrefabCreatingCompletableFuture(
      @Nonnull PrefabEditorCreationContext context, @Nonnull PrefabEditSession editSession, @Nonnull WorldConfig worldConfig
   ) {
      this.applyWorldGenWorldConfig(context, context.getPasteLevelGoal() - 1, worldConfig);
      return this.getWorldCreatingFuture(context, worldConfig)
         .thenCompose(
            world -> CompletableFuture.supplyAsync(
               () -> {
                  Vector3i pastePosition = new Vector3i(0, context.getPasteLevelGoal(), 0);
                  Vector3i anchorPosition = pastePosition.clone();
                  editSession.addPrefab(
                     context.getPrefabPaths().getFirst(),
                     new Vector3i(-1, context.getPasteLevelGoal() - 1, -1),
                     new Vector3i(1, context.getPasteLevelGoal() + 1, 1),
                     anchorPosition,
                     pastePosition
                  );
                  Store<EntityStore> store = world.getEntityStore().getStore();
                  WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
                  worldTimeResource.setDayTime(0.5, world, store);
                  world.setBlock(0, context.getPasteLevelGoal(), 0, "Rock_Stone");
                  return world;
               },
               world
            )
         );
   }

   @Nullable
   private CompletableFuture<World> getPrefabLoadingCompletableFuture(
      @Nonnull PrefabEditorCreationContext context,
      @Nonnull PrefabEditSession editSession,
      @Nonnull WorldConfig worldConfig,
      @Nullable PrefabLoadingState loadingState,
      @Nullable Consumer<PrefabLoadingState> progressCallback,
      @Nonnull UUID playerUuid
   ) {
      CompletableFuture[] initializationFutures = new CompletableFuture[context.getPrefabPaths().size()];
      if (loadingState != null) {
         loadingState.setPhase(PrefabLoadingState.Phase.LOADING_PREFABS);
         this.notifyProgress(progressCallback, loadingState);
      }

      for (int i = 0; i < context.getPrefabPaths().size(); i++) {
         Path prefabPath = context.getPrefabPaths().get(i);
         CompletableFuture<IPrefabBuffer> prefabLoadingFuture = this.getPrefabBuffer(context.getEditor(), prefabPath);
         if (prefabLoadingFuture == null) {
            if (loadingState != null) {
               loadingState.addError("server.commands.editprefab.error.prefabLoadFailed", prefabPath.toString());
               this.notifyProgress(progressCallback, loadingState);
            }

            return null;
         }

         initializationFutures[i] = prefabLoadingFuture.thenApply(buffer -> {
            if (loadingState != null) {
               loadingState.onPrefabLoaded(prefabPath);
               this.notifyProgress(progressCallback, loadingState);
            }

            return (IPrefabBuffer)buffer;
         });
      }

      return CompletableFuture.allOf(initializationFutures)
         .thenApply(unused -> {
            if (this.isLoadingCancelled(playerUuid)) {
               return null;
            } else {
               List<IPrefabBuffer> prefabAccessors = new ObjectArrayList(initializationFutures.length);
               int heightOfTallestPrefab = 0;

               for (CompletableFuture initializationFuture : initializationFutures) {
                  IPrefabBuffer prefabAccessor = (IPrefabBuffer)initializationFuture.join();
                  prefabAccessors.add(prefabAccessor);
                  if (context.loadChildPrefabs()) {
                     for (PrefabBuffer.ChildPrefab var18 : prefabAccessor.getChildPrefabs()) {
                        ;
                     }
                  }

                  int prefabHeight = Math.abs(prefabAccessor.getMaxY() - prefabAccessor.getMinY());
                  if (prefabHeight > heightOfTallestPrefab) {
                     heightOfTallestPrefab = prefabHeight;
                  }
               }

               int yLevelToPastePrefabsAt = this.getAmountOfBlocksBelowPrefab(heightOfTallestPrefab, context.getPasteLevelGoal());
               this.applyWorldGenWorldConfig(context, yLevelToPastePrefabsAt, worldConfig);
               if (loadingState != null) {
                  loadingState.setPhase(PrefabLoadingState.Phase.PASTING_PREFABS);
                  this.notifyProgress(progressCallback, loadingState);
               }

               if (this.isLoadingCancelled(playerUuid)) {
                  return null;
               } else {
                  String worldName = this.getWorldName(context);
                  if (Universe.get().getWorld(worldName) != null) {
                     LOGGER.at(Level.WARNING).log("Aborting prefab editor creation for %s: world '%s' already exists", playerUuid, worldName);
                     return null;
                  } else {
                     return new Tri<>(prefabAccessors, yLevelToPastePrefabsAt, this.getWorldCreatingFuture(context, worldConfig).join());
                  }
               }
            }
         })
         .thenCompose(
            passedData -> passedData == null
               ? CompletableFuture.completedFuture(null)
               : CompletableFuture.supplyAsync(
                  () -> {
                     if (this.isLoadingCancelled(playerUuid)) {
                        return null;
                     } else {
                        World world = (World)passedData.getRight();
                        int yLevelToPastePrefabsAt = (Integer)passedData.getMiddle();
                        List<IPrefabBuffer> prefabAccessors = (List<IPrefabBuffer>)passedData.getLeft();
                        if (world != null && world.isAlive()) {
                           Store<EntityStore> store = world.getEntityStore().getStore();
                           int[] rowGroupIndices = this.calculateRowGroups(context, prefabAccessors.size());
                           IntArrayList rowDepths = new IntArrayList();
                           int currentRowGroup = -1;
                           int currentRowIndex = -1;

                           for (int ix = 0; ix < prefabAccessors.size(); ix++) {
                              IPrefabBuffer prefabAccessor = prefabAccessors.get(ix);
                              int rowGroup = rowGroupIndices[ix];
                              if (rowGroup != currentRowGroup) {
                                 currentRowGroup = rowGroup;
                                 currentRowIndex++;
                                 rowDepths.add(0);
                              }

                              int depth;
                              if (context.getStackingAxis().equals(PrefabStackingAxis.X)) {
                                 depth = prefabAccessor.getMaxZ() - prefabAccessor.getMinZ();
                              } else {
                                 depth = prefabAccessor.getMaxX() - prefabAccessor.getMinX();
                              }

                              rowDepths.set(currentRowIndex, Math.max(rowDepths.getInt(currentRowIndex), depth));
                           }

                           int[] rowStarts = new int[rowDepths.size()];
                           int cumulativeDepth = 0;

                           for (int r = 0; r < rowDepths.size(); r++) {
                              rowStarts[r] = cumulativeDepth;
                              cumulativeDepth += rowDepths.getInt(r) + context.getBlocksBetweenEachPrefab() + 1;
                           }

                           currentRowGroup = -1;
                           currentRowIndex = -1;
                           int lineOffset = 0;

                           for (int ixx = 0; ixx < prefabAccessors.size(); ixx++) {
                              if (this.isLoadingCancelled(playerUuid) || !world.isAlive()) {
                                 return null;
                              }

                              IPrefabBuffer prefabAccessorx = prefabAccessors.get(ixx);
                              Path prefabPathx = context.getPrefabPaths().get(ixx);
                              int rowGroupx = rowGroupIndices[ixx];
                              if (rowGroupx != currentRowGroup) {
                                 currentRowGroup = rowGroupx;
                                 currentRowIndex++;
                                 lineOffset = 0;
                              }

                              int rowOffset = rowStarts[currentRowIndex];
                              int prefabXSize = prefabAccessorx.getMaxX() - prefabAccessorx.getMinX();
                              int prefabZSize = prefabAccessorx.getMaxZ() - prefabAccessorx.getMinZ();
                              Vector3i pastePosition;
                              if (context.getAlignment().equals(PrefabAlignment.ZERO)) {
                                 pastePosition = new Vector3i(0, yLevelToPastePrefabsAt, 0);
                                 pastePosition.subtract(
                                    Math.min(prefabAccessorx.getMinX(), 0), prefabAccessorx.getMinY(), Math.min(prefabAccessorx.getMinZ(), 0)
                                 );
                                 if (context.getStackingAxis().equals(PrefabStackingAxis.X)) {
                                    pastePosition.add(lineOffset, 0, rowOffset);
                                    lineOffset += prefabXSize + context.getBlocksBetweenEachPrefab() + 1;
                                 } else {
                                    pastePosition.add(rowOffset, 0, lineOffset);
                                    lineOffset += prefabZSize + context.getBlocksBetweenEachPrefab() + 1;
                                 }
                              } else {
                                 pastePosition = new Vector3i(0, yLevelToPastePrefabsAt - Math.min(prefabAccessorx.getMinY(), 0), 0);
                                 if (context.getStackingAxis().equals(PrefabStackingAxis.X)) {
                                    int xPos = lineOffset + Math.abs(Math.min(prefabAccessorx.getMinX(), 0));
                                    int zPos = rowOffset + Math.abs(Math.min(prefabAccessorx.getMinZ(), 0));
                                    pastePosition.add(xPos, 0, zPos);
                                    lineOffset += prefabXSize + context.getBlocksBetweenEachPrefab() + 1;
                                 } else {
                                    int xPos = rowOffset + Math.abs(Math.min(prefabAccessorx.getMinX(), 0));
                                    int zPos = lineOffset + Math.abs(Math.min(prefabAccessorx.getMinZ(), 0));
                                    pastePosition.add(xPos, 0, zPos);
                                    lineOffset += prefabZSize + context.getBlocksBetweenEachPrefab() + 1;
                                 }
                              }

                              Vector3i minPoint = new Vector3i(
                                 pastePosition.x + prefabAccessorx.getMinX(),
                                 pastePosition.y + prefabAccessorx.getMinY(),
                                 pastePosition.z + prefabAccessorx.getMinZ()
                              );
                              Vector3i maxPoint = new Vector3i(
                                 pastePosition.x + prefabAccessorx.getMaxX(),
                                 pastePosition.y + prefabAccessorx.getMaxY(),
                                 pastePosition.z + prefabAccessorx.getMaxZ()
                              );
                              Vector3i anchorPosition = new Vector3i(
                                 pastePosition.x + prefabAccessorx.getAnchorX(),
                                 pastePosition.y + prefabAccessorx.getAnchorY(),
                                 pastePosition.z + prefabAccessorx.getAnchorZ()
                              );

                              try {
                                 PrefabUtil.paste(
                                    prefabAccessorx,
                                    world,
                                    pastePosition,
                                    Rotation.None,
                                    true,
                                    new FastRandom(),
                                    0,
                                    true,
                                    false,
                                    context.shouldLoadEntities(),
                                    store
                                 );
                                 editSession.addPrefab(prefabPathx, minPoint, maxPoint, anchorPosition, pastePosition.clone());
                                 if (loadingState != null) {
                                    loadingState.onPrefabPasted(prefabPathx);
                                    this.notifyProgress(progressCallback, loadingState);
                                 }
                              } catch (Exception var30) {
                                 if (!this.isLoadingCancelled(playerUuid) && world.isAlive()) {
                                    ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var30)).log("Error pasting prefab: %s", prefabPathx);
                                    if (loadingState != null) {
                                       loadingState.addError(
                                          "server.commands.editprefab.error.pasteFailed", prefabPathx.getFileName().toString() + ": " + var30.getMessage()
                                       );
                                       this.notifyProgress(progressCallback, loadingState);
                                    }

                                    throw var30;
                                 }

                                 return null;
                              }
                           }

                           WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
                           worldTimeResource.setDayTime(0.5, world, store);
                           return world;
                        } else {
                           return null;
                        }
                     }
                  },
                  passedData.getRight()
               )
         );
   }

   @Nonnull
   private int[] calculateRowGroups(@Nonnull PrefabEditorCreationContext context, int prefabCount) {
      int[] rowGroups = new int[prefabCount];
      PrefabRowSplitMode rowSplitMode = context.getRowSplitMode();
      List<Path> prefabPaths = context.getPrefabPaths();
      if (rowSplitMode != PrefabRowSplitMode.NONE && prefabCount != 0) {
         if (rowSplitMode == PrefabRowSplitMode.BY_SPECIFIED_FOLDER) {
            List<String> unprocessedPaths = context.getUnprocessedPrefabPaths();
            Path rootPath = context.getPrefabRootDirectory().getPrefabPath();
            int currentGroup = 0;
            int prefabIndex = 0;

            for (String unprocessedPath : unprocessedPaths) {
               for (Path resolvedPath = rootPath.resolve(unprocessedPath.replace('/', File.separatorChar).replace('\\', File.separatorChar));
                  prefabIndex < prefabCount;
                  prefabIndex++
               ) {
                  Path prefabPath = prefabPaths.get(prefabIndex);
                  if (!prefabPath.startsWith(resolvedPath)
                     && (
                        !unprocessedPath.endsWith("/") && !unprocessedPath.endsWith("\\")
                           ? !prefabPath.equals(resolvedPath)
                           : !prefabPath.startsWith(resolvedPath)
                     )) {
                     break;
                  }

                  rowGroups[prefabIndex] = currentGroup;
               }

               currentGroup++;
            }
         } else if (rowSplitMode == PrefabRowSplitMode.BY_ALL_SUBFOLDERS) {
            Object2ObjectOpenHashMap<Path, Integer> parentDirToGroup = new Object2ObjectOpenHashMap();
            int nextGroup = 0;

            for (int i = 0; i < prefabCount; i++) {
               Path prefabPath = prefabPaths.get(i);
               Path parentDir = prefabPath.getParent();
               if (parentDir != null) {
                  Integer group = (Integer)parentDirToGroup.get(parentDir);
                  if (group == null) {
                     parentDirToGroup.put(parentDir, nextGroup);
                     rowGroups[i] = nextGroup++;
                  } else {
                     rowGroups[i] = group;
                  }
               } else {
                  rowGroups[i] = 0;
               }
            }
         }

         return rowGroups;
      } else {
         return rowGroups;
      }
   }

   private int getAmountOfBlocksBelowPrefab(int prefabHeight, int desiredYLevel) {
      if (desiredYLevel < 0) {
         throw new IllegalArgumentException("Cannot have a negative y level for pasting prefabs");
      } else if (desiredYLevel >= 320) {
         throw new IllegalArgumentException("Cannot paste above or at the world height");
      } else {
         return Math.min(desiredYLevel, 320 - prefabHeight);
      }
   }

   @Nullable
   public CompletableFuture<Void> exitEditSession(
      @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull PlayerRef playerRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      PrefabEditSession prefabEditSession = this.activeEditSessions.get(playerRef.getUuid());
      if (prefabEditSession == null) {
         return null;
      } else {
         prefabEditSession.hidePrefabAnchors(playerRef.getPacketHandler());
         World returnWorld = Universe.get().getWorld(prefabEditSession.getWorldArrivedFrom());
         Transform returnLocation = prefabEditSession.getTransformArrivedFrom();
         if (returnWorld == null || returnLocation == null) {
            LOGGER.at(Level.WARNING)
               .log(
                  "Prefab editor exit fallback triggered for player %s: returnWorld=%s (worldArrivedFrom=%s), returnLocation=%s. Using default world spawn.",
                  playerRef.getUuid(),
                  returnWorld != null ? returnWorld.getName() : "null",
                  prefabEditSession.getWorldArrivedFrom(),
                  returnLocation
               );
            returnWorld = Universe.get().getDefaultWorld();
            returnLocation = returnWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, componentAccessor);
         }

         Teleport teleportComponent = Teleport.createForPlayer(returnWorld, returnLocation);
         return CompletableFuture.runAsync(() -> componentAccessor.putComponent(ref, Teleport.getComponentType(), teleportComponent), world)
            .thenRunAsync(() -> {
               World worldToRemove = Universe.get().getWorld(prefabEditSession.getWorldName());
               if (worldToRemove != null) {
                  Universe.get().removeWorld(prefabEditSession.getWorldName());
               }

               for (PrefabEditingMetadata prefab : prefabEditSession.getLoadedPrefabMetadata().values()) {
                  this.prefabsBeingEdited.remove(prefab.getPrefabPath());
               }

               this.activeEditSessions.remove(playerRef.getUuid());
            });
      }
   }

   @Nonnull
   public CompletableFuture<Void> cleanupCancelledSession(
      @Nonnull UUID playerUuid, @Nonnull String worldName, @Nullable Consumer<PrefabLoadingState> progressCallback
   ) {
      this.cancelLoading(playerUuid);
      PrefabLoadingState loadingState = new PrefabLoadingState();
      loadingState.setPhase(PrefabLoadingState.Phase.CANCELLING);
      this.notifyProgress(progressCallback, loadingState);
      return CompletableFuture.runAsync(() -> {
         World world = Universe.get().getWorld(worldName);
         if (world != null) {
            loadingState.setPhase(PrefabLoadingState.Phase.SHUTTING_DOWN_WORLD);
            this.notifyProgress(progressCallback, loadingState);
            world.getWorldConfig().setDeleteOnRemove(true);
            loadingState.setPhase(PrefabLoadingState.Phase.DELETING_WORLD);
            this.notifyProgress(progressCallback, loadingState);
            Universe.get().removeWorld(worldName);
         }

         PrefabEditSession session = this.activeEditSessions.remove(playerUuid);
         if (session != null) {
            for (PrefabEditingMetadata prefab : session.getLoadedPrefabMetadata().values()) {
               this.prefabsBeingEdited.remove(prefab.getPrefabPath());
            }
         }

         this.inProgressLoading.remove(playerUuid);
         loadingState.setPhase(PrefabLoadingState.Phase.SHUTDOWN_COMPLETE);
         this.notifyProgress(progressCallback, loadingState);
      });
   }

   @Nonnull
   public CompletableFuture<Void> cleanupCancelledSession(@Nonnull UUID playerUuid, @Nonnull String worldName) {
      return this.cleanupCancelledSession(playerUuid, worldName, null);
   }

   @Nullable
   private CompletableFuture<IPrefabBuffer> getPrefabBuffer(@Nonnull CommandSender sender, @Nonnull Path path) {
      if (!Files.exists(path)) {
         sender.sendMessage(Message.translation("server.commands.editprefab.prefabNotFound").param("name", path.toString()));
         return null;
      } else {
         return CompletableFuture.supplyAsync(() -> PrefabBufferUtil.getCached(path));
      }
   }
}
