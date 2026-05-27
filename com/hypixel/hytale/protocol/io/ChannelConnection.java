package com.hypixel.hytale.protocol.io;

import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.connection.QuicApplicationErrorCode;
import java.net.SocketAddress;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ChannelConnection {
   void flush();

   void write(ToClientPacket var1);

   void writeAndFlush(ToClientPacket var1);

   void write(ToClientPacket[] var1);

   void writeAndFlush(ToClientPacket[] var1);

   boolean isActive();

   boolean isWritable();

   SocketAddress remoteAddress();

   String formatRemoteAddress();

   void disconnect(@Nonnull FormattedMessage var1);

   @Nullable
   PacketStatsRecorder getPacketStatsRecorder();

   @Nullable
   String getSniHostname();

   boolean isFromSameOrigin(ChannelConnection var1);

   void execute(Runnable var1);

   X509Certificate getClientCertificate();

   void initTimeoutContext(@Nonnull String var1, @Nonnull String var2);

   void updateTimeoutContext(@Nonnull String var1, @Nonnull String var2);

   void updateTimeoutContext(@Nonnull String var1);

   void setPacketTimeout(@Nonnull Duration var1);

   void setStageTimeout(@Nonnull String var1, @Nonnull Duration var2, @Nonnull BooleanSupplier var3, @Nonnull Runnable var4);

   void clearStageTimeout();

   void logConnectionTimings(@Nonnull String var1, @Nonnull Level var2);

   @Nonnull
   CompletableFuture<Void> setupAuxiliaryChannels(@Nonnull ConnectionHandler var1, @Nonnull BiConsumer<NetworkChannel, ChannelConnection> var2);

   void setChannelHandler(@Nonnull ConnectionHandler var1);

   void closeConnection();

   void closeApplicationConnection();

   void closeApplicationConnection(@Nonnull QuicApplicationErrorCode var1);

   void closeApplicationConnection(@Nonnull QuicApplicationErrorCode var1, @Nonnull String var2);

   void updateStreamPriority(int var1, boolean var2);
}
