package com.hypixel.hytale.server.core.universe;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.packets.auth.ClientReferral;
import com.hypixel.hytale.protocol.packets.connection.PongType;
import com.hypixel.hytale.protocol.packets.interface_.ChatType;
import com.hypixel.hytale.protocol.packets.interface_.ServerMessage;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.HiddenPlayersManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.PacketStatsRecorderImpl;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerRef implements Component<EntityStore>, MetricProvider, IMessageReceiver {
   @Nonnull
   public static final MetricsRegistry<PlayerRef> METRICS_REGISTRY = new MetricsRegistry<PlayerRef>()
      .register("Username", PlayerRef::getUsername, Codec.STRING)
      .register("Language", PlayerRef::getLanguage, Codec.STRING)
      .register("QueuedPacketsCount", ref -> ref.getPacketHandler().getQueuedPacketsCount(), Codec.INTEGER)
      .register("PingInfo", ref -> {
         PacketHandler handler = ref.getPacketHandler();
         PongType[] pongTypes = PongType.values();
         PacketHandler.PingInfo[] pingInfos = new PacketHandler.PingInfo[pongTypes.length];

         for (int i = 0; i < pongTypes.length; i++) {
            pingInfos[i] = handler.getPingInfo(pongTypes[i]);
         }

         return pingInfos;
      }, new ArrayCodec<>(PacketHandler.PingInfo.METRICS_REGISTRY, PacketHandler.PingInfo[]::new))
      .register(
         "PacketStatsRecorder",
         ref -> ref.getPacketHandler().getPacketStatsRecorder() instanceof PacketStatsRecorderImpl impl ? impl : null,
         PacketStatsRecorderImpl.METRICS_REGISTRY
      )
      .register("ChunkTracker", PlayerRef::getChunkTracker, ChunkTracker.METRICS_REGISTRY);
   @Nonnull
   public static final MetricsRegistry<PlayerRef> COMPONENT_METRICS_REGISTRY = new MetricsRegistry<PlayerRef>().register("MovementStates", ref -> {
      Ref<EntityStore> entityRef = ref.getReference();
      if (entityRef == null) {
         return null;
      } else {
         MovementStatesComponent component = entityRef.getStore().getComponent(entityRef, MovementStatesComponent.getComponentType());
         return component != null ? component.getMovementStates().toString() : null;
      }
   }, Codec.STRING).register("MovementManager", ref -> {
      Ref<EntityStore> entityRef = ref.getReference();
      if (entityRef == null) {
         return null;
      } else {
         MovementManager component = entityRef.getStore().getComponent(entityRef, MovementManager.getComponentType());
         return component != null ? component.toString() : null;
      }
   }, Codec.STRING).register("CameraManager", ref -> {
      Ref<EntityStore> entityRef = ref.getReference();
      if (entityRef == null) {
         return null;
      } else {
         CameraManager component = entityRef.getStore().getComponent(entityRef, CameraManager.getComponentType());
         return component != null ? component.toString() : null;
      }
   }, Codec.STRING);
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final UUID uuid;
   @Nonnull
   private final String username;
   @Nonnull
   private final PacketHandler packetHandler;
   @Nonnull
   private final ChunkTracker chunkTracker;
   @Nonnull
   private final HiddenPlayersManager hiddenPlayersManager = new HiddenPlayersManager();
   @Nonnull
   private String language;
   @Nullable
   private Ref<EntityStore> entity;
   @Nullable
   private Holder<EntityStore> holder;
   @Nullable
   private UUID worldUuid;
   private Transform transform = new Transform(0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
   private Vector3f headRotation = new Vector3f(0.0F, 0.0F, 0.0F);

   @Nonnull
   public static ComponentType<EntityStore, PlayerRef> getComponentType() {
      return Universe.get().getPlayerRefComponentType();
   }

   public PlayerRef(
      @Nonnull Holder<EntityStore> holder,
      @Nonnull UUID uuid,
      @Nonnull String username,
      @Nonnull String language,
      @Nonnull PacketHandler packetHandler,
      @Nonnull ChunkTracker chunkTracker
   ) {
      this.holder = holder;
      this.uuid = uuid;
      this.username = username;
      this.language = language;
      this.packetHandler = packetHandler;
      this.chunkTracker = chunkTracker;
   }

   @Nullable
   public Ref<EntityStore> addToStore(@Nonnull Store<EntityStore> store) {
      store.assertThread();
      if (this.holder == null) {
         throw new IllegalStateException("Already in world");
      } else {
         return store.addEntity(this.holder, AddReason.LOAD);
      }
   }

   public void addedToStore(Ref<EntityStore> ref) {
      this.holder = null;
      this.entity = ref;
   }

   @Nonnull
   public Holder<EntityStore> removeFromStore() {
      if (this.entity == null) {
         throw new IllegalStateException("Not in world");
      } else {
         this.entity.getStore().assertThread();
         Ref<EntityStore> entity = this.entity;
         this.entity = null;
         return this.holder = entity.getStore().removeEntity(entity, RemoveReason.UNLOAD);
      }
   }

   public boolean isValid() {
      return this.entity != null || this.holder != null;
   }

   @Nullable
   public Ref<EntityStore> getReference() {
      return this.entity != null && this.entity.isValid() ? this.entity : null;
   }

   @Nullable
   public Holder<EntityStore> getHolder() {
      return this.holder;
   }

   @Nullable
   @Deprecated
   public <T extends Component<EntityStore>> T getComponent(@Nonnull ComponentType<EntityStore, T> componentType) {
      if (this.holder != null) {
         return this.holder.getComponent(componentType);
      } else {
         Store<EntityStore> store = this.entity.getStore();
         if (store.isInThread()) {
            return store.getComponent(this.entity, componentType);
         } else {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(new SkipSentryException()))
               .log("PlayerRef.getComponent(%s) called async with player in world", componentType.getTypeClass().getSimpleName());
            return CompletableFuture.<T>supplyAsync(() -> this.getComponent(componentType), store.getExternalData().getWorld()).join();
         }
      }
   }

   @Nonnull
   public UUID getUuid() {
      return this.uuid;
   }

   @Nonnull
   public String getUsername() {
      return this.username;
   }

   @Nonnull
   public PacketHandler getPacketHandler() {
      return this.packetHandler;
   }

   @Nonnull
   public ChunkTracker getChunkTracker() {
      return this.chunkTracker;
   }

   @Nonnull
   public HiddenPlayersManager getHiddenPlayersManager() {
      return this.hiddenPlayersManager;
   }

   @Nonnull
   public String getLanguage() {
      return this.language;
   }

   public void setLanguage(@Nonnull String language) {
      this.language = language;
   }

   @Nonnull
   public Transform getTransform() {
      return this.transform;
   }

   @Nullable
   public UUID getWorldUuid() {
      return this.worldUuid;
   }

   @Nonnull
   public Vector3f getHeadRotation() {
      return this.headRotation;
   }

   public void updatePosition(@Nonnull World world, @Nonnull Transform transform, @Nonnull Vector3f headRotation) {
      this.worldUuid = world.getWorldConfig().getUuid();
      this.transform.assign(transform);
      this.headRotation.assign(headRotation);
   }

   @Deprecated
   public void replaceHolder(@Nonnull Holder<EntityStore> holder) {
      if (holder == null) {
         throw new IllegalStateException("Player is still in the world");
      } else {
         this.holder = holder;
      }
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return this;
   }

   @Nonnull
   @Override
   public MetricResults toMetricResults() {
      return METRICS_REGISTRY.toMetricResults(this);
   }

   public void referToServer(@Nonnull String host, int port) {
      this.referToServer(host, port, null);
   }

   public void referToServer(@Nonnull String host, int port, @Nullable byte[] data) {
      int MAX_REFERRAL_DATA_SIZE = 4096;
      Objects.requireNonNull(host, "Host cannot be null");
      if (port > 0 && port <= 65535) {
         if (data != null && data.length > 4096) {
            throw new IllegalArgumentException("Referral data exceeds maximum size of 4096 bytes (got " + data.length + ")");
         } else {
            HytaleLogger.getLogger()
               .at(Level.INFO)
               .log("Referring player %s (%s) to %s:%d with %d bytes of data", this.username, this.uuid, host, port, data != null ? data.length : 0);
            this.packetHandler.writeNoCache(new ClientReferral(new HostAddress(host, (short)port), data));
         }
      } else {
         throw new IllegalArgumentException("Port must be between 1 and 65535");
      }
   }

   @Override
   public void sendMessage(@Nonnull Message message) {
      this.packetHandler.writeNoCache(new ServerMessage(ChatType.Chat, message.getFormattedMessage()));
   }
}
