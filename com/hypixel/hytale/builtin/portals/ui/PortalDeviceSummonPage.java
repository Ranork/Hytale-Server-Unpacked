package com.hypixel.hytale.builtin.portals.ui;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.instances.config.InstanceDiscoveryConfig;
import com.hypixel.hytale.builtin.instances.config.InstanceWorldConfig;
import com.hypixel.hytale.builtin.portals.PortalsPlugin;
import com.hypixel.hytale.builtin.portals.components.PortalDevice;
import com.hypixel.hytale.builtin.portals.components.PortalDeviceConfig;
import com.hypixel.hytale.builtin.portals.integrations.PortalGameplayConfig;
import com.hypixel.hytale.builtin.portals.integrations.PortalRemovalCondition;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.PortalKey;
import com.hypixel.hytale.server.core.asset.type.portalworld.PillTag;
import com.hypixel.hytale.server.core.asset.type.portalworld.PortalDescription;
import com.hypixel.hytale.server.core.asset.type.portalworld.PortalType;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.IndividualSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PortalDeviceSummonPage extends InteractiveCustomUIPage<PortalDeviceSummonPage.Data> {
   private final PortalDeviceConfig config;
   private final Ref<ChunkStore> blockRef;
   @Nullable
   private final ItemStack offeredItemStack;

   public PortalDeviceSummonPage(@Nonnull PlayerRef playerRef, PortalDeviceConfig config, Ref<ChunkStore> blockRef, @Nullable ItemStack offeredItemStack) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PortalDeviceSummonPage.Data.CODEC);
      this.config = config;
      this.blockRef = blockRef;
      this.offeredItemStack = offeredItemStack;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PortalDeviceSummonPage.State state = this.computeState(playerComponent, store);
      if (state != PortalDeviceSummonPage.Error.INVALID_BLOCK) {
         if (state instanceof PortalDeviceSummonPage.CanSpawnPortal canSpawn) {
            commandBuilder.append("Pages/PortalDeviceSummon.ui");
            PortalKey var22 = canSpawn.portalKey();
            PortalType portalType = canSpawn.portalType();
            PortalDescription var23 = portalType.getDescription();
            commandBuilder.set("#Artwork.Background", "Pages/Portals/" + var23.getSplashImageFilename());
            commandBuilder.set("#Title0.TextSpans", var23.getDisplayName());
            commandBuilder.set("#FlavorLabel.TextSpans", var23.getFlavorText());
            updateCustomPills(commandBuilder, portalType);
            String[] objectivesKeys = var23.getObjectivesKeys();
            String[] var25 = var23.getWisdomKeys();
            commandBuilder.set("#Objectives.Visible", objectivesKeys.length > 0);
            commandBuilder.set("#Tips.Visible", var25.length > 0);
            updateBulletList(commandBuilder, "#ObjectivesList", objectivesKeys);
            updateBulletList(commandBuilder, "#TipsList", var25);
            PortalGameplayConfig gameplayConfig = portalType.getGameplayConfig().getPluginConfig().get(PortalGameplayConfig.class);
            long totalTimeLimit = TimeUnit.SECONDS.toMinutes(var22.getTimeLimitSeconds());
            if (portalType.isVoidInvasionEnabled()) {
               long minutesBreach = TimeUnit.SECONDS.toMinutes(gameplayConfig.getVoidEvent().getDurationSeconds());
               long exploMinutes = totalTimeLimit - minutesBreach;
               commandBuilder.set(
                  "#ExplorationTimeText.TextSpans", Message.translation("server.customUI.portalDevice.minutesToExplore").param("time", exploMinutes)
               );
               commandBuilder.set("#BreachTimeBullet.Visible", true);
               commandBuilder.set(
                  "#BreachTimeText.TextSpans", Message.translation("server.customUI.portalDevice.minutesVoidInvasion").param("time", minutesBreach)
               );
            } else {
               commandBuilder.set(
                  "#ExplorationTimeText.TextSpans", Message.translation("server.customUI.portalDevice.durationMins").param("time", totalTimeLimit)
               );
            }

            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SummonButton", EventData.of("Action", "SummonActivated"), false);
            eventBuilder.addEventBinding(CustomUIEventBindingType.MouseEntered, "#SummonButton", EventData.of("Action", "SummonMouseEntered"), false);
            eventBuilder.addEventBinding(CustomUIEventBindingType.MouseExited, "#SummonButton", EventData.of("Action", "SummonMouseExited"), false);
         } else {
            commandBuilder.append("Pages/PortalDeviceError.ui");
            if (state == PortalDeviceSummonPage.Error.NOTHING_OFFERED || state == PortalDeviceSummonPage.Error.NOT_A_PORTAL_KEY) {
               commandBuilder.set("#UsageErrorTitle.Text", Message.translation("server.customUI.portalDevice.needPortalKey"));
               commandBuilder.set("#UsageErrorLabel.Text", Message.translation("server.customUI.portalDevice.nothingHeld"));
            } else if (state == PortalDeviceSummonPage.Error.PORTAL_INSIDE_PORTAL) {
               commandBuilder.set("#UsageErrorLabel.Text", Message.translation("server.customUI.portalDevice.portalInsidePortal"));
            } else if (state == PortalDeviceSummonPage.Error.MAX_ACTIVE_PORTALS) {
               commandBuilder.set("#UsageErrorLabel.Text", Message.translation("server.customUI.portalDevice.maxFragments").param("max", 4));
            } else if (state instanceof PortalDeviceSummonPage.InstanceKeyNotFound(String wisdomKeys)) {
               commandBuilder.set(
                  "#UsageErrorLabel.Text", "The instance id '" + wisdomKeys + "' does not exist, this is a developer error with the portaltype."
               );
            } else if (state instanceof PortalDeviceSummonPage.PortalTypeNotFound(String var24)) {
               commandBuilder.set("#UsageErrorLabel.Text", "The portaltype id '" + var24 + "' does not exist, this is a developer error with the portal key.");
            } else if (state == PortalDeviceSummonPage.Error.BOTCHED_GAMEPLAY_CONFIG) {
               commandBuilder.set(
                  "#UsageErrorLabel.Text",
                  "The gameplay config set on the PortalType set in the key does not have a Portal plugin configuration, this is a developer error."
               );
            } else {
               commandBuilder.set("#UsageErrorLabel.Text", Message.translation("server.customUI.portalDevice.unknownError").param("state", state.toString()));
            }
         }
      }
   }

   private static void updateCustomPills(@Nonnull UICommandBuilder commandBuilder, @Nonnull PortalType portalType) {
      List<PillTag> pills = portalType.getDescription().getPillTags();

      for (int i = 0; i < pills.size(); i++) {
         PillTag pillTag = pills.get(i);
         String child = "#Pills[" + i + "]";
         commandBuilder.append("#Pills", "Pages/Portals/Pill.ui");
         commandBuilder.set(child + ".Background.Color", ColorParseUtil.colorToHexString(pillTag.getColor()));
         commandBuilder.set(child + " #Label.TextSpans", pillTag.getMessage());
      }
   }

   private static void updateBulletList(@Nonnull UICommandBuilder commandBuilder, @Nonnull String selector, @Nonnull String[] messageKeys) {
      for (int i = 0; i < messageKeys.length; i++) {
         String messageKey = messageKeys[i];
         String child = selector + "[" + i + "]";
         commandBuilder.append(selector, "Pages/Portals/BulletPoint.ui");
         commandBuilder.set(child + " #Label.TextSpans", Message.translation(messageKey));
      }
   }

   @Nonnull
   public static Message createDescription(@Nonnull PortalType portalType, int timeLimitSeconds) {
      Message message = Message.empty();
      Message durationMessage = formatDurationCrudely(timeLimitSeconds);
      message.insert(Message.translation("server.customUI.portalDevice.timeLimit").param("limit", durationMessage.color("#f9cb13")));
      return message;
   }

   @Nonnull
   private static Message formatDurationCrudely(int seconds) {
      if (seconds < 0) {
         return Message.translation("server.customUI.portalDevice.durationUnlimited");
      } else if (seconds >= 120) {
         int minutes = seconds / 60;
         return Message.translation("server.customUI.portalDevice.durationMinutes").param("duration", minutes);
      } else {
         return Message.translation("server.customUI.portalDevice.durationSeconds").param("duration", seconds);
      }
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PortalDeviceSummonPage.Data data) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (this.computeState(playerComponent, store) instanceof PortalDeviceSummonPage.CanSpawnPortal canSpawn) {
         if ("SummonMouseEntered".equals(data.action)) {
            UICommandBuilder commandBuilder = new UICommandBuilder();
            commandBuilder.set("#Vignette.Visible", true);
            this.sendUpdate(commandBuilder, null, false);
         } else if ("SummonMouseExited".equals(data.action)) {
            UICommandBuilder commandBuilder = new UICommandBuilder();
            commandBuilder.set("#Vignette.Visible", false);
            this.sendUpdate(commandBuilder, null, false);
         } else {
            playerComponent.getPageManager().setPage(ref, store, Page.None);
            World originWorld = store.getExternalData().getWorld();
            int index = canSpawn.blockState().getIndex();
            int x = ChunkUtil.xFromBlockInColumn(index);
            int y = ChunkUtil.yFromBlockInColumn(index);
            int z = ChunkUtil.zFromBlockInColumn(index);
            WorldChunk worldChunk = canSpawn.worldChunk();
            PortalKey portalKey = canSpawn.portalKey();
            PortalDevice portalDevice = canSpawn.portalDevice();
            BlockType blockType = worldChunk.getBlockType(x, y, z);
            if (blockType == portalDevice.getBaseBlockType()) {
               if (this.config.areBlockStatesValid(blockType)) {
                  int rotation = worldChunk.getRotationIndex(x, y, z);
                  BlockType spawningType = blockType.getBlockForState(this.config.getSpawningState());
                  BlockType onType = blockType.getBlockForState(this.config.getOnState());
                  BlockType offType = blockType.getBlockForState(this.config.getOffState());
                  int setting = 6;
                  worldChunk.setBlock(x, y, z, BlockType.getAssetMap().getIndex(spawningType.getId()), spawningType, rotation, 0, 6);
                  double worldX = ChunkUtil.worldCoordFromLocalCoord(worldChunk.getX(), x) + 0.5;
                  double worldY = y + 0.5;
                  double worldZ = ChunkUtil.worldCoordFromLocalCoord(worldChunk.getZ(), z) + 0.5;
                  if (spawningType.getInteractionSoundEventIndex() != 0) {
                     SoundUtil.playSoundEvent3d(spawningType.getInteractionSoundEventIndex(), SoundCategory.SFX, worldX, worldY, worldZ, store);
                  }

                  decrementItemInHand(playerComponent.getInventory(), 1);
                  Transform transform = new Transform(x + 0.5, y + 1.0, z + 0.5);
                  UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

                  assert uuidComponent != null;

                  PortalType portalType = canSpawn.portalType;
                  UUID playerUUID = uuidComponent.getUuid();
                  PortalGameplayConfig gameplayConfig = canSpawn.portalGameplayConfig;
                  InstancesPlugin.get()
                     .spawnInstance(portalType.getInstanceId(), originWorld, transform)
                     .thenCompose(spawnedWorld -> {
                        WorldConfig worldConfig = spawnedWorld.getWorldConfig();
                        worldConfig.setDeleteOnUniverseStart(true);
                        worldConfig.setDeleteOnRemove(true);
                        worldConfig.setGameplayConfig(portalType.getGameplayConfigId());
                        InstanceWorldConfig instanceConfig = InstanceWorldConfig.ensureAndGet(worldConfig);
                        if (instanceConfig.getDiscovery() == null) {
                           InstanceDiscoveryConfig discoveryConfig = new InstanceDiscoveryConfig();
                           discoveryConfig.setTitleKey(portalType.getDescription().getDisplayNameKey());
                           discoveryConfig.setSubtitleKey("server.portals.discoverySubtitle");
                           discoveryConfig.setDisplay(true);
                           discoveryConfig.setAlwaysDisplay(true);
                           instanceConfig.setDiscovery(discoveryConfig);
                        }

                        PortalRemovalCondition portalRemoval = new PortalRemovalCondition(portalKey.getTimeLimitSeconds());
                        instanceConfig.setRemovalConditions(portalRemoval);
                        PortalWorld portalWorld = spawnedWorld.getEntityStore().getStore().getResource(PortalWorld.getResourceType());
                        portalWorld.init(portalType, portalKey.getTimeLimitSeconds(), portalRemoval, gameplayConfig);
                        String returnBlockType = portalDevice.getConfig().getReturnBlock();
                        if (returnBlockType == null) {
                           throw new RuntimeException("Return block type on PortalDevice is misconfigured");
                        } else {
                           return spawnReturnPortal(spawnedWorld, portalWorld, playerUUID, returnBlockType);
                        }
                     })
                     .thenAcceptAsync(spawnedWorld -> {
                        portalDevice.setDestinationWorld(spawnedWorld);
                        worldChunk.setBlock(x, y, z, BlockType.getAssetMap().getIndex(onType.getId()), onType, rotation, 0, 6);
                     }, originWorld)
                     .exceptionallyAsync(
                        t -> {
                           playerComponent.sendMessage(Message.translation("server.portals.device.internalErrorSpawning"));
                           ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(t))
                              .log("Error creating instance for Portal Device " + portalKey, t);
                           worldChunk.setBlock(x, y, z, BlockType.getAssetMap().getIndex(offType.getId()), offType, rotation, 0, 6);
                           return null;
                        },
                        originWorld
                     );
               }
            }
         }
      }
   }

   @Nonnull
   private static CompletableFuture<World> spawnReturnPortal(
      @Nonnull World world, @Nonnull PortalWorld portalWorld, @Nonnull UUID sampleUuid, @Nonnull String portalBlockType
   ) {
      return getSpawnTransform(world, sampleUuid)
         .thenCompose(
            spawnTransform -> {
               Vector3d spawnPoint = spawnTransform.getPosition();
               return world.getChunkAsync(ChunkUtil.indexChunkFromBlock((int)spawnPoint.x, (int)spawnPoint.z))
                  .thenAccept(
                     chunk -> {
                        for (int dy = 0; dy < 3; dy++) {
                           for (int dx = -1; dx <= 1; dx++) {
                              for (int dz = -1; dz <= 1; dz++) {
                                 chunk.setBlock((int)spawnPoint.x + dx, (int)spawnPoint.y + dy, (int)spawnPoint.z + dz, BlockType.EMPTY);
                              }
                           }
                        }

                        chunk.setBlock((int)spawnPoint.x, (int)spawnPoint.y, (int)spawnPoint.z, portalBlockType);
                        portalWorld.setSpawnPoint(spawnTransform);
                        world.getWorldConfig().setSpawnProvider(new IndividualSpawnProvider(spawnTransform));
                        HytaleLogger.getLogger()
                           .at(Level.INFO)
                           .log(
                              "Spawned return portal for " + world.getName() + " at " + (int)spawnPoint.x + ", " + (int)spawnPoint.y + ", " + (int)spawnPoint.z
                           );
                     }
                  )
                  .thenApply(nothing -> world);
            }
         );
   }

   @Nonnull
   private static CompletableFuture<Transform> getSpawnTransform(@Nonnull World world, @Nonnull UUID sampleUuid) {
      return CompletableFuture.supplyAsync(() -> {
         List<Vector3d> hintedSpawns = fetchHintedSpawns(world, sampleUuid);
         return PortalSpawnFinder.computeSpawnTransform(world, hintedSpawns);
      }, world);
   }

   private static List<Vector3d> fetchHintedSpawns(World world, UUID sampleUuid) {
      ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();
      if (spawnProvider == null) {
         return Collections.emptyList();
      } else {
         Transform[] spawnTransforms = spawnProvider.getSpawnPoints();
         if (spawnTransforms != null && spawnTransforms.length > 0) {
            return Arrays.stream(spawnTransforms).map(Transform::getPosition).toList();
         } else {
            Transform spawnPoint = spawnProvider.getSpawnPoint(world, sampleUuid);
            return spawnPoint != null ? Collections.singletonList(spawnPoint.getPosition()) : Collections.emptyList();
         }
      }
   }

   @Nonnull
   private PortalDeviceSummonPage.State computeState(@Nonnull Player player, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.blockRef.isValid()) {
         return PortalDeviceSummonPage.Error.INVALID_BLOCK;
      } else {
         int activeFragments = PortalsPlugin.getInstance().countActiveFragments();
         if (activeFragments >= 4) {
            return PortalDeviceSummonPage.Error.MAX_ACTIVE_PORTALS;
         } else {
            Store<ChunkStore> chunkStore = this.blockRef.getStore();
            BlockModule.BlockStateInfo blockStateInfo = chunkStore.getComponent(this.blockRef, BlockModule.BlockStateInfo.getComponentType());
            PortalDevice portalDevice = chunkStore.getComponent(this.blockRef, PortalDevice.getComponentType());
            if (blockStateInfo != null && portalDevice != null) {
               Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
               if (!chunkRef.isValid()) {
                  return PortalDeviceSummonPage.Error.INVALID_BLOCK;
               } else {
                  WorldChunk worldChunk = chunkStore.getComponent(chunkRef, WorldChunk.getComponentType());
                  if (worldChunk == null) {
                     return PortalDeviceSummonPage.Error.INVALID_BLOCK;
                  } else {
                     World existingDestinationWorld = portalDevice.getDestinationWorld();
                     if (existingDestinationWorld != null) {
                        return PortalDeviceSummonPage.Error.INVALID_DESTINATION;
                     } else if (this.offeredItemStack == null) {
                        return PortalDeviceSummonPage.Error.NOTHING_OFFERED;
                     } else {
                        ItemStack inHand = player.getInventory().getItemInHand();
                        if (!this.offeredItemStack.equals(inHand)) {
                           return PortalDeviceSummonPage.Error.OFFERED_IS_NOT_HELD;
                        } else {
                           Item offeredItem = this.offeredItemStack.getItem();
                           PortalKey portalKey = offeredItem.getPortalKey();
                           if (portalKey == null) {
                              return PortalDeviceSummonPage.Error.NOT_A_PORTAL_KEY;
                           } else {
                              String portalTypeId = portalKey.getPortalTypeId();
                              PortalType portalType = PortalType.getAssetMap().getAsset(portalTypeId);
                              if (portalType == null) {
                                 return new PortalDeviceSummonPage.PortalTypeNotFound(portalTypeId);
                              } else {
                                 String instanceId = portalType.getInstanceId();
                                 InstancesPlugin.get();
                                 boolean instanceExists = InstancesPlugin.doesInstanceAssetExist(instanceId);
                                 if (!instanceExists) {
                                    return new PortalDeviceSummonPage.InstanceKeyNotFound(instanceId);
                                 } else {
                                    PortalWorld insidePortalWorld = componentAccessor.getResource(PortalWorld.getResourceType());
                                    if (insidePortalWorld.exists()) {
                                       return PortalDeviceSummonPage.Error.PORTAL_INSIDE_PORTAL;
                                    } else {
                                       String gameplayConfigId = portalType.getGameplayConfigId();
                                       GameplayConfig gameplayConfig = GameplayConfig.getAssetMap().getAsset(gameplayConfigId);
                                       PortalGameplayConfig portalGameplayConfig = gameplayConfig == null
                                          ? null
                                          : gameplayConfig.getPluginConfig().get(PortalGameplayConfig.class);
                                       return (PortalDeviceSummonPage.State)(portalGameplayConfig == null
                                          ? PortalDeviceSummonPage.Error.BOTCHED_GAMEPLAY_CONFIG
                                          : new PortalDeviceSummonPage.CanSpawnPortal(
                                             portalKey, portalType, worldChunk, blockStateInfo, portalDevice, portalGameplayConfig
                                          ));
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } else {
               return PortalDeviceSummonPage.Error.INVALID_BLOCK;
            }
         }
      }
   }

   private static void decrementItemInHand(@Nonnull Inventory inventory, int amount) {
      if (!inventory.usingToolsItem()) {
         byte hotbarSlot = inventory.getActiveHotbarSlot();
         if (hotbarSlot != -1) {
            ItemContainer hotbar = inventory.getHotbar();
            ItemStack inHand = hotbar.getItemStack(hotbarSlot);
            if (inHand != null) {
               hotbar.removeItemStackFromSlot(hotbarSlot, inHand, amount, false, true);
            }
         }
      }
   }

   private record CanSpawnPortal(
      PortalKey portalKey,
      PortalType portalType,
      WorldChunk worldChunk,
      BlockModule.BlockStateInfo blockState,
      PortalDevice portalDevice,
      PortalGameplayConfig portalGameplayConfig
   ) implements PortalDeviceSummonPage.State {
   }

   protected static class Data {
      @Nonnull
      private static final String KEY_ACTION = "Action";
      @Nonnull
      public static final BuilderCodec<PortalDeviceSummonPage.Data> CODEC = BuilderCodec.builder(
            PortalDeviceSummonPage.Data.class, PortalDeviceSummonPage.Data::new
         )
         .append(new KeyedCodec<>("Action", Codec.STRING), (entry, s) -> entry.action = s, entry -> entry.action)
         .add()
         .build();
      private String action;
   }

   private static enum Error implements PortalDeviceSummonPage.State {
      NOTHING_OFFERED,
      OFFERED_IS_NOT_HELD,
      NOT_A_PORTAL_KEY,
      INVALID_BLOCK,
      INVALID_DESTINATION,
      PORTAL_INSIDE_PORTAL,
      BOTCHED_GAMEPLAY_CONFIG,
      MAX_ACTIVE_PORTALS;
   }

   private record InstanceKeyNotFound(String instanceId) implements PortalDeviceSummonPage.State {
   }

   private record PortalTypeNotFound(String portalTypeId) implements PortalDeviceSummonPage.State {
   }

   private sealed interface State
      permits PortalDeviceSummonPage.CanSpawnPortal,
      PortalDeviceSummonPage.Error,
      PortalDeviceSummonPage.InstanceKeyNotFound,
      PortalDeviceSummonPage.PortalTypeNotFound {
   }
}
