package com.hypixel.hytale.server.core.modules.serverplayerlist;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.protocol.packets.connection.PongType;
import com.hypixel.hytale.protocol.packets.interface_.AddToServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.RemoveFromServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.ServerPlayerListPlayer;
import com.hypixel.hytale.protocol.packets.interface_.ServerPlayerListUpdate;
import com.hypixel.hytale.protocol.packets.interface_.UpdateServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.UpdateServerPlayerListPing;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class ServerPlayerListModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(ServerPlayerListModule.class).depends(Universe.class).build();
   private static final int PING_UPDATE_INTERVAL_SECONDS = 10;
   private static ServerPlayerListModule instance;

   @Nonnull
   public static ServerPlayerListModule get() {
      return instance;
   }

   public ServerPlayerListModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      EventRegistry eventRegistry = this.getEventRegistry();
      eventRegistry.register(PlayerConnectEvent.class, this::onPlayerConnect);
      eventRegistry.register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
      eventRegistry.registerGlobal(AddPlayerToWorldEvent.class, this::onPlayerAddedToWorld);
      HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(this::broadcastPingUpdates, 10L, 10L, TimeUnit.SECONDS);
   }

   private void onPlayerConnect(@Nonnull PlayerConnectEvent event) {
      PlayerRef joiningPlayerRef = event.getPlayerRef();
      UUID joiningPlayerUuid = joiningPlayerRef.getUuid();
      List<PlayerRef> allPlayers = Universe.get().getPlayers();
      ServerPlayerListPlayer[] serverListPlayers = new ServerPlayerListPlayer[allPlayers.size()];
      int index = 0;

      for (PlayerRef playerRef : allPlayers) {
         serverListPlayers[index++] = createServerPlayerListPlayer(playerRef);
      }

      AddToServerPlayerList fullListPacket = new AddToServerPlayerList(serverListPlayers);
      joiningPlayerRef.getPacketHandler().write(fullListPacket);
      AddToServerPlayerList newPlayerPacket = new AddToServerPlayerList(new ServerPlayerListPlayer[]{createServerPlayerListPlayer(joiningPlayerRef)});

      for (PlayerRef playerRef : allPlayers) {
         if (!playerRef.getUuid().equals(joiningPlayerUuid)) {
            playerRef.getPacketHandler().write(newPlayerPacket);
         }
      }
   }

   private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
      PlayerRef leavingPlayerRef = event.getPlayerRef();
      UUID leavingPlayerUuid = leavingPlayerRef.getUuid();
      RemoveFromServerPlayerList removePacket = new RemoveFromServerPlayerList(new UUID[]{leavingPlayerUuid});

      for (PlayerRef playerRef : Universe.get().getPlayers()) {
         if (!playerRef.getUuid().equals(leavingPlayerUuid)) {
            playerRef.getPacketHandler().write(removePacket);
         }
      }
   }

   private void onPlayerAddedToWorld(@Nonnull AddPlayerToWorldEvent event) {
      Holder<EntityStore> holder = event.getHolder();
      PlayerRef playerRefComponent = holder.getComponent(PlayerRef.getComponentType());
      if (playerRefComponent != null) {
         UUID playerUuid = playerRefComponent.getUuid();
         UUID worldUuid = event.getWorld().getWorldConfig().getUuid();
         UpdateServerPlayerList updatePacket = new UpdateServerPlayerList(new ServerPlayerListUpdate[]{new ServerPlayerListUpdate(playerUuid, worldUuid)});

         for (PlayerRef otherPlayerRef : Universe.get().getPlayers()) {
            otherPlayerRef.getPacketHandler().write(updatePacket);
         }
      }
   }

   private void broadcastPingUpdates() {
      List<PlayerRef> allPlayers = Universe.get().getPlayers();
      if (!allPlayers.isEmpty()) {
         Object2IntOpenHashMap<UUID> pingMap = new Object2IntOpenHashMap(allPlayers.size());

         for (PlayerRef playerRef : allPlayers) {
            pingMap.put(playerRef.getUuid(), getPingValue(playerRef.getPacketHandler()));
         }

         UpdateServerPlayerListPing packet = new UpdateServerPlayerListPing(pingMap);

         for (PlayerRef playerRef : allPlayers) {
            playerRef.getPacketHandler().writeNoCache(packet);
         }
      }
   }

   private static int getPingValue(@Nonnull PacketHandler handler) {
      HistoricMetric historicMetric = handler.getPingInfo(PongType.Direct).getPingMetricSet();
      double average = historicMetric.getAverage(0);
      return (int)PacketHandler.PingInfo.TIME_UNIT.toMillis(MathUtil.fastCeil(average));
   }

   @Nonnull
   private static ServerPlayerListPlayer createServerPlayerListPlayer(@Nonnull PlayerRef playerRef) {
      return new ServerPlayerListPlayer(playerRef.getUuid(), playerRef.getUsername(), playerRef.getWorldUuid(), getPingValue(playerRef.getPacketHandler()));
   }
}
