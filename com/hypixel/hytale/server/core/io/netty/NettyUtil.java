package com.hypixel.hytale.server.core.io.netty;

import com.google.common.flogger.LazyArgs;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import com.hypixel.hytale.protocol.io.ServerListener;
import com.hypixel.hytale.protocol.io.netty.PacketDecoder;
import com.hypixel.hytale.protocol.io.netty.PacketEncoder;
import com.hypixel.hytale.protocol.io.netty.ProtocolUtil;
import com.hypixel.hytale.protocol.packets.connection.DisconnectType;
import com.hypixel.hytale.protocol.packets.connection.QuicApplicationErrorCode;
import com.hypixel.hytale.protocol.packets.connection.ServerDisconnect;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.transport.QUICTransport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.concurrent.ThreadUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.quic.QuicChannel;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.handler.codec.quic.QuicStreamPriority;
import io.netty.handler.codec.quic.QuicStreamType;
import io.netty.handler.codec.quic.QuicTransportError;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NettyUtil {
   public static final HytaleLogger CONNECTION_EXCEPTION_LOGGER = HytaleLogger.get("ConnectionExceptionLogging");
   public static final HytaleLogger PACKET_LOGGER = HytaleLogger.get("PacketLogging");
   public static final String PACKET_DECODER = "packetDecoder";
   public static final String PACKET_ARRAY_ENCODER = "packetArrayEncoder";
   public static final PacketArrayEncoder PACKET_ARRAY_ENCODER_INSTANCE = new PacketArrayEncoder();
   public static final String PACKET_ENCODER = "packetEncoder";
   public static final String LOGGER_KEY = "logger";
   public static final LoggingHandler LOGGER = new LoggingHandler("PacketLogging", LogLevel.INFO);
   public static final String HANDLER = "handler";
   public static final String RATE_LIMIT = "rateLimit";
   public static final ChannelFutureListener CLOSE_ON_COMPLETE = future -> closeApplicationConnection(future.channel());

   public static void init() {
   }

   public static void closeConnection(@Nonnull Channel channel) {
      int errorCode = (int)QuicTransportError.PROTOCOL_VIOLATION.code();
      if (channel instanceof QuicChannel quicChannel) {
         quicChannel.close(false, errorCode, Unpooled.EMPTY_BUFFER);
      } else if (channel.parent() instanceof QuicChannel quicChannel) {
         quicChannel.close(false, errorCode, Unpooled.EMPTY_BUFFER);
      } else {
         channel.close();
      }
   }

   public static void closeApplicationConnection(@Nonnull Channel channel) {
      closeApplicationConnection(channel, QuicApplicationErrorCode.NoError);
   }

   public static void closeApplicationConnection(@Nonnull Channel channel, @Nonnull QuicApplicationErrorCode errorCode) {
      if (channel instanceof QuicChannel quicChannel) {
         quicChannel.close(true, errorCode.ordinal(), Unpooled.EMPTY_BUFFER);
      } else if (channel.parent() instanceof QuicChannel quicChannel) {
         quicChannel.close(true, errorCode.ordinal(), Unpooled.EMPTY_BUFFER);
      } else {
         channel.close();
      }
   }

   private static void injectLogger(@Nonnull Channel channel) {
      if (channel.pipeline().get("logger") == null) {
         channel.pipeline().addAfter("packetArrayEncoder", "logger", LOGGER);
      }
   }

   private static void uninjectLogger(@Nonnull Channel channel) {
      channel.pipeline().remove("logger");
   }

   private static void setChannelHandler(@Nonnull Channel channel, @Nonnull PacketHandler packetHandler) {
      PlayerChannelHandler newHandler = new PlayerChannelHandler(packetHandler);
      PacketHandler oldPlayerConnection = null;
      ChannelHandler existingHandler = channel.pipeline().get("handler");
      if (existingHandler != null) {
         channel.pipeline().replace("handler", "handler", newHandler);
         if (existingHandler instanceof PlayerChannelHandler playerHandler) {
            oldPlayerConnection = playerHandler.getHandler();
            oldPlayerConnection.unregistered(packetHandler);
         }
      } else {
         channel.pipeline().addLast("handler", newHandler);
      }

      if (channel instanceof QuicStreamChannel quicStreamChannel) {
         quicStreamChannel.parent().attr(HytaleChannelInitializer.GAME_PACKET_HANDLER_ATTR).set(packetHandler);
      }

      packetHandler.registered(oldPlayerConnection);
   }

   @Nonnull
   private static CompletableFuture<Void> createStream(
      @Nonnull QuicChannel conn,
      @Nonnull QuicStreamType streamType,
      @Nonnull NetworkChannel networkChannel,
      @Nullable QuicStreamPriority priority,
      @Nonnull PacketHandler packetHandler,
      @Nonnull BiConsumer<NetworkChannel, ChannelConnection> onChannelReady
   ) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      conn.createStream(streamType, new ChannelInitializer<Channel>() {
         protected void initChannel(@Nonnull Channel ch) {
            ch.pipeline().addLast("packetDecoder", new PacketDecoder());
            ch.pipeline().addLast("packetEncoder", new PacketEncoder());
            ch.pipeline().addLast("packetArrayEncoder", NettyUtil.PACKET_ARRAY_ENCODER_INSTANCE);
         }
      }).addListener(result -> {
         if (!result.isSuccess()) {
            future.completeExceptionally(result.cause());
         } else {
            QuicStreamChannel channel = (QuicStreamChannel)result.getNow();
            channel.attr(ProtocolUtil.STREAM_CHANNEL_KEY).set(networkChannel);
            if (priority != null) {
               channel.updatePriority(priority);
            }

            setChannelHandler(channel, packetHandler);
            NettyUtil.NettyChannelConnection connection = new NettyUtil.NettyChannelConnection(channel);
            onChannelReady.accept(networkChannel, connection);
            future.complete(null);
         }
      });
      return future;
   }

   @Nonnull
   public static EventLoopGroup getEventLoopGroup(String name) {
      return getEventLoopGroup(0, name);
   }

   @Nonnull
   public static EventLoopGroup getEventLoopGroup(int nThreads, String name) {
      if (nThreads == 0) {
         nThreads = Math.max(1, SystemPropertyUtil.getInt("server.io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
      }

      ThreadFactory factory = ThreadUtil.daemonCounted(name + " - %d");
      if (Epoll.isAvailable()) {
         return new EpollEventLoopGroup(nThreads, factory);
      } else {
         return (EventLoopGroup)(KQueue.isAvailable() ? new KQueueEventLoopGroup(nThreads, factory) : new NioEventLoopGroup(nThreads, factory));
      }
   }

   @Nonnull
   public static Class<? extends ServerChannel> getServerChannel() {
      if (Epoll.isAvailable()) {
         return EpollServerSocketChannel.class;
      } else {
         return KQueue.isAvailable() ? KQueueServerSocketChannel.class : NioServerSocketChannel.class;
      }
   }

   @Nonnull
   public static NettyUtil.ReflectiveChannelFactory<? extends DatagramChannel> getDatagramChannelFactory(SocketProtocolFamily family) {
      if (Epoll.isAvailable()) {
         return new NettyUtil.ReflectiveChannelFactory(EpollDatagramChannel.class, family);
      } else {
         return KQueue.isAvailable()
            ? new NettyUtil.ReflectiveChannelFactory(KQueueDatagramChannel.class, family)
            : new NettyUtil.ReflectiveChannelFactory(NioDatagramChannel.class, family);
      }
   }

   public static String formatRemoteAddress(Channel channel) {
      if (channel instanceof QuicChannel quicChannel) {
         return quicChannel.remoteAddress() + " (" + quicChannel.remoteSocketAddress() + ")";
      } else {
         return channel instanceof QuicStreamChannel quicStreamChannel
            ? quicStreamChannel.parent().localAddress()
               + " ("
               + quicStreamChannel.parent().remoteSocketAddress()
               + ", streamId="
               + quicStreamChannel.remoteAddress().streamId()
               + ")"
            : channel.remoteAddress().toString();
      }
   }

   public static String formatLocalAddress(Channel channel) {
      if (channel instanceof QuicChannel quicChannel) {
         return quicChannel.localAddress() + " (" + quicChannel.localSocketAddress() + ")";
      } else {
         return channel instanceof QuicStreamChannel quicStreamChannel
            ? quicStreamChannel.parent().localAddress()
               + " ("
               + quicStreamChannel.parent().localSocketAddress()
               + ", streamId="
               + quicStreamChannel.localAddress().streamId()
               + ")"
            : channel.localAddress().toString();
      }
   }

   @Nullable
   public static SocketAddress getRemoteSocketAddress(Channel channel) {
      if (channel instanceof QuicChannel quicChannel) {
         return quicChannel.remoteSocketAddress();
      } else {
         return channel instanceof QuicStreamChannel quicStreamChannel ? quicStreamChannel.parent().remoteSocketAddress() : channel.remoteAddress();
      }
   }

   public static boolean isFromSameOrigin(Channel channel1, Channel channel2) {
      SocketAddress remoteSocketAddress1 = getRemoteSocketAddress(channel1);
      SocketAddress remoteSocketAddress2 = getRemoteSocketAddress(channel2);
      if (remoteSocketAddress1 == null || remoteSocketAddress2 == null) {
         return false;
      } else if (Objects.equals(remoteSocketAddress1, remoteSocketAddress2)) {
         return true;
      } else if (!remoteSocketAddress1.getClass().equals(remoteSocketAddress2.getClass())) {
         return false;
      } else if (!(
         remoteSocketAddress1 instanceof InetSocketAddress remoteInetSocketAddress1
            && remoteSocketAddress2 instanceof InetSocketAddress remoteInetSocketAddress2
      )) {
         return false;
      } else {
         return remoteInetSocketAddress1.getAddress().isLoopbackAddress() && remoteInetSocketAddress2.getAddress().isLoopbackAddress()
            ? true
            : remoteInetSocketAddress1.getAddress().equals(remoteInetSocketAddress2.getAddress());
      }
   }

   @Nonnull
   public static <T> CompletableFuture<T> wrapChannelFuture(@Nonnull ChannelFuture future, @Nonnull Function<ChannelFuture, T> func) {
      CompletableFuture<T> fut = new CompletableFuture<>();
      future.addListener(future1 -> {
         if (future1.isSuccess()) {
            fut.complete(func.apply(future));
         } else {
            fut.completeExceptionally(future1.cause());
         }
      });
      return fut;
   }

   static {
      HytaleLoggerBackend loggerBackend = HytaleLoggerBackend.getLogger(PACKET_LOGGER.getName());
      loggerBackend.setOnLevelChange((oldLevel, newLevel) -> {
         Universe universe = Universe.get();
         if (universe != null) {
            if (newLevel == Level.OFF) {
               for (PlayerRef p : universe.getPlayers()) {
                  if (p.getPacketHandler().getChannel() instanceof NettyUtil.NettyChannelConnection(Channel patt1$temp)) {
                     uninjectLogger(patt1$temp);
                  }
               }
            } else {
               for (PlayerRef px : universe.getPlayers()) {
                  if (px.getPacketHandler().getChannel() instanceof NettyUtil.NettyChannelConnection(Channel patt1$temp)) {
                     injectLogger(patt1$temp);
                  }
               }
            }
         }
      });
      PACKET_LOGGER.setLevel(Level.OFF);
      loggerBackend.loadLogLevel();
      CONNECTION_EXCEPTION_LOGGER.setLevel(Level.ALL);
   }

   public record NettyChannelConnection(Channel channel) implements ChannelConnection {
      private static final HytaleLogger LOGIN_TIMING_LOGGER = HytaleLogger.get("LoginTiming");
      private static final AttributeKey<ScheduledFuture<?>> STAGE_TIMEOUT_KEY = AttributeKey.newInstance("STAGE_TIMEOUT");
      private static final AttributeKey<Long> LOGIN_START_KEY = AttributeKey.newInstance("LOGIN_START");

      @Override
      public void flush() {
         this.channel.flush();
      }

      @Override
      public void write(ToClientPacket packet) {
         this.channel.write(packet, this.channel.voidPromise());
      }

      @Override
      public void writeAndFlush(ToClientPacket packet) {
         this.channel.writeAndFlush(packet, this.channel.voidPromise());
      }

      @Override
      public void write(ToClientPacket[] packets) {
         this.channel.write(packets, this.channel.voidPromise());
      }

      @Override
      public void writeAndFlush(ToClientPacket[] packets) {
         this.channel.writeAndFlush(packets, this.channel.voidPromise());
      }

      @Override
      public boolean isActive() {
         return this.channel.isActive();
      }

      @Override
      public boolean isWritable() {
         return this.channel.isWritable();
      }

      @Override
      public SocketAddress remoteAddress() {
         SocketAddress socketAddress;
         if (this.channel instanceof QuicStreamChannel quicStreamChannel) {
            socketAddress = quicStreamChannel.parent().remoteSocketAddress();
         } else {
            socketAddress = this.channel.remoteAddress();
         }

         return socketAddress;
      }

      @Override
      public String formatRemoteAddress() {
         return NettyUtil.formatRemoteAddress(this.channel);
      }

      @Override
      public void disconnect(@Nonnull FormattedMessage message) {
         this.channel
            .writeAndFlush(new ServerDisconnect(message, DisconnectType.Disconnect))
            .addListener((ChannelFutureListener)future -> this.closeApplicationConnection());
      }

      @Nullable
      @Override
      public PacketStatsRecorder getPacketStatsRecorder() {
         return (PacketStatsRecorder)this.channel.attr(PacketStatsRecorder.CHANNEL_KEY).get();
      }

      @Nullable
      @Override
      public String getSniHostname() {
         return this.channel instanceof QuicStreamChannel quicStreamChannel
            ? (String)quicStreamChannel.parent().attr(QUICTransport.SNI_HOSTNAME_ATTR).get()
            : null;
      }

      @Override
      public boolean isFromSameOrigin(ChannelConnection other) {
         return other instanceof NettyUtil.NettyChannelConnection otherNetty ? NettyUtil.isFromSameOrigin(this.channel, otherNetty.channel) : false;
      }

      @Override
      public void execute(Runnable runnable) {
         this.channel.eventLoop().execute(runnable);
      }

      @Nonnull
      @Override
      public CompletableFuture<Void> setupAuxiliaryChannels(
         @Nonnull ConnectionHandler handler, @Nonnull BiConsumer<NetworkChannel, ChannelConnection> onChannelReady
      ) {
         if (this.channel instanceof QuicStreamChannel streamChannel) {
            QuicChannel conn = streamChannel.parent();
            conn.attr(ProtocolUtil.STREAM_CHANNEL_KEY).set(NetworkChannel.Default);
            streamChannel.updatePriority(PacketHandler.DEFAULT_STREAM_PRIORITIES.get(NetworkChannel.Default));
            PacketHandler packetHandler = (PacketHandler)handler;
            CompletableFuture<Void> chunkFuture = NettyUtil.createStream(
               conn,
               QuicStreamType.UNIDIRECTIONAL,
               NetworkChannel.Chunks,
               PacketHandler.DEFAULT_STREAM_PRIORITIES.get(NetworkChannel.Chunks),
               packetHandler,
               onChannelReady
            );
            CompletableFuture<Void> worldMapFuture = NettyUtil.createStream(
               conn,
               QuicStreamType.UNIDIRECTIONAL,
               NetworkChannel.WorldMap,
               PacketHandler.DEFAULT_STREAM_PRIORITIES.get(NetworkChannel.WorldMap),
               packetHandler,
               onChannelReady
            );
            return CompletableFuture.allOf(chunkFuture, worldMapFuture);
         } else {
            onChannelReady.accept(NetworkChannel.WorldMap, this);
            onChannelReady.accept(NetworkChannel.Chunks, this);
            return CompletableFuture.completedFuture(null);
         }
      }

      @Override
      public void setChannelHandler(@Nonnull ConnectionHandler handler) {
         NettyUtil.setChannelHandler(this.channel, (PacketHandler)handler);
      }

      @Override
      public X509Certificate getClientCertificate() {
         return (X509Certificate)this.channel.attr(QUICTransport.CLIENT_CERTIFICATE_ATTR).get();
      }

      @Override
      public void updateStreamPriority(int urgency, boolean incremental) {
         if (this.channel instanceof QuicStreamChannel quicStreamChannel) {
            quicStreamChannel.updatePriority(new QuicStreamPriority(urgency, incremental));
         }
      }

      @Override
      public void initTimeoutContext(@Nonnull String stage, @Nonnull String identifier) {
         NettyUtil.TimeoutContext.init(this.channel, stage, identifier);
      }

      @Override
      public void updateTimeoutContext(@Nonnull String stage, @Nonnull String identifier) {
         NettyUtil.TimeoutContext.update(this.channel, stage, identifier);
      }

      @Override
      public void updateTimeoutContext(@Nonnull String stage) {
         NettyUtil.TimeoutContext.update(this.channel, stage);
      }

      @Override
      public void setPacketTimeout(@Nonnull Duration timeout) {
         this.channel.attr(ProtocolUtil.PACKET_TIMEOUT_KEY).set(timeout);
      }

      @Override
      public void setStageTimeout(@Nonnull String stage, @Nonnull Duration timeout, @Nonnull BooleanSupplier condition, @Nonnull Runnable onTimeout) {
         ScheduledFuture<?> existing = (ScheduledFuture<?>)this.channel.attr(STAGE_TIMEOUT_KEY).get();
         if (existing != null) {
            existing.cancel(false);
         }

         this.logConnectionTimings("Entering stage '" + stage + "'", Level.FINEST);
         long timeoutMillis = timeout.toMillis();
         io.netty.util.concurrent.ScheduledFuture<?> task = this.channel
            .eventLoop()
            .schedule(
               () -> {
                  if (this.channel.isOpen()) {
                     if (!condition.getAsBoolean()) {
                        NettyUtil.TimeoutContext context = (NettyUtil.TimeoutContext)this.channel.attr(NettyUtil.TimeoutContext.KEY).get();
                        String duration = context != null ? FormatUtil.nanosToString(System.nanoTime() - context.connectionStartNs()) : "unknown";
                        HytaleLogger.getLogger()
                           .at(Level.WARNING)
                           .log(
                              "Stage timeout for %s at stage '%s' after %s connected",
                              context != null ? context.playerIdentifier() : "unknown",
                              stage,
                              duration
                           );
                        onTimeout.run();
                     }
                  }
               },
               timeoutMillis,
               TimeUnit.MILLISECONDS
            );
         this.channel.attr(STAGE_TIMEOUT_KEY).set(task);
      }

      @Override
      public void clearStageTimeout() {
         ScheduledFuture<?> existing = (ScheduledFuture<?>)this.channel.attr(STAGE_TIMEOUT_KEY).get();
         if (existing != null) {
            existing.cancel(false);
         }
      }

      @Override
      public void logConnectionTimings(@Nonnull String message, @Nonnull Level level) {
         long now = System.nanoTime();
         NettyUtil.TimeoutContext context = (NettyUtil.TimeoutContext)this.channel.attr(NettyUtil.TimeoutContext.KEY).get();
         String identifier = context != null ? context.playerIdentifier() : NettyUtil.formatRemoteAddress(this.channel);
         Long before = (Long)this.channel.attr(LOGIN_START_KEY).getAndSet(now);
         if (before == null) {
            LOGIN_TIMING_LOGGER.at(level).log("[%s] %s", identifier, message);
         } else {
            long delta = now - before;
            LOGIN_TIMING_LOGGER.at(level).log("[%s] %s took %s", identifier, message, LazyArgs.lazy(() -> FormatUtil.nanosToString(delta)));
         }
      }

      @Override
      public void closeConnection() {
         this.close(false, 0);
      }

      @Override
      public void closeApplicationConnection() {
         this.close(true, QuicApplicationErrorCode.NoError.ordinal());
      }

      @Override
      public void closeApplicationConnection(@Nonnull QuicApplicationErrorCode errorCode) {
         this.close(true, errorCode.ordinal());
      }

      @Override
      public void closeApplicationConnection(@Nonnull QuicApplicationErrorCode errorCode, @Nonnull String reason) {
         ByteBuf reasonBuf = Unpooled.copiedBuffer(reason, StandardCharsets.UTF_8);
         if (this.channel instanceof QuicChannel quicChannel) {
            quicChannel.close(true, errorCode.ordinal(), reasonBuf);
         } else if (this.channel.parent() instanceof QuicChannel quicChannel) {
            quicChannel.close(true, errorCode.ordinal(), reasonBuf);
         } else {
            reasonBuf.release();
            this.channel.close();
         }
      }

      private void close(boolean applicationClose, int errorCode) {
         if (this.channel instanceof QuicChannel quicChannel) {
            quicChannel.close(applicationClose, errorCode, Unpooled.EMPTY_BUFFER);
         } else if (this.channel.parent() instanceof QuicChannel quicChannel) {
            quicChannel.close(applicationClose, errorCode, Unpooled.EMPTY_BUFFER);
         } else {
            this.channel.close();
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return obj instanceof NettyUtil.NettyChannelConnection(Channel var4) ? this.channel.equals(var4) : false;
         }
      }

      @Override
      public int hashCode() {
         return this.channel.hashCode();
      }

      @Nonnull
      @Override
      public String toString() {
         return this.channel.toString();
      }
   }

   public record NettyChannelServerListener(Channel channel) implements ServerListener {
      @Override
      public Future<Void> close() {
         return NettyUtil.wrapChannelFuture(this.channel.close(), future -> null);
      }

      @Override
      public SocketAddress localAddress() {
         return this.channel.localAddress();
      }
   }

   public static class ReflectiveChannelFactory<T extends Channel> implements ChannelFactory<T> {
      @Nonnull
      private final Constructor<? extends T> constructor;
      private final SocketProtocolFamily family;

      public ReflectiveChannelFactory(@Nonnull Class<? extends T> clazz, SocketProtocolFamily family) {
         ObjectUtil.checkNotNull(clazz, "clazz");

         try {
            this.constructor = clazz.getConstructor(SocketProtocolFamily.class);
            this.family = family;
         } catch (NoSuchMethodException var4) {
            throw new IllegalArgumentException("Class " + StringUtil.simpleClassName(clazz) + " does not have a public non-arg constructor", var4);
         }
      }

      @Nonnull
      public T newChannel() {
         try {
            return (T)this.constructor.newInstance(this.family);
         } catch (Throwable var2) {
            throw new ChannelException("Unable to create Channel from class " + this.constructor.getDeclaringClass(), var2);
         }
      }

      @Nonnull
      public String getSimpleName() {
         return StringUtil.simpleClassName(this.constructor.getDeclaringClass()) + "(" + this.family + ")";
      }

      @Nonnull
      @Override
      public String toString() {
         return StringUtil.simpleClassName(io.netty.channel.ReflectiveChannelFactory.class)
            + "("
            + StringUtil.simpleClassName(this.constructor.getDeclaringClass())
            + ".class, "
            + this.family
            + ")";
      }
   }

   public record TimeoutContext(@Nonnull String stage, long connectionStartNs, @Nonnull String playerIdentifier) {
      public static final AttributeKey<NettyUtil.TimeoutContext> KEY = AttributeKey.newInstance("TIMEOUT_CONTEXT");

      public static void init(@Nonnull Channel channel, @Nonnull String stage, @Nonnull String identifier) {
         channel.attr(KEY).set(new NettyUtil.TimeoutContext(stage, System.nanoTime(), identifier));
      }

      public static void update(@Nonnull Channel channel, @Nonnull String stage, @Nonnull String identifier) {
         NettyUtil.TimeoutContext existing = get(channel);
         channel.attr(KEY).set(new NettyUtil.TimeoutContext(stage, existing.connectionStartNs, identifier));
      }

      public static void update(@Nonnull Channel channel, @Nonnull String stage) {
         NettyUtil.TimeoutContext existing = get(channel);
         channel.attr(KEY).set(new NettyUtil.TimeoutContext(stage, existing.connectionStartNs, existing.playerIdentifier));
      }

      @Nonnull
      public static NettyUtil.TimeoutContext get(@Nonnull Channel channel) {
         NettyUtil.TimeoutContext context = (NettyUtil.TimeoutContext)channel.attr(KEY).get();
         if (context == null) {
            throw new IllegalStateException("TimeoutContext not initialized - this indicates a bug in the connection flow");
         } else {
            return context;
         }
      }
   }
}
