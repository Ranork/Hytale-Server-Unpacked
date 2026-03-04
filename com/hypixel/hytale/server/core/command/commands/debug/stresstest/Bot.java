package com.hypixel.hytale.server.core.command.commands.debug.stresstest;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.TeleportAck;
import com.hypixel.hytale.protocol.TransformUpdate;
import com.hypixel.hytale.protocol.io.netty.PacketDecoder;
import com.hypixel.hytale.protocol.io.netty.PacketEncoder;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.protocol.packets.connection.Connect;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.protocol.packets.connection.DisconnectType;
import com.hypixel.hytale.protocol.packets.connection.Ping;
import com.hypixel.hytale.protocol.packets.connection.Pong;
import com.hypixel.hytale.protocol.packets.connection.PongType;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.protocol.packets.player.ClientReady;
import com.hypixel.hytale.protocol.packets.player.ClientTeleport;
import com.hypixel.hytale.protocol.packets.player.SetClientId;
import com.hypixel.hytale.protocol.packets.setup.PlayerOptions;
import com.hypixel.hytale.protocol.packets.setup.RequestAssets;
import com.hypixel.hytale.protocol.packets.setup.ViewRadius;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.util.PositionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Bot extends SimpleChannelInboundHandler<Packet> {
   private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(8);
   private static final EventLoopGroup WORKER_GROUP = NettyUtil.getEventLoopGroup(8, "BotWorkerGroup");
   public static final Asset[] EMPTY_ASSET_ARRAY = new Asset[0];
   @Nonnull
   private final HytaleLogger logger;
   private final String name;
   @Nonnull
   private final BotConfig config;
   @Nonnull
   private final ScheduledFuture<?> tickFuture;
   private final ObjectArrayFIFOQueue<Ping> pingPackets = new ObjectArrayFIFOQueue();
   private final MovementStates movementStates = new MovementStates();
   @Nullable
   private SocketChannel channel;
   private int id = -1;
   private Vector3d pos;
   private final Vector3f rotation = new Vector3f();
   private final Vector3d destination = new Vector3d();
   private final Vector3d temp = new Vector3d();
   private final Vector3f targetRotation = new Vector3f();

   public Bot(String name, @Nonnull BotConfig config, int tickStepNanos) throws InterruptedException, SocketException {
      this.logger = HytaleLogger.get(name);
      this.name = name;
      this.config = config;
      this.destination.assign(config.spawn.getPosition());
      this.destination.y = ThreadLocalRandom.current().nextDouble(config.flyYHeight.getX(), config.flyYHeight.getY());
      InetSocketAddress address = ServerManager.get().getLocalOrPublicAddress();
      this.logger.at(Level.INFO).log("Booting Bot! Connecting to %s", address);
      ((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(WORKER_GROUP))
                  .channel(Epoll.isAvailable() ? EpollSocketChannel.class : (KQueue.isAvailable() ? KQueueSocketChannel.class : NioSocketChannel.class)))
               .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE))
            .handler(new ChannelInitializer<SocketChannel>() {
               protected void initChannel(@Nonnull SocketChannel channel) {
                  Bot.this.channel = channel;
                  channel.pipeline().addLast("packetDecoder", new PacketDecoder());
                  channel.pipeline().addLast("packetEncoder", new PacketEncoder());
                  if (NettyUtil.PACKET_LOGGER.getLevel() != Level.OFF) {
                     channel.pipeline().addLast("logger", NettyUtil.LOGGER);
                  }

                  channel.pipeline().addLast("handler", Bot.this);
               }
            }))
         .connect(address)
         .sync();
      float dt = (float)(tickStepNanos / 1.0E9);
      this.tickFuture = EXECUTOR.scheduleAtFixedRate(() -> {
         if (this.channel != null) {
            try {
               this.tick(dt);
            } catch (Throwable var4x) {
               ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var4x)).log("Exception ticking %s", name);
            }
         }
      }, tickStepNanos, tickStepNanos, TimeUnit.NANOSECONDS);
   }

   public void shutdown() {
      this.tickFuture.cancel(false);
      if (this.channel != null && !this.channel.isShutdown()) {
         try {
            this.channel.shutdown().await(1L, TimeUnit.SECONDS);
            this.channel = null;
         } catch (InterruptedException var2) {
            var2.printStackTrace();
         }
      }
   }

   public void tick(float dt) {
      while (!this.pingPackets.isEmpty()) {
         Ping packet = (Ping)this.pingPackets.dequeue();
         this.channel.write(new Pong(packet.id, WorldTimeResource.instantToInstantData(Instant.now()), PongType.Tick, (short)0));
      }

      if (this.pos == null) {
         this.channel.flush();
      } else {
         double movementDistance = this.config.flySpeed * dt;
         if (this.pos.distanceSquaredTo(this.destination) <= movementDistance * movementDistance) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double randX = random.nextDouble(-this.config.radius, this.config.radius);
            double randY = random.nextDouble(this.config.flyYHeight.getX(), this.config.flyYHeight.getY());
            double randZ = random.nextDouble(-this.config.radius, this.config.radius);
            this.destination.assign(this.config.spawn.getPosition());
            this.destination.y = randY;
            this.destination.add(randX, 0.0, randZ);
         }

         this.temp.assign(this.destination).subtract(this.pos);
         Vector3f.lookAt(this.temp, this.targetRotation);
         Vector3f.lerpAngle(this.rotation, this.targetRotation, 0.3F, this.rotation);
         this.temp.normalize();
         this.temp.scale(movementDistance);
         this.pos.add(this.temp);
         this.movementStates.flying = true;
         this.channel.writeAndFlush(this.createMovementPacket());
      }
   }

   public void channelActive(@Nonnull ChannelHandlerContext ctx) {
      UUID uuid = UUID.nameUUIDFromBytes(("BOT|" + this.name).getBytes(StandardCharsets.UTF_8));
      ctx.writeAndFlush(new Connect(-1356075132, 20, "bot", ClientType.Game, uuid, this.name, null, "en", null, null));
      this.logger.at(Level.INFO).log("Connected!");
   }

   public void channelInactive(ChannelHandlerContext ctx) {
      this.logger.at(Level.INFO).log("Disconnected!");
      this.shutdown();
      StressTestStartCommand.BOTS.remove(this);
   }

   public void exceptionCaught(@Nonnull ChannelHandlerContext ctx, @Nonnull Throwable cause) {
      ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(cause)).log("Got exception from netty pipeline");
      if (ctx.channel().isWritable()) {
         ctx.channel().writeAndFlush(new Disconnect(cause.getMessage(), DisconnectType.Crash)).addListener(ChannelFutureListener.CLOSE);
      } else {
         ctx.channel().close();
      }

      this.shutdown();
      StressTestStartCommand.BOTS.remove(this);
   }

   public void channelRead0(@Nonnull ChannelHandlerContext ctx, @Nonnull Packet packet) {
      switch (packet.getId()) {
         case 1:
            this.logger.at(Level.INFO).log("Disconnected for: %s %s", ((Disconnect)packet).reason, ((Disconnect)packet).type);
            ctx.close();
            break;
         case 2:
            Ping ping = (Ping)packet;
            InstantData instantData = WorldTimeResource.instantToInstantData(Instant.now());
            ctx.write(new Pong(ping.id, instantData, PongType.Raw, (short)0));
            ctx.writeAndFlush(new Pong(ping.id, instantData, PongType.Direct, (short)0));
            this.pingPackets.enqueue(ping);
            break;
         case 20:
            ctx.write(new RequestAssets(EMPTY_ASSET_ARRAY));
            ctx.write(new ViewRadius(this.config.viewRadius));
            ctx.writeAndFlush(new PlayerOptions(CosmeticsModule.get().generateRandomSkin(ThreadLocalRandom.current())));
            break;
         case 100:
            this.id = ((SetClientId)packet).clientId;
            break;
         case 104:
            ctx.writeAndFlush(new ClientReady(true, this.id != -1));
            break;
         case 109:
            ClientTeleport clientTeleport = (ClientTeleport)packet;
            ModelTransform modelTransform = clientTeleport.modelTransform;
            if (modelTransform == null) {
               return;
            }

            this.updateModelTransform(modelTransform);
            this.logger.at(Level.INFO).log("TP: %s (sending ack for teleportId: %s)", this.pos, clientTeleport.teleportId);
            ClientMovement movement = this.createMovementPacket();
            movement.teleportAck = new TeleportAck(clientTeleport.teleportId);
            ctx.writeAndFlush(movement);
            break;
         case 161:
            EntityUpdates entityUpdates = (EntityUpdates)packet;
            EntityUpdate entry = findEntityUpdate(entityUpdates, this.id);
            if (entry == null) {
               return;
            }

            for (ComponentUpdate update : entry.updates) {
               if (update instanceof TransformUpdate transformUpdate) {
                  this.updateModelTransform(transformUpdate.transform);
                  break;
               }
            }
      }
   }

   public void updateModelTransform(@Nonnull ModelTransform modelTransform) {
      Position position = modelTransform.position;
      if (position != null) {
         if (this.pos == null) {
            this.pos = new Vector3d();
         }

         this.pos.assign(position.x, position.y, position.z);
      }

      Direction lookOrientation = modelTransform.lookOrientation;
      if (lookOrientation != null) {
         this.updateRotation(lookOrientation);
      }
   }

   public void updateRotation(@Nonnull Direction lookOrientation) {
      if (!Float.isNaN(lookOrientation.yaw)) {
         this.rotation.setYaw(lookOrientation.yaw);
      }

      if (!Float.isNaN(lookOrientation.pitch)) {
         this.rotation.setPitch(lookOrientation.pitch);
      }

      if (!Float.isNaN(lookOrientation.roll)) {
         this.rotation.setRoll(lookOrientation.roll);
      }
   }

   @Nonnull
   public ClientMovement createMovementPacket() {
      ClientMovement movement = new ClientMovement();
      movement.absolutePosition = PositionUtil.toPositionPacket(this.pos);
      movement.lookOrientation = PositionUtil.toDirectionPacket(this.rotation);
      movement.bodyOrientation = PositionUtil.toDirectionPacket(this.rotation);
      movement.bodyOrientation.pitch = 0.0F;
      movement.movementStates = this.movementStates;
      return movement;
   }

   @Nonnull
   public String toString() {
      return "Bot{name='" + this.name + "', id=" + this.id + "}";
   }

   @Nullable
   public static EntityUpdate findEntityUpdate(@Nonnull EntityUpdates bulkList, int id) {
      if (bulkList.updates == null) {
         return null;
      } else {
         for (EntityUpdate otherEntry : bulkList.updates) {
            if (otherEntry.networkId == id) {
               return otherEntry;
            }
         }

         return null;
      }
   }
}
