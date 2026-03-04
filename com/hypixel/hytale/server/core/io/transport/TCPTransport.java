package com.hypixel.hytale.server.core.io.transport;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.io.netty.HytaleChannelInitializer;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class TCPTransport implements Transport {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final EventLoopGroup bossGroup = NettyUtil.getEventLoopGroup(1, "ServerBossGroup");
   @Nonnull
   private final EventLoopGroup workerGroup = NettyUtil.getEventLoopGroup("ServerWorkerGroup");
   private final ServerBootstrap bootstrap;

   public TCPTransport() throws InterruptedException {
      Class<? extends ServerChannel> serverChannel = NettyUtil.getServerChannel();
      LOGGER.at(Level.INFO).log("Using Server Channel: %s...", serverChannel.getSimpleName());
      this.bootstrap = ((ServerBootstrap)((ServerBootstrap)((ServerBootstrap)new ServerBootstrap()
                  .group(this.bossGroup, this.workerGroup)
                  .channel(serverChannel))
               .option(ChannelOption.SO_BACKLOG, 256))
            .option(ChannelOption.SO_REUSEADDR, true))
         .childHandler(new HytaleChannelInitializer())
         .validate();
      this.bootstrap.register().sync();
   }

   @Nonnull
   @Override
   public TransportType getType() {
      return TransportType.TCP;
   }

   @Override
   public ChannelFuture bind(InetSocketAddress address) throws InterruptedException {
      return this.bootstrap.bind(address).sync();
   }

   @Override
   public void shutdown() {
      LOGGER.at(Level.INFO).log("Shutting down bossGroup...");

      try {
         this.bossGroup.shutdownGracefully(0L, 1L, TimeUnit.SECONDS).await(1L, TimeUnit.SECONDS);
      } catch (InterruptedException var3) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var3)).log("Failed to await for listener to close!");
         Thread.currentThread().interrupt();
      }

      LOGGER.at(Level.INFO).log("Shutting down workerGroup...");

      try {
         this.workerGroup.shutdownGracefully(0L, 1L, TimeUnit.SECONDS).await(1L, TimeUnit.SECONDS);
      } catch (InterruptedException var2) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var2)).log("Failed to await for listener to close!");
         Thread.currentThread().interrupt();
      }
   }
}
