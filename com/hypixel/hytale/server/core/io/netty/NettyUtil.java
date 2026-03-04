package com.hypixel.hytale.server.core.io.netty;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.io.netty.ProtocolUtil;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.concurrent.ThreadUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandler;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
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

   public static void init() {
   }

   private static void injectLogger(@Nonnull Channel channel) {
      if (channel.pipeline().get("logger") == null) {
         channel.pipeline().addAfter("packetArrayEncoder", "logger", LOGGER);
      }
   }

   private static void uninjectLogger(@Nonnull Channel channel) {
      channel.pipeline().remove("logger");
   }

   public static void setChannelHandler(@Nonnull Channel channel, @Nonnull PacketHandler packetHandler) {
      ChannelHandler oldHandler = channel.pipeline().replace("handler", "handler", new PlayerChannelHandler(packetHandler));
      PacketHandler oldPlayerConnection = null;
      if (oldHandler instanceof PlayerChannelHandler) {
         oldPlayerConnection = ((PlayerChannelHandler)oldHandler).getHandler();
         oldPlayerConnection.unregistered(packetHandler);
      }

      packetHandler.registered(oldPlayerConnection);
   }

   @Nonnull
   public static CompletableFuture<Void> createStream(
      @Nonnull QuicChannel conn,
      @Nonnull QuicStreamType streamType,
      @Nonnull NetworkChannel networkChannel,
      @Nullable QuicStreamPriority priority,
      @Nonnull PacketHandler packetHandler
   ) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      conn.createStream(streamType, new HytaleChannelInitializer()).addListener(result -> {
         if (!result.isSuccess()) {
            future.completeExceptionally(result.cause());
         } else {
            QuicStreamChannel channel = (QuicStreamChannel)result.getNow();
            channel.attr(ProtocolUtil.STREAM_CHANNEL_KEY).set(networkChannel);
            if (priority != null) {
               channel.updatePriority(priority);
            }

            setChannelHandler(channel, packetHandler);
            packetHandler.setChannel(networkChannel, channel);
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

   static {
      HytaleLoggerBackend loggerBackend = HytaleLoggerBackend.getLogger(PACKET_LOGGER.getName());
      loggerBackend.setOnLevelChange((oldLevel, newLevel) -> {
         Universe universe = Universe.get();
         if (universe != null) {
            if (newLevel == Level.OFF) {
               for (PlayerRef p : universe.getPlayers()) {
                  uninjectLogger(p.getPacketHandler().getChannel());
               }
            } else {
               for (PlayerRef p : universe.getPlayers()) {
                  injectLogger(p.getPacketHandler().getChannel());
               }
            }
         }
      });
      PACKET_LOGGER.setLevel(Level.OFF);
      loggerBackend.loadLogLevel();
      CONNECTION_EXCEPTION_LOGGER.setLevel(Level.ALL);
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
