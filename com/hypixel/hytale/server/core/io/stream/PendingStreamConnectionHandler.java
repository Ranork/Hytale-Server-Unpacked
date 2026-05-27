package com.hypixel.hytale.server.core.io.stream;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.packets.stream.StreamOpen;
import com.hypixel.hytale.protocol.packets.stream.StreamOpenResponse;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.server.core.io.PacketHandler;
import io.netty.handler.codec.quic.QuicStreamPriority;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PendingStreamConnectionHandler implements ConnectionHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int MAX_AUXILIARY_STREAMS = 4;
   private final PacketHandler packetHandler;
   private final StreamManager streamManager;
   private final ChannelConnection channel;
   private boolean handled;

   public PendingStreamConnectionHandler(@Nonnull PacketHandler packetHandler, @Nonnull ChannelConnection channel) {
      this(packetHandler, StreamManager.getInstance(), channel);
   }

   public PendingStreamConnectionHandler(@Nonnull PacketHandler packetHandler, @Nonnull StreamManager streamManager, @Nonnull ChannelConnection channel) {
      this.packetHandler = packetHandler;
      this.streamManager = streamManager;
      this.channel = channel;
   }

   @Override
   public void handle(@Nonnull ToServerPacket packet) {
      if (this.handled) {
         LOGGER.at(Level.WARNING)
            .log("Received packet after StreamOpen on pending stream from %s: %s", this.packetHandler.getIdentifier(), packet.getClass().getSimpleName());
      } else if (packet instanceof StreamOpen open) {
         this.handled = true;
         StreamType type = open.type;
         if (this.packetHandler.getAuth() == null) {
            LOGGER.at(Level.WARNING).log("Rejecting auxiliary stream from unauthenticated connection %s", this.packetHandler.getIdentifier());
            this.rejectAndClose(type, "Authentication required");
         } else if (this.packetHandler.checkStreamOpenRateLimit()) {
            LOGGER.at(Level.WARNING).log("Stream open rate limited for %s requesting %s", this.packetHandler.getIdentifier(), type.name());
            this.rejectAndClose(type, "Rate limited - try again later");
         } else if (type == StreamType.Game) {
            LOGGER.at(Level.WARNING).log("Cannot open Game stream - stream 0 is already the game stream, from %s", this.packetHandler.getIdentifier());
            this.rejectAndClose(type, "Game stream cannot be opened explicitly");
         } else if (!this.streamManager.isSupported(type)) {
            LOGGER.at(Level.INFO).log("Unsupported stream type %s from %s", type.name(), this.packetHandler.getIdentifier());
            this.rejectAndClose(type, "Stream type not supported");
         } else {
            ChannelConnection existingChannel = this.packetHandler.getChannel(type);
            if (existingChannel != null) {
               LOGGER.at(Level.INFO)
                  .log("Replacing stale %s stream for %s (old channel active=%s)", type.name(), this.packetHandler.getIdentifier(), existingChannel.isActive());
               this.packetHandler.compareAndSetChannel(type, existingChannel, null);
               existingChannel.closeConnection();
            }

            if (this.packetHandler.getAuxiliaryChannelCount() >= 4) {
               LOGGER.at(Level.WARNING).log("Maximum auxiliary streams exceeded for %s requesting %s", this.packetHandler.getIdentifier(), type.name());
               this.rejectAndClose(type, "Maximum auxiliary streams exceeded");
            } else {
               ConnectionHandler handler = this.streamManager.createHandler(type, this.packetHandler, this.channel);
               if (handler == null) {
                  LOGGER.at(Level.SEVERE).log("Failed to create handler for stream type %s from %s", type.name(), this.packetHandler.getIdentifier());
                  this.rejectAndClose(type, "Internal error");
               } else {
                  LOGGER.at(Level.INFO).log("Opening %s stream for %s", type.name(), this.packetHandler.getIdentifier());
                  QuicStreamPriority priority = this.streamManager.getStreamPriority(type);
                  this.channel.updateStreamPriority(priority.urgency(), priority.isIncremental());
                  this.channel.setChannelHandler(handler);
                  this.channel.writeAndFlush(new StreamOpenResponse(type, true, null));
               }
            }
         }
      } else {
         LOGGER.at(Level.WARNING).log("Auxiliary stream first packet was not StreamOpen, closing: %s", packet.getClass().getSimpleName());
         this.channel.closeConnection();
      }
   }

   private void rejectAndClose(@Nonnull StreamType type, @Nonnull String reason) {
      this.channel.writeAndFlush(new StreamOpenResponse(type, false, reason));
      this.channel.closeConnection();
   }

   @Override
   public void closed(@Nullable NetworkChannel networkChannel) {
   }

   @Override
   public void registered(@Nullable ConnectionHandler oldHandler) {
   }

   @Override
   public void unregistered(@Nullable ConnectionHandler newHandler) {
   }

   @Override
   public void logCloseMessage() {
   }
}
