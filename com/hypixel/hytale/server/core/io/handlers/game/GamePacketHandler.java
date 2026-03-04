package com.hypixel.hytale.server.core.io.handlers.game;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockRotation;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.io.netty.ProtocolUtil;
import com.hypixel.hytale.protocol.packets.camera.RequestFlyCameraMode;
import com.hypixel.hytale.protocol.packets.camera.SetFlyCameraMode;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.protocol.packets.connection.Pong;
import com.hypixel.hytale.protocol.packets.entities.MountMovement;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.protocol.packets.interface_.ChatMessage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageEvent;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.protocol.packets.interface_.SetPage;
import com.hypixel.hytale.protocol.packets.interface_.UpdateLanguage;
import com.hypixel.hytale.protocol.packets.machinima.RequestMachinimaActorModel;
import com.hypixel.hytale.protocol.packets.machinima.SetMachinimaActorModel;
import com.hypixel.hytale.protocol.packets.machinima.UpdateMachinimaScene;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.protocol.packets.player.ClientPlaceBlock;
import com.hypixel.hytale.protocol.packets.player.ClientReady;
import com.hypixel.hytale.protocol.packets.player.MouseInteraction;
import com.hypixel.hytale.protocol.packets.player.RemoveMapMarker;
import com.hypixel.hytale.protocol.packets.player.SyncPlayerPreferences;
import com.hypixel.hytale.protocol.packets.serveraccess.SetServerAccess;
import com.hypixel.hytale.protocol.packets.serveraccess.UpdateServerAccess;
import com.hypixel.hytale.protocol.packets.setup.RequestAssets;
import com.hypixel.hytale.protocol.packets.setup.ViewRadius;
import com.hypixel.hytale.protocol.packets.window.ClientOpenWindow;
import com.hypixel.hytale.protocol.packets.window.CloseWindow;
import com.hypixel.hytale.protocol.packets.window.SendWindowAction;
import com.hypixel.hytale.protocol.packets.window.UpdateWindow;
import com.hypixel.hytale.protocol.packets.world.SetPaused;
import com.hypixel.hytale.protocol.packets.worldmap.CreateUserMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.TeleportToWorldMapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.TeleportToWorldMapPosition;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMapVisible;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ValidatedWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.io.handlers.GenericPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.IWorldPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerCreativeSettings;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.modules.interaction.BlockPlaceUtils;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.utils.MapMarkerUtils;
import com.hypixel.hytale.server.core.util.MessageUtil;
import com.hypixel.hytale.server.core.util.PositionUtil;
import com.hypixel.hytale.server.core.util.ValidateUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class GamePacketHandler extends GenericPacketHandler implements IPacketHandler {
   private static final double RELATIVE_POSITION_DELTA_SCALE = 10000.0;
   private PlayerRef playerRef;
   @Deprecated
   private Player playerComponent;
   @Nonnull
   private final Deque<SyncInteractionChain> interactionPacketQueue = new ConcurrentLinkedDeque<>();

   public GamePacketHandler(@Nonnull Channel channel, @Nonnull ProtocolVersion protocolVersion, @Nonnull PlayerAuthentication auth) {
      super(channel, protocolVersion);
      this.auth = auth;
      ServerManager.get().populateSubPacketHandlers(this);
      this.registerHandlers();
   }

   @Nonnull
   public Deque<SyncInteractionChain> getInteractionPacketQueue() {
      return this.interactionPacketQueue;
   }

   @Nonnull
   @Override
   public PlayerRef getPlayerRef() {
      return this.playerRef;
   }

   public void setPlayerRef(@Nonnull PlayerRef playerRef, @Nonnull Player playerComponent) {
      this.playerRef = playerRef;
      this.playerComponent = playerComponent;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Playing("
         + NettyUtil.formatRemoteAddress(this.getChannel())
         + "), "
         + (this.playerRef != null ? this.playerRef.getUuid() + ", " + this.playerRef.getUsername() : "null player")
         + "}";
   }

   @Override
   protected void registered0(PacketHandler oldHandler) {
      HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
      this.enterStage("play", timeouts.getPlay());
   }

   protected void registerHandlers() {
      this.registerHandler(1, p -> this.handle((Disconnect)p));
      this.registerHandler(3, p -> this.handlePong((Pong)p));
      this.registerHandler(108, p -> this.handle((ClientMovement)p));
      this.registerHandler(211, p -> this.handle((ChatMessage)p));
      this.registerHandler(23, p -> this.handle((RequestAssets)p));
      this.registerHandler(219, p -> this.handle((CustomPageEvent)p));
      IWorldPacketHandler.registerHandler(this, 32, this::handleViewRadius);
      IWorldPacketHandler.registerHandler(this, 232, this::handleUpdateLanguage);
      IWorldPacketHandler.registerHandler(this, 111, this::handleMouseInteraction);
      this.registerHandler(251, p -> this.handle((UpdateServerAccess)p));
      this.registerHandler(252, p -> this.handle((SetServerAccess)p));
      IWorldPacketHandler.registerHandler(this, 204, this::handleClientOpenWindow);
      IWorldPacketHandler.registerHandler(this, 203, this::handleSendWindowAction);
      IWorldPacketHandler.registerHandler(this, 202, this::handleCloseWindow);
      this.registerHandler(260, p -> this.handle((RequestMachinimaActorModel)p));
      IWorldPacketHandler.registerHandler(this, 262, this::handleUpdateMachinimaScene);
      this.registerHandler(105, p -> this.handle((ClientReady)p));
      IWorldPacketHandler.registerHandler(this, 166, this::handleMountMovement);
      IWorldPacketHandler.registerHandler(this, 116, this::handleSyncPlayerPreferences);
      IWorldPacketHandler.registerHandler(this, 117, this::handleClientPlaceBlock);
      IWorldPacketHandler.registerHandler(this, 119, this::handleRemoveMapMarker);
      IWorldPacketHandler.registerHandler(this, 243, this::handleUpdateWorldMapVisible);
      IWorldPacketHandler.registerHandler(this, 244, this::handleTeleportToWorldMapMarker);
      IWorldPacketHandler.registerHandler(this, 245, this::handleTeleportToWorldMapPosition);
      IWorldPacketHandler.registerHandler(this, 246, this::handleCreateUserMarker);
      this.registerHandler(290, p -> this.handle((SyncInteractionChains)p));
      IWorldPacketHandler.registerHandler(this, 158, this::handleSetPaused);
      IWorldPacketHandler.registerHandler(this, 282, this::handleRequestFlyCameraMode);
      this.packetHandlers.forEach(SubPacketHandler::registerHandlers);
   }

   @Override
   public void closed(ChannelHandlerContext ctx) {
      super.closed(ctx);
      NetworkChannel streamChannel = (NetworkChannel)ctx.channel().attr(ProtocolUtil.STREAM_CHANNEL_KEY).get();
      if (streamChannel == null || streamChannel == NetworkChannel.Default) {
         Universe.get().removePlayer(this.playerRef);
      }
   }

   @Override
   public void disconnect(@Nonnull String message) {
      this.disconnectReason.setServerDisconnectReason(message);
      if (this.playerRef != null) {
         HytaleLogger.getLogger()
            .at(Level.INFO)
            .log(
               "Disconnecting %s at %s (SNI: %s) with the message: %s",
               this.playerRef.getUsername(),
               NettyUtil.formatRemoteAddress(this.getChannel()),
               this.getSniHostname(),
               message
            );
         this.disconnect0(message);
         Universe.get().removePlayer(this.playerRef);
      } else {
         super.disconnect(message);
      }
   }

   public void handle(@Nonnull Disconnect packet) {
      this.disconnectReason.setClientDisconnectType(packet.type);
      HytaleLogger.getLogger()
         .at(Level.INFO)
         .log(
            "%s - %s at %s left with reason: %s - %s",
            this.playerRef.getUuid(),
            this.playerRef.getUsername(),
            NettyUtil.formatRemoteAddress(this.getChannel()),
            packet.type.name(),
            packet.reason
         );
      ProtocolUtil.closeApplicationConnection(this.getChannel());
   }

   public void handleMouseInteraction(
      @Nonnull MouseInteraction packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      InteractionModule.get().doMouseInteraction(ref, store, packet, playerComponent, playerRef);
   }

   public void handle(@Nonnull ClientMovement packet) {
      if (packet.absolutePosition != null && !ValidateUtil.isSafePosition(packet.absolutePosition)) {
         this.disconnect("Sent impossible position data!");
      } else if ((packet.bodyOrientation == null || ValidateUtil.isSafeDirection(packet.bodyOrientation))
         && (packet.lookOrientation == null || ValidateUtil.isSafeDirection(packet.lookOrientation))) {
         Ref<EntityStore> ref = this.playerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(
               () -> {
                  if (ref.isValid()) {
                     Player playerComponent = store.getComponent(ref, Player.getComponentType());

                     assert playerComponent != null;

                     if (!playerComponent.isWaitingForClientReady()) {
                        PlayerInput playerInputComponent = store.getComponent(ref, PlayerInput.getComponentType());
                        if (playerInputComponent != null) {
                           if (packet.movementStates != null) {
                              playerInputComponent.queue(new PlayerInput.SetMovementStates(packet.movementStates));
                           }

                           if (packet.velocity != null) {
                              playerInputComponent.queue(new PlayerInput.SetClientVelocity(packet.velocity));
                           }

                           PendingTeleport pendingTeleport = store.getComponent(ref, PendingTeleport.getComponentType());
                           if (pendingTeleport != null) {
                              if (packet.teleportAck == null) {
                                 return;
                              }

                              switch (pendingTeleport.validate(packet.teleportAck.teleportId, packet.absolutePosition)) {
                                 case OK:
                                 default:
                                    if (!pendingTeleport.isEmpty()) {
                                       return;
                                    }

                                    store.removeComponent(ref, PendingTeleport.getComponentType());
                                    break;
                                 case INVALID_ID:
                                    this.disconnect("Incorrect teleportId");
                                    return;
                                 case INVALID_POSITION:
                                    this.disconnect("Invalid teleport");
                                    return;
                              }
                           }

                           if (packet.mountedTo != 0) {
                              if (packet.mountedTo != playerInputComponent.getMountId()) {
                                 return;
                              }

                              if (packet.riderMovementStates != null) {
                                 playerInputComponent.queue(new PlayerInput.SetRiderMovementStates(packet.riderMovementStates));
                              }
                           }

                           if (packet.bodyOrientation != null) {
                              playerInputComponent.queue(new PlayerInput.SetBody(packet.bodyOrientation));
                           }

                           if (packet.lookOrientation != null) {
                              playerInputComponent.queue(new PlayerInput.SetHead(packet.lookOrientation));
                           }

                           if (packet.wishMovement != null) {
                              playerInputComponent.queue(new PlayerInput.WishMovement(packet.wishMovement.x, packet.wishMovement.y, packet.wishMovement.z));
                           }

                           if (packet.absolutePosition != null) {
                              playerInputComponent.queue(
                                 new PlayerInput.AbsoluteMovement(packet.absolutePosition.x, packet.absolutePosition.y, packet.absolutePosition.z)
                              );
                           } else if (packet.relativePosition != null
                              && (
                                 packet.relativePosition.x != 0
                                    || packet.relativePosition.y != 0
                                    || packet.relativePosition.z != 0
                                    || packet.movementStates != null
                              )) {
                              playerInputComponent.queue(
                                 new PlayerInput.RelativeMovement(
                                    packet.relativePosition.x / 10000.0, packet.relativePosition.y / 10000.0, packet.relativePosition.z / 10000.0
                                 )
                              );
                           }
                        }
                     }
                  }
               }
            );
         }
      } else {
         this.disconnect("Sent impossible orientation data!");
      }
   }

   public void handle(@Nonnull ChatMessage packet) {
      if (packet.message != null && !packet.message.isEmpty()) {
         String message = packet.message;
         char firstChar = message.charAt(0);
         if (firstChar == '/') {
            CommandManager.get().handleCommand(this.playerComponent, message.substring(1));
         } else if (firstChar == '.') {
            this.playerRef.sendMessage(Message.translation("server.io.gamepackethandler.localCommandDenied").param("msg", message));
         } else {
            Ref<EntityStore> ref = this.playerRef.getReference();
            if (ref == null || !ref.isValid()) {
               return;
            }

            UUID playerUUID = this.playerRef.getUuid();
            List<PlayerRef> targetPlayerRefs = new ObjectArrayList(Universe.get().getPlayers());
            targetPlayerRefs.removeIf(targetPlayerRef -> targetPlayerRef.getHiddenPlayersManager().isPlayerHidden(playerUUID));
            HytaleServer.get()
               .getEventBus()
               .<String, PlayerChatEvent>dispatchForAsync(PlayerChatEvent.class)
               .dispatch(new PlayerChatEvent(this.playerRef, targetPlayerRefs, message))
               .whenComplete(
                  (playerChatEvent, throwable) -> {
                     if (throwable != null) {
                        ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(throwable))
                           .log("An error occurred while dispatching PlayerChatEvent for player %s", this.playerRef.getUsername());
                     } else if (!playerChatEvent.isCancelled()) {
                        Message sentMessage = playerChatEvent.getFormatter().format(this.playerRef, playerChatEvent.getContent());
                        HytaleLogger.getLogger().at(Level.INFO).log(MessageUtil.toAnsiString(sentMessage).toAnsi(ConsoleModule.get().getTerminal()));

                        for (PlayerRef targetPlayerRef : playerChatEvent.getTargets()) {
                           targetPlayerRef.sendMessage(sentMessage);
                        }
                     }
                  }
               );
         }
      } else {
         this.disconnect("Invalid chat message packet! Message was empty.");
      }
   }

   public void handle(@Nonnull RequestAssets packet) {
      CommonAssetModule.get().sendAssetsToPlayer(this, packet.assets, true);
   }

   public void handle(@Nonnull CustomPageEvent packet) {
      Ref<EntityStore> ref = this.playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(() -> {
            if (ref.isValid()) {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               PageManager pageManager = playerComponent.getPageManager();
               pageManager.handleEvent(ref, store, packet);
            }
         });
      } else {
         this.playerRef.getPacketHandler().writeNoCache(new SetPage(Page.None, true));
      }
   }

   public void handleViewRadius(
      @Nonnull ViewRadius packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      EntityTrackerSystems.EntityViewer entityViewerComponent = store.getComponent(ref, EntityTrackerSystems.EntityViewer.getComponentType());

      assert entityViewerComponent != null;

      int viewRadiusChunks = MathUtil.ceil(packet.value / 32.0F);
      playerComponent.setClientViewRadius(viewRadiusChunks);
      entityViewerComponent.viewRadiusBlocks = playerComponent.getViewRadius() * 32;
   }

   public void handleUpdateLanguage(
      @Nonnull UpdateLanguage packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      playerRef.setLanguage(packet.language);
      I18nModule.get().sendTranslations(this, packet.language);
   }

   protected void handleClientOpenWindow(
      @Nonnull ClientOpenWindow packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Supplier<? extends Window> supplier = Window.CLIENT_REQUESTABLE_WINDOW_TYPES.get(packet.type);
      if (supplier == null) {
         throw new RuntimeException("Unable to process ClientOpenWindow packet. Window type is not supported!");
      } else {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         UpdateWindow updateWindowPacket = playerComponent.getWindowManager().clientOpenWindow(ref, supplier.get(), store);
         if (updateWindowPacket != null) {
            this.writeNoCache(updateWindowPacket);
         }
      }
   }

   public void handleSendWindowAction(
      @Nonnull SendWindowAction packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      Window window = playerComponent.getWindowManager().getWindow(packet.id);
      if (window != null) {
         if (window instanceof ValidatedWindow validatedWindow && !validatedWindow.validate(ref, store)) {
            window.close(ref, store);
         } else {
            window.handleAction(ref, store, packet.action);
         }
      }
   }

   public void handleSyncPlayerPreferences(
      @Nonnull SyncPlayerPreferences packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      ComponentType<EntityStore, PlayerSettings> componentType = EntityModule.get().getPlayerSettingsComponentType();
      store.putComponent(
         ref,
         componentType,
         new PlayerSettings(
            packet.showEntityMarkers,
            packet.armorItemsPreferredPickupLocation,
            packet.weaponAndToolItemsPreferredPickupLocation,
            packet.usableItemsItemsPreferredPickupLocation,
            packet.solidBlockItemsPreferredPickupLocation,
            packet.miscItemsPreferredPickupLocation,
            new PlayerCreativeSettings(packet.allowNPCDetection, packet.respondToHit),
            packet.hideHelmet,
            packet.hideCuirass,
            packet.hideGauntlets,
            packet.hidePants
         )
      );
      store.getComponent(ref, Player.getComponentType()).invalidateEquipmentNetwork();
   }

   public void handleClientPlaceBlock(
      @Nonnull ClientPlaceBlock packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      Inventory inventory = playerComponent.getInventory();
      Vector3i targetBlock = new Vector3i(packet.position.x, packet.position.y, packet.position.z);
      BlockRotation blockRotation = new BlockRotation(packet.rotation.rotationYaw, packet.rotation.rotationPitch, packet.rotation.rotationRoll);
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
      Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
      Ref<ChunkStore> chunkReference = chunkStore.getExternalData().getChunkReference(chunkIndex);
      if (chunkReference != null) {
         BlockChunk blockChunk = chunkStore.getComponent(chunkReference, BlockChunk.getComponentType());
         if (blockChunk != null) {
            BlockSection section = blockChunk.getSectionAtBlockY(targetBlock.y);
            if (section != null) {
               if (transformComponent != null && playerComponent.getGameMode() != GameMode.Creative) {
                  Vector3d position = transformComponent.getPosition();
                  Vector3d blockCenter = new Vector3d(targetBlock.x + 0.5, targetBlock.y + 0.5, targetBlock.z + 0.5);
                  if (position.distanceSquaredTo(blockCenter) > 49.0) {
                     section.invalidateBlock(targetBlock.x, targetBlock.y, targetBlock.z);
                     return;
                  }
               }

               ItemStack itemInHand = playerComponent.getInventory().getItemInHand();
               if (itemInHand == null) {
                  section.invalidateBlock(targetBlock.x, targetBlock.y, targetBlock.z);
               } else {
                  String heldBlockKey = itemInHand.getBlockKey();
                  if (heldBlockKey == null) {
                     section.invalidateBlock(targetBlock.x, targetBlock.y, targetBlock.z);
                  } else {
                     if (packet.placedBlockId != -1) {
                        String clientPlacedBlockTypeKey = BlockType.getAssetMap().getAsset(packet.placedBlockId).getId();
                        BlockType heldBlockType = BlockType.getAssetMap().getAsset(heldBlockKey);
                        if (heldBlockType != null && BlockPlaceUtils.canPlaceBlock(heldBlockType, clientPlacedBlockTypeKey)) {
                           heldBlockKey = clientPlacedBlockTypeKey;
                        }
                     }

                     BlockPlaceUtils.placeBlock(
                        ref,
                        itemInHand,
                        heldBlockKey,
                        inventory.getHotbar(),
                        Vector3i.ZERO,
                        targetBlock,
                        blockRotation,
                        inventory,
                        inventory.getActiveHotbarSlot(),
                        playerComponent.getGameMode() != GameMode.Creative,
                        chunkReference,
                        chunkStore,
                        store
                     );
                  }
               }
            }
         }
      }
   }

   public void handleRemoveMapMarker(
      @Nonnull RemoveMapMarker packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      world.getWorldMapManager().handleUserRemoveMarker(playerRef, packet);
   }

   public void handleCloseWindow(
      @Nonnull CloseWindow packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      playerComponent.getWindowManager().closeWindow(ref, packet.id, store);
   }

   public void handle(@Nonnull UpdateServerAccess packet) {
      if (!Constants.SINGLEPLAYER) {
         throw new IllegalArgumentException("UpdateServerAccess can only be used in singleplayer!");
      } else if (!SingleplayerModule.isOwner(this.playerRef)) {
         throw new IllegalArgumentException("UpdateServerAccess can only be by the owner of the singleplayer world!");
      } else {
         List<InetSocketAddress> publicAddresses = new CopyOnWriteArrayList<>();

         for (HostAddress host : packet.hosts) {
            publicAddresses.add(InetSocketAddress.createUnresolved(host.host, host.port & '\uffff'));
         }

         SingleplayerModule singleplayerModule = SingleplayerModule.get();
         singleplayerModule.setPublicAddresses(publicAddresses);
         singleplayerModule.updateAccess(packet.access);
      }
   }

   public void handle(@Nonnull SetServerAccess packet) {
      if (!Constants.SINGLEPLAYER) {
         throw new IllegalArgumentException("SetServerAccess can only be used in singleplayer!");
      } else if (!SingleplayerModule.isOwner(this.playerRef)) {
         throw new IllegalArgumentException("SetServerAccess can only be used by the owner of the singleplayer world!");
      } else {
         HytaleServerConfig config = HytaleServer.get().getConfig();
         if (config != null) {
            config.setPassword(packet.password != null ? packet.password : "");
            HytaleServerConfig.save(config);
         }

         SingleplayerModule.get().requestServerAccess(packet.access);
      }
   }

   public void handle(@Nonnull RequestMachinimaActorModel packet) {
      ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(packet.modelId);
      this.writeNoCache(new SetMachinimaActorModel(Model.createUnitScaleModel(modelAsset).toPacket(), packet.sceneName, packet.actorName));
   }

   public void handleUpdateMachinimaScene(
      @Nonnull UpdateMachinimaScene packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
   }

   public void handle(@Nonnull ClientReady packet) {
      HytaleLogger.getLogger().at(Level.WARNING).log("%s: Received %s", this.getIdentifier(), packet);
      CompletableFuture<Void> future = this.clientReadyForChunksFuture;
      if (packet.readyForChunks && !packet.readyForGameplay && future != null) {
         this.clientReadyForChunksFutureStack = null;
         this.clientReadyForChunksFuture = null;
         future.completeAsync(() -> null);
      }

      if (packet.readyForGameplay) {
         Ref<EntityStore> ref = this.playerRef.getReference();
         if (ref == null || !ref.isValid()) {
            return;
         }

         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(() -> {
            if (ref.isValid()) {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               playerComponent.handleClientReady(false);
            }
         });
      }
   }

   public void handleUpdateWorldMapVisible(
      @Nonnull UpdateWorldMapVisible packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      playerComponent.getWorldMapTracker().setClientHasWorldMapVisible(packet.visible);
   }

   public void handleTeleportToWorldMapMarker(
      @Nonnull TeleportToWorldMapMarker packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      WorldMapTracker worldMapTracker = playerComponent.getWorldMapTracker();
      if (!worldMapTracker.isAllowTeleportToMarkers()) {
         this.disconnect("You are not allowed to use TeleportToWorldMapMarker!");
      } else {
         MapMarker marker = worldMapTracker.getSentMarkers().get(packet.id);
         if (marker != null) {
            Transform transform = PositionUtil.toTransform(marker.transform);
            if (MapMarkerUtils.isUserMarker(marker)) {
               int blockX = (int)transform.getPosition().getX();
               int blockZ = (int)transform.getPosition().getZ();
               WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
               int height = chunk == null ? 319 : chunk.getHeight(blockX, blockZ);
               transform.getPosition().setY(height);
            }

            Teleport teleportComponent = Teleport.createForPlayer(transform);
            world.getEntityStore().getStore().addComponent(playerRef.getReference(), Teleport.getComponentType(), teleportComponent);
         }
      }
   }

   public void handleTeleportToWorldMapPosition(
      @Nonnull TeleportToWorldMapPosition packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      WorldMapTracker worldMapTracker = playerComponent.getWorldMapTracker();
      if (!worldMapTracker.isAllowTeleportToCoordinates()) {
         this.disconnect("You are not allowed to use TeleportToWorldMapMarker!");
      } else {
         world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunkFromBlock(packet.x, packet.y)).thenAcceptAsync(chunkRef -> {
            BlockChunk blockChunkComponent = world.getChunkStore().getStore().getComponent((Ref<ChunkStore>)chunkRef, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            Vector3d position = new Vector3d(packet.x, blockChunkComponent.getHeight(packet.x, packet.y) + 2, packet.y);
            Teleport teleportComponent = Teleport.createForPlayer(null, position, new Vector3f(0.0F, 0.0F, 0.0F));
            world.getEntityStore().getStore().addComponent(playerRef.getReference(), Teleport.getComponentType(), teleportComponent);
         }, world);
      }
   }

   public void handleCreateUserMarker(
      @Nonnull CreateUserMarker packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      WorldMapManager worldMapManager = world.getWorldMapManager();
      worldMapManager.handleUserCreateMarker(playerRef, packet);
   }

   public void handle(@Nonnull SyncInteractionChains packet) {
      Collections.addAll(this.interactionPacketQueue, packet.updates);
   }

   public void handleMountMovement(
      @Nonnull MountMovement packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      Ref<EntityStore> entityReference = world.getEntityStore().getRefFromNetworkId(playerComponent.getMountEntityId());
      if (entityReference != null && entityReference.isValid()) {
         TransformComponent transformComponent = store.getComponent(entityReference, TransformComponent.getComponentType());

         assert transformComponent != null;

         transformComponent.setPosition(PositionUtil.toVector3d(packet.absolutePosition));
         transformComponent.setRotation(PositionUtil.toRotation(packet.bodyOrientation));
         MovementStatesComponent movementStatesComponent = store.getComponent(entityReference, MovementStatesComponent.getComponentType());

         assert movementStatesComponent != null;

         movementStatesComponent.setMovementStates(packet.movementStates);
      }
   }

   public void handleSetPaused(
      @Nonnull SetPaused packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      if (world.getPlayerCount() == 1 && Constants.SINGLEPLAYER) {
         world.setPaused(packet.paused);
      }
   }

   public void handleRequestFlyCameraMode(
      @Nonnull RequestFlyCameraMode packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (playerComponent.hasPermission("hytale.camera.flycam")) {
         this.writeNoCache(new SetFlyCameraMode(packet.entering));
         if (packet.entering) {
            playerRef.sendMessage(Message.translation("server.general.flyCamera.enabled"));
         } else {
            playerRef.sendMessage(Message.translation("server.general.flyCamera.disabled"));
         }
      } else {
         playerRef.sendMessage(Message.translation("server.general.flyCamera.noPermission"));
      }
   }
}
