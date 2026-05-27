package com.hypixel.hytale.server.core.io.stream;

import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.server.core.io.PacketHandler;
import io.netty.handler.codec.quic.QuicStreamPriority;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StreamManager {
   public static final int MAX_AUXILIARY_STREAMS = 4;
   public static final QuicStreamPriority GAME_STREAM_PRIORITY = new QuicStreamPriority(0, false);
   public static final QuicStreamPriority DEFAULT_AUXILIARY_PRIORITY = new QuicStreamPriority(64, false);
   private static final StreamManager INSTANCE = new StreamManager();
   private final Map<StreamType, StreamManager.StreamRegistration> handlers = new ConcurrentHashMap<>();

   StreamManager() {
   }

   @Nonnull
   public static StreamManager getInstance() {
      return INSTANCE;
   }

   public void registerHandler(@Nonnull StreamType type, @Nonnull StreamManager.StreamHandlerFactory factory) {
      this.registerHandler(type, factory, DEFAULT_AUXILIARY_PRIORITY);
   }

   public void registerHandler(@Nonnull StreamType type, @Nonnull StreamManager.StreamHandlerFactory factory, @Nonnull QuicStreamPriority priority) {
      if (type == StreamType.Game) {
         throw new IllegalArgumentException("Cannot register handler for Game stream type - it uses the main pipeline");
      } else {
         this.handlers.put(type, new StreamManager.StreamRegistration(factory, priority));
      }
   }

   public void unregisterHandler(@Nonnull StreamType type) {
      this.handlers.remove(type);
   }

   public boolean isSupported(@Nonnull StreamType type) {
      return type == StreamType.Game || this.handlers.containsKey(type);
   }

   @Nullable
   public ConnectionHandler createHandler(@Nonnull StreamType type, @Nonnull PacketHandler packetHandler, @Nonnull ChannelConnection channel) {
      StreamManager.StreamRegistration registration = this.handlers.get(type);
      return registration != null ? registration.factory().create(packetHandler, channel) : null;
   }

   public void clearAll() {
      this.handlers.clear();
   }

   @Nonnull
   public QuicStreamPriority getStreamPriority(@Nonnull StreamType type) {
      StreamManager.StreamRegistration registration = this.handlers.get(type);
      return registration != null ? registration.priority() : DEFAULT_AUXILIARY_PRIORITY;
   }

   @FunctionalInterface
   public interface StreamHandlerFactory {
      @Nonnull
      ConnectionHandler create(@Nonnull PacketHandler var1, @Nonnull ChannelConnection var2);
   }

   private record StreamRegistration(@Nonnull StreamManager.StreamHandlerFactory factory, @Nonnull QuicStreamPriority priority) {
   }
}
