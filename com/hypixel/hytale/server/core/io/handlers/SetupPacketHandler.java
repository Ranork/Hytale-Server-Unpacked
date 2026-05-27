package com.hypixel.hytale.server.core.io.handlers;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.packets.auth.ClientReferral;
import com.hypixel.hytale.protocol.packets.connection.ClientDisconnect;
import com.hypixel.hytale.protocol.packets.connection.DisconnectType;
import com.hypixel.hytale.protocol.packets.interface_.ServerInfo;
import com.hypixel.hytale.protocol.packets.setup.RequestAssets;
import com.hypixel.hytale.protocol.packets.setup.SetupFinalize;
import com.hypixel.hytale.protocol.packets.setup.ViewRadius;
import com.hypixel.hytale.protocol.packets.setup.WorldLoadFinished;
import com.hypixel.hytale.protocol.packets.setup.WorldLoadProgress;
import com.hypixel.hytale.protocol.packets.setup.WorldSettings;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetRegistryLoader;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.asset.common.PlayerCommonAssets;
import com.hypixel.hytale.server.core.asset.common.events.SendCommonAssetsEvent;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupDisconnectEvent;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.DumpUtil;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetupPacketHandler extends GenericConnectionPacketHandler {
   private PlayerCommonAssets assets;
   private boolean receivedRequest;
   private boolean setupFinalized;
   private int clientViewRadiusChunks = 6;

   public SetupPacketHandler(@Nonnull ChannelConnection channel, @Nonnull ProtocolVersion protocolVersion, String language, @Nonnull PlayerAuthentication auth) {
      super(channel, protocolVersion, language);
      this.auth = auth;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Setup(" + this.getChannel().formatRemoteAddress() + "), " + this.auth.getUsername() + ", " + this.auth.getUuid() + "}";
   }

   @Override
   public void registered0(@Nonnull ConnectionHandler oldHandler) {
      HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
      this.enterStage("setup:world-settings", timeouts.getSetupWorldSettings(), () -> this.assets != null);
      if (this.auth.getReferralSource() != null) {
         HytaleLogger.getLogger()
            .at(Level.INFO)
            .log(
               "Player %s referred from %s:%d with %d bytes of data",
               this.auth.getUsername(),
               this.auth.getReferralSource().host,
               this.auth.getReferralSource().port,
               this.auth.getReferralData() != null ? this.auth.getReferralData().length : 0
            );
      }

      PlayerSetupConnectEvent event = HytaleServer.get()
         .getEventBus()
         .<Void, PlayerSetupConnectEvent>dispatchFor(PlayerSetupConnectEvent.class)
         .dispatch(
            new PlayerSetupConnectEvent(
               this, this.auth.getUsername(), this.auth.getUuid(), this.auth, this.auth.getReferralData(), this.auth.getReferralSource()
            )
         );
      if (event.isCancelled()) {
         this.disconnect(event.getReason());
      } else {
         ClientReferral clientReferral = event.getClientReferral();
         if (clientReferral != null) {
            this.writeNoCache(clientReferral);
         } else {
            PlayerRef otherPlayer = Universe.get().getPlayer(this.auth.getUuid());
            if (otherPlayer != null) {
               HytaleLogger.getLogger().at(Level.INFO).log("Found match of player %s on %s", this.auth.getUuid(), otherPlayer.getUsername());
               ChannelConnection otherPlayerChannel = otherPlayer.getPacketHandler().getChannel();
               if (!this.getChannel().isFromSameOrigin(otherPlayerChannel)) {
                  this.disconnect(Message.translation("client.general.disconnect.alreadyLoggedIn"));
                  otherPlayer.sendMessage(Message.translation("server.io.setuppackethandler.otherLoginAttempt"));
                  return;
               }

               Ref<EntityStore> reference = otherPlayer.getReference();
               if (reference != null) {
                  World world = reference.getStore().getExternalData().getWorld();
                  if (world != null) {
                     CompletableFuture<Void> removalFuture = new CompletableFuture<>();
                     world.execute(() -> {
                        otherPlayer.getPacketHandler().disconnect(Message.translation("server.general.disconnect.loggedInAgain"));
                        world.execute(() -> removalFuture.complete(null));
                     });
                     removalFuture.join();
                  } else {
                     otherPlayer.getPacketHandler().disconnect(Message.translation("server.general.disconnect.loggedInAgain"));
                  }
               }
            }

            this.getChannel().logConnectionTimings("Load Player Config", Level.FINE);
            WorldSettings worldSettings = new WorldSettings();
            worldSettings.worldHeight = 320;
            Asset[] requiredAssets = CommonAssetModule.get().getRequiredAssets();
            this.assets = new PlayerCommonAssets(requiredAssets);
            worldSettings.requiredAssets = requiredAssets;
            this.write(worldSettings);
            HytaleServerConfig serverConfig = HytaleServer.get().getConfig();
            this.write(
               new ServerInfo(HytaleServer.get().getServerName(), serverConfig.getMotd(), serverConfig.getMaxPlayers(), serverConfig.getFallbackServer())
            );
            this.continueStage("setup:assets-request", timeouts.getSetupAssetsRequest(), () -> this.receivedRequest);
         }
      }
   }

   @Override
   public void accept(@Nonnull ToServerPacket packet) {
      switch (packet.getId()) {
         case 1:
            this.handle((ClientDisconnect)packet);
            break;
         case 23:
            this.handle((RequestAssets)packet);
            break;
         case 32:
            this.handle((ViewRadius)packet);
            break;
         case 33:
            this.handle((SetupFinalize)packet);
            break;
         default:
            this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket").param("packetId", packet.getId()));
      }
   }

   @Override
   public void closed(@Nullable NetworkChannel networkChannel) {
      super.closed(networkChannel);
      IEventDispatcher<PlayerSetupDisconnectEvent, PlayerSetupDisconnectEvent> dispatcher = HytaleServer.get()
         .getEventBus()
         .dispatchFor(PlayerSetupDisconnectEvent.class);
      if (dispatcher.hasListener()) {
         dispatcher.dispatch(new PlayerSetupDisconnectEvent(this.auth.getUsername(), this.auth.getUuid(), this.auth, this.disconnectReason));
      }

      if (Constants.SINGLEPLAYER) {
         if (Universe.get().getPlayerCount() == 0) {
            HytaleLogger.getLogger().at(Level.INFO).log("No players left on singleplayer server shutting down!");
            HytaleServer.get().shutdownServer();
         } else if (SingleplayerModule.isOwner(this.auth.getUuid())) {
            HytaleLogger.getLogger().at(Level.INFO).log("Owner left the singleplayer server shutting down!");
            Universe.get()
               .getPlayers()
               .forEach(
                  p -> p.getPacketHandler()
                     .disconnect(Message.translation("server.general.disconnect.singleplayerOwnerLeft").param("username", this.auth.getUsername()))
               );
            HytaleServer.get().shutdownServer();
         }
      }
   }

   public void handle(@Nonnull ClientDisconnect packet) {
      this.disconnectReason.setClientDisconnectType(packet.type);
      HytaleLogger.getLogger()
         .at(Level.INFO)
         .log(
            "%s - %s at %s left with reason: %s - %s",
            this.auth.getUuid(),
            this.auth.getUsername(),
            this.getChannel().formatRemoteAddress(),
            packet.type.name(),
            packet.reason.name()
         );
      this.getChannel().closeApplicationConnection();
      if (packet.type == DisconnectType.Crash
         && Constants.SINGLEPLAYER
         && (Universe.get().getPlayerCount() == 0 || SingleplayerModule.isOwner(this.auth.getUuid()))) {
         DumpUtil.dump(true, false);
      }
   }

   public void handle(@Nonnull RequestAssets packet) {
      if (this.receivedRequest) {
         throw new IllegalArgumentException("Received duplicate RequestAssets!");
      } else {
         this.receivedRequest = true;
         this.getChannel().logConnectionTimings("Request Assets", Level.FINE);
         CompletableFuture<Void> future = CompletableFutureUtil._catch(
            HytaleServer.get()
               .getEventBus()
               .<Void, SendCommonAssetsEvent>dispatchForAsync(SendCommonAssetsEvent.class)
               .dispatch(new SendCommonAssetsEvent(this, packet.assets))
               .thenAccept(event -> {
                  if (this.getChannel().isActive()) {
                     this.getChannel().logConnectionTimings("Send Common Assets", Level.FINE);
                     this.assets.sent(event.getRequestedAssets());
                     AssetRegistryLoader.sendAssets(this);
                     I18nModule.get().sendTranslations(this, this.language);
                     this.getChannel().logConnectionTimings("Send Config Assets", Level.FINE);
                     this.write(new WorldLoadProgress(Message.translation("client.general.worldLoad.loadingWorld").getFormattedMessage(), 0, 0));
                     this.write(new WorldLoadFinished());
                  }
               })
               .exceptionally(throwable -> {
                  if (!this.getChannel().isActive()) {
                     return null;
                  } else {
                     this.disconnect(Message.translation("client.general.disconnect.loginException"));
                     throw new RuntimeException("Exception when player was joining", throwable);
                  }
               })
         );
         HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
         this.continueStage("setup:send-assets", timeouts.getSetupSendAssets(), () -> future.isDone() || !future.cancel(true));
      }
   }

   public void handle(@Nonnull ViewRadius packet) {
      this.clientViewRadiusChunks = MathUtil.ceil(packet.value / 32.0F);
   }

   public void handle(@Nonnull SetupFinalize packet) {
      if (!this.receivedRequest) {
         throw new IllegalArgumentException("Hasn't received RequestAssets yet!");
      } else {
         this.getChannel().logConnectionTimings("Setup Finalize", Level.FINE);
         if (this.getChannel().isActive()) {
            if (this.setupFinalized) {
               this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket"));
            } else {
               this.setupFinalized = true;
               if (this.auth == null) {
                  this.disconnect(Message.translation("server.general.disconnect.authenticationRequired"));
               } else {
                  CompletableFuture<Void> future = CompletableFutureUtil._catch(
                     Universe.get()
                        .addPlayer(this.getChannel(), this.language, this.protocolVersion, this.auth, this.clientViewRadiusChunks)
                        .thenAccept(player -> {
                           if (this.getChannel().isActive()) {
                              this.getChannel().logConnectionTimings("Add To Universe", Level.FINE);
                              this.clearTimeout();
                           }
                        })
                        .exceptionally(throwable -> {
                           if (!this.getChannel().isActive()) {
                              return null;
                           } else {
                              this.disconnect(Message.translation("client.general.disconnect.universeException"));
                              throw new RuntimeException("Exception when player adding to universe", throwable);
                           }
                        })
                  );
                  HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
                  this.continueStage("setup:add-to-universe", timeouts.getSetupAddToUniverse(), () -> future.isDone() || !future.cancel(true));
               }
            }
         }
      }
   }
}
